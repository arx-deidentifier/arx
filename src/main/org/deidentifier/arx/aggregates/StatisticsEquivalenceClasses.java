package org.deidentifier.arx.aggregates;

import org.deidentifier.arx.framework.check.groupify.HashGroupify.GroupStatistics;

/**
 * Statistics about the equivalence classes
 * @author Fabian Prasser
 */
public class StatisticsEquivalenceClasses {

    private GroupStatistics groupStatistics;
    
    /**
     * Creates a new instance
     * @param groupStatistics Statistics obtained from hash groupify
     */
    public StatisticsEquivalenceClasses(GroupStatistics groupStatistics) {
        this.groupStatistics = groupStatistics;
    }

    /**
     * Returns the maximal size of an equivalence class.
     * This number takes into account one additional equivalence class containing all outliers
     * @return
     */
    public double getAverageEquivalenceClassSizeIncludingOutliers(){
        return groupStatistics.getAverageEquivalenceClassSizeIncludingOutliers();
    }

    /**
     * Returns the maximal size of an equivalence class.
     * This number takes into account one additional equivalence class containing all outliers
     * @return
     */
    public int getMaximalEquivalenceClassSizeIncludingOutliers(){
        return groupStatistics.getMaximalEquivalenceClassSizeIncludingOutliers();
    }

    /**
     * Returns the minimal size of an equivalence class. 
     * This number takes into account one additional equivalence class containing all outliers
     * @return
     */
    public int getMinimalEquivalenceClassSizeIncludingOutliers(){
        return groupStatistics.getMinimalEquivalenceClassSizeIncludingOutliers();
    }


    /**
     * Returns the maximal size of an equivalence class
     * @return
     */
    public double getAverageEquivalenceClassSize(){
        return groupStatistics.getAverageEquivalenceClassSize();
    }

    /**
     * Returns the maximal size of an equivalence class
     * @return
     */
    public int getMaximalEquivalenceClassSize(){
        return groupStatistics.getMaximalEquivalenceClassSize();
    }

    /**
     * Returns the minimal size of an equivalence class
     * @return
     */
    public int getMinimalEquivalenceClassSize(){
        return groupStatistics.getMinimalEquivalenceClassSize();
    }

    /**
     * Returns the number of equivalence classes in the currently selected data
     * representation
     * 
     * @return
     */
    public int getNumberOfGroups() {
        return groupStatistics.getNumberOfGroups();
    }

    /**
     * Returns the number of outlying equivalence classes in the currently selected data
     * representation
     * 
     * @return
     */
    public int getNumberOfOutlyingEquivalenceClasses() {
        return groupStatistics.getNumberOfOutlyingEquivalenceClasses();
    }

    /**
     * Returns the number of outliers in the currently selected data
     * representation
     * 
     * @return
     */
    public int getNumberOfOutlyingTuples() {
        return groupStatistics.getNumberOfOutlyingTuples();
    }
}