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

package org.deidentifier.arx.metric.v2;

import java.util.HashMap;
import java.util.Map;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.certificate.elements.ElementData;
import org.deidentifier.arx.criteria.EDDifferentialPrivacy;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.framework.check.groupify.HashGroupify;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.metric.MetricConfiguration;


/**
 * This class provides an implementation of the non-uniform entropy
 * metric. TODO: Add reference
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 * @author Raffael Bild
 */
public class MetricMDNUNMEntropy extends MetricMDNUNMEntropyPrecomputed {

    /** SVUID. */
    private static final long serialVersionUID = -7428794463838685004L;
    
    /** Total number of records */
    double                    numRecords;
    
    /** Total number of attributes (having a generalization hierarchy) */
    double                    numAttrs;
    
    /** Minimal size of equivalence classes enforced by the differential privacy model */
    double                    k;
    
    /** The root values of the generalization hierarchy of each attribute */
    int[]                     rootValues; // TODO: This assumes that a single root node exists in all hierarchies

    /**
     * Creates a new instance.
     */
    protected MetricMDNUNMEntropy() {
        super();
    }
    
    /**
     * Creates a new instance.
     *
     * @param gsFactor
     * @param function
     * 
     */
    protected MetricMDNUNMEntropy(double gsFactor, AggregateFunction function){
        super(gsFactor, function);
    }

    /**
     * Returns the configuration of this metric.
     *
     * @return
     */
    public MetricConfiguration getConfiguration() {
        return new MetricConfiguration(false,                                       // monotonic
                                       super.getGeneralizationSuppressionFactor(),  // gs-factor
                                       false,                                       // precomputed
                                       0.0d,                                        // precomputation threshold
                                       this.getAggregateFunction()                  // aggregate function
                                       );
    }

    @Override
    public boolean isGSFactorSupported() {
        return true;
    }

    @Override
    public ElementData render(ARXConfiguration config) {
        ElementData result = new ElementData("Non-uniform entropy");
        result.addProperty("Aggregate function", super.getAggregateFunction().toString());
        result.addProperty("Monotonic", this.isMonotonic(config.getMaxOutliers()));
        result.addProperty("Generalization factor", this.getGeneralizationFactor());
        result.addProperty("Suppression factor", this.getSuppressionFactor());
        return result;
    }

    @Override
    public String toString() {
        return "Non-monotonic non-uniform entropy";
    }

    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Transformation node) {
        return null;
    }
    
    @Override
    public double getScore(final Transformation node, final HashGroupify groupify) {
        
        // Prepare
        double score = 0d;
        

        // For every attribute
        for (int j = 0; j < numAttrs; ++j) {

            Map<Integer, Integer> nonSuppressedValueToCount = new HashMap<Integer, Integer>();

            HashGroupifyEntry entry = groupify.getFirstEquivalenceClass();
            while (entry != null) {

                // Process values of records which have not been suppressed by sampling
                if (entry.isNotOutlier && entry.key[j] != rootValues[j]) {
                    // The attribute value has neither been suppressed because of record suppression nor because of generalization
                    int value = entry.key[j];
                    int valueCount = nonSuppressedValueToCount.containsKey(value) ?
                            (nonSuppressedValueToCount.get(value) + entry.count) : entry.count;
                    nonSuppressedValueToCount.put(value, valueCount);
                } else {
                    // The attribute value has been suppressed because of record suppression or because of generalization
                    score += entry.count * numRecords;
                }
                
                // Add values for records which have been suppressed by sampling
                score += (entry.pcount - entry.count) * numRecords;

                // Next group
                entry = entry.nextOrdered;
            }

            // Add values for all attribute values which were not suppressed
            for (int count : nonSuppressedValueToCount.values()) {
                score += count * count;
            }
        }

        // Produce score function divided through sensitivity
        score *= -1d / (numRecords * numAttrs);
        return (k==1) ? score / 5d : score / (k * k / (k - 1d) + 1d);
    }
    
    @Override
    protected void initializeInternal(final DataManager manager,
                                      final DataDefinition definition, 
                                      final Data input, 
                                      final GeneralizationHierarchy[] hierarchies, 
                                      final ARXConfiguration config) {
        
        super.initializeInternal(manager, definition, input, hierarchies, config);
        
        numRecords = manager.getDataGeneralized().getDataLength();
        numAttrs = manager.getHierarchies().length;
        
        for (PrivacyCriterion c : config.getPrivacyModels()) {
            if (c instanceof EDDifferentialPrivacy) {
                k = c.getMinimalClassSize();
                break;
            }
        }

        rootValues = new int[(int)numAttrs];
        for (int i = 0; i < numAttrs; i++) {
            int[] row = manager.getHierarchies()[i].getArray()[0];
            rootValues[i] = row[row.length - 1];
        }
    }
}
