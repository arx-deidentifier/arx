/*
 * Kettle re-identification risk management step
 * Copyright (C) 2018 TUM/MRI
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.deidentifier.arx.test;

import java.util.HashSet;
import java.util.Set;


/**
 * This class encapsulates parameters related to risk management. It can be used
 * for threshold definitions as well as for storing/retrieving results.
 * 
 * @author Fabian Prasser
 * @author Helmut Spengler
 */
public class ParametersRisk {
    
    /** Default value*/
    public static final double  DEFAULT_HIGHEST_RISK     = 0.2d;
    /** Default value*/
    public static final double  DEFAULT_AVERAGE_RISK     = 0.05d;
    /** Default value*/
    public static final double  DEFAULT_RECORDS_AT_RISK  = 0.01d;
    /** Default value*/
    public static final boolean DEFAULT_USE_WC_MATCH     = true;
    
    /** Threshold for the average risk */
    private double averageRisk   = DEFAULT_AVERAGE_RISK;
    /** Threshold for the highest risk */
    private double highestRisk   = DEFAULT_HIGHEST_RISK;
    /** Threshold for records at risk. */
    private double recordsAtRisk = DEFAULT_RECORDS_AT_RISK;
    /** Whether to interpret missing values as wild card */
    private boolean useWcMatch = DEFAULT_USE_WC_MATCH;

    /** The quasi-identifiers */
    private Set<String> qis = new HashSet<String>();
    
    /**
     * Default constructor, no qis
     */
    public ParametersRisk() {
        super();
    }

    /**
     * Creates a new instance
     * @param qis
     */
    public ParametersRisk(Set<String> qis) {
        this.qis = qis;
    }
    
    @Override
    public ParametersRisk clone() {
        
        // Clone
        Set<String> qis = new HashSet<>(this.qis);
        ParametersRisk result = new ParametersRisk(qis);
        result.setAverageRisk(this.averageRisk);
        result.setHighestRisk(this.highestRisk);
        result.setRecordsAtRisk(this.recordsAtRisk);
        result.setUseWcMatch(this.useWcMatch);
        return result;
    }
    
    /**
     * Returns whether this object satisfies the given threshold
     * @param thresholds
     * @return
     */
    public boolean satisfies(ParametersRisk thresholds) {
        return this.recordsAtRisk <= thresholds.recordsAtRisk &&
               this.averageRisk <= thresholds.averageRisk &&
               this.highestRisk <= thresholds.highestRisk;
    }

    /**
     * Return the highest risk.
     * @return
     */
    public double getHighestRisk() {
        return highestRisk;
    }
    
    /**
     * Set the highest risk.
     * @param highestRisk
     */
    public void setHighestRisk(double highestRisk) {
        this.highestRisk = highestRisk;
    }
    
    /**
     * Return the average risk.
     * @return
     */
    public double getAverageRisk() {
        return averageRisk;
    }
    
    /**
     * Set the average risk.
     * @param averageRisk
     */
    public void setAverageRisk(double averageRisk) {
        this.averageRisk = averageRisk;
    }
    
    /**
     * Return the fraction of records at risk w.r.t. the highest risk.
     * @see #getHighestRisk()
     * @see #setHighestRisk(double)
     * @return
     */
    public double getRecordsAtRisk() {
        return recordsAtRisk;
    }

    /**
     * Set the fraction of records at risk w.r.t. the highest risk.
     * @see #getHighestRisk()
     * @see #setHighestRisk(double)
     * @return
     */
    public void setRecordsAtRisk(double recordsAtRisk) {
        this.recordsAtRisk = recordsAtRisk;
    }
    
    /**
     * Returns whether wild card matching is active.
     * @return
     */
    public boolean useWcMatch() {
        return useWcMatch;
    }

    /**
     * Define whether to use wild card matching.
     * @param useWcMatch
     */
    public void setUseWcMatch(boolean useWcMatch) {
        this.useWcMatch = useWcMatch;
    }

    /**
     * Return the quasi-identifiers.
     * @return
     */
    public Set<String> getQis() {
        return qis;
    }
    
    /**
     * Returns whether a field is a quasi-identifier.
     * @param field
     * @return
     */
    public boolean isQi(String field) {
        return qis.contains(field);
    }

    /**
     * Set the quasi-identifiers.
     * @param qis
     */
    public void setQis(Set<String> qis) {
        this.qis = qis;        
    }
}
