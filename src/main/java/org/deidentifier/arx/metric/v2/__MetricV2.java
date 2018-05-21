/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.deidentifier.arx.metric.v2;

import java.util.List;
import java.util.Map;

import org.deidentifier.arx.metric.InformationLoss;
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
     * Creates a new instance of the AECS metric.
     * 
     * @param gsFactor A factor [0,1] weighting generalization and suppression.
     *            The default value is 0.5, which means that generalization
     *            and suppression will be treated equally. A factor of 0
     *            will favor suppression, and a factor of 1 will favor
     *            generalization. The values in between can be used for
     *            balancing both methods.
     * 
     * @return
     */
    public static Metric<ILSingleDimensional> createAECSMetric(double gsFactor) {
        return new MetricSDAECS(gsFactor);
    }
    
    /**
     * Creates a new instance of the AECS metric.
     *
     * @param rowCount
     * @return
     */
    public static Metric<ILSingleDimensional> createAECSMetric(int rowCount) {
        return new MetricSDAECS(rowCount);
    }
    
    /**
     * Creates an instance of the ambiguity metric.
     *
     * @return
     */
    public static Metric<ILSingleDimensional> createAmbiguityMetric() {
        return new MetricSDNMAmbiguity();
    }
    
    /**
     * Creates an instance of the classification metric.
     * 
     * @param gsFactor
     * @return
     */
    public static Metric<ILSingleDimensional> createClassificationMetric(double gsFactor) {
        return new MetricSDClassification(gsFactor);
    }

    /**
     * Creates an instance of the classification metric.
     * @return
     */
    public static Metric<ILSingleDimensional> createClassificationMetric() {
        return new MetricSDClassification();
    }
    
    /**
     * Creates an instance of the discernability metric.
     *
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
        return createDiscernabilityMetric(monotonic, 0);
    }


    /**
     * Creates an instance of the discernability metric. The monotonic variant is DM*.
     * 
     * @param monotonic If set to true, the monotonic variant (DM*) will be created
     * @param numTuples Pre-initialization
     * @return
     */
    public static Metric<ILSingleDimensional> createDiscernabilityMetric(boolean monotonic, double numTuples) {
        if (monotonic) {
            MetricSDDiscernability result = new MetricSDDiscernability();
            result.setNumTuples(numTuples);
            return result;
        } else {
            MetricSDNMDiscernability result = new MetricSDNMDiscernability();
            result.setNumTuples(numTuples);
            return result;
        }
    }

    /**
     * Creates an instance of the entropy-based information loss metric.
     * 
     * @param gsFactor A factor [0,1] weighting generalization and suppression.
     *            The default value is 0.5, which means that generalization
     *            and suppression will be treated equally. A factor of 0
     *            will favor suppression, and a factor of 1 will favor
     *            generalization. The values in between can be used for
     *            balancing both methods.
     * 
     * @return
     */
    public static MetricSDNMEntropyBasedInformationLoss createEntropyBasedInformationLossMetric(double gsFactor) {
        return new MetricSDNMEntropyBasedInformationLoss(gsFactor);
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
            return new MetricMDNUEntropy(0.5d, function);
        } else {
            return new MetricMDNUNMEntropy(0.5d, function);
        }
    }

    /**
     * Creates an instance of the non-uniform entropy metric. The default aggregate function,
     * which is the sum-function, will be used for comparing results.
     * This metric will respect attribute weights defined in the configuration.
     * 
     * @param monotonic If set to true, the monotonic variant of the metric will be created
     * @param gsFactor A factor [0,1] weighting generalization and suppression.
     *            The default value is 0.5, which means that generalization
     *            and suppression will be treated equally. A factor of 0
     *            will favor suppression, and a factor of 1 will favor
     *            generalization. The values in between can be used for
     *            balancing both methods.
     * 
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createEntropyMetric(boolean monotonic, double gsFactor) {
        return createEntropyMetric(monotonic, gsFactor, AggregateFunction.SUM);
    }

    /**
     * Creates an instance of the non-uniform entropy metric.
     * This metric will respect attribute weights defined in the configuration.
     * 
     * @param monotonic If set to true, the monotonic variant of the metric will be created
     * @param gsFactor A factor [0,1] weighting generalization and suppression.
     *            The default value is 0.5, which means that generalization
     *            and suppression will be treated equally. A factor of 0
     *            will favor suppression, and a factor of 1 will favor
     *            generalization. The values in between can be used for
     *            balancing both methods.
     * @param function The aggregate function to be used for comparing results
     * 
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createEntropyMetric(boolean monotonic, double gsFactor, AggregateFunction function) {
        if (monotonic) {
            return new MetricMDNUEntropy(gsFactor, function);
        } else {
            return new MetricMDNUNMEntropy(gsFactor, function);
        }
    }

    
    /**
     * Creates an instance of the non-uniform entropy metric. The default aggregate function,
     * which is the sum-function, will be used for comparing results.
     * This metric will respect attribute weights defined in the configuration.
     *
     * @param monotonic If set to true, the monotonic variant of the metric will be created
     * @param cache
     * @param cardinalities
     * @param hierarchies
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createEntropyMetric(boolean monotonic, double[][] cache, int[][][] cardinalities, int[][][] hierarchies) {
        MetricMDNUEntropyPrecomputed result = (MetricMDNUEntropyPrecomputed)createEntropyMetric(monotonic, AggregateFunction.SUM);
        result.initialize(cache,  cardinalities, hierarchies);
        return result;
    }

    /**
     * Creates an instance of the non-monotonic non-uniform entropy metric. The default aggregate function,
     * which is the sum-function, will be used for comparing results.
     * This metric will respect attribute weights defined in the configuration.
     * 
     * @param gsFactor A factor [0,1] weighting generalization and suppression.
     *            The default value is 0.5, which means that generalization
     *            and suppression will be treated equally. A factor of 0
     *            will favor suppression, and a factor of 1 will favor
     *            generalization. The values in between can be used for
     *            balancing both methods.
     *             
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createEntropyMetric(double gsFactor) {
        return createEntropyMetric(false, gsFactor, AggregateFunction.SUM);
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
     * Creates an instance of the height metric. The default aggregate function, which is the sum-function,
     * will be used for comparing results.
     * This metric will respect attribute weights defined in the configuration.
     *
     * @param minHeight
     * @param maxHeight
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createHeightMetric(int minHeight, int maxHeight) {
        MetricMDHeight result = new MetricMDHeight();
        result.initialize(minHeight, maxHeight);
        return result;
    }

    /**
     * Helper method. Normally, there should be no need to call this
     * @param value
     * @return
     */
    public static InformationLoss<?> createILMultiDimensionalArithmeticMean(double value) {
        return new ILMultiDimensionalArithmeticMean(value);
    }
    /**
     * Helper method. Normally, there should be no need to call this
     * @param value
     * @return
     */
    public static InformationLoss<?> createILMultiDimensionalSum(double value) {
        return new ILMultiDimensionalSum(value);
    }

    /**
     * Helper method. Normally, there should be no need to call this
     * @param value
     * @return
     */
    public static InformationLoss<?> createILSingleDimensional(double value) {
        return new ILSingleDimensional(value);
    }
    

    /**
     * Creates an instance of the KL Divergence metric.
     *
     * @return
     */
    public static Metric<ILSingleDimensional> createKLDivergenceMetric() {
        return new MetricSDNMKLDivergence();
    }

    /**
     * Creates an instance of the loss metric which treats generalization and suppression equally.
     * The default aggregate function, which is the geometric mean, will be used.
     * This metric will respect attribute weights defined in the configuration.
     *
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createLossMetric() {
        return new MetricMDNMLoss();
    }

    /**
     * Creates an instance of the loss metric which treats generalization and suppression equally.
     * This metric will respect attribute weights defined in the configuration.
     *
     * @param function The aggregate function to use for comparing results
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createLossMetric(AggregateFunction function) {
        return new MetricMDNMLoss(function);
    }

    /**
     * Creates an instance of the loss metric with factors for weighting generalization and suppression.
     * The default aggregate function, which is the geometric mean, will be used.
     * This metric will respect attribute weights defined in the configuration.
     *
     * @param gsFactor A factor [0,1] weighting generalization and suppression.
     *            The default value is 0.5, which means that generalization
     *            and suppression will be treated equally. A factor of 0
     *            will favor suppression, and a factor of 1 will favor
     *            generalization. The values in between can be used for
     *            balancing both methods.
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createLossMetric(double gsFactor) {
        return new MetricMDNMLoss(gsFactor, AggregateFunction.GEOMETRIC_MEAN);
    }
    
    /**
     * Creates an instance of the loss metric with factors for weighting generalization and suppression.
     * This metric will respect attribute weights defined in the configuration.
     *
     * @param gsFactor A factor [0,1] weighting generalization and suppression.
     *            The default value is 0.5, which means that generalization
     *            and suppression will be treated equally. A factor of 0
     *            will favor suppression, and a factor of 1 will favor
     *            generalization. The values in between can be used for
     *            balancing both methods.
     * 
     * @param function The aggregate function to use for comparing results
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createLossMetric(double gsFactor, AggregateFunction function) {
        return new MetricMDNMLoss(gsFactor, function);
    }

    /**
     * Creates an instance of the normalized entropy metric.
     * The default aggregate function, which is the sum function, will be used.
     * This metric will respect attribute weights defined in the configuration.
     *
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createNormalizedEntropyMetric() {
        return new MetricMDNUNMNormalizedEntropy();
    }

    /**
     * Creates an instance of the normalized entropy metric.
     * This metric will respect attribute weights defined in the configuration.
     *
     * @param function The aggregate function to use for comparing results
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createNormalizedEntropyMetric(AggregateFunction function) {
        return new MetricMDNUNMNormalizedEntropy(function);
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
     * @param function
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
     * Creates an instance of the precision metric.
     * The default aggregate function, which is the arithmetic mean, will be used.
     * This metric will respect attribute weights defined in the configuration.
     * 
     * @param monotonic If set to true, the monotonic variant of the metric will be created
     * @param gsFactor A factor [0,1] weighting generalization and suppression.
     *            The default value is 0.5, which means that generalization
     *            and suppression will be treated equally. A factor of 0
     *            will favor suppression, and a factor of 1 will favor
     *            generalization. The values in between can be used for
     *            balancing both methods.
     * 
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createPrecisionMetric(boolean monotonic, double gsFactor) {
        return createPrecisionMetric(monotonic, gsFactor, AggregateFunction.ARITHMETIC_MEAN);
    }
    
    /**
     * Creates an instance of the precision metric.
     * This metric will respect attribute weights defined in the configuration.
     *
     * @param monotonic If set to true, the monotonic variant of the metric will be created
     * @param gsFactor A factor [0,1] weighting generalization and suppression.
     *            The default value is 0.5, which means that generalization
     *            and suppression will be treated equally. A factor of 0
     *            will favor suppression, and a factor of 1 will favor
     *            generalization. The values in between can be used for
     *            balancing both methods.
     * @param function
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createPrecisionMetric(boolean monotonic, double gsFactor, AggregateFunction function) {
        if (monotonic) {
            return new MetricMDPrecision(gsFactor, function);
        } else {
            return new MetricMDNMPrecision(gsFactor, function);
        }
    }

    /**
     * Creates an instance of the precision metric.
     * The default aggregate function, which is the arithmetic mean, will be used.
     * This metric will respect attribute weights defined in the configuration.
     *
     * @param monotonic If set to true, the monotonic variant of the metric will be created
     * @param heights
     * @param cells
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createPrecisionMetric(boolean monotonic, int[] heights, double cells) {
        MetricMDNMPrecision result = (MetricMDNMPrecision)createPrecisionMetric(monotonic, AggregateFunction.ARITHMETIC_MEAN);
        result.initialize(heights, cells);
        return result;
    }
    
    /**
     * Creates an instance of the non-monotonic precision metric.
     * The default aggregate function, which is the arithmetic mean, will be used.
     * This metric will respect attribute weights defined in the configuration.
     * 
     * @param gsFactor A factor [0,1] weighting generalization and suppression.
     *            The default value is 0.5, which means that generalization
     *            and suppression will be treated equally. A factor of 0
     *            will favor suppression, and a factor of 1 will favor
     *            generalization. The values in between can be used for
     *            balancing both methods.
     * 
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createPrecisionMetric(double gsFactor) {
        return createPrecisionMetric(false, gsFactor, AggregateFunction.ARITHMETIC_MEAN);
    }

    /**
     * Creates an instance of the non-monotonic precision metric.
     * This metric will respect attribute weights defined in the configuration.
     * 
     * @param gsFactor A factor [0,1] weighting generalization and suppression.
     *            The default value is 0.5, which means that generalization
     *            and suppression will be treated equally. A factor of 0
     *            will favor suppression, and a factor of 1 will favor
     *            generalization. The values in between can be used for
     *            balancing both methods.
     * @param function The aggregate function to use for comparing results
     * 
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createPrecisionMetric(double gsFactor, AggregateFunction function) {
        return createPrecisionMetric(false, gsFactor, function);
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
            return new MetricMDNUEntropyPotentiallyPrecomputed(threshold, 0.5d, function);
        } else {
            return new MetricMDNUNMEntropyPotentiallyPrecomputed(threshold, 0.5d, function);
        }
    }


    /**
     * Creates a potentially precomputed instance of the non-uniform entropy metric. The default aggregate function,
     * which is the sum-function, will be used for comparing results.
     * This metric will respect attribute weights defined in the configuration.
     * 
     * @param threshold The precomputed variant of the metric will be used if 
     *                  #distinctValues / #rows <= threshold for all quasi-identifiers.
     * @param monotonic If set to true, the monotonic variant of the metric will be created
     * @param gsFactor A factor [0,1] weighting generalization and suppression.
     *            The default value is 0.5, which means that generalization
     *            and suppression will be treated equally. A factor of 0
     *            will favor suppression, and a factor of 1 will favor
     *            generalization. The values in between can be used for
     *            balancing both methods.
     * 
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createPrecomputedEntropyMetric(double threshold, boolean monotonic, double gsFactor) {
        return createPrecomputedEntropyMetric(threshold, monotonic, gsFactor, AggregateFunction.SUM);
    }

    /**
     * Creates a potentially precomputed instance of the non-uniform entropy metric.
     * This metric will respect attribute weights defined in the configuration.
     * 
     * @param threshold The precomputed variant of the metric will be used if 
     *                  #distinctValues / #rows <= threshold for all quasi-identifiers.
     * @param monotonic If set to true, the monotonic variant of the metric will be created
     * @param gsFactor A factor [0,1] weighting generalization and suppression.
     *            The default value is 0.5, which means that generalization
     *            and suppression will be treated equally. A factor of 0
     *            will favor suppression, and a factor of 1 will favor
     *            generalization. The values in between can be used for
     *            balancing both methods.
     * @param function The aggregate function to be used for comparing results
     * 
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createPrecomputedEntropyMetric(double threshold, boolean monotonic, double gsFactor, AggregateFunction function) {
        if (monotonic) {
            return new MetricMDNUEntropyPotentiallyPrecomputed(threshold, gsFactor, function);
        } else {
            return new MetricMDNUNMEntropyPotentiallyPrecomputed(threshold, gsFactor, function);
        }
    }

    /**
     * Creates a potentially precomputed instance of the non-monotonic non-uniform entropy metric. The default aggregate function,
     * which is the sum-function, will be used for comparing results.
     * This metric will respect attribute weights defined in the configuration.
     * 
     * @param threshold The precomputed variant of the metric will be used if 
     *                  #distinctValues / #rows <= threshold for all quasi-identifiers.
     * @param gsFactor A factor [0,1] weighting generalization and suppression.
     *            The default value is 0.5, which means that generalization
     *            and suppression will be treated equally. A factor of 0
     *            will favor suppression, and a factor of 1 will favor
     *            generalization. The values in between can be used for
     *            balancing both methods.
     *                  
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createPrecomputedEntropyMetric(double threshold, double gsFactor) {
        return createPrecomputedEntropyMetric(threshold, false, gsFactor, AggregateFunction.SUM);
    }

    /**
     * Creates a potentially precomputed instance of the loss metric which treats generalization
     * and suppression equally.
     * The default aggregate function, which is the geometric mean, will be used.
     * This metric will respect attribute weights defined in the configuration.
     *
     * @param threshold The precomputed variant of the metric will be used if
     *            #distinctValues / #rows <= threshold for all quasi-identifiers.
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createPrecomputedLossMetric(double threshold) {
        return new MetricMDNMLossPotentiallyPrecomputed(threshold);
    }

    /**
     * Creates a potentially precomputed instance of the loss metric which treats generalization and suppression equally.
     * This metric will respect attribute weights defined in the configuration.
     *
     * @param threshold The precomputed variant of the metric will be used if
     *            #distinctValues / #rows <= threshold for all quasi-identifiers.
     * @param function The aggregate function to use for comparing results
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createPrecomputedLossMetric(double threshold, AggregateFunction function) {
        return new MetricMDNMLossPotentiallyPrecomputed(threshold, function);
    }

    /**
     * Creates a potentially precomputed instance of the loss metric with factors for weighting generalization and suppression.
     * The default aggregate function, which is the gemetric mean, will be used.
     * This metric will respect attribute weights defined in the configuration.
     *
     * @param threshold The precomputed variant of the metric will be used if
     *            #distinctValues / #rows <= threshold for all quasi-identifiers.
     * @param gsFactor A factor [0,1] weighting generalization and suppression.
     *            The default value is 0.5, which means that generalization
     *            and suppression will be treated equally. A factor of 0
     *            will favor suppression, and a factor of 1 will favor
     *            generalization. The values in between can be used for
     *            balancing both methods.
     * 
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createPrecomputedLossMetric(double threshold, double gsFactor) {
        return new MetricMDNMLossPotentiallyPrecomputed(threshold, gsFactor, AggregateFunction.GEOMETRIC_MEAN);
    }

    /**
     * Creates a potentially precomputed instance of the loss metric with factors for weighting generalization and suppression.
     * This metric will respect attribute weights defined in the configuration.
     *
     * @param threshold The precomputed variant of the metric will be used if
     *            #distinctValues / #rows <= threshold for all quasi-identifiers.
     * @param gsFactor A factor [0,1] weighting generalization and suppression.
     *            The default value is 0.5, which means that generalization
     *            and suppression will be treated equally. A factor of 0
     *            will favor suppression, and a factor of 1 will favor
     *            generalization. The values in between can be used for
     *            balancing both methods.
     * 
     * @param function The aggregate function to use for comparing results
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createPrecomputedLossMetric(double threshold, double gsFactor, AggregateFunction function) {
        return new MetricMDNMLossPotentiallyPrecomputed(threshold, gsFactor, function);
    }


    /**
     * Creates a potentially precomputed instance of the normalized entropy metric.
     * The default aggregate function, which is the sum function, will be used.
     * This metric will respect attribute weights defined in the configuration.
     * 
     * @param threshold The precomputed variant of the metric will be used if
     *            #distinctValues / #rows <= threshold for all quasi-identifiers.
     *
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createPrecomputedNormalizedEntropyMetric(double threshold) {
        return new MetricMDNUNMNormalizedEntropyPotentiallyPrecomputed(threshold);
    }

    /**
     * Creates a potentially precomputed instance of the normalized entropy metric.
     * This metric will respect attribute weights defined in the configuration.
     * 
     * @param threshold The precomputed variant of the metric will be used if
     *            #distinctValues / #rows <= threshold for all quasi-identifiers.
     *
     * @param function The aggregate function to use for comparing results
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createPrecomputedNormalizedEntropyMetric(double threshold, AggregateFunction function) {
        return new MetricMDNUNMNormalizedEntropyPotentiallyPrecomputed(threshold, function);
    }

    /**
     * Creates an instance of the model for maximizing publisher benefit in the game-theoretic privacy
     * model based on a cost/benefit analysis.
     * 
     * @param journalistAttackerModel If set to true, the journalist attacker model will be assumed, 
     *                                the prosecutor model will be assumed, otherwise
     *                                
     * @param gsFactor A factor [0,1] weighting generalization and suppression.
     *            The default value is 0.5, which means that generalization
     *            and suppression will be treated equally. A factor of 0
     *            will favor suppression, and a factor of 1 will favor
     *            generalization. The values in between can be used for
     *            balancing both methods.
     * @return
     */
    public static MetricSDNMPublisherPayout createPublisherBenefitMetric(boolean journalistAttackerModel,
                                                                         double gsFactor) {
        return new MetricSDNMPublisherPayout(journalistAttackerModel, gsFactor);
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
