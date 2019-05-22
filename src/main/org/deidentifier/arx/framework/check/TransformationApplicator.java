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

package org.deidentifier.arx.framework.check;

import org.deidentifier.arx.ARXConfiguration.ARXConfigurationInternal;
import org.deidentifier.arx.framework.check.groupify.HashGroupify;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.DataAggregationInformation;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.data.DataMatrix;
import org.deidentifier.arx.framework.data.Dictionary;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.metric.InformationLoss;
import org.deidentifier.arx.metric.Metric;

/**
 * This class applies a transformation to the dataset.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class TransformationApplicator {

    /** The config. */
    private final ARXConfigurationInternal   config;

    /** The data. */
    private final Data                       inputGeneralized;

    /** The data. */
    private final DataMatrix                 inputAnalyzed;

    /** The data. */
    private final DataMatrix                 outputGeneralized;

    /** Data about microaggregation */
    private final DataAggregationInformation aggregation;

    /** Hierarchies */
    private final GeneralizationHierarchy[]  hierarchies;

    /** The metric. */
    private final Metric<?>                  metric;

    /** Is a minimal class size required */
    private final boolean                    minimalClassSizeRequired;

    /**
     * Creates a new transformation applicator instance.
     * 
     * @param manager The manager
     * @param buffer The buffer
     * @param metric The metric
     * @param config The configuration
     */
    public TransformationApplicator(final DataManager manager,
                                    final DataMatrix buffer,
                                    final Metric<?> metric,
                                    final ARXConfigurationInternal config) {

        // Initialize all operators
        this.metric = metric;
        this.config = config;
        this.inputGeneralized = manager.getDataGeneralized();
        this.inputAnalyzed = manager.getDataAnalyzed().getArray();
        this.hierarchies = manager.getHierarchies();
        this.aggregation = manager.getAggregationInformation();
        this.outputGeneralized = buffer;
        this.minimalClassSizeRequired = config.getMinimalGroupSize() != Integer.MAX_VALUE;
    }

    
    /**
     * Applies the given transformation and returns the dataset
     * @param transformation
     * @return
     */
    public TransformedData applyTransformation(final Transformation<?> transformation) {
        return applyTransformation(transformation, new Dictionary(aggregation.getMicroaggregationHeader().length));
    }
        
    /**
     * Applies the given transformation and returns the dataset
     * @param transformation
     * @param microaggregationDictionary A dictionary for microaggregated values
     * @return
     */
    public TransformedData applyTransformation(final Transformation<?> transformation,
                                               final Dictionary microaggregationDictionary) {
        
        // Prepare
        microaggregationDictionary.definalizeAll();
        

        int initialSize = (int) (inputGeneralized.getDataLength() * 0.01d);
        Transformer transformer = new Transformer(inputGeneralized.getArray(),
                                                  inputAnalyzed,
                                                  outputGeneralized,
                                                  aggregation.getHeader().length,
                                                  hierarchies,
                                                  config);
        
        HashGroupify currentGroupify = new HashGroupify(initialSize,
                                                        config, 
                                                        aggregation.getHeader().length,
                                                        inputGeneralized.getArray(),
                                                        outputGeneralized,
                                                        inputAnalyzed,
                                                        inputGeneralized.getDictionary().getSuppressedCodes());
        
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
        Data microaggregatedOutput = Data.createWrapper(new DataMatrix(0,0), new String[0], new int[0], new Dictionary(0));
        Data generalizedOutput = Data.createWrapper(transformer.getBuffer(), inputGeneralized.getHeader(), inputGeneralized.getColumns(), inputGeneralized.getDictionary());
        
        // Perform microaggregation. This has to be done before suppression.
        if (aggregation.getColdQIsFunctions().length > 0 ||
            aggregation.getHotQIsNotGeneralizedFunctions().length > 0 ||
            aggregation.getHotQIsGeneralizedFunctions().length > 0) {
            microaggregatedOutput = currentGroupify.performMicroaggregation(aggregation, microaggregationDictionary);
        }
        
        // Perform suppression
        if (config.getAbsoluteSuppressionLimit() != 0 || !currentGroupify.isPrivacyModelFulfilled()) {
            currentGroupify.performSuppression();
        }
        
        // Return the buffer
        return new TransformedData(generalizedOutput, microaggregatedOutput, 
                                   new TransformationResult(currentGroupify.isPrivacyModelFulfilled(), 
                                              minimalClassSizeRequired ? currentGroupify.isMinimalClassSizeFulfilled() : null, 
                                              loss, null));
    }
    
}
