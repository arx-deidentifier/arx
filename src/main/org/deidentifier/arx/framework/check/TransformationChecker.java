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
import org.deidentifier.arx.framework.check.TransformationCheckerStateMachine.Transition;
import org.deidentifier.arx.framework.check.distribution.IntArrayDictionary;
import org.deidentifier.arx.framework.check.groupify.HashGroupify;
import org.deidentifier.arx.framework.check.history.History;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.data.DataMatrix;
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
public class TransformationChecker {

    /** The config. */
    private final ARXConfigurationInternal          config;

    /** The data manager. */
    private final DataManager                       manager;
    
    /** The data. */
    private final Data                              dataGeneralized;

    /** The current hash groupify. */
    private HashGroupify                            currentGroupify;

    /** The last hash groupify. */
    private HashGroupify                            lastGroupify;

    /** The history. */
    private final History                           history;

    /** The metric. */
    private final Metric<?>                         metric;

    /** The state machine. */
    private final TransformationCheckerStateMachine stateMachine;

    /** The data transformer. */
    private final Transformer                       transformer;

    /** The solution space */
    private final SolutionSpace                     solutionSpace;

    /** Is a minimal class size required */
    private final boolean                           minimalClassSizeRequired;

    /**
     * Creates a new transformation checker.
     * 
     * @param manager The manager
     * @param metric The metric
     * @param config The configuration
     * @param historyMaxSize The history max size
     * @param snapshotSizeDataset A history threshold
     * @param snapshotSizeSnapshot A history threshold
     * @param solutionSpace
     */
    public TransformationChecker(final DataManager manager,
                                 final Metric<?> metric,
                                 final ARXConfigurationInternal config,
                                 final int historyMaxSize,
                                 final double snapshotSizeDataset,
                                 final double snapshotSizeSnapshot,
                                 final SolutionSpace solutionSpace) {
        
        // Store data
        this.metric = metric;
        this.manager = manager;
        this.config = config;
        this.dataGeneralized = manager.getDataGeneralized();
        this.solutionSpace = solutionSpace;
        this.minimalClassSizeRequired = config.getMinimalGroupSize() != Integer.MAX_VALUE;
        
        // Initialize all operators
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
        
        this.history = new History(manager.getDataGeneralized().getArray().getNumRows(),
                                   historyMaxSize,
                                   snapshotSizeDataset,
                                   snapshotSizeSnapshot,
                                   config,
                                   dictionarySensValue,
                                   dictionarySensFreq,
                                   solutionSpace);
        
        this.stateMachine = new TransformationCheckerStateMachine(history);
        this.transformer = new Transformer(manager.getDataGeneralized().getArray(),
                                           manager.getDataAnalyzed().getArray(),
                                           manager.getAggregationInformation().getHotThreshold(),
                                           manager.getHierarchies(),
                                           config,
                                           dictionarySensValue,
                                           dictionarySensFreq);
        
        this.currentGroupify = new HashGroupify(initialSize, config, manager.getAggregationInformation().getHotThreshold(),
                                                manager.getDataGeneralized().getArray(),
                                                transformer.getBuffer(),
                                                manager.getDataAnalyzed().getArray());
        
        this.lastGroupify = new HashGroupify(initialSize, config, manager.getAggregationInformation().getHotThreshold(),
                                             manager.getDataGeneralized().getArray(),
                                             transformer.getBuffer(),
                                             manager.getDataAnalyzed().getArray());
    }

    /**
     * Checks the given transformation, computes the utility if it fulfills the privacy model
     * @param node
     * @return
     */
    public TransformationResult check(final Transformation node) {
        return check(node, false, false);
    }
    
    /**
     * Checks the given transformation
     * @param node
     * @param forceMeasureInfoLoss
     * @param score
     * @return
     */
    public TransformationResult check(final Transformation node, final boolean forceMeasureInfoLoss, final boolean score) {
        
        // If the result is already know, simply return it
        if (node.getData() != null && node.getData() instanceof TransformationResult) {
            return (TransformationResult) node.getData();
        }
        
        // Store snapshot from last check
        if (stateMachine.getLastTransformation() != null) {
            history.store(solutionSpace.getTransformation(stateMachine.getLastTransformation()), currentGroupify, stateMachine.getLastTransition().snapshot);
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
        currentGroupify.stateAnalyze(node, forceMeasureInfoLoss, config.isReliableSearchProcessEnabled());
        if (forceMeasureInfoLoss && !currentGroupify.isPrivacyModelFulfilled() && !config.isSuppressionAlwaysEnabled()) {
            currentGroupify.stateResetSuppression();
        }
        
        // Compute information loss and lower bound
        InformationLoss<?> loss = null;
        InformationLoss<?> bound = null;
        
        if (score) {
            
            // Calculate score
            loss = metric.getScore(node, currentGroupify);
            
        } else {
            
            // Calculate information loss and bound
            InformationLossWithBound<?> result = (currentGroupify.isPrivacyModelFulfilled() || forceMeasureInfoLoss) ?
                                                  metric.getInformationLoss(node, currentGroupify) : null;
            loss = result != null ? result.getInformationLoss() : null;
            bound = result != null ? result.getLowerBound() : metric.getLowerBound(node, currentGroupify);
        }
        
        // Return result;
        return new TransformationResult(currentGroupify.isPrivacyModelFulfilled(),
                                        minimalClassSizeRequired ? currentGroupify.isMinimalClassSizeFulfilled() : null,
                                        loss, bound);
    }
    
    /**
     * Returns an associated transformation applicator
     * @return
     */
    public TransformationApplicator getApplicator() {
        return new TransformationApplicator(this.manager, this.getOutputBuffer(), this.metric, this.config);
    }

    /**
     * Returns the configuration
     * @return
     */
    public ARXConfigurationInternal getConfiguration() {
        return config;
    }
    
    /**
     * Returns the header of generalized data
     * @return
     */
    public String[] getHeader() {
        return dataGeneralized.getHeader();
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
    public DataMatrix getInputBuffer() {
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
     * Returns the output buffer
     * @return
     */
    public DataMatrix getOutputBuffer() {
        return this.transformer.getBuffer();
    }

    /**
     * Frees memory
     */
    public void reset() {
        stateMachine.reset();
        history.reset();
        history.setSize(0);
        currentGroupify.stateClear();
        lastGroupify.stateClear();
    }
}