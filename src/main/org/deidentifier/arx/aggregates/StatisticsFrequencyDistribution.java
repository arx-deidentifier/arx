package org.deidentifier.arx.aggregates;

/**
 * A frequency distribution.
 *
 * @author Fabian Prasser
 */
public class StatisticsFrequencyDistribution {

    /** The data values, sorted. */
    public final String[] values;
    
    /** The corresponding frequencies. */
    public final double[] frequency;
    
    /** The total number of data values. */
    public final int      count;

    /**
     * Internal constructor.
     *
     * @param items
     * @param frequency
     * @param count
     */
    StatisticsFrequencyDistribution(String[] items, double[] frequency, int count) {
        this.values = items;
        this.count = count;
        this.frequency = frequency;
    }
}