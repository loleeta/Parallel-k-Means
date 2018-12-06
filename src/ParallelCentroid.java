import java.util.*;

/**
 * Given a cluster of data, find the average of all datapoints to generate a
 * new centroid for next iteration of k-means.
 */
public class ParallelCentroid implements Runnable {
    private List<IrisData> cluster; //data passed in
    private List<IrisData> newCentroidsList; //data I will write to
    private IrisData newCentroid;   //the new centroid

    /**
     * Constructor
     * @param data  List of IrisData objects
     * @param newCentroids  List of newCentrods to write to
     */
    public ParallelCentroid(List<IrisData> data, List<IrisData> newCentroids) {
        this.cluster = data;
        this.newCentroidsList = newCentroids;
    }

    /**
     * Starts the thread by calculating the cluster average
     */
    @Override
    public void run() {
        newCentroid = getClusterAvg();
        putLocalData();
    }

    /**
     * Given a cluster, generate a new centroid by calculating the average.
     * @return a new IrisData object
     */
    private IrisData getClusterAvg() {
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
     * Adds the newly calculated centroid to a list.
     */
    private void putLocalData() {
        synchronized (newCentroidsList) {
            newCentroidsList.add(newCentroid);
        }
    }
}
