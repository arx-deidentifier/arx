/*
 * FLASH: Efficient, Stable and Optimal Data Anonymization
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

package org.deidentifier.flash;

import org.deidentifier.flash.AttributeType.Hierarchy;

public interface FLASHConfiguration {

    public static enum Criterion {
        K_ANONYMITY,
        L_DIVERSITY,
        T_CLOSENESS,
        D_PRESENCE;
    }

    public static enum LDiversityCriterion {
        DISTINCT,
        ENTROPY,
        RECURSIVE;
    }

    public static enum TClosenessCriterion {
        EMD_EQUAL,
        EMD_HIERARCHICAL;
    }

    /**
     * Returns the absolute maximum of outliers
     * 
     * @return
     */
    public abstract int getAbsoluteMaxOutliers();

    /**
     * @return the c
     */
    public abstract double getC();

    /**
     * @return the criterion
     */
    public abstract FLASHConfiguration.Criterion getCriterion();

    /**
     * @return the k
     */
    public abstract int getK();

    /**
     * @return the l
     */
    public abstract int getL();

    /**
     * @return the ldiversityCriterion
     */
    public abstract FLASHConfiguration.LDiversityCriterion
            getLDiversityCriterion();

    /**
     * @return the relativeMaxOutliers
     */
    public abstract double getRelativeMaxOutliers();

    /**
     * Returns the hierarchy required for EMD_HIERARCHICAL t-closeness
     * 
     * @return
     */
    public abstract Hierarchy getSensitiveHierarchy();

    /**
     * @return the t
     */
    public abstract double getT();

    /**
     * @return the tclosenessCriterion
     */
    public abstract FLASHConfiguration.TClosenessCriterion
            getTClosenessCriterion();

    /**
     * Returns whether practical monotonicity has been assumed
     * 
     * @return
     */
    public abstract boolean isPracticalMonotonicity();
}
