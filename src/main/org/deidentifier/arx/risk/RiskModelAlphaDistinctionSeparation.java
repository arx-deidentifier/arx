package org.deidentifier.arx.risk;


import java.util.Arrays;

class RiskModelAlphaDistinctionSeparation extends RiskModelSample {

    /**
     * Creates a new instance
     *
     * @param histogram
     */
    RiskModelAlphaDistinctionSeparation(RiskModelHistogram histogram) {
        super(histogram);
    }

    double getAlphaDistinction() {
        // alpha-distinct = count of distinct values / count of all values
        return getHistogram().getNumClasses() / getHistogram().getNumRecords();

    }

    double getAlphaSeparation() {
        RiskModelHistogram histogram = getHistogram();
        int[] classes = histogram.getHistogram();
        int comparesTotal = sum((int)histogram.getNumRecords()-1);

        int distinctTuples = 0;
        for (int i = 0; i < classes.length; i+=2) {
            int classSize = classes[i];
            int classCount = classes[i+1];
            int countRecordsLeft = getNumRecords(classes, i+2);

            distinctTuples += calculateDistinctTuples(classSize, classCount, countRecordsLeft);

        }
        return (double)distinctTuples / (double)comparesTotal;
    }

    private static int calculateDistinctTuples(int classSize, int classCount, int recordsLeft) {
        return ((classCount-1) * classSize * (classCount * classSize + 2 * recordsLeft))/2 + classSize * recordsLeft;
    }

    private int getNumRecords(int[] classes, int start) {
        if (start % 2 != 0) {
            throw new IllegalArgumentException("Start must be multiple of 2 due to format of classes array.");
        }
        int num = 0;
        for (int i = start; i<classes.length;i+=2) {
            num += classes[i+1] * classes[i];
        }
        return num;
    }

    private static int sum(int n) {
        return (n*(n+1))/2; // Rechenregel Summe i, von i=1 bis n entspricht (n(n+1))/2
    }

}