package org.deidentifier.arx.risk;

/**
 * This class is based on the work stated in R. Motwani et al., “Efficient algorithms for masking and finding quasi-identifiers,” Proc. VLDB Conf., 2007.
 * Interdisciplinary training
 * Author: Max Zitzmann
 */
public class RiskModelAlphaDistinctionSeparation extends RiskModelSample {

    /**
     * Creates a new instance
     *
     * @param histogram
     */
    public RiskModelAlphaDistinctionSeparation(RiskModelHistogram histogram) {
        super(histogram);
    }

    /**
     * We calculate a value alpha in [0,1] such that the set of attributes becomes a key
     * after the removal of a fraction of at most 1-alpha of the records in the table.
     * This equals the number of distinct combinations of values (= number of eqClasses) / number of all records.
     *
     * @return the calculated alpha distinction
     */
    double getAlphaDistinction() {
        return getHistogram().getNumClasses() / getHistogram().getNumRecords();
    }

    /**
     * We calculate a value alpha in [0,1] such that a record of a subset of attributes, which differs at least
     * on one value in all records of this subset, differs at least on an alpha fraction of all
     * possible record combinations in this dataset
     *
     * @return the calculated alpha separation
     */
    double getAlphaSeparation() {
        RiskModelHistogram histogram = getHistogram();
        int[] classes = histogram.getHistogram();

        // when we want to compare 4 values (only in one direction this means we compare "a" to "b" but not "b" to "a")
        // we have 3 + 2 + 1 comparisons
        // => numberComparisons = number of values - 1
        double totalNumberOfComparisons = sum(histogram.getNumRecords() - 1d);

        // a record separates another record when it has on at least one attribute a different value
        double separatedRecords = 0;

        // no record have been compared yet
        double numberRecordsLeft = histogram.getNumRecords();

        // For each class-size
        for (int i = 0; i < classes.length; i += 2) {

            // Obtain size and multiplicity of that class
            double classSize = classes[i];
            double classMultiplicity = classes[i + 1];

            // Calculate records remaining in all classes of a size larger than the current one
            numberRecordsLeft -= classSize * classMultiplicity;

            // All records in classes of the current size are different from all remaining records
            double separatedRecordsCurrentClass = classMultiplicity * classSize * numberRecordsLeft;

            // Moreover, all records in each class of the current size are different from all other records
            // in other classes of the same size
            separatedRecordsCurrentClass += ((classMultiplicity - 1d) * classMultiplicity * (classSize * classSize)) / 2d;

            // add number of separated classes to result
            separatedRecords += separatedRecordsCurrentClass;
        }

        // alpha separation indicates a value alpha [0,1] such that a subset of attributes separates
        // at least an alpha fraction of all record pairs
        return separatedRecords / totalNumberOfComparisons;
    }

    /***
     * Calculates the gaussian sum formula
     * @param n the number to sum to
     * @return the sum from 1 to n
     */
    private static double sum(double n) {
        // see https://de.wikipedia.org/wiki/Gau%C3%9Fsche_Summenformel
        return (n * (n + 1d)) / 2d;
    }

}