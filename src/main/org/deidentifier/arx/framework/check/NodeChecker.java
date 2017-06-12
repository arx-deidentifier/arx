/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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

package org.deidentifier.arx.framework.check;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXConfiguration.ARXConfigurationInternal;
import org.deidentifier.arx.framework.check.StateMachine.Transition;
import org.deidentifier.arx.framework.check.distribution.DistributionAggregateFunction;
import org.deidentifier.arx.framework.check.distribution.IntArrayDictionary;
import org.deidentifier.arx.framework.check.groupify.HashGroupify;
import org.deidentifier.arx.framework.check.history.History;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.data.Dictionary;
import org.deidentifier.arx.framework.lattice.SolutionSpace;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.metric.InformationLoss;
import org.deidentifier.arx.metric.InformationLossWithBound;
import org.deidentifier.arx.metric.Metric;

/**
 * This class orchestrates the process of transforming and analyzing a dataset.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class NodeChecker {

    /**
     * The result of a check.
     */
    public static class Result {
        
        /** Overall anonymity. */
        public final Boolean privacyModelFulfilled;
        
        /** k-Anonymity sub-criterion. */
        public final Boolean minimalClassSizeFulfilled;
        
        /** Information loss. */
        public final InformationLoss<?> informationLoss;
        
        /** Lower bound. */
        public final InformationLoss<?> lowerBound;

        /**
         * Creates a new instance.
         * 
         * @param privacyModelFulfilled
         * @param minimalClassSizeFulfilled
         * @param infoLoss
         * @param lowerBound
         */
        Result(Boolean privacyModelFulfilled,
               Boolean minimalClassSizeFulfilled,
               InformationLoss<?> infoLoss,
               InformationLoss<?> lowerBound) {
            this.privacyModelFulfilled = privacyModelFulfilled;
            this.minimalClassSizeFulfilled = minimalClassSizeFulfilled;
            this.informationLoss = infoLoss;
            this.lowerBound = lowerBound;
        }
    }

    /** The config. */
    private final ARXConfigurationInternal        config;

    /** The data. */
    private final Data                            dataGeneralized;

    /** The microaggregation functions. */
    private final DistributionAggregateFunction[] microaggregationFunctions;

    /** The start index of the attributes with microaggregation in the data array */
    private final int                             microaggregationStartIndex;

    /** The number of attributes with microaggregation in the data array */
    private final int                             microaggregationNumAttributes;

    /** Map for the microaggregated data subset */
    private final int[]                           microaggregationMap;

    /** Header of the microaggregated data subset */
    private final String[]                        microaggregationHeader;

    /** The current hash groupify. */
    private HashGroupify                          currentGroupify;

    /** The last hash groupify. */
    private HashGroupify                          lastGroupify;

    /** The history. */
    private final History                         history;

    /** The metric. */
    private final Metric<?>                       metric;

    /** The state machine. */
    private final StateMachine                    stateMachine;

    /** The data transformer. */
    private final Transformer                     transformer;

    /** The solution space */
    private final SolutionSpace                   solutionSpace;

    /** Is a minimal class size required */
    private final boolean                         minimalClassSizeRequired;

    /**
     * Creates a new NodeChecker instance.
     * 
     * @param manager The manager
     * @param metric The metric
     * @param config The configuration
     * @param historyMaxSize The history max size
     * @param snapshotSizeDataset A history threshold
     * @param snapshotSizeSnapshot A history threshold
     * @param solutionSpace
     */
    public NodeChecker(final DataManager manager,
                       final Metric<?> metric,
                       final ARXConfigurationInternal config,
                       final int historyMaxSize,
                       final double snapshotSizeDataset,
                       final double snapshotSizeSnapshot,
                       final SolutionSpace solutionSpace) {
        
        // Initialize all operators
        this.metric = metric;
        this.config = config;
        this.dataGeneralized = manager.getDataGeneralized();
        this.microaggregationFunctions = manager.getMicroaggregationFunctions();
        this.microaggregationStartIndex = manager.getMicroaggregationStartIndex();
        this.microaggregationNumAttributes = manager.getMicroaggregationNumAttributes();
        this.microaggregationMap = manager.getMicroaggregationMap();
        this.microaggregationHeader = manager.getMicroaggregationHeader();
        this.solutionSpace = solutionSpace;
        this.minimalClassSizeRequired = config.getMinimalGroupSize() != Integer.MAX_VALUE;
        
        int initialSize = (int) (manager.getDataGeneralized().getDataLength() * 0.01d);
        IntArrayDictionary dictionarySensValue;
        IntArrayDictionary dictionarySensFreq;
        if ((config.getRequirements() & ARXConfiguration.REQUIREMENT_DISTRIBUTION) != 0) {
            dictionarySensValue = new IntArrayDictionary(initialSize);
            dictionarySensFreq = new IntArrayDictionary(initialSize);
        } else {
            // Just to allow byte code instrumentation
            dictionarySensValue = new IntArrayDictionary(0);
            dictionarySensFreq = new IntArrayDictionary(0);
        }
        
        this.history = new History(manager.getDataGeneralized().getArray().length,
                                   historyMaxSize,
                                   snapshotSizeDataset,
                                   snapshotSizeSnapshot,
                                   config,
                                   dictionarySensValue,
                                   dictionarySensFreq,
                                   solutionSpace);
        
        this.stateMachine = new StateMachine(history);
        this.currentGroupify = new HashGroupify(initialSize, config);
        this.lastGroupify = new HashGroupify(initialSize, config);
        this.transformer = new Transformer(manager.getDataGeneralized().getArray(),
                                           manager.getDataAnalyzed().getArray(),
                                           manager.getHierarchies(),
                                           config,
                                           dictionarySensValue,
                                           dictionarySensFreq);
    }

    
    /**
     * Applies the given transformation and returns the dataset
     * @param transformation
     * @return
     */
    public TransformedData applyTransformation(final Transformation transformation) {
        return applyTransformation(transformation,
                                   new Dictionary(microaggregationNumAttributes));
    }
        
    /**
     * Applies the given transformation and returns the dataset
     * @param transformation
     * @param microaggregationDictionary A dictionary for microaggregated values
     * @return
     */
    public TransformedData applyTransformation(final Transformation transformation,
                                               final Dictionary microaggregationDictionary) {
        
        // Prepare
        microaggregationDictionary.definalizeAll();
        
        // Apply transition and groupify
        currentGroupify = transformer.apply(0L, transformation.getGeneralization(), currentGroupify);
        currentGroupify.stateAnalyze(transformation, true);
        if (!currentGroupify.isPrivacyModelFulfilled() && !config.isSuppressionAlwaysEnabled()) {
            currentGroupify.stateResetSuppression();
        }
        
        // Determine information loss
        InformationLoss<?> loss = transformation.getInformationLoss();
        if (loss == null) {
            loss = metric.getInformationLoss(transformation, currentGroupify).getInformationLoss();
        }
        
        // Prepare buffers
        Data microaggregatedOutput = new Data(new int[0][0], new String[0], new int[0], new Dictionary(0));
        Data generalizedOutput = new Data(transformer.getBuffer(), dataGeneralized.getHeader(), dataGeneralized.getMap(), dataGeneralized.getDictionary());
        
        // Perform microaggregation. This has to be done before suppression.
        if (microaggregationFunctions.length > 0) {
            microaggregatedOutput = currentGroupify.performMicroaggregation(transformer.getBuffer(), 
                                                                            microaggregationStartIndex,
                                                                            microaggregationNumAttributes,
                                                                            microaggregationFunctions,
                                                                            microaggregationMap,
                                                                            microaggregationHeader,
                                                                            microaggregationDictionary);
        }
        
        // Perform suppression
        if (config.getAbsoluteMaxOutliers() != 0 || !currentGroupify.isPrivacyModelFulfilled()) {
            currentGroupify.performSuppression(transformer.getBuffer());
        }
        
        // Return the buffer
        return new TransformedData(generalizedOutput, microaggregatedOutput, 
                                   new Result(currentGroupify.isPrivacyModelFulfilled(), 
                                              minimalClassSizeRequired ? currentGroupify.isMinimalClassSizeFulfilled() : null, 
                                              loss, null));
    }
    
    /**
     * Checks the given transformation, computes the utility if it fulfills the privacy model
     * @param node
     * @return
     */
    public NodeChecker.Result check(final Transformation node) {
        return check(node, false);
    }
    
    /**
     * Checks the given transformation
     * @param node
     * @param forceMeasureInfoLoss
     * @return
     */
    public NodeChecker.Result check(final Transformation node, final boolean forceMeasureInfoLoss) {
        
        // If the result is already know, simply return it
        if (node.getData() != null && node.getData() instanceof NodeChecker.Result) {
            return (NodeChecker.Result) node.getData();
        }
        
        // Store snapshot from last check
        if (stateMachine.getLastNode() != null) {
            history.store(solutionSpace.getTransformation(stateMachine.getLastNode()), currentGroupify, stateMachine.getLastTransition().snapshot);
        }
        
        // Transition
        final Transition transition = stateMachine.transition(node.getGeneralization());
        
        // Switch groupifies
        final HashGroupify temp = lastGroupify;
        lastGroupify = currentGroupify;
        currentGroupify = temp;
        
        // Apply transition
        switch (transition.type) {
        case UNOPTIMIZED:
            currentGroupify = transformer.apply(transition.projection, node.getGeneralization(), currentGroupify);
            break;
        case ROLLUP:
            currentGroupify = transformer.applyRollup(transition.projection, node.getGeneralization(), lastGroupify, currentGroupify);
            break;
        case SNAPSHOT:
            currentGroupify = transformer.applySnapshot(transition.projection, node.getGeneralization(), currentGroupify, transition.snapshot);
            break;
        }
        
        // We are done with transforming and adding
        currentGroupify.stateAnalyze(node, forceMeasureInfoLoss);
        if (forceMeasureInfoLoss && !currentGroupify.isPrivacyModelFulfilled() && !config.isSuppressionAlwaysEnabled()) {
            currentGroupify.stateResetSuppression();
        }
        
        // Compute information loss and lower bound
        InformationLossWithBound<?> result = (currentGroupify.isPrivacyModelFulfilled() || forceMeasureInfoLoss) ?
                metric.getInformationLoss(node, currentGroupify) : null;
        InformationLoss<?> loss = result != null ? result.getInformationLoss() : null;
        InformationLoss<?> bound = result != null ? result.getLowerBound() : metric.getLowerBound(node, currentGroupify);
        
        // Return result;
        return new NodeChecker.Result(currentGroupify.isPrivacyModelFulfilled(),
                                      minimalClassSizeRequired ? currentGroupify.isMinimalClassSizeFulfilled() : null,
                                      loss,
                                      bound);
    }
    
    /**
     * Returns the configuration
     * @return
     */
    public ARXConfigurationInternal getConfiguration() {
        return config;
    }

    /**
     * Returns the checkers history, if any.
     *
     * @return
     */
    public History getHistory() {
        return history;
    }
    
    /**
     * Returns the input buffer
     * @return
     */
    public int[][] getInputBuffer() {
        return this.dataGeneralized.getArray();
    }
    
    /**
     * Returns the utility measure
     * @return
     */
    public Metric<?> getMetric() {
        return metric;
    }

    /**
     * Frees resources
     */
    public void reset() {
        stateMachine.reset();
        history.reset();
        history.setSize(0);
        currentGroupify.stateClear();
        lastGroupify.stateClear();
    }
}
