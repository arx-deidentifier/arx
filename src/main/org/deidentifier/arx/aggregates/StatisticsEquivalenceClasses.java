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
    private final double                     averageEquivalenceClassSizeIncludingOutliers;
    /** Statistic value */
    private final int                        maximalEquivalenceClassSize;
    /** Statistic value */
    private final int                        maximalEquivalenceClassSizeIncludingOutliers;
    /** Statistic value */
    private final int                        minimalEquivalenceClassSize;
    /** Statistic value */
    private final int                        minimalEquivalenceClassSizeIncludingOutliers;
    /** Statistic value */
    private final int                        numberOfEquivalenceClasses;
    /** Statistic value */
    private final int                        numberOfEquivalenceClassesIncludingOutliers;
    /** Statistic value */
    private final int                        numberOfTuples;
    /** Statistic value */
    private final int                        numberOfOutlyingTuples;
    
    /**
     * Creates a new instance
     * @param averageEquivalenceClassSize
     * @param averageEquivalenceClassSizeIncludingOutliers
     * @param maximalEquivalenceClassSize
     * @param maximalEquivalenceClassSizeIncludingOutliers
     * @param minimalEquivalenceClassSize
     * @param minimalEquivalenceClassSizeIncludingOutliers
     * @param numberOfEquivalenceClasses
     * @param numberOfEquivalenceClassesIncludingOutliers
     * @param numberOfTuples
     * @param numberOfOutlyingTuples
     */
    StatisticsEquivalenceClasses(double averageEquivalenceClassSize,
                                 double averageEquivalenceClassSizeIncludingOutliers,
                                 int maximalEquivalenceClassSize,
                                 int maximalEquivalenceClassSizeIncludingOutliers,
                                 int minimalEquivalenceClassSize,
                                 int minimalEquivalenceClassSizeIncludingOutliers,
                                 int numberOfEquivalenceClasses,
                                 int numberOfEquivalenceClassesIncludingOutliers,
                                 int numberOfTuples,
                                 int numberOfOutlyingTuples) {
        this.averageEquivalenceClassSize = averageEquivalenceClassSize;
        this.averageEquivalenceClassSizeIncludingOutliers = averageEquivalenceClassSizeIncludingOutliers;
        this.maximalEquivalenceClassSize = maximalEquivalenceClassSize;
        this.maximalEquivalenceClassSizeIncludingOutliers = maximalEquivalenceClassSizeIncludingOutliers;
        this.minimalEquivalenceClassSize = minimalEquivalenceClassSize;
        this.minimalEquivalenceClassSizeIncludingOutliers = minimalEquivalenceClassSizeIncludingOutliers;
        this.numberOfEquivalenceClasses = numberOfEquivalenceClasses;
        this.numberOfEquivalenceClassesIncludingOutliers = numberOfEquivalenceClassesIncludingOutliers;
        this.numberOfTuples = numberOfTuples;
        this.numberOfOutlyingTuples = numberOfOutlyingTuples;
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
     * This number takes into account one additional equivalence class containing all outliers
     * @return
     */
    public double getAverageEquivalenceClassSizeIncludingOutliers(){
        return averageEquivalenceClassSizeIncludingOutliers;
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
     * Returns the maximal size of an equivalence class.
     * This number takes into account one additional equivalence class containing all outliers
     * @return
     */
    public int getMaximalEquivalenceClassSizeIncludingOutliers(){
        return maximalEquivalenceClassSizeIncludingOutliers;
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
     * Returns the minimal size of an equivalence class. 
     * This number takes into account one additional equivalence class containing all outliers
     * @return
     */
    public int getMinimalEquivalenceClassSizeIncludingOutliers(){
        return minimalEquivalenceClassSizeIncludingOutliers;
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
     * Returns the number of outlying equivalence classes in the currently selected data
     * representation.
     *
     * @return
     */
    public int getNumberOfEquivalenceClassesIncludingOutliers() {
        return numberOfEquivalenceClassesIncludingOutliers;
    }

    /**
     * Returns the number of outliers in the currently selected data
     * representation.
     *
     * @return
     */
    public int getNumberOfOutlyingTuples() {
        return numberOfOutlyingTuples;
    }
    /**
     * Returns the number of tuples in the currently selected data
     * representation.
     *
     * @return
     */
    public int getNumberOfTuples() {
        return numberOfTuples - numberOfOutlyingTuples;
    }
    
    /**
     * Returns the number of tuples in the currently selected data
     * representation.
     *
     * @return
     */
    public int getNumberOfTuplesIncludingOutliers() {
        return numberOfTuples;
    }

    @Override
    public String toString() {
        return "EquivalenceClassStatistics {\n- Average equivalence class size = " + getAverageEquivalenceClassSize() + "\n" +
               "- Average equivalence class size (including outliers) = " + getAverageEquivalenceClassSizeIncludingOutliers() + "\n" +
               "- Maximal equivalence class size = " + getMaximalEquivalenceClassSize() + "\n" +
               "- Maximal equivalence class size (including outliers) = " + getMaximalEquivalenceClassSizeIncludingOutliers() + "\n" +
               "- Minimal equivalence class size = " + getMinimalEquivalenceClassSize() + "\n" +
               "- Minimal equivalence class size (including outliers) = " + getMinimalEquivalenceClassSizeIncludingOutliers() + "\n" +
               "- Number of equivalence classes = " + getNumberOfEquivalenceClasses() + "\n" +
               "- Number of equivalence classes (including outliers) = " + getNumberOfEquivalenceClassesIncludingOutliers() + "\n" +
               "- Number of tuples = " + getNumberOfTuples() + "\n" +
               "- Number of outlying tuples = " + getNumberOfOutlyingTuples() + "\n}";
    }
    
    
}
