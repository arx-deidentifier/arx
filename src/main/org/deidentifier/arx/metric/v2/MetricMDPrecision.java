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

import org.deidentifier.arx.framework.check.groupify.IHashGroupify;
import org.deidentifier.arx.framework.lattice.Node;
import org.deidentifier.arx.metric.MetricConfiguration;

/**
 * This class provides an implementation of a weighted precision metric as 
 * proposed in: <br>
 * Sweeney, L. (2002). Achieving k-anonymity privacy protection using generalization and suppression.<br> 
 * International Journal of Uncertainty Fuzziness and, 10(5), 2002.<br>
 * <br>
 * This metric will respect attribute weights defined in the configuration.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class MetricMDPrecision extends MetricMDNMPrecision {

    /** SVUID. */
    private static final long serialVersionUID = 8514706682676049814L;

    /**
     * Creates a new instance.
     */
    protected MetricMDPrecision() {
        super(true, true, AggregateFunction.ARITHMETIC_MEAN);
    }
    
    /**
     * Creates a new instance.
     *
     * @param function
     */
    protected MetricMDPrecision(AggregateFunction function){
        super(true, true, function);
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
     * @see org.deidentifier.arx.metric.v2.MetricMDNMPrecision#toString()
     */
    @Override
    public String toString() {
        return "Monotonic precision";
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.v2.MetricMDNMPrecision#getInformationLossInternal(org.deidentifier.arx.framework.lattice.Node, org.deidentifier.arx.framework.check.groupify.IHashGroupify)
     */
    @Override
    protected ILMultiDimensionalWithBound getInformationLossInternal(final Node node, final IHashGroupify g) {
        AbstractILMultiDimensional loss = super.getLowerBoundInternal(node);
        return new ILMultiDimensionalWithBound(loss, loss);
    }
}
