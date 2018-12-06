import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CyclicBarrier;

/**
 * K-means clustering of Iris dataset in parallel using threading.
 * https://archive.ics.uci.edu/ml/datasets/iris
 */
public class ParallelKMeans {
    private static final int NUM_THREADS = 3;   //number of threads
    private static final int NUM_CENTROIDS = 3; //number of centroids

    public static void main(String[] args) {
        int iterations = 0; //counter for iterations in algorithm

        //read data from file
        List<IrisData> data = readFromFile("src/iris-data.csv");

        //pick data points to be the centroids
        List<IrisData> centroids = chooseCentroids(data, NUM_CENTROIDS);

        //initialize variables
        HashMap<Integer, List<IrisData>> clusters = initClusters(NUM_CENTROIDS);
        CyclicBarrier barrier = new CyclicBarrier(NUM_THREADS);

        boolean converged = false;
        Long startTime = System.currentTimeMillis();
        while (!converged) {
            iterations++;
            //threads for clustering
            Thread [] clusterThreads = getClusterThreads(data, centroids,
                    clusters, barrier);
            startThreads(clusterThreads);
            joinThreads(clusterThreads);

            List<IrisData> newCentroids = new ArrayList<>();

            //threads for centroids
            Thread [] centroidThreads = getCentroidThreads(clusters,
                    newCentroids);
            startThreads(centroidThreads);
            joinThreads(centroidThreads);

            //check for convergence
            converged = isConverged(centroids, newCentroids);
            if (!converged) {
                centroids = newCentroids;
                clusters = initClusters(NUM_CENTROIDS);
            }
        }
        Long endTime = System.currentTimeMillis();
        System.out.println("Time until convergence: " + (endTime-startTime)
                + "ms");
        System.out.println("Num iterations: " + iterations);

        printClusterStats(clusters);
    }

    /**
     * Takes a file name and reads in corresponding CSV data, creates
     * IrisData object for each line of data, and returns a list of the
     * IrisData objects.
     *
     * @param fileName String for the file name
     * @return List of IrisData objects
     */
    private static List<IrisData> readFromFile(String fileName) {
        BufferedReader br = null;
        String line = "";
        List<IrisData> dataFromFile = new ArrayList<>();
        try {
            br = new BufferedReader(new FileReader(fileName));
            while ((line = br.readLine()) != null) {
                String[] data = line.split(", ");
                IrisData i = new IrisData(data);
                dataFromFile.add(i);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Collections.shuffle(dataFromFile);
        return dataFromFile;
    }

    private static HashMap<Integer, List<IrisData>> initClusters(int numC) {
        HashMap<Integer, List<IrisData>> clusters = new HashMap<>();
        for (int i = 1; i <= numC; i++) {
            clusters.put(i, new ArrayList<IrisData>());
        }
        return clusters;
    }

    /**
     * Initial step of the clustering algorithm.
     * Given a list of IrisData objects, selects three of them randomly by
     * shuffling a copy of the list and returning the first three in a list.
     * @param data  List of IrisData objects
     * @param numCentroids  Number of objects to be picked as centroids
     * @return  a List of IrisData objects
     */
    private static List<IrisData> chooseCentroids(List<IrisData> data, int
            numCentroids) {
        List<IrisData> dataCopy = new ArrayList<>(data);
        Collections.shuffle(dataCopy);
        return dataCopy.subList(0, numCentroids);
    }

    /**
     * Partitions the list of IrisData objects to get equivalent amount of
     * work per thread.
     * @param data  List of IrisData objects
     * @param centroids List of IrisData objects
     * @param clusters  HashMap of current clusters
     * @param barrier   CyclicBarrier for threads to meet at
     * @return  an array of Threads to start
     */
    private static Thread[] getClusterThreads(List<IrisData> data,
        List<IrisData> centroids, HashMap<Integer, List<IrisData>> clusters,
             CyclicBarrier barrier) {
        ParallelCluster[] parallelThreads = new ParallelCluster[NUM_THREADS];

        //partition work among threads
        int dataSize = data.size();
        if (data.size() % NUM_THREADS != 0) { //uneven work among threads
            int dataPerThread = dataSize / NUM_THREADS;
            for (int i = 0; i < NUM_THREADS-1; i++) {
                parallelThreads[i] = new ParallelCluster(data, centroids,
                        clusters, dataPerThread, i, barrier);
                System.out.println(dataPerThread);
            }
            int dataLeft = dataSize - (dataPerThread * (NUM_THREADS-1));
            parallelThreads[NUM_THREADS] = new ParallelCluster(data, centroids,
                    clusters, dataLeft, NUM_THREADS, barrier);
        }
        else { //even work among threads
            for (int i = 0; i < NUM_THREADS; i++) {
                parallelThreads[i] = new ParallelCluster(data, centroids,
                        clusters, (dataSize/NUM_THREADS), i, barrier);
            }
        }

        //create array of threads
        Thread [] threads = new Thread[NUM_THREADS];
        for (int i = 0; i < parallelThreads.length; i++) {
            threads[i] = new Thread(parallelThreads[i]);
        }

        return threads;
    }

    /**
     * Given an array of threads, start them
     * @param workerThreads List of threads
     */
    private static void startThreads(Thread[] workerThreads) {
        for (int i = 0; i < workerThreads.length; i++) {
            workerThreads[i].start();
        }
    }

    /**
     * Given an array of threads, wait for them to end
     * @param workerThreads List of threads
     */
    private static void joinThreads(Thread[] workerThreads) {
        for (int i = 0; i < workerThreads.length; i++) {
            try {
                workerThreads[i].join();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("EXCEPTION IN JOINING THREADS. EXITING.");
            }
        }
    }

    /**
     * Partition each cluster to a thread to calculate new centroids.
     * @param clusters  HashMap of clusters
     * @param newCentroids  List to place newly calculated centroids
     * @return an array of Threads to start
     */
    private static Thread[] getCentroidThreads(HashMap<Integer,
            List<IrisData>> clusters, List<IrisData> newCentroids) {

        ParallelCentroid [] centroidThreads = new ParallelCentroid[NUM_THREADS];
        for (Map.Entry<Integer, List<IrisData>> e: clusters.entrySet()) {
            Integer i = e.getKey()-1;
            List<IrisData> data = e.getValue();
            centroidThreads[i] = new ParallelCentroid(data, newCentroids);
        }

        Thread [] threads = new Thread[NUM_THREADS];
        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i] = new Thread(centroidThreads[i]);
        }

        return threads;
    }

    /**
     * Given two lists of IrisData objects, check to see if their differences
     * are significant by looking at the distance between a and a', b and b',
     * c and c'
     * @param oldCentroids list of IrisData objects
     * @param newCentroids list of IrisData objects
     * @return True if difference is lower than threshold, False otherwise
     */
    private static boolean isConverged(List<IrisData> oldCentroids,
                                          List<IrisData> newCentroids) {
        Double threshold = 0.01; //difference allowed
        Double delta = 0.0;
        Iterator oldIt = oldCentroids.iterator();
        Iterator newIt = newCentroids.iterator();
        while(oldIt.hasNext() && newIt.hasNext()) {
            IrisData oldCentroid = (IrisData)oldIt.next();
            IrisData newCentroid = (IrisData)newIt.next();
            delta += getDistance(oldCentroid, newCentroid);
        }
        return delta < threshold;
    }

    /**
     * Calculates the Euclidean norm to find distance between two data points.
     * @param datum1    one IrisData object
     * @param datum2    another IrisData object
     * @return a Double that is the distance
     */
    private static double getDistance(IrisData datum1, IrisData datum2) {
        double w = Math.pow((datum1.sepialLength - datum2.sepialLength), 2);
        double x = Math.pow((datum1.sepialWidth - datum2.sepialWidth), 2);
        double y = Math.pow((datum1.petalLength - datum2.petalLength), 2);
        double z = Math.pow((datum1.petalWidth - datum2.petalWidth), 2);
        return Math.sqrt(w+x+y+z);
    }

    /**
     * Prints the count of each class per cluster.
     * @param data HashMap of clusters
     */
    public static void printClusterStats(HashMap<Integer, List<IrisData>>
                                                 data) {
        for (Map.Entry<Integer, List<IrisData>> entry: data.entrySet()) {
            Integer i = entry.getKey();
            List<IrisData> clusterList = entry.getValue();
            int setosa = 0, versicolor = 0, virginica = 0;
            for (IrisData dat: clusterList) {
                if (dat.irisClass.contains("setosa"))
                    setosa++;
                else if (dat.irisClass.contains("virginica"))
                    virginica++;
                else
                    versicolor++;
            }
            System.out.println("Cluster " + i + ": " + setosa + " setosa, " +
                    versicolor + " versicolor, " + virginica + " virginica");
        }
    }
}
