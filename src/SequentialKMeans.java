import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class SequentialKMeans {
    /**
     * Takes a file name and reads in corresponding CSV data, creates
     * IrisData object for each line of data, and returns a list of the
     * IrisData objects.
     * @param fileName  String for the file name
     * @return  List of IrisData objects
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
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return dataFromFile;
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
     * K-means algorithm. While there is no converengce, assign data points
     * to clusters and recompute centroids.
     * @param data  List of IrisData objects
     * @param centroids List of IrisData objects as centroids for clusters
     * @return HashMap of clusters
     */
    private static HashMap<Integer, List<IrisData>> runKMeans(List<IrisData>
              data, List<IrisData> centroids) {
        boolean convergence = false;
        int iteration = 0;
        HashMap<Integer, List<IrisData>> currentClusters = new HashMap<>();
        while (!convergence) {
            iteration++;
            //System.out.println("Iteration: " + ++iteration);
            //System.out.println("\tCentroids are " + centroids);

            // create initial cluster
            currentClusters = cluster(data, centroids);


            //check that no cluster is empty
            //checkClusterPopulation(clusters);

            // find the new centroids of each cluster
            //System.out.println("\tFinding average of each cluster:");

            // for every cluster, find the average point
            List<IrisData> newCentroids = getNewCentroids(currentClusters);

            //compare new and old centroids for convergence
            //System.out.println("\tChecking convergence");
            //System.out.println("\t\tOld: " + centroids);
            //System.out.println("\t\tNew: " + newCentroids + "\n");
            convergence = checkConvergence(centroids, newCentroids);

            //if converged, clustering is complete
            if (convergence) {
                //System.out.println("Convergence found with " + iteration +
                // " " +
                //        "iterations\n");
                break;
            }
            //System.out.println();

            //otherwise, continue clustering with new centroids
            centroids = newCentroids;
            currentClusters = cluster(data, centroids);
        }
        System.out.println("Iterations: " + iteration);
        return currentClusters;
    }

    /**
     * Given a list of data, and the centroids, assign the data to the
     * closest centroid by calculating their distances.
     * @param data  List of IrisData objects
     * @param centroids List of IrisData objects
     * @return HashMap of clusters
     */
    private static HashMap<Integer, List<IrisData>> cluster (List<IrisData>
                                      data, List<IrisData> centroids) {
        HashMap<Integer, List<IrisData>> clusters = new HashMap<>();
        List<IrisData> cluster1 = new ArrayList<>();
        List<IrisData> cluster2 = new ArrayList<>();
        List<IrisData> cluster3 = new ArrayList<>();
        clusters.put(1, cluster1);
        clusters.put(2, cluster2);
        clusters.put(3, cluster3);
        //for each data point, find the distance for all centroids. It
        // belongs in the cluster it has the smallest distance to.
        for (IrisData i: data) {
            List<Double> distances = new ArrayList<>();
            for (IrisData c: centroids) {
                distances.add(getDistance(i, c));
            }
            //get smallest distance and assign datapoint to nearest cluster
            int minIndex = distances.indexOf(Collections.min(distances));
            clusters.get(minIndex+1).add(i);
        }
        return clusters;
    }

    /**
     * From each cluster, calculate a new centroid by finding the average of
     * its attributes.
     * @param currentClusters HashMap of current clusters of an iteration
     * @return list of new IrisData objects
     */
    private static List<IrisData> getNewCentroids(HashMap<Integer,
            List<IrisData>> currentClusters) {
        List<IrisData> newCentroids = new ArrayList<>();

        for (Map.Entry<Integer, List<IrisData>> entry: currentClusters.entrySet
                ()) {
            Integer i = entry.getKey(); //cluster number
            List<IrisData> clusterList = entry.getValue();  //cluster
            IrisData clusterAvg = getClusterAverage(clusterList);
            newCentroids.add(clusterAvg);
            //System.out.println("\t\t" + i + ": " + clusterAvg);
        }
        //System.out.println();
        return newCentroids;
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
     * Given a cluster, generate a new centroid by calculating the average.
     * @param cluster a List of IrisData objects
     * @return a new IrisData object
     */
    private static IrisData getClusterAverage(List<IrisData> cluster) {
        int clusterSize = cluster.size();
        Double sepialLengthAvg = 0.0,
                sepialWidthAvg = 0.0,
                petalLengthAvg = 0.0,
                petalWidthAvg = 0.0;
        for (IrisData i: cluster) {
            sepialLengthAvg += i.sepialLength;
            sepialWidthAvg += i.sepialWidth;
            petalLengthAvg += i.petalLength;
            petalWidthAvg += i.petalWidth;
        }

        //round it up to two trailing decimal places
        sepialLengthAvg = Math.round((sepialLengthAvg/clusterSize) * 100.0) /
                100.0;
        sepialWidthAvg = Math.round((sepialWidthAvg/clusterSize) * 100.0) /
                100.0;
        petalLengthAvg = Math.round((petalLengthAvg/clusterSize) * 100.0) /
                100.0;
        petalWidthAvg = Math.round((petalWidthAvg/clusterSize) * 100.0) /
                100.0;

        return new IrisData(sepialLengthAvg, sepialWidthAvg, petalLengthAvg,
                petalWidthAvg);
    }

    /**
     * Given two lists of IrisData objects, check to see if their differences
     * are significant by looking at the distance between a and a', b and b',
     * c and c'
     * @param oldCentroids list of IrisData objects
     * @param newCentroids list of IrisData objects
     * @return True if difference is lower than threshold, False otherwise
     */
    private static boolean checkConvergence(List<IrisData> oldCentroids,
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

    public static void main(String[] args) {
        //read data into list
        String file = "src/iris-data.csv";
        int numberCentroids = 3;
        List<IrisData> data = readFromFile(file);

        //pick data points to be the centroids
        List<IrisData> centroids = chooseCentroids(data, numberCentroids);
        //System.out.println("Picked three centroids: ");
        /*for (IrisData d: centroids) {
            System.out.println(d);
        }
        System.out.println();*/

        //cluster the data around the centroids and repeat until convergence
        Long startTime = System.currentTimeMillis();
        HashMap<Integer, List<IrisData>> finalClusters = runKMeans
                (data, centroids);
        Long endTime = System.currentTimeMillis();
        System.out.println("Time until convergence: " + (endTime-startTime)
                + "ms");

        printClusterStats(finalClusters);

    }

}
