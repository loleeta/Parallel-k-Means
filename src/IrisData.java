/**
 * Class representing Iris data read from file
 */
class IrisData {
    public double sepialLength; // double representing sepial length in cm
    public double sepialWidth;  // double representing sepial width in cm
    public double petalLength;  // double representing petal length in cm
    public double petalWidth;  // double representing petal width in cm
    public String irisClass;  // double representing class of Iris species

    /**
     * Default constructor for representing Iris data point
     * @param attributes    String array of data
     */
    public IrisData(String[] attributes) {
        this.sepialLength = Double.parseDouble(attributes[0]);
        this.sepialWidth = Double.parseDouble(attributes[1]);
        this.petalLength = Double.parseDouble(attributes[2]);
        this.petalWidth = Double.parseDouble(attributes[3]);
        if (attributes.length == 5) {
            this.irisClass = attributes[4];
        }
    }

    /**
     * Overloaded constructor for representing a centroid data point
     * @param sl    passed in double
     * @param sw    passed in double
     * @param pl    passed in double
     * @param pw    passed in double
     */
    public IrisData(Double sl, Double sw, Double pl, Double pw) {
        this.sepialLength = sl;
        this.sepialWidth = sw;
        this.petalLength = pl;
        this.petalWidth = pw;
    }

    /**
     * String representation of data
     * @return a String
     */
    public String toString() {
        String s = "(";
        s += this.sepialLength + ", " + this.sepialWidth + ", " +
                this.petalLength + ", " + this.petalWidth + ",  " +
                this.irisClass + ")";
        return s;
    }
}