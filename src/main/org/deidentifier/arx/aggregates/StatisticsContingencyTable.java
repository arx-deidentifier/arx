package org.deidentifier.arx.aggregates;

import java.util.Iterator;

/**
 * A contingency table
 * @author Fabian Prasser
 */
public class StatisticsContingencyTable {
    
    /**
     * An entry in the contingency table
     * @author Fabian Prasser
     */
    public static class Entry {
        
        /** Index of the value from the first column*/
        public int    value1;
        /** Index of the value from the second column*/
        public int    value2;
        /** Associated frequency*/
        public double frequency;
        
        /**
         * Internal constructor
         * @param value1
         * @param value2
         */
        Entry(int value1, int value2){
            this.value1 = value1;
            this.value2 = value2;
        }
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + value1;
            result = prime * result + value2;
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            StatisticsContingencyTable.Entry other = (StatisticsContingencyTable.Entry) obj;
            if (value1 != other.value1) return false;
            if (value2 != other.value2) return false;
            return true;
        }
    }

    /** The data values from the first column, sorted*/
    public final String[]        values1;
    /** The data values from the second column, sorted*/
    public final String[]        values2;
    /** The total number of entries in the contingency table*/
    public final int             count;
    /** Maximal frequency in the contingency table*/
    public final double          maxFrequency;
    /** An iterator over the elements in the contingency table*/
    public final Iterator<StatisticsContingencyTable.Entry> iterator;

    /**
     * Internal constructor
     * @param value1
     * @param value2
     * @param count
     * @param iterator
     */
    StatisticsContingencyTable(String[] value1, String[] value2, int count, 
                               double maxFrequency, Iterator<StatisticsContingencyTable.Entry> iterator) {
        this.values1 = value1;
        this.values2 = value2;
        this.count = count;
        this.maxFrequency = maxFrequency;
        this.iterator = iterator;
    }
}