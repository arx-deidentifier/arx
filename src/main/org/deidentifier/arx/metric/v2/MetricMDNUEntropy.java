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

import org.deidentifier.arx.framework.lattice.Node;
import org.deidentifier.arx.metric.MetricConfiguration;


/**
 * This class provides an implementation of the non-uniform entropy
 * metric. TODO: Add reference
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class MetricMDNUEntropy extends MetricMDNUEntropyPrecomputed {

    /** SVUID. */
    private static final long serialVersionUID = -8114158144622853288L;

    /**
     * Creates a new instance.
     */
    protected MetricMDNUEntropy() {
        super();
    }
    
    /**
     * Creates a new instance.
     *
     * @param function
     */
    protected MetricMDNUEntropy(AggregateFunction function){
        super(function);
    }

    /**
     * Returns the configuration of this metric.
     *
     * @return
     */
    public MetricConfiguration getConfiguration() {
        return new MetricConfiguration(true,                       // monotonic
                                       0.5d,                       // gs-factor
                                       false,                      // precomputed
                                       0.0d,                       // precomputation threshold
                                       this.getAggregateFunction() // aggregate function
                                       );
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#isIndependent()
     */
    @Override
    public boolean isIndependent() {
        // TODO: Remove
        return false;
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.v2.MetricMDNUEntropyPrecomputed#toString()
     */
    @Override
    public String toString() {
        return "Non-uniform entropy";
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.v2.MetricMDNUEntropyPrecomputed#getLowerBoundInternal(org.deidentifier.arx.framework.lattice.Node)
     */
    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Node node) {
        return null;
    }
}
