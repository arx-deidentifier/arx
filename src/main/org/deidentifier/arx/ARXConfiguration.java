/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.arx;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.criteria.LDiversity;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.criteria.TCloseness;
import org.deidentifier.arx.framework.data.DataManager;

public class ARXConfiguration implements Serializable{
    
    private static final long serialVersionUID = -6713510386735241964L;

    /** Do we assume practical monotonicity */
    private boolean                                practicalMonotonicity           = false;

    /** Relative tuple outliers */
    private double                                 relMaxOutliers = -1;
    
    /** Absolute tuple outliers*/
    private int                                    absMaxOutliers = 0;
    
    /** Do the criteria require distributions of sensitive values in the equivalence classes */
    private boolean requiresDistributions = false;
    
    /** Do the criteria require a second counter */
    private boolean requiresSecondCounter = false;

    /** Criteria*/
    private PrivacyCriterion[] criteria = new PrivacyCriterion[0];
    
    public final double getRelativeMaxOutliers() {
        return relMaxOutliers;
    }
    
    public final int getAbsoluteMaxOutliers() {
        return this.absMaxOutliers;
    }

    public boolean isPracticalMonotonicity() {
        return practicalMonotonicity;
    }

    public boolean containsCriterion(Class<? extends PrivacyCriterion> clazz){
        for (PrivacyCriterion c : criteria){
            if (c.getClass().equals(clazz)){
                return true;
            }
        }
        return false;
    }
    
    public void addCriterion(PrivacyCriterion c){
        criteria = Arrays.copyOf(criteria, criteria.length+1);
        criteria[criteria.length-1] = c;
    }
    
    @SuppressWarnings("unchecked")
    public<T extends PrivacyCriterion> T[] getCriteria(Class<T> clazz){
        Set<T> result = new HashSet<T>();
        for (PrivacyCriterion c : criteria){
            if (c.getClass().equals(clazz)){
                result.add((T)c);
            }
        }
        return (T[]) result.toArray();
    }

    public void initialize(DataManager manager) {
        for (PrivacyCriterion c : criteria){
            c.initialize(manager);
        }
        absMaxOutliers = (int)Math.floor(this.relMaxOutliers * (double)manager.getDataQI().getDataLength());

        if (containsCriterion(LDiversity.class) ||
            containsCriterion(TCloseness.class)){
            this.requiresDistributions = true;
        } else {
            this.requiresDistributions = false;
        }
        
        if (containsCriterion(DPresence.class)){
            this.requiresSecondCounter = true;
        } else {
            this.requiresSecondCounter = false;
        }
    }
    

    /**
     * Determines whether the anonymity criterion is montonic
     * 
     * @return
     */
    public final boolean isCriterionMonotonic() {

        if (relMaxOutliers == 0d) { return true; }

        for (PrivacyCriterion c : criteria){
            if (c instanceof TCloseness) {
                return false;
            }
            else if (c instanceof LDiversity) {
                return false;
            }
            else if (c instanceof DPresence) {
                throw new UnsupportedOperationException();
            }
        }
        // Only k-anonymity
        return true;
    }

    /**
     * Returns the specific length of each entry in a snapshot
     * @return
     */
    public int getSnapshotLength() {
        int length = 2;
        if (this.containsCriterion(LDiversity.class) ||
            this.containsCriterion(TCloseness.class)){
            length += 2;
        }
        if (this.containsCriterion(DPresence.class)) {
            length += 1;
        }
        return length;
    }
    
    /**
     * Do the criteria require distributions of sensitive values in the equivalence classes
     * @return
     */
    public boolean requiresDistributions() {
        return this.requiresDistributions;
    }
    
    /**
     * Do the criteria require a second counter per equivalence class
     * @return
     */
    public boolean requiresSecondCounter() {
        return this.requiresSecondCounter;
    }
}
