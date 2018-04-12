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

package org.deidentifier.arx.metric;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.certificate.elements.ElementData;
import org.deidentifier.arx.framework.check.groupify.HashGroupify;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Transformation;

import com.carrotsearch.hppc.IntIntOpenHashMap;

/**
 * This class provides an efficient implementation of a non-monotonic and
 * non-uniform entropy metric. It avoids a cell-by-cell process by utilizing a
 * three-dimensional array that maps identifiers to their frequency for all
 * quasi-identifiers and generalization levels. It further reduces the overhead
 * induced by subsequent calls by caching the results for previous columns and
 * generalization levels. It takes supressed tuples into account by adding the
 * information loss induced by suppressing the transformed representation of the
 * outliers.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class MetricNMEntropy extends MetricEntropy {

    /** SVUID. */
    private static final long serialVersionUID = 5789738609326541247L;
    
    /**
     * Creates a new instance.
     */
    protected MetricNMEntropy() {
        super(true, false, false);
    }
    
    @Override
    public ElementData render(ARXConfiguration config) {
        ElementData result = new ElementData("Non-uniform entropy");
        result.addProperty("Monotonic", this.isMonotonic(config.getSuppressionLimit()));
        return result;
    }

    @Override
    public String toString() {
        return "Non-Monotonic Non-Uniform Entropy";
    }

    @Override
    protected InformationLossWithBound<InformationLossDefault> getInformationLossInternal(final Transformation node, final HashGroupify g) {

        // Obtain "standard" value
        final InformationLossDefault originalInfoLossDefault = node.getLowerBound() != null ? (InformationLossDefault)node.getLowerBound() :
                                                               super.getInformationLossInternal(node, g).getInformationLoss();
        
        // Compute loss induced by suppression
        double originalInfoLoss = originalInfoLossDefault.getValue();
        double suppressedTuples = 0;
        double additionalInfoLoss = 0;
        final IntIntOpenHashMap[] original = new IntIntOpenHashMap[node.getGeneralization().length];
        for (int i = 0; i < original.length; i++) {
            original[i] = new IntIntOpenHashMap();
        }

        // Compute counts for suppressed values in each column 
        // m.count only counts tuples from the research subset
        HashGroupifyEntry m = g.getFirstEquivalenceClass();
        while (m != null) {
            if (!m.isNotOutlier && m.count > 0) {
                suppressedTuples += m.count;
                m.read();
                for (int i = 0; i < original.length; i++) {
                    original[i].putOrAdd(m.next(), m.count, m.count);
                }
            }
            m = m.nextOrdered;
        }

        // Evaluate entropy for suppressed tuples
        if (suppressedTuples != 0){
	        for (int i = 0; i < original.length; i++) {
	            IntIntOpenHashMap map = original[i];
	            for (int j = 0; j < map.allocated.length; j++) {
	                if (map.allocated[j]) {
	                    double count = map.values[j];
	                    additionalInfoLoss += count * MetricEntropy.log2(count / suppressedTuples);
	                }
	            }
	        }
        }
        
        // Return sum of both values
        return new InformationLossDefaultWithBound(round(originalInfoLoss - additionalInfoLoss), originalInfoLossDefault.getValue());
    }
    
    @Override
    protected InformationLossDefault getLowerBoundInternal(Transformation node) {
        return super.getInformationLossInternal(node, (HashGroupify)null).getInformationLoss();
    }

    @Override
    protected InformationLossDefault getLowerBoundInternal(Transformation node,
                                                           HashGroupify groupify) {
        return getLowerBoundInternal(node);
    }

    @Override
    protected void initializeInternal(final DataManager manager,
                                      final DataDefinition definition, 
                                      final Data input, 
                                      final GeneralizationHierarchy[] hierarchies, 
                                      final ARXConfiguration config) {
        super.initializeInternal(manager, definition, input, hierarchies, config);
    }

}
