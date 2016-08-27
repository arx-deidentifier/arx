package org.deidentifier.arx.risk;


import org.apache.hadoop.mapred.JobHistory;

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
        // alpha-distinct = countUniqueTuples / countAllTuples
        return getHistogram().getNumClasses() / getHistogram().getNumRecords();

    }
    double getAlphaSeparation() {
        RiskModelHistogram histogram = getHistogram();
        int[] classes = histogram.getHistogram();
        int comparesTotal = sum((int)histogram.getNumRecords()-1);

        int collisions = 0;
        for (int i = 0; i < classes.length; i+=2) {
            int class_size = classes[i];
            int class_count = classes[i+1];
            int[] classesFromCurrent = Arrays.copyOfRange(classes, i+2, classes.length);
            int numRecordsLeft = getNumRecords(classesFromCurrent);

            //for (int ii = 0; ii < class_count-1; ii++) {
                //collisions += class_size * (class_size*(class_count-1)+numRecordsLeft); // bei 3 gruppen mit größe 1 müssen die anderen 2 gruppen, die übrig bleiben auch mit einberechnet werden, nicht nur eine
            collisions += collisionsOneClassSizeIterative(class_size, class_count, numRecordsLeft);
           // }

            //int tmp = class_size * (class_size+numRecordsLeft) * max((class_count-1),1); // make sure no multiplication with 0
            //collisions += class_size * numRecordsLeft;
            //collisions += tmp;
        }

        return (double)collisions/(double)comparesTotal;
    }

    private static int collisionsOneClassSizeIterative(int size, int count, int restNumRecords) {
        // aa bb ccc dddd
        // 2,2   3,1  4,1
        int collisions = 0;
        for (int i = 1; i < count; i++) {
            //System.out.println("Schritt " + i + ":");
            int cc = size * ((count - i) * size + restNumRecords);
            //System.out.println(size + " * " + "((" + ((count + "-" + i)) + ") * " + size + ")");
            //System.out.println("Kollisionen: " + cc + "");
            collisions += cc;
            //System.out.println("Collisionen bisher: " + collisions + "");

        }
        collisions += size * restNumRecords;
        return collisions;
    }

    private int max(int a, int b) {
        return a > b ? a : b;
    }

    private int getNumRecords(int[] classes) {
        int num = 0;
        for (int i = 0; i<classes.length;i+=2) {
            num += classes[i+1] * classes[i];
        }

        return num;
    }

    private int sum(int n) {
        // 4 3 2 1
        if (n % 2 == 0) {
            return (n/2)*(n+1);
        } else {
            // 5 4 3 2 1
            return n + ((n-1)/2)*(n);
        }
        //return sum_r(n, 0);
    }
    private int sum_r(int n, int sum) {
        // 1. Summe rekursiv sum(n)+sum(n-1)
        // 2. Summe mit Gauß 4+3+2+1 = 4+1 + 3+2 = 5 + 5 = 10 => 1.elem/2 * (1.elem +1)
        // paralell for von Java?
        return sum_r(n-1, sum+n);
    }

}