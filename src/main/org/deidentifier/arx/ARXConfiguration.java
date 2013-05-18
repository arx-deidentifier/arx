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

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.framework.CompressedBitSet;
import org.deidentifier.arx.framework.data.DataManager;

public class ARXConfiguration implements Serializable{
    
    private static final long serialVersionUID = -6713510386735241964L;

    public static enum CriterionType implements Serializable{
        K_ANONYMITY,
        L_DIVERSITY,
        T_CLOSENESS,
        D_PRESENCE;
    }
    
    public static abstract class PrivacyCriterion implements Serializable{

        private static final long serialVersionUID = -8460571120677880409L;
        public final CriterionType type;
        public final int snapshotLength;
        public final boolean monotonic;
        public PrivacyCriterion(CriterionType type, int snapshotLength, boolean monotonic){
            this.type = type;
            this.snapshotLength = snapshotLength;
            this.monotonic = monotonic;
        }
        public abstract void initialize(DataManager manager);
    }
    
    public static class KAnonymityCriterion extends PrivacyCriterion{

        private static final long serialVersionUID = -8370928677928140572L;
        public final int k;
        public KAnonymityCriterion(int k){
            super(CriterionType.K_ANONYMITY, 2, true);
            this.k = k;
        }
        @Override
        public void initialize(DataManager manager) {
            // Nothing to do
        }
    }
    public static class LDiversityCriterion extends PrivacyCriterion{

        private static final long serialVersionUID = -5893481096346270328L;

        public static enum DiversityMeasure implements Serializable{
            ENTROPY,
            RECURSIVE,
            DISTINCT
        }
        
        public final int l;
        public final double c;
        public final DiversityMeasure measure;
        
        public LDiversityCriterion(int l, boolean entropy){
            super(CriterionType.L_DIVERSITY, 4, false);
            this.l = l;
            this.c = Double.NaN;
            if (entropy){
                this.measure = DiversityMeasure.DISTINCT;
            } else {
                this.measure = DiversityMeasure.RECURSIVE;
            }
            
        }
        
        public LDiversityCriterion(double c, int l){
            super(CriterionType.L_DIVERSITY, 4, false);
            this.c = c;
            this.l = l;
            this.measure = DiversityMeasure.RECURSIVE;
        }

        @Override
        public void initialize(DataManager manager) {
            // Nothing to do
        }
        
    }
    public static class TClosenessCriterion extends PrivacyCriterion{
        
        private static final long serialVersionUID = -1383357036299011323L;

        public static enum ClosenessMeasure implements Serializable{
            HIERARCHICAL_DISTANCE_EMD,
            EQUAL_DISTANCE_EMD
        }
        
        public final double t;
        public final ClosenessMeasure measure;
        public final Hierarchy hierarchy;
        
        protected double[] distribution;
        protected int[] tree;
        
        public TClosenessCriterion(double t){
            super(CriterionType.T_CLOSENESS, 4, false);
            this.t = t;
            this.measure = ClosenessMeasure.EQUAL_DISTANCE_EMD;
            this.hierarchy = null;
        }
        
        public TClosenessCriterion(double t, Hierarchy h){
            super(CriterionType.T_CLOSENESS, 4, false);
            this.t = t;
            this.measure = ClosenessMeasure.HIERARCHICAL_DISTANCE_EMD;
            hierarchy = h;
        }

        @Override
        public void initialize(DataManager manager) {
            switch (measure){
            case EQUAL_DISTANCE_EMD:
                distribution = manager.getDistribution();
                break;
            case HIERARCHICAL_DISTANCE_EMD:
                tree = manager.getTree();
                break;
            }
        }
    }
    public static class DPresenceCriterion extends PrivacyCriterion{
        
        private static final long serialVersionUID = 8534004943055128797L;
        
        public final double dMin;
        public final double dMax;
        private final Set<Integer> subset;
        protected CompressedBitSet bitset;
        public DPresenceCriterion(double dMin, double dMax, Set<Integer> subset) {
            super(CriterionType.D_PRESENCE, 3, false);
            this.dMin = dMin;
            this.dMax = dMax;
            this.subset = subset;
        }
            
        @Override
        public void initialize(DataManager manager) {
            bitset = new CompressedBitSet(manager.getDataQI().getDataLength());
            for (Integer line : subset) {
                bitset.set(line);
            }
        }
    }
    

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

        if (containsCriterion(LDiversityCriterion.class) ||
            containsCriterion(TClosenessCriterion.class)){
            this.requiresDistributions = true;
        } else {
            this.requiresDistributions = false;
        }
        
        if (containsCriterion(DPresenceCriterion.class)){
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
            if (c instanceof TClosenessCriterion) {
                return false;
            }
            else if (c instanceof LDiversityCriterion) {
                return false;
            }
            else if (c instanceof DPresenceCriterion) {
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
        if (this.containsCriterion(LDiversityCriterion.class) ||
            this.containsCriterion(TClosenessCriterion.class)){
            length += 2;
        }
        if (this.containsCriterion(DPresenceCriterion.class)) {
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
