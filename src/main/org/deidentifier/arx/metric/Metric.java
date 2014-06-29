/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.arx.metric;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.framework.check.groupify.IHashGroupify;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Node;

/**
 * Abstract base class for metrics
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 *
 * @param <T>
 */
public abstract class Metric<T extends InformationLoss<?>> implements Serializable {

    private static final long serialVersionUID = -2657745103125430229L;

    /**
     * Creates an average equivalence class size
     * 
     * @return
     */
    public static Metric<InformationLossDefault> createAECSMetric() {
        return new MetricAECS();
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
     * Creates an instance of the NDS metric that treats suppression, generalization and
     * all attributes equally.
     * This metric will respect attribute weights defined in the configuration.
     * @return
     */
    public static Metric<?> createNDSMetric() {
        return new MetricNDS();
    }
    

    /**
     * Creates an NDS metric with factors for weighting generalization and suppression.
     * This metric will respect attribute weights defined in the configuration.
     * 
     * @param gsFactor A factor [0,1] weighting generalization and suppression. 
     *                 The default value is 0.5, which means that generalization
     *                 and suppression will be treated equally. A factor of 0
     *                 will favor generalization, and a factor of 1 will favor
     *                 suppression. The values in between can be used for
     *                 balancing both methods. 
     */
    public static MetricNDS createNDSMetric(double gsFactor) {
        return new MetricNDS(gsFactor);
    }
    
    /**
     * Creates an non-monotonic entropy metric
     * 
     * @return
     */
    public static Metric<InformationLossDefault> createNMEntropyMetric() {
        return new MetricNMEntropy();
    }

    /**
     * Creates a precision metric. 
     * This metric will respect attribute weights defined in the configuration.
     * 
     * @return
     */
    public static Metric<InformationLossDefault> createPrecisionMetric() {
        return new MetricPrecision();
    }

    /**
     * Creates a static metric. It requires a map, which maps the generalization levels
     * (starting at index of the given list) onto an information loss defined as a
     * decimal number.
     * This metric will respect attribute weights defined in the configuration.
     * 
     * @return
     */
    public static Metric<InformationLossDefault> createStaticMetric(Map<String, List<Double>> informationLoss) {
        return new MetricStatic(informationLoss);
    }

    /** The global optimum */
    private transient Node               globalOptimum          = null;

    /** Is the metric independent? */
    private boolean                      independent            = false;

    /** Is the metric monotonic? */
    private boolean                      monotonic              = false;

    /** The optimal information loss */
    private transient InformationLoss<?> optimalInformationLoss = null;

    /**
     * Create a new metric
     * 
     * @param monotonic
     * @param independent
     */
    protected Metric(final boolean monotonic, final boolean independent) {
        this.monotonic = monotonic;
        this.independent = independent;
    }

    /**
     * Returns an instance of the maximal value
     * 
     * @return
     */
    public abstract InformationLoss<?> createMaxInformationLoss();

    /**
     * Returns an instance of the minimal value
     * 
     * @return
     */
    public abstract InformationLoss<?> createMinInformationLoss();

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
        if (node.isAnonymous() && ((globalOptimum == null) || (node.getInformationLoss().compareTo(optimalInformationLoss) < 0) || (node.getInformationLoss().compareTo(optimalInformationLoss) == 0 && node.getLevel() < globalOptimum.getLevel()))) {
            this.globalOptimum = node;
            this.optimalInformationLoss = node.getInformationLoss();
        }
    }

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
     * @param definition 
     * 
     * @param input
     * @param hierarchies
     */
    public final void initialize(final DataDefinition definition, final Data input, final GeneralizationHierarchy[] hierarchies, final ARXConfiguration config) {
        this.globalOptimum = null;
        this.optimalInformationLoss = null;
        initializeInternal(definition, input, hierarchies, config);
    }

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
     * Evaluates the metric for the given node
     * 
     * @param node
     *            The node for which to compute the information loss
     * @param groupify
     *            The groupify operator of the previous check
     * @return the double
     */
    protected abstract T evaluateInternal(final Node node, final IHashGroupify groupify);

    /**
     * Implement this to initialize the metric.
     * 
     * @param input
     * @param hierarchies
     */
    protected abstract void initializeInternal(final DataDefinition definition, final Data input, final GeneralizationHierarchy[] hierarchies, final ARXConfiguration config);
}
