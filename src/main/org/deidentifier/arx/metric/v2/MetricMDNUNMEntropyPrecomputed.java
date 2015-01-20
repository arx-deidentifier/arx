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

package org.deidentifier.arx.metric.v2;

import java.util.Arrays;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.check.groupify.IHashGroupify;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Node;
import org.deidentifier.arx.metric.MetricConfiguration;

import com.carrotsearch.hppc.IntIntOpenHashMap;

/**
 * This class provides an implementation of the non-uniform entropy
 * metric. TODO: Add reference
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class MetricMDNUNMEntropyPrecomputed extends MetricMDNUEntropyPrecomputed {

    /** SVUID. */
    private static final long serialVersionUID = -7428794463838685004L;

    /**
     * Creates a new instance.
     */
    protected MetricMDNUNMEntropyPrecomputed() {
        super(false, false, AggregateFunction.SUM);
    }
    
    /**
     * Creates a new instance.
     *
     * @param function
     */
    protected MetricMDNUNMEntropyPrecomputed(AggregateFunction function){
        super(false, false, function);
    }
    
    /**
     * Returns the configuration of this metric.
     *
     * @return
     */
    public MetricConfiguration getConfiguration() {
        return new MetricConfiguration(false,                      // monotonic
                                       0.5d,                       // gs-factor
                                       true,                       // precomputed
                                       1.0d,                       // precomputation threshold
                                       this.getAggregateFunction() // aggregate function
                                       );
    }


    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.v2.MetricMDNUEntropyPrecomputed#toString()
     */
    @Override
    public String toString() {
        return "Non-monotonic non-uniform entropy";
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.v2.MetricMDNUEntropyPrecomputed#getInformationLossInternal(org.deidentifier.arx.framework.lattice.Node, org.deidentifier.arx.framework.check.groupify.IHashGroupify)
     */
    @Override
    protected ILMultiDimensionalWithBound getInformationLossInternal(final Node node, final IHashGroupify g) {
        
        // Compute non-uniform entropy
        double[] result = super.getInformationLossInternalRaw(node, g);
        double[] bound = new double[result.length];
        System.arraycopy(result, 0, bound, 0, result.length);
        
        // Compute loss induced by suppression
        double suppressed = 0;
        final IntIntOpenHashMap[] original = new IntIntOpenHashMap[node.getTransformation().length];
        for (int i = 0; i < original.length; i++) {
            original[i] = new IntIntOpenHashMap();
        }

        // Compute counts for suppressed values in each column 
        // m.count only counts tuples from the research subset
        HashGroupifyEntry m = g.getFirstEntry();
        while (m != null) {
            if (!m.isNotOutlier && m.count > 0) {
                suppressed += m.count;
                for (int i = 0; i < original.length; i++) {
                    original[i].putOrAdd(m.key[i], m.count, m.count);
                }
            }
            m = m.nextOrdered;
        }

        // Evaluate non-uniform entropy for suppressed tuples
        if (suppressed != 0){
            for (int i = 0; i < original.length; i++) {
                IntIntOpenHashMap map = original[i];
                for (int j = 0; j < map.allocated.length; j++) {
                    if (map.allocated[j]) {
                        double count = map.values[j];
                        result[i] += count * log2(count / suppressed);
                    }
                }
            }
        }
        
        // Switch sign bit and round
        for (int column = 0; column < result.length; column++) {
            result[column] = round(result[column] == 0.0d ? result[column] : -result[column]);
        }

        
        
        // Return
        return new ILMultiDimensionalWithBound(createInformationLoss(result),
                                               createInformationLoss(bound));
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.v2.MetricMDNUEntropyPrecomputed#getLowerBoundInternal(org.deidentifier.arx.framework.lattice.Node)
     */
    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Node node) {
        return super.getInformationLossInternal(node, null).getLowerBound();
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.v2.MetricMDNUEntropyPrecomputed#getLowerBoundInternal(org.deidentifier.arx.framework.lattice.Node, org.deidentifier.arx.framework.check.groupify.IHashGroupify)
     */
    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Node node,
                                                       IHashGroupify groupify) {
        return super.getInformationLossInternal(node, null).getLowerBound();
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.v2.MetricMDNUEntropyPrecomputed#initializeInternal(org.deidentifier.arx.DataDefinition, org.deidentifier.arx.framework.data.Data, org.deidentifier.arx.framework.data.GeneralizationHierarchy[], org.deidentifier.arx.ARXConfiguration)
     */
    @Override
    protected void initializeInternal(DataDefinition definition,
                                      Data input,
                                      GeneralizationHierarchy[] hierarchies,
                                      ARXConfiguration config) {
        
        super.initializeInternal(definition, input, hierarchies, config);

        // Compute a reasonable minimum & maximum
        double[] min = new double[hierarchies.length];
        Arrays.fill(min, 0d);
        
        double[] max = new double[hierarchies.length];
        for (int i=0; i<max.length; i++) {
            max[i] = 2d * input.getDataLength() * log2(input.getDataLength());
        }
        
        super.setMax(max);
        super.setMin(min);
    }
}
