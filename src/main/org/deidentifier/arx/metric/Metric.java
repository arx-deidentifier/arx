/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.arx.metric;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.framework.check.groupify.IHashGroupify;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Node;
import org.deidentifier.arx.metric.v2.AbstractILMultiDimensional;
import org.deidentifier.arx.metric.v2.AbstractMetricMultiDimensional;
import org.deidentifier.arx.metric.v2.ILSingleDimensional;
import org.deidentifier.arx.metric.v2.MetricMDHeight;
import org.deidentifier.arx.metric.v2.MetricMDNMLoss;
import org.deidentifier.arx.metric.v2.MetricMDNMLossPotentiallyPrecomputed;
import org.deidentifier.arx.metric.v2.MetricMDNMLossPrecomputed;
import org.deidentifier.arx.metric.v2.MetricMDNMPrecision;
import org.deidentifier.arx.metric.v2.MetricMDNUEntropy;
import org.deidentifier.arx.metric.v2.MetricMDNUEntropyPotentiallyPrecomputed;
import org.deidentifier.arx.metric.v2.MetricMDNUEntropyPrecomputed;
import org.deidentifier.arx.metric.v2.MetricMDNUNMEntropy;
import org.deidentifier.arx.metric.v2.MetricMDNUNMEntropyPotentiallyPrecomputed;
import org.deidentifier.arx.metric.v2.MetricMDNUNMEntropyPrecomputed;
import org.deidentifier.arx.metric.v2.MetricMDPrecision;
import org.deidentifier.arx.metric.v2.MetricSDAECS;
import org.deidentifier.arx.metric.v2.MetricSDDiscernability;
import org.deidentifier.arx.metric.v2.MetricSDNMDiscernability;
import org.deidentifier.arx.metric.v2.__MetricV2;

/**
 * Abstract base class for metrics.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 * @param <T>
 */
public abstract class Metric<T extends InformationLoss<?>> implements Serializable {
    
    /**
     * Pluggable aggregate functions.
     *
     * @author Fabian Prasser
     */
    public static enum AggregateFunction implements Serializable{
        
        /** Sum */
        SUM("Sum"),
        
        /** Maximum */
        MAXIMUM("Maximum"),
        
        /** Arithmetic mean */
        ARITHMETIC_MEAN("Arithmetric Mean"),
        
        /** Geometric mean: To handle zero values while not violating guarantees required for pruning
         * based on lower bounds, 1d is added to every individual value and 1d is subtracted from the
         * final result. */
        GEOMETRIC_MEAN("Geometric Mean"),
        
        /** Rank: Ordered list of values, compared lexicographically. */
        RANK("Rank");
        
        /**  TODO */
        private String name;
        
        /**
         * 
         *
         * @param name
         */
        private AggregateFunction(String name){
            this.name = name;
        }
        
        /* (non-Javadoc)
         * @see java.lang.Enum#toString()
         */
        public String toString() {
            return name;
        }
    }
    
    /**  TODO */
    private static final long serialVersionUID = -2657745103125430229L;

    /** For comparisons. */
    private static final double DIGITS           = 10d;

    /** For comparisons. */
    private static final double FACTOR           = Math.pow(10d, DIGITS);
    
    /**
     * Creates a new instance of the AECS metric.
     * 
     * @return
     */
    public static Metric<ILSingleDimensional> createAECSMetric() {
        return __MetricV2.createAECSMetric();
    }
    

    /**
     * Creates an instance of the discernability metric.
     *
     * @return
     */
    public static Metric<ILSingleDimensional> createDiscernabilityMetric() {
        return __MetricV2.createDiscernabilityMetric();
    }


    /**
     * Creates an instance of the discernability metric. The monotonic variant is DM*.
     * 
     * @param monotonic If set to true, the monotonic variant (DM*) will be created
     * @return
     */
    public static Metric<ILSingleDimensional> createDiscernabilityMetric(boolean monotonic) {
        return __MetricV2.createDiscernabilityMetric(monotonic);
    }
    

    /**
     * Creates an instance of the non-monotonic non-uniform entropy metric. The default aggregate function,
     * which is the sum-function, will be used for comparing results.
     * This metric will respect attribute weights defined in the configuration.
     * 
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createEntropyMetric() {
        return __MetricV2.createEntropyMetric();
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
        return __MetricV2.createEntropyMetric(monotonic);
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
        return __MetricV2.createEntropyMetric(monotonic, function);
    }


    /**
     * Creates an instance of the height metric. The default aggregate function, which is the sum-function,
     * will be used for comparing results.
     * This metric will respect attribute weights defined in the configuration.
     * 
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createHeightMetric() {
        return __MetricV2.createHeightMetric();
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
        return __MetricV2.createHeightMetric(function);
    }

    /**
     * Creates an instance of the loss metric which treats generalization and suppression equally.
     * The default aggregate function, which is the rank function, will be used.
     * This metric will respect attribute weights defined in the configuration.
     *
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createLossMetric() {
        return __MetricV2.createLossMetric();
    }
    
    /**
     * Creates an instance of the loss metric which treats generalization and suppression equally.
     * This metric will respect attribute weights defined in the configuration.
     *
     * @param function The aggregate function to use for comparing results
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createLossMetric(AggregateFunction function) {
        return __MetricV2.createLossMetric(function);
    }

    /**
     * Creates an instance of the loss metric with factors for weighting generalization and suppression.
     * The default aggregate function, which is the rank function, will be used.
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
        return __MetricV2.createLossMetric(gsFactor);
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
        return __MetricV2.createLossMetric(gsFactor, function);
    }

    /**
     * This method supports backwards compatibility. It will transform implementations from version 1 to 
     * implementations from version 2, if necessary.
     * @param metric
     * @param minLevel
     * @param maxLevel
     * @return
     */
    public static Metric<?> createMetric(Metric<?> metric, int minLevel, int maxLevel) {
        
        if (metric instanceof MetricHeight) {
            return __MetricV2.createHeightMetric(minLevel, maxLevel);
        } else {
            return createMetric(metric);
        }
    }
    
    /**
     * This method supports backwards compatibility. It will transform implementations from version 1 to 
     * implementations from version 2, if necessary. 
     * @param metric
     * @return
     */
    private static Metric<?> createMetric(Metric<?> metric) {
        
        if (metric instanceof MetricAECS) {
            return __MetricV2.createAECSMetric(((MetricAECS)metric).getRowCount());
        } else if (metric instanceof MetricDM) {
            return __MetricV2.createDiscernabilityMetric(false, ((MetricDM)metric).getRowCount());
        } else if (metric instanceof MetricDMStar) {
            return __MetricV2.createDiscernabilityMetric(true, ((MetricDMStar)metric).getRowCount());
        } else if (metric instanceof MetricEntropy) {
            return __MetricV2.createEntropyMetric(true, 
                                                  ((MetricEntropy)metric).getCache(), 
                                                  ((MetricEntropy)metric).getCardinalities(), 
                                                  ((MetricEntropy)metric).getHierarchies());
        } else if (metric instanceof MetricNMEntropy) {
            return __MetricV2.createEntropyMetric(false, 
                                                  ((MetricEntropy)metric).getCache(), 
                                                  ((MetricEntropy)metric).getCardinalities(), 
                                                  ((MetricEntropy)metric).getHierarchies());
        } else if (metric instanceof MetricNMPrecision) {
            return __MetricV2.createPrecisionMetric(false, ((MetricNMPrecision)metric).getHeights(),
                                                           ((MetricNMPrecision)metric).getCells());
        } else if (metric instanceof MetricPrecision) {
            return __MetricV2.createPrecisionMetric(true, ((MetricPrecision)metric).getHeights(),
                                                          ((MetricPrecision)metric).getCells());
        } else if (metric instanceof MetricStatic) {
            return __MetricV2.createStaticMetric(((MetricStatic)metric)._infoloss);
        } else {
            return metric;
        }
    }

    /**
     * Creates an instance of the non-monotonic precision metric.
     * The default aggregate function, which is the arithmetic mean, will be used.
     * This metric will respect attribute weights defined in the configuration.
     * 
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createPrecisionMetric() {
        return __MetricV2.createPrecisionMetric();
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
        return __MetricV2.createPrecisionMetric(function);
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
        return __MetricV2.createPrecisionMetric(monotonic);
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
        return __MetricV2.createPrecisionMetric(monotonic, function);
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
        return __MetricV2.createPrecomputedEntropyMetric(threshold);
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
        return __MetricV2.createPrecomputedEntropyMetric(threshold, monotonic);
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
        return __MetricV2.createPrecomputedEntropyMetric(threshold, monotonic, function);
    }

    /**
     * Creates a potentially precomputed instance of the loss metric which treats generalization
     * and suppression equally.
     * The default aggregate function, which is the rank function, will be used.
     * This metric will respect attribute weights defined in the configuration.
     *
     * @param threshold The precomputed variant of the metric will be used if
     *            #distinctValues / #rows <= threshold for all quasi-identifiers.
     * @return
     */
    public static Metric<AbstractILMultiDimensional> createPrecomputedLossMetric(double threshold) {
        return __MetricV2.createPrecomputedLossMetric(threshold);
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
        return __MetricV2.createPrecomputedLossMetric(threshold, function);
    }
    

    /**
     * Creates a potentially precomputed instance of the loss metric with factors for weighting generalization and suppression.
     * The default aggregate function, which is the rank function, will be used.
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
        return __MetricV2.createPrecomputedLossMetric(threshold, gsFactor);
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
        return __MetricV2.createPrecomputedLossMetric(threshold, gsFactor, function);
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
        return __MetricV2.createStaticMetric(loss);
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
        return __MetricV2.createStaticMetric(loss, function);
    }

    /**
     * Returns a list of all available metrics for information loss.
     *
     * @return
     */
    public static List<MetricDescription> list(){
        return Arrays.asList(new MetricDescription[]{
               new MetricDescription("Average equivalence class size",
                                     false,  // monotonic variant supported
                                     false,  // attribute weights supported
                                     false,  // configurable coding model supported
                                     false,  // pre-computation supported
                                     false){ // aggregate function supported

                                     private static final long serialVersionUID = 5194477380451716051L;

                                    @Override
                                     public Metric<?> createInstance(MetricConfiguration config) {
                                         return createAECSMetric();
                                     }

                                    @Override
                                    public boolean isInstance(Metric<?> metric) {
                                        return (metric instanceof MetricSDAECS);
                                    } 
               },
               new MetricDescription("Discernability",
                                     true,   // monotonic variant supported
                                     false,  // attribute weights supported
                                     false,  // configurable coding model supported
                                     false,  // pre-computation supported
                                     false){ // aggregate function supported

                                     private static final long serialVersionUID = 183842500322023095L;

                                    @Override
                                     public Metric<?> createInstance(MetricConfiguration config) {
                                         return createDiscernabilityMetric(config.isMonotonic());
                                     } 

                                     @Override
                                     public boolean isInstance(Metric<?> metric) {
                                         return (metric instanceof MetricSDDiscernability) ||
                                                (metric instanceof MetricSDNMDiscernability);
                                     } 
               },
               new MetricDescription("Height",
                                     false,  // monotonic variant supported
                                     true,   // attribute weights supported
                                     false,  // configurable coding model supported
                                     false,  // pre-computation supported
                                     true){  // aggregate function supported

                                     private static final long serialVersionUID = 9125639204133496116L;

                                    @Override
                                     public Metric<?> createInstance(MetricConfiguration config) {
                                         return createHeightMetric(config.getAggregateFunction());
                                     }  

                                     @Override
                                     public boolean isInstance(Metric<?> metric) {
                                         return (metric instanceof MetricMDHeight);
                                     } 
               },
               new MetricDescription("Loss",
                                     false, // monotonic variant supported
                                     true,  // attribute weights supported
                                     true,  // configurable coding model supported
                                     true,  // pre-computation supported
                                     true){ // aggregate function supported

                                     private static final long serialVersionUID = 4274885123814166707L;

                                    @Override
                                     public Metric<?> createInstance(MetricConfiguration config) {
                                         if (config.isPrecomputed()) {
                                             return createPrecomputedLossMetric(config.getPrecomputationThreshold(), 
                                                                                config.getGsFactor(),
                                                                                config.getAggregateFunction());
                                         } else {
                                             return createLossMetric(config.getGsFactor(), 
                                                                     config.getAggregateFunction());
                                         }
                                     }        

                                     @Override
                                     public boolean isInstance(Metric<?> metric) {
                                         return (metric instanceof MetricMDNMLoss) ||
                                                (metric instanceof MetricMDNMLossPrecomputed) ||
                                                (metric instanceof MetricMDNMLossPotentiallyPrecomputed);
                                     } 
               },
               new MetricDescription("Non-uniform entropy",
                                     true,  // monotonic variant supported
                                     true,  // attribute weights supported
                                     false, // configurable coding model supported
                                     true,  // pre-computation supported
                                     true){ // aggregate function supported

                                     private static final long serialVersionUID = 2578476174209277258L;

                                    @Override
                                     public Metric<?> createInstance(MetricConfiguration config) {
                                         if (config.isPrecomputed()) {
                                             return createPrecomputedEntropyMetric(config.getPrecomputationThreshold(), 
                                                                                   config.isMonotonic(),
                                                                                   config.getAggregateFunction());                                             
                                         } else {
                                             return createEntropyMetric(config.isMonotonic(), 
                                                                        config.getAggregateFunction());
                                         }
                                     } 

                                     @Override
                                     public boolean isInstance(Metric<?> metric) {
                                         return (metric instanceof MetricMDNUEntropy) ||
                                                (metric instanceof MetricMDNUEntropyPrecomputed) ||
                                                (metric instanceof MetricMDNUEntropyPotentiallyPrecomputed) ||
                                                (metric instanceof MetricMDNUNMEntropy) ||
                                                (metric instanceof MetricMDNUNMEntropyPrecomputed) ||
                                                (metric instanceof MetricMDNUNMEntropyPotentiallyPrecomputed);
                                     } 
               },
               new MetricDescription("Precision",
                                     true,  // monotonic variant supported
                                     true,  // attribute weights supported
                                     false, // configurable coding model supported
                                     false, // pre-computation supported
                                     true){ // aggregate function supported

                                     private static final long serialVersionUID = 2992096817427174514L;

                                    @Override
                                     public Metric<?> createInstance(MetricConfiguration config) {
                                         return createPrecisionMetric(config.isMonotonic(), 
                                                                      config.getAggregateFunction());
                                     }        

                                     @Override
                                     public boolean isInstance(Metric<?> metric) {
                                         return (metric instanceof MetricMDPrecision) ||
                                                (metric instanceof MetricMDNMPrecision);
                                     } 
               }
        });
    }

    /**
     * Returns a description for the given metric, if there is any, null otherwise.
     *
     * @param metric
     * @return
     */
    protected static MetricDescription getDescription(Metric<?> metric) {
        for (MetricDescription description : Metric.list()){
            if (description.isInstance(metric)) {
                return description;
            }
        }
        return null;
    }

    /** Is the metric independent?. */
    private boolean           independent      = false;

    /** Is the metric monotonic?. */
    private boolean           monotonic        = false;
    

    /**
     * Create a new metric.
     *
     * @param monotonic
     * @param independent
     */
    protected Metric(final boolean monotonic, final boolean independent) {
        this.monotonic = monotonic;
        this.independent = independent;
    }
    
    /**
     * Returns an instance of the maximal value.
     *
     * @return
     */
    public abstract InformationLoss<?> createMaxInformationLoss();

    /**
     * Returns an instance of the minimal value.
     *
     * @return
     */
    public abstract InformationLoss<?> createMinInformationLoss();

    /**
     * Returns the aggregate function of a multi-dimensional metric, null otherwise.
     *
     * @return
     */
    public AggregateFunction getAggregateFunction(){
        return null;
    }
    
    /**
     * Returns the configuration of this metric.
     *
     * @return
     */
    public MetricConfiguration getConfiguration() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Returns a description of this metric.
     *
     * @return
     */
    public MetricDescription getDescription() {
        return Metric.getDescription(this);
    }
    
    /**
     * Evaluates the metric for the given node.
     *
     * @param node The node for which to compute the information loss
     * @param groupify The groupify operator of the previous check
     * @return the information loss
     */
    public final InformationLossWithBound<T> getInformationLoss(final Node node, final IHashGroupify groupify) {
        return this.getInformationLossInternal(node, groupify);
    }
    
    /**
     * Returns a lower bound for the information loss for the given node. 
     * This can be used to expose the results of monotonic shares of a metric,
     * which can significantly speed-up the anonymization process. If no
     * such metric exists, the method returns <code>null</code>.
     * 
     * @param node
     * @return
     */
    @SuppressWarnings("unchecked")
    public T getLowerBound(final Node node) {
        if (node.getLowerBound() != null) {
            return (T)node.getLowerBound();
        } else {
            return getLowerBoundInternal(node);
        }
    }
    
    /**
     * Returns a lower bound for the information loss for the given node.
     * This can be used to expose the results of monotonic shares of a metric,
     * which can significantly speed-up the anonymization process. If no
     * such metric exists the method returns <code>null</code>.
     *
     * @param node
     * @param groupify
     * @return
     */
    @SuppressWarnings("unchecked")
    public T getLowerBound(final Node node, final IHashGroupify groupify) {
        if (node.getLowerBound() != null) {
            return (T)node.getLowerBound();
        } else {
            return getLowerBoundInternal(node, groupify);
        }
    }
    
    /**
     * Returns the name of metric.
     *
     * @return
     */
    public String getName() {
        return this.toString();
    }

    /**
     * Initializes the metric.
     *
     * @param definition
     * @param input
     * @param hierarchies
     * @param config
     */
    public final void initialize(final DataDefinition definition, final Data input, final GeneralizationHierarchy[] hierarchies, final ARXConfiguration config) {
        initializeInternal(definition, input, hierarchies, config);
    }

    /**
     * Returns whether this metric requires the transformed data or groups to
     * determine information loss.
     *
     * @return
     */
    public boolean isIndependent() {
        return independent;
    }

    /**
     * Returns false if the metric is non-monotone using suppression.
     *
     * @return the monotonic
     */
    public final boolean isMonotonic() {
        return monotonic;
    }
    
    /**
     * Returns true if the metric is multi-dimensional.
     *
     * @return
     */
    public final boolean isMultiDimensional(){
        return (this instanceof AbstractMetricMultiDimensional);
    }
    
    /**
     * Returns true if the metric is weighted.
     *
     * @return
     */
    public final boolean isWeighted() {
        return (this instanceof MetricWeighted) || this.isMultiDimensional();
    }

    /**
     * Returns the name of metric.
     *
     * @return
     */
    public String toString() {
        return this.getClass().getSimpleName();
    }
    
 
    /**
     * Evaluates the metric for the given node.
     *
     * @param node The node for which to compute the information loss
     * @param groupify The groupify operator of the previous check
     * @return the double
     */
    protected abstract InformationLossWithBound<T> getInformationLossInternal(final Node node, final IHashGroupify groupify);

    /**
     * Returns a lower bound for the information loss for the given node. 
     * This can be used to expose the results of monotonic shares of a metric,
     * which can significantly speed-up the anonymization process. If no
     * such metric exists, simply return <code>null</code>.
     * 
     * @param node
     * @return
     */
    protected abstract T getLowerBoundInternal(Node node);

    /**
     * Returns a lower bound for the information loss for the given node.
     * This can be used to expose the results of monotonic shares of a metric,
     * which can significantly speed-up the anonymization process. If no
     * such metric exists, simply return <code>null</code>. <br>
     * <br>
     * This variant of the method allows computing a monotonic share based on
     * a groupified data representation. IMPORTANT NOTE: The groups may not have
     * been classified correctly when the method is called, i.e.,
     * HashGroupifyEntry.isNotOutlier may not be set correctly!
     *
     * @param node
     * @param groupify
     * @return
     */
    protected abstract T getLowerBoundInternal(final Node node, final IHashGroupify groupify);

    /**
     * Implement this to initialize the metric.
     *
     * @param definition
     * @param input
     * @param hierarchies
     * @param config
     */
    protected abstract void initializeInternal(final DataDefinition definition, final Data input, final GeneralizationHierarchy[] hierarchies, final ARXConfiguration config);

    /**
     * Ignore anything but the first DIGITS digits. 
     * 
     * @param value
     * @return
     */
    protected double round(double value) {
       return Math.floor(value * FACTOR) / FACTOR;
    }
}
