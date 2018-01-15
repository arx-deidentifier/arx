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

import java.util.Arrays;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.certificate.elements.ElementData;
import org.deidentifier.arx.framework.check.groupify.HashGroupify;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.metric.MetricConfiguration;

import com.carrotsearch.hppc.IntIntOpenHashMap;

/**
 * This class provides an implementation of the non-uniform entropy
 * metric. See:<br>
 * A. De Waal and L. Willenborg: 
 * "Information loss through global recoding and local suppression" 
 * Netherlands Off Stat, vol. 14, pp. 17–20, 1999.
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
        super(true, false, false, 0.5d, AggregateFunction.SUM);
    }
    
    /**
     * Creates a new instance.
     *
     * @param gsFactor
     * @param function
     */
    protected MetricMDNUNMEntropyPrecomputed(double gsFactor, AggregateFunction function){
        super(true, false, false, gsFactor, function);
    }
    
    /**
     * Returns the configuration of this metric.
     *
     * @return
     */
    public MetricConfiguration getConfiguration() {
        return new MetricConfiguration(false, // monotonic
                                       super.getGeneralizationSuppressionFactor(), // gs-factor
                                       true, // precomputed
                                       1.0d, // precomputation threshold
                                       this.getAggregateFunction() // aggregate function
        );
    }

    @Override
    public boolean isGSFactorSupported() {
        return true;
    }

    @Override
    public boolean isPrecomputed() {
        return true;
    }

    @Override
    public ElementData render(ARXConfiguration config) {
        ElementData result = new ElementData("Non-uniform entropy");
        result.addProperty("Aggregate function", super.getAggregateFunction().toString());
        result.addProperty("Monotonic", this.isMonotonic(config.getSuppressionLimit()));
        result.addProperty("Generalization factor", this.getGeneralizationFactor());
        result.addProperty("Suppression factor", this.getSuppressionFactor());
        return result;
    }

    @Override
    public String toString() {
        return "Non-monotonic non-uniform entropy";
    }

    @Override
    protected ILMultiDimensionalWithBound getInformationLossInternal(final Transformation node, final HashGroupify g) {
        
        // Prepare
        double sFactor = super.getSuppressionFactor();
        
        // Compute non-uniform entropy
        double[] result = super.getInformationLossInternalRaw(node, g);
        double[] bound = new double[result.length];
        System.arraycopy(result, 0, bound, 0, result.length);
        
        // Compute loss induced by suppression
        double suppressed = 0;
        final IntIntOpenHashMap[] original = new IntIntOpenHashMap[node.getGeneralization().length];
        for (int i = 0; i < original.length; i++) {
            original[i] = new IntIntOpenHashMap();
        }

        // Compute counts for suppressed values in each column 
        // m.count only counts tuples from the research subset
        HashGroupifyEntry m = g.getFirstEquivalenceClass();
        while (m != null) {
            if (!m.isNotOutlier && m.count > 0) {
                suppressed += m.count;
                m.read();
                for (int i = 0; i < original.length; i++) {
                    original[i].putOrAdd(m.next(), m.count, m.count);
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
                        result[i] += count * log2(count / suppressed) * sFactor;
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

    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Transformation node) {
        return super.getInformationLossInternal(node, (HashGroupify)null).getLowerBound();
    }

    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Transformation node,
                                                       HashGroupify groupify) {
        return super.getInformationLossInternal(node, (HashGroupify)null).getLowerBound();
    }

    @Override
    protected void initializeInternal(final DataManager manager,
                                      final DataDefinition definition, 
                                      final Data input, 
                                      final GeneralizationHierarchy[] hierarchies, 
                                      final ARXConfiguration config) {
        
        super.initializeInternal(manager, definition, input, hierarchies, config);

        // Prepare
        double gFactor = super.getGeneralizationFactor();
        double sFactor = super.getSuppressionFactor();
        
        // Compute a reasonable minimum & maximum
        double[] min = new double[hierarchies.length];
        Arrays.fill(min, 0d);
        
        double[] max = new double[hierarchies.length];
        for (int i=0; i<max.length; i++) {
            max[i] = (2d * input.getDataLength() * log2(input.getDataLength())) * Math.max(gFactor, sFactor);
        }
        
        super.setMax(max);
        super.setMin(min);
    }
}
