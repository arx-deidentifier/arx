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
 * This class provides an implementation of the monotonic DM* metric.
 * TODO: Add reference
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class MetricSDDiscernability extends MetricSDNMDiscernability {

    /** SVUID. */
    private static final long serialVersionUID = -9156839234909657895L;

    /**
     * Creates a new instance.
     */
    protected MetricSDDiscernability() {
        super(true);
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
                                       AggregateFunction.SUM       // aggregate function
                                       );
    }
    
    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.v2.MetricSDNMDiscernability#toString()
     */
    @Override
    public String toString() {
        return "Monotonic discernability (DM*)";
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.v2.MetricSDNMDiscernability#getInformationLossInternal(org.deidentifier.arx.framework.lattice.Node, org.deidentifier.arx.framework.check.groupify.IHashGroupify)
     */
    @Override
    protected ILSingleDimensionalWithBound getInformationLossInternal(final Node node, final IHashGroupify g) {
        ILSingleDimensional result = super.getLowerBoundInternal(node, g);
        return new ILSingleDimensionalWithBound(result.getValue(), result.getValue());
    }
}
