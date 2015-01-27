/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.deidentifier.arx.aggregates;

import java.util.Iterator;

/**
 * A contingency table.
 *
 * @author Fabian Prasser
 */
public class StatisticsContingencyTable {
    
    /**
     * An entry in the contingency table.
     *
     * @author Fabian Prasser
     */
    public static class Entry {
        
        /** Index of the value from the first column. */
        public int    value1;
        
        /** Index of the value from the second column. */
        public int    value2;
        
        /** Associated frequency. */
        public double frequency;
        
        /**
         * Internal constructor.
         *
         * @param value1
         * @param value2
         */
        Entry(int value1, int value2){
            this.value1 = value1;
            this.value2 = value2;
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + value1;
            result = prime * result + value2;
            return result;
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
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

    /** The data values from the first column, sorted. */
    public final String[]        values1;
    
    /** The data values from the second column, sorted. */
    public final String[]        values2;
    
    /** The total number of entries in the contingency table. */
    public final int             count;
    
    /** Maximal frequency in the contingency table. */
    public final double          maxFrequency;
    
    /** An iterator over the elements in the contingency table. */
    public final Iterator<StatisticsContingencyTable.Entry> iterator;

    /**
     * Internal constructor.
     *
     * @param value1
     * @param value2
     * @param count
     * @param maxFrequency
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