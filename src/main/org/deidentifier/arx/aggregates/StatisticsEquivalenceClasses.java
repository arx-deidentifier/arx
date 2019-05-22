/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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

/**
 * Statistics about the equivalence classes.
 *
 * @author Fabian Prasser
 */
public class StatisticsEquivalenceClasses {

    /** Statistic value */
    private final double                     averageEquivalenceClassSize;
    /** Statistic value */
    private final int                        maximalEquivalenceClassSize;
    /** Statistic value */
    private final int                        minimalEquivalenceClassSize;
    /** Statistic value */
    private final int                        numberOfEquivalenceClasses;
    /** Statistic value */
    private final int                        numberOfRecordsIncludingSuppressedRecords;
    /** Statistic value */
    private final int                        numberOfSuppressedRecords;
    
    /**
     * Creates a new instance
     * @param averageEquivalenceClassSize
     * @param maximalEquivalenceClassSize
     * @param minimalEquivalenceClassSize
     * @param numberOfEquivalenceClasses
     * @param numberOfRecordsIncludingSuppressedRecords
     * @param numberOfSuppressedRecords
     */
    StatisticsEquivalenceClasses(double averageEquivalenceClassSize,
                                 int maximalEquivalenceClassSize,
                                 int minimalEquivalenceClassSize,
                                 int numberOfEquivalenceClasses,
                                 int numberOfRecordsIncludingSuppressedRecords,
                                 int numberOfSuppressedRecords) {
        this.averageEquivalenceClassSize = averageEquivalenceClassSize;
        this.maximalEquivalenceClassSize = maximalEquivalenceClassSize;
        this.minimalEquivalenceClassSize = minimalEquivalenceClassSize;
        this.numberOfEquivalenceClasses = numberOfEquivalenceClasses;
        this.numberOfRecordsIncludingSuppressedRecords = numberOfRecordsIncludingSuppressedRecords;
        this.numberOfSuppressedRecords = numberOfSuppressedRecords;
    }

    /**
     * Returns the maximal size of an equivalence class.
     *
     * @return
     */
    public double getAverageEquivalenceClassSize(){
        return averageEquivalenceClassSize;
    }

    /**
     * Returns the maximal size of an equivalence class.
     *
     * @return
     */
    public int getMaximalEquivalenceClassSize(){
        return maximalEquivalenceClassSize;
    }

    /**
     * Returns the minimal size of an equivalence class.
     *
     * @return
     */
    public int getMinimalEquivalenceClassSize(){
        return minimalEquivalenceClassSize;
    }

    /**
     * Returns the number of equivalence classes in the currently selected data
     * representation.
     *
     * @return
     */
    public int getNumberOfEquivalenceClasses() {
        return numberOfEquivalenceClasses;
    }

    /**
     * Returns the number of outliers in the currently selected data
     * representation.
     *
     * @return
     */
    public int getNumberOfSuppressedRecords() {
        return numberOfSuppressedRecords;
    }
    /**
     * Returns the number of tuples in the currently selected data
     * representation.
     *
     * @return
     */
    public int getNumberOfRecords() {
        return numberOfRecordsIncludingSuppressedRecords - numberOfSuppressedRecords;
    }
    
    /**
     * Returns the number of tuples in the currently selected data
     * representation.
     *
     * @return
     */
    public int getNumberOfRecordsIncludingSuppressedRecords() {
        return numberOfRecordsIncludingSuppressedRecords;
    }

    @Override
    public String toString() {
        return "EquivalenceClassStatistics {\n- Average equivalence class size = " + getAverageEquivalenceClassSize() + "\n" +
               "- Maximal equivalence class size = " + getMaximalEquivalenceClassSize() + "\n" +
               "- Minimal equivalence class size = " + getMinimalEquivalenceClassSize() + "\n" +
               "- Number of equivalence classes = " + getNumberOfEquivalenceClasses() + "\n" +
               "- Number of records = " + getNumberOfRecords() + "\n" +
               "- Number of suppressed records = " + getNumberOfSuppressedRecords() + "\n}";
    }
    
    
}
