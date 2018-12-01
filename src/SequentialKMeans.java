import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

class IrisData {
    public double sepialLength;
    public double sepialWidth;
    public double petalLength;
    public double petalWidth;
    public String irisClass;

    public IrisData(String[] attributes) {
        this.sepialLength = Double.parseDouble(attributes[0]);
        this.sepialWidth = Double.parseDouble(attributes[1]);
        this.petalLength = Double.parseDouble(attributes[2]);
        this.petalWidth = Double.parseDouble(attributes[3]);
        //this.irisClass = attributes[4];
    }

    public IrisData(Double sl, Double sw, Double pl, Double pw) {
        this.sepialLength = sl;
        this.sepialWidth = sw;
        this.petalLength = pl;
        this.petalWidth = pw;
    }

    public String toString() {
        String s = "(";
        s += this.sepialLength + ", " + this.sepialWidth + ", " + this
                .petalLength + ", " + this.petalWidth + ")";
        return s;
    }
}


public class SequentialKMeans {
    public static void main(String[] args) {
        //read data into list
        String file = "src/iris-data.csv";
        List<IrisData> data = readFromFile(file);
        Collections.shuffle(data);

        //pick three data points to be the centroids
        List<IrisData> centroids = chooseCentroids(data, 3);
        System.out.println("Picked three centroids: ");
        for (IrisData d: centroids) {
            System.out.println(d);
        }

        //cluster the data around the centroids until convergence
        cluster(data, centroids);
    }

    // Reads data from file into a list
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

    // Picks 3 random data points to be the centroids by shuffling list
    private static List<IrisData> chooseCentroids(List<IrisData> data, int
            numCentroids) {
        List<IrisData> dataCopy = new ArrayList<>(data);
        Collections.shuffle(dataCopy);
        return dataCopy.subList(0, numCentroids);
    }

    // Main loop of k-means:
    // While there is no convergence (convergence is true if new centroids
    // calculated are the same as old centroids)
    //     assign data points to clusters
    //     recompute the centroid
    private static void cluster(List<IrisData> data, List<IrisData> centroids) {
        boolean convergence = false;
        int iteration = 0;
        HashMap<Integer, List<IrisData>> currentClusters = new HashMap<>();
        while (!convergence) {
            System.out.println("Iteration: " + ++iteration);
            System.out.println("Centroids are " + centroids);
            System.out.println("Creating clusters");

            // create initial cluster
            if (currentClusters.isEmpty()) {
                System.out.println("Creating initial clusters.");
                currentClusters = createClusters(data, centroids);
            }

            //check that no cluster is empty
            //checkClusterPopulation(clusters);

            // find the new centroids of each cluster
            System.out.println("Finding average of each cluster.");
            List<IrisData> newCentroids = new ArrayList<>();
            // for every cluster, find the average point
            for (Map.Entry<Integer, List<IrisData>> entry: currentClusters.entrySet
                    ()) {
                Integer i = entry.getKey();
                List<IrisData> clusterList = entry.getValue();
                IrisData clusterAvg = getCentroidAverage(clusterList);
                newCentroids.add(clusterAvg);
                System.out.println("clusterAvg of " + i + ": " + clusterAvg);
            }

            System.out.println("Old centroids are: " + centroids);
            System.out.println("New centroids are: " + newCentroids);

            System.out.println("Checking difference between old and new " +
                    "centroids");
            convergence = checkConvergence(centroids, newCentroids);
            if (convergence) {
                System.out.println("Convergence found with " + iteration + " " +
                        "iterations");
                break;
            }

            centroids = newCentroids;
            createClusters(data, centroids);
        }

        System.out.println("Final clusters are: ");
        for (Map.Entry<Integer, List<IrisData>> entry: currentClusters.entrySet
                ()) {
            Integer i = entry.getKey();
            List<IrisData> clusterList = entry.getValue();
            System.out.println("cluster: " + i);
            System.out.println(clusterList);
        }


    }

    // assign each data point to a cluster by calculating distances
    private static HashMap<Integer, List<IrisData>> createClusters
                            (List<IrisData> data, List<IrisData> centroids) {
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
                //System.out.println("Distance for " + i + " and centroid " +
                // c + getDistance(i, c));
            }
            //get smallest distance and assign datapoint to nearest cluster
            int minIndex = distances.indexOf(Collections.min(distances));
            clusters.get(minIndex+1).add(i);
        }
        return clusters;
    }

    //calculate Euclidean norm to find distance between centroid and data point
    private static double getDistance(IrisData datum1, IrisData datum2) {
        double w = Math.pow((datum1.sepialLength - datum2.sepialLength), 2);
        double x = Math.pow((datum1.sepialWidth - datum2.sepialWidth), 2);
        double y = Math.pow((datum1.petalLength - datum2.petalLength), 2);
        double z = Math.pow((datum1.petalWidth - datum2.petalWidth), 2);
        return Math.sqrt(w+x+y+z);
    }

    // find the new centroid in a cluster by finding the average
    private static IrisData getCentroidAverage(List<IrisData> cluster) {
        System.out.println("in getCentroidAverage");
        int size = cluster.size();
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
        System.out.println("Avgs: " + sepialLengthAvg + ", " + sepialWidthAvg +
                        ", " +
                petalLengthAvg + ", " + petalWidthAvg);
        System.out.println("Size is: " + size);
        sepialLengthAvg /= size;
        sepialWidthAvg /= size;
        petalLengthAvg /= size;
        petalWidthAvg /= size;
        return new IrisData(sepialLengthAvg, sepialWidthAvg, petalLengthAvg,
                petalWidthAvg);
    }

    // ignore this, add in writeup
    private static void checkClusterPopulation(HashMap<Integer,
            List<IrisData>> clusters) {
        System.out.println("size of cluster 1" + clusters.get(1).size());
        System.out.println("size of cluster 2" + clusters.get(2).size());
        System.out.println("size of cluster 3" + clusters.get(3).size());
    }

    // check the difference between a and a-prime, b and b-prime, c and c-prime
    private static boolean checkConvergence(List<IrisData>
                                                    oldCentroids,
                                            List<IrisData> newCentroids) {
        System.out.println("in checkConvergence()");
        Double threshold = 0.00001;
        Double delta = 0.0;
        Iterator oldIt = oldCentroids.iterator();
        Iterator newIt = newCentroids.iterator();
        while(oldIt.hasNext() && newIt.hasNext()) {
            IrisData oldCentroid = (IrisData)oldIt.next();
            //System.out.println(oldCentroid);
            IrisData newCentroid = (IrisData)newIt.next();
            System.out.println("Comparing old: " + oldCentroid + " and new: "
                    + newCentroid);
            delta += getDistance(oldCentroid, newCentroid);
            System.out.println("delta: " + delta);
        }
        return (delta/3) < threshold;
    }
}
