/*
 * ARX: Powerful Data Anonymization
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
package org.deidentifier.arx.metric.v2;

import java.util.List;
import java.util.Map;

import org.deidentifier.arx.metric.Metric;
import org.deidentifier.arx.metric.Metric.AggregateFunction;

/**
 * This internal class provides access to version 2 of all metrics. Users of the API should use
 * <code>org.deidentifier.arx.metric.Metric<code> for creating instances of metrics for information loss.
 * 
 * @author Fabian Prasser
 */
public class __MetricV2 {

    /**
     * Creates a new instance of the AECS metric.
     * 
     * @return
     */
    public static Metric<ILSingleDimensional> createAECSMetric() {
        return new MetricSDAECS();
    }
    
    /**
     * Creates an instance of the discernability metric
     * 
     * @param monotonic If set to true, the monotonic variant (DM*) will be created
     * @return
     */
    public static Metric<ILSingleDimensional> createDiscernabilityMetric() {
        return createDiscernabilityMetric(false);
    }
    

    /**
     * Creates an instance of the discernability metric. The monotonic variant is DM*.
     * 
     * @param monotonic If set to true, the monotonic variant (DM*) will be created
     * @return
     */
    public static Metric<ILSingleDimensional> createDiscernabilityMetric(boolean monotonic) {
        if (monotonic) {
            return new MetricSDDiscernability();
        } else {
            return new MetricSDNMDiscernability();
        }
    }


    /**
     * Creates an instance of the non-monotonic non-uniform entropy metric. The default aggregate function,
     * which is the sum-function, will be used for comparing results.
     * This metric will respect attribute weights defined in the configuration.
     * 
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createEntropyMetric() {
        return createEntropyMetric(false, AggregateFunction.SUM);
    }
    

    /**
     * Creates an instance of the non-uniform entropy metric. The default aggregate function,
     * which is the sum-function, will be used for comparing results.
     * This metric will respect attribute weights defined in the configuration.
     * 
     * @param monotonic If set to true, the monotonic variant of the metric will be created
     * 
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createEntropyMetric(boolean monotonic) {
        return createEntropyMetric(monotonic, AggregateFunction.SUM);
    }
    
    /**
     * Creates an instance of the non-uniform entropy metric.
     * This metric will respect attribute weights defined in the configuration.
     * 
     * @param monotonic If set to true, the monotonic variant of the metric will be created
     * @param function The aggregate function to be used for comparing results
     * 
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createEntropyMetric(boolean monotonic, AggregateFunction function) {
        if (monotonic) {
            return new MetricMDNUEntropy(function);
        } else {
            return new MetricMDNUNMEntropy(function);
        }
    }

    /**
     * Creates an instance of the height metric. The default aggregate function, which is the sum-function,
     * will be used for comparing results.
     * This metric will respect attribute weights defined in the configuration.
     * 
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createHeightMetric() {
        return new MetricMDHeight();
    }


    /**
     * Creates an instance of the height metric.
     * This metric will respect attribute weights defined in the configuration. 
     * 
     * @param function The aggregate function to use for comparing results
     * 
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createHeightMetric(AggregateFunction function) {
        return new MetricMDHeight(function);
    }

    /**
     * Creates an instance of the loss metric which treats generalization and suppression equally.
     * The default aggregate function, which is the rank function, will be used.
     * This metric will respect attribute weights defined in the configuration.
     */
    public static Metric<AbstractILMultiDimensional> createLossMetric() {
        return new MetricMDNMLoss();
    }

    /**
     * Creates an instance of the loss metric which treats generalization and suppression equally.
     * This metric will respect attribute weights defined in the configuration.
     * 
     * @param function The aggregate function to use for comparing results
     */
    public static Metric<AbstractILMultiDimensional> createLossMetric(AggregateFunction function) {
        return new MetricMDNMLoss(function);
    }
    /**
     * Creates an instance of the loss metric with factors for weighting generalization and suppression.
     * The default aggregate function, which is the rank function, will be used.
     * This metric will respect attribute weights defined in the configuration.
     * 
     * @param gsFactor A factor [0,1] weighting generalization and suppression. 
     *                 The default value is 0.5, which means that generalization
     *                 and suppression will be treated equally. A factor of 0
     *                 will favor suppression, and a factor of 1 will favor
     *                 generalization. The values in between can be used for
     *                 balancing both methods. 
     */
    public static Metric<AbstractILMultiDimensional> createLossMetric(double gsFactor) {
        return new MetricMDNMLoss(gsFactor, AggregateFunction.RANK);
    }

    /**
     * Creates an instance of the loss metric with factors for weighting generalization and suppression.
     * This metric will respect attribute weights defined in the configuration.
     * 
     * @param gsFactor A factor [0,1] weighting generalization and suppression. 
     *                 The default value is 0.5, which means that generalization
     *                 and suppression will be treated equally. A factor of 0
     *                 will favor suppression, and a factor of 1 will favor
     *                 generalization. The values in between can be used for
     *                 balancing both methods. 
     *                 
     * @param function The aggregate function to use for comparing results
     */
    public static Metric<AbstractILMultiDimensional> createLossMetric(double gsFactor, AggregateFunction function) {
        return new MetricMDNMLoss(gsFactor, function);
    }

    /**
     * Creates an instance of the non-monotonic precision metric.
     * The default aggregate function, which is the arithmetic mean, will be used.
     * This metric will respect attribute weights defined in the configuration.
     * 
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createPrecisionMetric() {
        return createPrecisionMetric(false, AggregateFunction.ARITHMETIC_MEAN);
    }
    
    /**
     * Creates an instance of the non-monotonic precision metric.
     * This metric will respect attribute weights defined in the configuration.
     * 
     * @param function The aggregate function to use for comparing results
     * 
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createPrecisionMetric(AggregateFunction function) {
        return createPrecisionMetric(false, function);
    }

    /**
     * Creates an instance of the precision metric.
     * The default aggregate function, which is the arithmetic mean, will be used.
     * This metric will respect attribute weights defined in the configuration.
     * 
     * @param monotonic If set to true, the monotonic variant of the metric will be created
     * 
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createPrecisionMetric(boolean monotonic) {
        return createPrecisionMetric(monotonic, AggregateFunction.ARITHMETIC_MEAN);
    }
    /**
     * Creates an instance of the precision metric.
     * This metric will respect attribute weights defined in the configuration.
     * 
     * @param monotonic If set to true, the monotonic variant of the metric will be created
     * 
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createPrecisionMetric(boolean monotonic, AggregateFunction function) {
        if (monotonic) {
            return new MetricMDPrecision(function);
        } else {
            return new MetricMDNMPrecision(function);
        }
    }

    /**
     * Creates a potentially precomputed instance of the non-monotonic non-uniform entropy metric. The default aggregate function,
     * which is the sum-function, will be used for comparing results.
     * This metric will respect attribute weights defined in the configuration.
     * 
     * @param threshold The precomputed variant of the metric will be used if 
     *                  #distinctValues / #rows <= threshold for all quasi-identifiers.
     *                  
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createPrecomputedEntropyMetric(double threshold) {
        return createPrecomputedEntropyMetric(threshold, false, AggregateFunction.SUM);
    }

    /**
     * Creates a potentially precomputed instance of the non-uniform entropy metric. The default aggregate function,
     * which is the sum-function, will be used for comparing results.
     * This metric will respect attribute weights defined in the configuration.
     * 
     * @param threshold The precomputed variant of the metric will be used if 
     *                  #distinctValues / #rows <= threshold for all quasi-identifiers.
     * @param monotonic If set to true, the monotonic variant of the metric will be created
     * 
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createPrecomputedEntropyMetric(double threshold, boolean monotonic) {
        return createPrecomputedEntropyMetric(threshold, monotonic, AggregateFunction.SUM);
    }


    /**
     * Creates a potentially precomputed instance of the non-uniform entropy metric.
     * This metric will respect attribute weights defined in the configuration.
     * 
     * @param threshold The precomputed variant of the metric will be used if 
     *                  #distinctValues / #rows <= threshold for all quasi-identifiers.
     * @param monotonic If set to true, the monotonic variant of the metric will be created
     * @param function The aggregate function to be used for comparing results
     * 
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createPrecomputedEntropyMetric(double threshold, boolean monotonic, AggregateFunction function) {
        if (monotonic) {
            return new MetricMDNUEntropyPotentiallyPrecomputed(threshold, function);
        } else {
            return new MetricMDNUNMEntropyPotentiallyPrecomputed(threshold, function);
        }
    }

    /**
     * Creates a potentially precomputed instance of the loss metric which treats generalization 
     * and suppression equally. 
     * The default aggregate function, which is the rank function, will be used.
     * This metric will respect attribute weights defined in the configuration.
     * 
     * @param threshold The precomputed variant of the metric will be used if 
     *                  #distinctValues / #rows <= threshold for all quasi-identifiers.
     */
    public static Metric<AbstractILMultiDimensional> createPrecomputedLossMetric(double threshold) {
        return new MetricMDNMLossPotentiallyPrecomputed(threshold);
    }

    /**
     * Creates a potentially precomputed instance of the loss metric which treats generalization and suppression equally.
     * This metric will respect attribute weights defined in the configuration.
     * 
     * @param function The aggregate function to use for comparing results
     * 
     * @param threshold The precomputed variant of the metric will be used if 
     *                  #distinctValues / #rows <= threshold for all quasi-identifiers.
     */
    public static Metric<AbstractILMultiDimensional> createPrecomputedLossMetric(double threshold, AggregateFunction function) {
        return new MetricMDNMLossPotentiallyPrecomputed(threshold, function);
    }

    /**
     * Creates a potentially precomputed instance of the loss metric with factors for weighting generalization and suppression.
     * The default aggregate function, which is the rank function, will be used.
     * This metric will respect attribute weights defined in the configuration.
     * 
     * @param gsFactor A factor [0,1] weighting generalization and suppression. 
     *                 The default value is 0.5, which means that generalization
     *                 and suppression will be treated equally. A factor of 0
     *                 will favor suppression, and a factor of 1 will favor
     *                 generalization. The values in between can be used for
     *                 balancing both methods. 
     *                 
     * @param threshold The precomputed variant of the metric will be used if 
     *                  #distinctValues / #rows <= threshold for all quasi-identifiers.
     */
    public static Metric<AbstractILMultiDimensional> createPrecomputedLossMetric(double threshold, double gsFactor) {
        return new MetricMDNMLossPotentiallyPrecomputed(threshold, gsFactor, AggregateFunction.RANK);
    }
    
    /**
     * Creates a potentially precomputed instance of the loss metric with factors for weighting generalization and suppression.
     * This metric will respect attribute weights defined in the configuration.
     * 
     * @param gsFactor A factor [0,1] weighting generalization and suppression. 
     *                 The default value is 0.5, which means that generalization
     *                 and suppression will be treated equally. A factor of 0
     *                 will favor suppression, and a factor of 1 will favor
     *                 generalization. The values in between can be used for
     *                 balancing both methods. 
     *                 
     * @param function The aggregate function to use for comparing results
     * 
     * @param threshold The precomputed variant of the metric will be used if 
     *                  #distinctValues / #rows <= threshold for all quasi-identifiers.
     */
    public static Metric<AbstractILMultiDimensional> createPrecomputedLossMetric(double threshold, double gsFactor, AggregateFunction function) {
        return new MetricMDNMLossPotentiallyPrecomputed(threshold, gsFactor, function);
    }

    /**
     * Creates an instance of a metric with statically defined information loss. 
     * The default aggregate function, which is the sum-function, will be used for comparing results.
     * This metric will respect attribute weights defined in the configuration.
     * 
     * @param loss User defined information loss per attribute
     * 
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createStaticMetric(Map<String, List<Double>> loss) {
        return new MetricMDStatic(loss);
    }


    /**
     * Creates an instance of a metric with statically defined information loss. 
     * This metric will respect attribute weights defined in the configuration. 
     * 
     * @param loss User defined information loss per attribute
     * @param function The aggregate function to use for comparing results
     * 
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createStaticMetric(Map<String, List<Double>> loss, AggregateFunction function) {
        return new MetricMDStatic(function, loss);
    }

}
