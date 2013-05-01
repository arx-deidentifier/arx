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

package org.deidentifier.arx.framework;

import java.util.Set;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.AttributeType.Hierarchy;

public final class Configuration implements ARXConfiguration {

    public static enum HistoryPruning {
        ANONYMOUS,
        K_ANONYMOUS,
        CHECKED
    }

    public static enum HistorySize {
        RESTRICTED_HISTORY_SIZE,
        UNRESTRICTED_HISTORY_SIZE
    }

    public static Configuration getDeserializedConfiguration(final ARXConfiguration.Criterion criterion,
                                                             final int absoluteMaxOutliers,
                                                             final double relativeMaxOutliers,
                                                             final int k,
                                                             final int l,
                                                             final double c,
                                                             final ARXConfiguration.LDiversityCriterion lDiversityCriterion,
                                                             final double t,
                                                             final ARXConfiguration.TClosenessCriterion tClosenessCriterion,
                                                             final boolean practicalMonotonicity) {

        return new Configuration(criterion, absoluteMaxOutliers, relativeMaxOutliers, k, l, c, lDiversityCriterion, t, tClosenessCriterion, practicalMonotonicity);
    }

    public static Configuration getKAnonymityConfiguration(final double relativeMaxOutliers, final int k) {
        return new Configuration(ARXConfiguration.Criterion.K_ANONYMITY, 2, relativeMaxOutliers, k);
    }

    public static Configuration getLDiversityConfiguration(final double relativeMaxOutliers, final double c, final int l) {
        return new Configuration(ARXConfiguration.Criterion.L_DIVERSITY, 4, relativeMaxOutliers, c, l, ARXConfiguration.LDiversityCriterion.RECURSIVE);
    }

    public static Configuration getLDiversityConfiguration(final double relativeMaxOutliers, final double c, final int l, final ARXConfiguration.LDiversityCriterion ldivmode) {
        return new Configuration(ARXConfiguration.Criterion.L_DIVERSITY, 4, relativeMaxOutliers, c, l, ldivmode);
    }

    public static Configuration getLDiversityConfiguration(final double relativeMaxOutliers, final int l, final ARXConfiguration.LDiversityCriterion ldivmode) {
        return new Configuration(ARXConfiguration.Criterion.L_DIVERSITY, 4, relativeMaxOutliers, l, ldivmode);
    }

    public static Configuration getTClosenessConfiguration(final double relativeMaxOutliers, final int k, final double t) {
        return new Configuration(ARXConfiguration.Criterion.T_CLOSENESS, 4, relativeMaxOutliers, k, t);
    }

    public static Configuration getTClosenessConfiguration(final double relativeMaxOutliers, final int k, final double t, final Hierarchy h) {
        return new Configuration(ARXConfiguration.Criterion.T_CLOSENESS, 4, relativeMaxOutliers, k, t, h);
    }

    public static Configuration getDPresenceConfiguration(final double relativeMaxOutliers, final int k, final double dMin, final double dMax, final Set<Integer> researchSubset) {
        return new Configuration(ARXConfiguration.Criterion.D_PRESENCE, 3, relativeMaxOutliers, k, dMin, dMax, researchSubset);
    }

    /** Criterion */
    private ARXConfiguration.Criterion           criterion                       = null;

    /** Snapshot length */
    private int                                    criterionSpecificSnapshotLength = -1;

    /** Absolute tuple relativeMaxOutliers */
    private int                                    absoluteMaxOutliers             = -1;

    /** Relative tuple relativeMaxOutliers */
    private double                                 relativeMaxOutliers             = -1;

    /** Absolute tuple relativeMaxOutliers computed? */
    private boolean                                absoluteMaxOutliersSet          = false;

    /** K-anonymity */
    private int                                    k                               = -1;

    /** L-Diversity */
    private int                                    l                               = -1;

    /** L-Diversity */
    private double                                 c                               = -1;

    /** L-Diversity */
    private ARXConfiguration.LDiversityCriterion lDiversityCriterion             = null;

    /** T-Closeness */
    private double                                 t                               = -1;

    /** T-Closeness */
    private ARXConfiguration.TClosenessCriterion tClosenessCriterion             = null;

    /** T-Closeness */
    private double[]                               tClosenessDistribution          = null;

    /** T-Closeness */
    private Hierarchy                              tClosenessHierarchy             = null;

    /**
     * Tree data format: #p_count, #leafs, height, freqLeaf_1, ..., freqLeaf_n,
     * extra_1,..., extra_n, [#childs, level, child_1, ... child_x, pos_e,
     * neg_e], ...
     */
    private int[]                                  tree                            = null;

    /** T-Closeness */
    private boolean                                tCloseParamsSet                 = false;

    /** D-Presence */
    private double                                 dMin                            = -1;

    /** D-Presence */
    private double                                 dMax                            = -1;

    /** D-Presence */
    private BitSetCompressed                       researchSubsetBitSet            = null;

    /** D-Presence */
    private Set<Integer>                           researchSubset                  = null;

    /** D-Presence */
    private boolean                                dPresenceParamsSet              = false;

    /** Do we assume practical monotonicity */
    private boolean                                practicalMonotonicity           = false;

    /** The pruning mode in the history */
    private HistoryPruning                         pruning                         = HistoryPruning.ANONYMOUS;

    /** The history size mode */
    private HistorySize                            historySize                     = HistorySize.RESTRICTED_HISTORY_SIZE;

    /**
     * Creates an l-diversity configuration
     * 
     * @param anonymizationMode
     * @param historyEntryLength
     * @param relativeMaxOutliers
     * @param c
     * @param l
     * @param ldivmode
     */
    private Configuration(final ARXConfiguration.Criterion anonymizationMode,
                          final int historyEntryLength,
                          final double relativeMaxOutliers,
                          final double c,
                          final int l,
                          final ARXConfiguration.LDiversityCriterion ldivmode) {
        criterion = anonymizationMode;
        criterionSpecificSnapshotLength = historyEntryLength;
        this.relativeMaxOutliers = relativeMaxOutliers;
        this.c = c;
        this.l = l;
        lDiversityCriterion = ldivmode;
    }

    /**
     * Creates a k-anonymity configuration
     * 
     * @param anonymizationMode
     * @param historyEntryLength
     * @param relativeMaxOutliers
     * @param k
     */
    private Configuration(final ARXConfiguration.Criterion anonymizationMode, final int historyEntryLength, final double relativeMaxOutliers, final int k) {
        criterion = anonymizationMode;
        criterionSpecificSnapshotLength = historyEntryLength;
        this.relativeMaxOutliers = relativeMaxOutliers;
        this.k = k;
    }

    /**
     * Creates a t-closeness configuration
     * 
     * @param anonymizationMode
     * @param historyEntryLength
     * @param relativeMaxOutliers
     * @param k
     * @param t
     */
    private Configuration(final ARXConfiguration.Criterion anonymizationMode, final int historyEntryLength, final double relativeMaxOutliers, final int k, final double t) {
        criterion = anonymizationMode;
        criterionSpecificSnapshotLength = historyEntryLength;
        this.relativeMaxOutliers = relativeMaxOutliers;
        absoluteMaxOutliersSet = false;
        this.k = k;
        this.t = t;
        tClosenessCriterion = ARXConfiguration.TClosenessCriterion.EMD_EQUAL;
    }

    /**
     * Creates a t-closeness configuration
     * 
     * @param anonymizationMode
     * @param historyEntryLength
     * @param relativeMaxOutliers
     * @param k
     * @param t
     * @param tclosedist
     */
    private Configuration(final ARXConfiguration.Criterion anonymizationMode, final int historyEntryLength, final double relativeMaxOutliers, final int k, final double t, final Hierarchy h) {
        criterion = anonymizationMode;
        criterionSpecificSnapshotLength = historyEntryLength;
        this.relativeMaxOutliers = relativeMaxOutliers;
        absoluteMaxOutliersSet = false;
        this.k = k;
        this.t = t;
        tClosenessCriterion = ARXConfiguration.TClosenessCriterion.EMD_HIERARCHICAL;
        tClosenessHierarchy = h;
    }

    /**
     * Creates an l-diversity configuration
     * 
     * @param anonymizationMode
     * @param historyEntryLength
     * @param relativeMaxOutliers
     * @param l
     * @param ldivmode
     */
    private Configuration(final ARXConfiguration.Criterion anonymizationMode,
                          final int historyEntryLength,
                          final double relativeMaxOutliers,
                          final int l,
                          final ARXConfiguration.LDiversityCriterion ldivmode) {
        criterion = anonymizationMode;
        criterionSpecificSnapshotLength = historyEntryLength;
        this.relativeMaxOutliers = relativeMaxOutliers;
        this.l = l;
        lDiversityCriterion = ldivmode;
    }

    /**
     * Creates an d-presence configuration
     * 
     * @param anonymizationMode
     * @param historyEntryLength
     * @param relativeMaxOutliers
     * @param k
     * @param dMin
     * @param dMax
     * @param researchSubset
     */
    private Configuration(final ARXConfiguration.Criterion anonymizationMode,
                          final int historyEntryLength,
                          final double relativeMaxOutliers,
                          final int k,
                          final double dMin,
                          final double dMax,
                          final Set<Integer> researchSubset) {
        criterion = anonymizationMode;
        criterionSpecificSnapshotLength = historyEntryLength;
        this.relativeMaxOutliers = relativeMaxOutliers;
        this.k = k;
        this.dMin = dMin;
        this.dMax = dMax;
        this.researchSubset = researchSubset;
    }

    private Configuration(final ARXConfiguration.Criterion criterion,
                          final int absoluteMaxOutliers,
                          final double relativeMaxOutliers,
                          final int k,
                          final int l,
                          final double c,
                          final ARXConfiguration.LDiversityCriterion lDiversityCriterion,
                          final double t,
                          final ARXConfiguration.TClosenessCriterion tClosenessCriterion,
                          final boolean practicalMonotonicity) {

        switch (criterion) {
        case K_ANONYMITY:
            criterionSpecificSnapshotLength = 2;
            break;
        case L_DIVERSITY:
        case T_CLOSENESS:
            criterionSpecificSnapshotLength = 4;
            break;
        case D_PRESENCE:
            criterionSpecificSnapshotLength = 3;
            break;
        }

        this.criterion = criterion;
        this.absoluteMaxOutliers = absoluteMaxOutliers;
        this.relativeMaxOutliers = relativeMaxOutliers;
        this.k = k;
        this.l = l;
        this.c = c;
        this.lDiversityCriterion = lDiversityCriterion;
        this.t = t;
        this.tClosenessCriterion = tClosenessCriterion;
        this.practicalMonotonicity = practicalMonotonicity;
        pruning = HistoryPruning.CHECKED;
        historySize = HistorySize.RESTRICTED_HISTORY_SIZE;
        absoluteMaxOutliersSet = true;
        tCloseParamsSet = true;
    }

    /**
     * @return the allowedTupelSupression
     */
    @Override
    public final int getAbsoluteMaxOutliers() {
        if (absoluteMaxOutliersSet) {
            return absoluteMaxOutliers;
        } else {
            throw new IllegalArgumentException("absoluteMaxOutliers not set!");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.ARX.f#getC()
     */
    @Override
    public final double getC() {
        return c;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.ARX.f#getCriterion()
     */
    @Override
    public final ARXConfiguration.Criterion getCriterion() {
        return criterion;
    }

    /**
     * Gets the criterion specific snapshot length.
     * 
     * @return the criterionSpecificSnapshotLength
     */
    public final int getCriterionSpecificSnapshotLength() {
        return criterionSpecificSnapshotLength;
    }

    /**
     * @return the dmax
     */
    public final double getDmax() {
        return dMax;
    }

    /**
     * @return the dmin
     */
    public final double getDmin() {
        return dMin;
    }

    public HistoryPruning getHistoryPruning() {
        return pruning;
    }

    public HistorySize getHistorySize() {
        return historySize;
    }

    /**
     * @return the tClosenessDistribution
     */
    public final double[] getInitialDistribution() {
        if (tCloseParamsSet) {
            return tClosenessDistribution;
        } else {
            throw new IllegalArgumentException("tClosenessDistribution not set!");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.ARX.f#getK()
     */
    @Override
    public final int getK() {
        return k;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.ARX.f#getL()
     */
    @Override
    public final int getL() {
        return l;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.ARX.f#getLDiversityCriterion()
     */
    @Override
    public final ARXConfiguration.LDiversityCriterion getLDiversityCriterion() {
        return lDiversityCriterion;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.ARX.f#getRelativeMaxOutliers()
     */
    @Override
    public final double getRelativeMaxOutliers() {
        return relativeMaxOutliers;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.ARX.f#getTClosenessHierarchy()
     */
    @Override
    public final Hierarchy getSensitiveHierarchy() {
        return tClosenessHierarchy;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.ARX.f#getT()
     */
    @Override
    public final double getT() {
        return t;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.ARX.f#getTClosenessCriterion()
     */
    @Override
    public final ARXConfiguration.TClosenessCriterion getTClosenessCriterion() {
        return tClosenessCriterion;
    }

    /**
     * @return the sensitve element tree
     */
    public int[] getTClosenessTree() {
        if (tCloseParamsSet) {
            return tree;
        } else {
            throw new IllegalArgumentException("tree not set!");
        }
    }

    /**
     * Determines whether the anonymity criterion is montonic
     * 
     * @return
     */
    public final boolean isCriterionMonotonic() {

        if (relativeMaxOutliers == 0d) { return true; }

        switch (criterion) {
        case K_ANONYMITY:
            return true;
        case L_DIVERSITY:
        case T_CLOSENESS:
            return false;
        case D_PRESENCE:
            throw new UnsupportedOperationException();
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.ARX.f#isPracticalMonotonicity()
     */
    @Override
    public boolean isPracticalMonotonicity() {
        return practicalMonotonicity;
    }

    public boolean isSubCriterionKAnonymity() {
        switch (criterion) {
        case L_DIVERSITY:
        case T_CLOSENESS:
        case K_ANONYMITY:
            return true;
        case D_PRESENCE:
            throw new UnsupportedOperationException();
        }
        return false;
    }

    /**
     * @param absoluteMaxOutliers
     *            the absoluteMaxOutliers to set
     */
    public final void setAbsoluteMaxOutliers(final int absoluteMaxOutliers) {
        this.absoluteMaxOutliers = absoluteMaxOutliers;
        absoluteMaxOutliersSet = true;
    }

    /**
     * @param tClosenessDistribution
     *            the tClosenessDistribution to set
     */
    public final void setDistribution(final double[] distribution) {
        tClosenessDistribution = distribution;
        if (tClosenessCriterion == ARXConfiguration.TClosenessCriterion.EMD_EQUAL) {
            tCloseParamsSet = true;
        }
    }

    public void setHistoryPruning(final HistoryPruning val) {
        pruning = val;
    }

    public void setHistorySize(final HistorySize val) {
        historySize = val;
    }

    public void setPracticalMonotonicity(final boolean val) {
        practicalMonotonicity = val;
    }

    /**
     * @param tree
     *            the tree to set
     */
    public final void setTree(final int[] tree) {
        this.tree = tree;
        if (tClosenessCriterion == ARXConfiguration.TClosenessCriterion.EMD_HIERARCHICAL) {
            tCloseParamsSet = true;
        }
    }

    /** 
     * 
     * @return the d-presence research subset bitset
     */
    public BitSetCompressed getResearchSubset() {
        if (dPresenceParamsSet) {
            return researchSubsetBitSet;
        } else {
            throw new IllegalArgumentException("dPresence not initialized!");
        }
    }

    public void createResearchBitSet(int dataLength) {
        dPresenceParamsSet = true;
        researchSubsetBitSet = new BitSetCompressed(dataLength);
        for (Integer line : researchSubset) {
            researchSubsetBitSet.set(line);
        }
    }
}
