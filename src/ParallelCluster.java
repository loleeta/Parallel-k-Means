import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class ParallelCluster implements Runnable {
    private List<IrisData> myData; // Data passed in
    private List<IrisData> centroids;   // Centroids for clustering
    private HashMap<Integer, List<IrisData>> finalClusters; // List to write to
    private HashMap<Integer, List<IrisData>> myClusters; // Clusters generated
    private int dataSize; // Size of data passed in
    private int startIndex, endIndex; // Indices for my data
    private int threadID; // Thread ID
    private CyclicBarrier barrier; // Barrier

    /** Constructor
     * @param data List of IrisData objects
     * @param centroids List of IrisData objects
     * @param clusters  HashMap of clusters
     * @param dataSize Size of the data I should cluster
     * @param threadID Thread ID
     * @param b Barrier
     */
    public ParallelCluster(List<IrisData> data, List<IrisData> centroids,
                           HashMap<Integer, List<IrisData>> clusters, int
                                dataSize, int threadID, CyclicBarrier b) {
        this.centroids = centroids;
        this.finalClusters = clusters;
        myClusters = new HashMap<>();
        for (int i = 1; i <= centroids.size(); i++) {
            myClusters.put(i, new ArrayList<IrisData>());
        }
        this.dataSize = dataSize;
        this.threadID = threadID;
        this.startIndex = threadID * dataSize;
        this.endIndex = startIndex + dataSize - 1;
        this.myData = data.subList(startIndex, endIndex+1);
        this.barrier = b;
    }

    /**
     * Starts the thread by clustering datapoints around centroids
     */
    @Override
    public void run() {
        //System.out.println("Thread: " + this.threadID + " starts: " +
         //       startIndex + " ends: " + endIndex);
        //System.out.println(threadID + ": " + localData);

        //cluster data to the centroids given
        clusterData();
        try {
            barrier.await();
        } catch(InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
        putLocalData();
    }

    /**
     * Creates clusters around centroids by calculating distances.
     */
    public void clusterData() {
        for (IrisData i: myData) {
            List<Double> distances = new ArrayList<>();
            for (IrisData c: centroids) {
                distances.add(getDistance(i, c));
            }
            //get smallest distance and assign datapoint to nearest cluster
            int minIndex = distances.indexOf(Collections.min(distances));
            myClusters.get(minIndex+1).add(i);
        }
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
     * Adds newly generated cluster to HashMap.
     */
    private void putLocalData() {
        synchronized (finalClusters) {
            for (Map.Entry<Integer, List<IrisData>> e: myClusters.entrySet()) {
                List<IrisData> localDat = e.getValue();
                if (!localDat.isEmpty()) {
                    List<IrisData> finalDat = finalClusters.get(e.getKey());
                    finalDat.addAll(localDat);
                }
            }
        }
    }
}
