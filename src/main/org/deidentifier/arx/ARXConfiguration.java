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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.criteria.LDiversity;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.criteria.TCloseness;
import org.deidentifier.arx.framework.data.DataManager;

/**
 * A generic configuration for the ARX anonymizer
 * @author Fabian Prasser
 */
public class ARXConfiguration implements Serializable{
    
    private static final long serialVersionUID = -6713510386735241964L;

    /** Do we assume practical monotonicity */
    private boolean                                practicalMonotonicity           = false;

    /** Relative tuple outliers */
    private double                                 relMaxOutliers = -1;
    
    /** Absolute tuple outliers*/
    private int                                    absMaxOutliers = 0;
    
    /** Criteria*/
    private PrivacyCriterion[] criteria = new PrivacyCriterion[0];

    /** Optimized list of criteria*/
    private PrivacyCriterion[] optimizedCriteria = new PrivacyCriterion[0];
    
    /** Do the criteria require a counter per equivalence class*/
    public static final int REQUIREMENT_COUNTER = 0x1;
    
    /** Do the criteria require a second counter */
    public static final int REQUIREMENT_SECONDARY_COUNTER = 0x2;
    
    /** Do the criteria require distributions of sensitive values in the equivalence classes */
    public static final int REQUIREMENT_DISTRIBUTION = 0x4;
    
    /** The requirements per equivalence class*/
    private int requirements = 0x0;
    
    /**
     * Creates a new config without tuple suppression
     */
    public ARXConfiguration(){
        this.relMaxOutliers = 0d;
    }
    
    /**
     * Allows for a certain percentage of outliers and thus
     * triggers tuple suppresion
     * @param supp
     */
    public void setAllowedOutliers(double supp){
        this.relMaxOutliers = supp;
    }
    
    /**
     * Creates a new config that allows the given percentage of outliers and
     * thus implements tuple suppression
     * @param supp
     */
    public ARXConfiguration(double supp){
        this.relMaxOutliers = supp;
    }
    
    /**
     * Returns the maximum number of allowed outliers
     * @return
     */
    public final double getRelativeMaxOutliers() {
        return relMaxOutliers;
    }
    
    /**
     * Returns the maximum number of allowed outliers
     * @return
     */
    public final int getAbsoluteMaxOutliers() {
        return this.absMaxOutliers;
    }

    /**
     * Is practical monotonicity assumed
     * @return
     */
    public boolean isPracticalMonotonicity() {
        return practicalMonotonicity;
    }
    
    /**
     * Returns the criterias requirements
     * @return
     */
    public int getRequirements(){
        return this.requirements;
    }
    
    /**
     * Convenience method for checking the requirements
     * @param requirement
     * @return
     */
    public boolean requires(int requirement){
        return (this.requirements & requirement) != 0;
    }

    public boolean containsCriterion(Class<? extends PrivacyCriterion> clazz){
        for (PrivacyCriterion c : criteria){
            if (clazz.isInstance(c)){
                return true;
            }
        }
        return false;
    }
    
    /**
     * Adds a criterion to the configuration
     * @param c
     */
    public void addCriterion(PrivacyCriterion c){
        criteria = Arrays.copyOf(criteria, criteria.length+1);
        criteria[criteria.length-1] = c;
    }
    
    /**
     * Returns all privacy criteria that are instances of the given class
     * @param clazz
     * @return
     */
    @SuppressWarnings("unchecked")
    public<T extends PrivacyCriterion> T getCriterion(Class<T> clazz){
        Set<T> result = new HashSet<T>();
        for (PrivacyCriterion c : criteria){
            if (clazz.isInstance(c)){
                result.add((T)c);
            }
        }
        if (result.size()>1) {
            throw new RuntimeException("More than one matches the query!");
        } else if (result.size()==1){ 
            return result.iterator().next();
        } else {
            return null;
        }
    }

    /**
     * Initializes the configuration
     * @param manager
     */
    protected void initialize(DataManager manager) {
        
        // Compute requirements
        this.requirements = 0x0;
        for (PrivacyCriterion c : criteria){
            c.initialize(manager);
            this.requirements |= c.getRequirements();
        }
        
        // Compute max outliers
        absMaxOutliers = (int)Math.floor(this.relMaxOutliers * (double)manager.getDataQI().getDataLength());
        
        // Compute optimized array with criteria, assuming complexities 
        // dPresence <= lDiversity <= tCloseness and ignoring kAnonymity
        // TODO: Configuration should not know anything about them
        List<PrivacyCriterion> list = new ArrayList<PrivacyCriterion>();
        if (this.containsCriterion(DPresence.class)){
            list.add(this.getCriterion(DPresence.class));
        }
        if (this.containsCriterion(LDiversity.class)){
            list.add(this.getCriterion(LDiversity.class));
        }
        if (this.containsCriterion(TCloseness.class)){
            list.add(this.getCriterion(TCloseness.class));
        }
        this.optimizedCriteria = list.toArray(new PrivacyCriterion[0]);
        
        // Change order of criteria
        if (this.containsCriterion(KAnonymity.class)){
            list.add(0, this.getCriterion(KAnonymity.class));
            this.criteria = list.toArray(new PrivacyCriterion[0]); 
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
            if (!c.isMonotonic()) return false;
        }
        // Yes
        return true;
    }

    /**
     * Returns the specific length of each entry in a snapshot
     * @return
     */
    public int getSnapshotLength() {
        int length = 2;
        if (this.requires(REQUIREMENT_DISTRIBUTION)){
            length += 2;
        }
        if (this.requires(REQUIREMENT_SECONDARY_COUNTER)){
            length +=1;
        }
        return length;
    }

    /**
     * Returns all criteria
     * @return
     */
    public PrivacyCriterion[] getCriteria() {
        return this.criteria;
    }

    /**
     * Returns all criteria, not including k-anonymity
     * @return
     */
    public PrivacyCriterion[] getCriteriaIgnoreKAnonymity() {
        return this.optimizedCriteria;
    }
}
