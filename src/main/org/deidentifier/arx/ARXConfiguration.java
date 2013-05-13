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

import java.util.Set;

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.framework.CompressedBitSet;

public class ARXConfiguration {
    
    public static enum CriterionType {
        K_ANONYMITY,
        L_DIVERSITY,
        T_CLOSENESS,
        D_PRESENCE;
    }
    
    public static abstract class PrivacyCriterion{
        public final CriterionType type;
        public final int snapshotLength;
        public final boolean monotonic;
        public PrivacyCriterion(CriterionType type, int snapshotLength, boolean monotonic){
            this.type = type;
            this.snapshotLength = snapshotLength;
            this.monotonic = monotonic;
        }
    }
    
    public static class KAnonymityCriterion extends PrivacyCriterion{
        
        public final int k;
        public KAnonymityCriterion(int k){
            super(CriterionType.K_ANONYMITY, 2, true);
            this.k = k;
        }
    }
    public static class LDiversityCriterion extends PrivacyCriterion{
        
        public static enum DiversityMeasure{
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
        
    }
    public static class TClosenessCriterion extends PrivacyCriterion{
        public static enum ClosenessMeasure{
            HIERARCHICAL_EMD,
            BASIC_EMD
        }
        
        public final double t;
        public final ClosenessMeasure measure;
        public final Hierarchy hierarchy;
        
        public TClosenessCriterion(double t){
            super(CriterionType.T_CLOSENESS, 4, false);
            this.t = t;
            this.measure = ClosenessMeasure.BASIC_EMD;
            this.hierarchy = null;
        }
        
        public TClosenessCriterion(double t, Hierarchy h){
            super(CriterionType.T_CLOSENESS, 4, false);
            this.t = t;
            this.measure = ClosenessMeasure.HIERARCHICAL_EMD;
            hierarchy = h;
        }
        
    }
    public static class DPresenceCriterion extends PrivacyCriterion{
        
        public final double dMin;
        public final double dMax;
        private final Set<Integer> subset;
        public DPresenceCriterion(double dMin, double dMax, Set<Integer> subset) {
            super(CriterionType.D_PRESENCE, 3, false);
            this.dMin = dMin;
            this.dMax = dMax;
            this.subset = subset;
        }
        public CompressedBitSet getResearchSubset(Data data){
            // TODO: Is it a good idea to call getHandle() here?
            CompressedBitSet c = new CompressedBitSet(data.getHandle().getNumRows());
                for (Integer line : subset) {
                    c.set(line);
                }
                return c;
            }
        }
    

    /** Do we assume practical monotonicity */
    private boolean                                practicalMonotonicity           = false;

    /** Relative tuple outliers */
    private double                                 maxOutliers             = -1;

    /** Criteria*/
    private PrivacyCriterion[] criteria;
    
    public final double getRelativeMaxOutliers() {
        return maxOutliers;
    }
    
    public final int getAbsoluteMaxOutliers(int dataLength) {
        return (int)Math.floor(this.maxOutliers * (double)dataLength);
    }

    public boolean isPracticalMonotonicity() {
        return practicalMonotonicity;
    }

    public boolean containsCriterion(CriterionType type){
        for (PrivacyCriterion c : criteria){
            if (c.type == type){
                return true;
            }
        }
        return false;
    }
}
