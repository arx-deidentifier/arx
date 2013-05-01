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

package org.deidentifier.flash.gui;

import java.io.Serializable;

import org.deidentifier.flash.AttributeType.Hierarchy;
import org.deidentifier.flash.Data;
import org.deidentifier.flash.FLASHAnonymizer;
import org.deidentifier.flash.FLASHConfiguration.Criterion;
import org.deidentifier.flash.FLASHConfiguration.LDiversityCriterion;
import org.deidentifier.flash.FLASHConfiguration.TClosenessCriterion;
import org.deidentifier.flash.metric.Metric;

public class Configuration implements Serializable {

    private static final long   serialVersionUID      = -2887699232096897527L;

    private transient Hierarchy sensitiveHierarchy    = null;
    private transient Data      input                 = null;

    private boolean             removeOutliers        = true;
    private int                 k                     = 2;
    private double              relativeMaxOutliers   = 0d;
    private Metric<?>           metric                = Metric.createNMEntropyMetric();
    private Criterion           criterion             = Criterion.K_ANONYMITY;
    private int                 l                     = 2;
    private double              c                     = 0.001d;
    private boolean             practicalMonotonicity = false;
    private double              t                     = 0.001d;

    private LDiversityCriterion lDiversityCriterion   = LDiversityCriterion.DISTINCT;
    private TClosenessCriterion tClosenessCriterion   = TClosenessCriterion.EMD_EQUAL;

    private boolean             modified              = true;

    @Override
    public Configuration clone() {

        final Configuration c = new Configuration();
        c.removeOutliers = removeOutliers;
        c.k = k;
        c.relativeMaxOutliers = relativeMaxOutliers;
        // TODO: Might be necessary to clone the metric, if e.g. weights can be
        // defined
        c.metric = metric;
        c.criterion = criterion;
        c.l = l;
        c.c = this.c;
        c.practicalMonotonicity = practicalMonotonicity;
        c.t = t;
        c.lDiversityCriterion = lDiversityCriterion;
        c.tClosenessCriterion = tClosenessCriterion;

        if (sensitiveHierarchy != null) {
            c.sensitiveHierarchy = sensitiveHierarchy.clone();
        } else {
            c.sensitiveHierarchy = null;
        }
        c.input = input.clone();

        return c;

    }

    public FLASHAnonymizer getAnonymizer(final FLASHAnonymizer anonymizer) {
        anonymizer.setMetric(getMetric());
        anonymizer.setPracticalMonotonicity(getPracticalMonotonicity());
        anonymizer.setRemoveOutliers(getRemoveOutliers());
        return anonymizer;
    }

    public double getC() {
        return c;
    }

    public Criterion getCriterion() {
        return criterion;
    }

    /**
     * @return the input
     */
    public Data getInput() {
        return input;
    }

    /**
     * @return the k
     */
    public int getK() {
        return k;
    }

    public int getL() {
        return l;
    }

    public LDiversityCriterion getLDiversityCriterion() {
        return lDiversityCriterion;
    }

    /**
     * @return the metric
     */
    public Metric<?> getMetric() {
        return metric;
    }

    public boolean getPracticalMonotonicity() {
        return practicalMonotonicity;
    }

    /**
     * @return the relativeMaxOutliers
     */
    public double getRelativeMaxOutliers() {
        return relativeMaxOutliers;
    }

    public boolean getRemoveOutliers() {
        return removeOutliers;
    }

    public Hierarchy getSensitiveHierarchy() {
        return sensitiveHierarchy;
    }

    public double getT() {
        return t;
    }

    public TClosenessCriterion getTClosenessCriterion() {
        return tClosenessCriterion;
    }

    public boolean isModified() {
        return modified;
    }

    public boolean isRemoveOutliers() {
        return removeOutliers;
    }

    public void setC(final double c) {
        setModified();
        this.c = c;
    }

    public void setCriterion(final Criterion criterion) {
        setModified();
        this.criterion = criterion;
    }

    /**
     * @param data
     *            the input to set
     */
    public void setInput(final Data data) {
        input = data;
        setModified();
    }

    /**
     * @param k
     *            the k to set
     */
    public void setK(final int k) {
        this.k = k;
        setModified();
    }

    public void setL(final int l) {
        setModified();
        this.l = l;
    }

    public void setLDiversityCriterion(final LDiversityCriterion criterion) {
        lDiversityCriterion = criterion;
        setModified();
    }

    public void setMetric(final Metric<?> metric) {
        this.metric = metric;
        setModified();
    }

    private void setModified() {
        modified = true;
    }

    public void setPracticalMonotonicity(final boolean practical) {
        setModified();
        practicalMonotonicity = practical;
    }

    /**
     * @param relativeMaxOutliers
     *            the relativeMaxOutliers to set
     */
    public void setRelativeMaxOutliers(final double relativeMaxOutliers) {
        this.relativeMaxOutliers = relativeMaxOutliers;
        setModified();
    }

    public void setRemoveOutliers(final boolean removeOutliers) {
        this.removeOutliers = removeOutliers;
        setModified();
    }

    public void setSensitiveHierarchy(final Hierarchy h) {
        sensitiveHierarchy = h;
        setModified();
    }

    public void setT(final double t) {
        setModified();
        this.t = t;
    }

    public void setTClosenessCriterion(final TClosenessCriterion criterion) {
        tClosenessCriterion = criterion;
        setModified();
    }

    public void setUnmodified() {
        modified = false;
    }

    /**
     * Checks whether the lattice is too large
     * 
     * @return
     */
    public boolean validLatticeSize(final int max) {
        int size = 1;
        for (final String attr : input.getDefinition()
                                      .getQuasiIdentifyingAttributes()) {
            final int factor = input.getDefinition()
                                    .getMaximumGeneralization(attr) -
                               input.getDefinition()
                                    .getMinimumGeneralization(attr);
            size *= factor;
        }
        return size <= max;
    }
}
