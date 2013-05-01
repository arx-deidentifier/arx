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

package org.deidentifier.flash.metric;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.deidentifier.flash.framework.check.groupify.IHashGroupify;
import org.deidentifier.flash.framework.data.Data;
import org.deidentifier.flash.framework.data.GeneralizationHierarchy;
import org.deidentifier.flash.framework.lattice.Node;

public abstract class Metric<T extends InformationLoss> implements Serializable {

    private static final long serialVersionUID = -2657745103125430229L;

    /**
     * Creates a weighted metric
     * 
     * @param main
     *            the main metric
     * @return
     */
    public static Metric<InformationLossCombined>
            createCombinedMetric(final Metric<?> main,
                                 final Set<Metric<?>> others) {
        return new MetricCombined(main, others);
    }

    /**
     * Creates a DM* metric
     * 
     * @param k
     * @return
     */
    public static Metric<InformationLossDefault> createDMMetric() {
        return new MetricDM();
    }

    /**
     * Creates a DM* metric
     * 
     * @return
     */
    public static Metric<InformationLossDefault> createDMStarMetric() {
        return new MetricDMStar();
    }

    /**
     * Creates an entropy metric
     * 
     * @return
     */
    public static Metric<InformationLossDefault> createEntropyMetric() {
        return new MetricEntropy();
    }

    /**
     * Creates a height metric
     * 
     * @return
     */
    public static Metric<InformationLossDefault> createHeightMetric() {
        return new MetricHeight();
    }

    /**
     * Creates an non-monotoncic entropy metric
     * 
     * @return
     */
    public static Metric<InformationLossDefault> createNMEntropyMetric() {
        return new MetricNMEntropy();
    }

    /**
     * Creates a precision metric
     * 
     * @return
     */
    public static Metric<InformationLossDefault> createPrecisionMetric() {
        return new MetricPrecision();
    }

    /**
     * Creates a weighted metric
     * 
     * @param weights
     *            the weights
     * @return
     */
    public static Metric<InformationLossCombined>
            createWeightedMetric(final Map<Metric<?>, Double> weights) {
        return new MetricWeighted(weights);
    }

    /** The global optimum */
    private transient Node            globalOptimum          = null;

    /** The optimal information loss */
    private transient InformationLoss optimalInformationLoss = null;

    /** Is the metric monotonic? */
    private boolean                   monotonic              = false;

    /** Is the metric independent? */
    private boolean                   independent            = false;

    /**
     * Create a new metric
     * 
     * @param monotonic
     * @param independent
     */
    public Metric(final boolean monotonic, final boolean independent) {
        this.monotonic = monotonic;
        this.independent = independent;
    }

    /**
     * Evaluates the metric for the given node
     * 
     * @param node
     *            The node for which to compute the information loss
     * @param groupify
     *            The groupify operator of the previous check
     * @return the double
     */
    public final void evaluate(final Node node, final IHashGroupify groupify) {

        // Store the computed values
        node.setInformationLoss(this.evaluateInternal(node, groupify));

        // Store optimum
        // Store global optimum
        if ((globalOptimum == null) ||
            (node.getInformationLoss().compareTo(optimalInformationLoss) < 0)) {
            this.globalOptimum = node;
            this.optimalInformationLoss = node.getInformationLoss();
        }
    }

    /**
     * Evaluates the metric for the given node
     * 
     * @param node
     *            The node for which to compute the information loss
     * @param groupify
     *            The groupify operator of the previous check
     * @return the double
     */
    protected abstract T evaluateInternal(final Node node,
                                          final IHashGroupify groupify);

    /**
     * Returns the global optimum
     * 
     * @return
     */
    public final Node getGlobalOptimum() {
        return globalOptimum;
    }

    /**
     * Initializes the metric.
     * 
     * @param input
     * @param hierarchies
     */
    public final void initialize(final Data input,
                                 final GeneralizationHierarchy[] hierarchies) {
        this.globalOptimum = null;
        this.optimalInformationLoss = null;
        initializeInternal(input, hierarchies);
    }

    /**
     * Implement this to initialize the metric.
     * 
     * @param input
     * @param hierarchies
     */
    protected abstract void
            initializeInternal(final Data input,
                               final GeneralizationHierarchy[] hierarchies);

    /**
     * Returns whether this metric requires the transformed data or groups to
     * determine information loss
     * 
     * @return
     */
    public boolean isIndependent() {
        return independent;
    }

    /**
     * @return the monotonic
     */
    public final boolean isMonotonic() {
        return monotonic;
    }

    /**
     * Returns the maximal value
     * 
     * @return
     */
    public final InformationLoss max() {
        return maxInternal().clone();
    }

    /**
     * Returns an instance of the maximal value
     * 
     * @return
     */
    protected abstract InformationLoss maxInternal();

    /**
     * Returns the minimal value
     * 
     * @return
     */
    public final InformationLoss min() {
        return minInternal().clone();
    }

    /**
     * Returns an instance of the minimal value
     * 
     * @return
     */
    protected abstract InformationLoss minInternal();
}
