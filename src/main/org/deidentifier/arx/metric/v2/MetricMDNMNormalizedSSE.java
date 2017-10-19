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


/**
 * This class implements a variant of the SSE metric.
 *
 * @author Fabian Prasser
 */
public class MetricMDNMNormalizedSSE extends MetricMDNMSSE {

    /** SUID. */
    private static final long serialVersionUID = -595852053177582878L;

    /**
     * Default constructor which treats all transformation methods equally.
     */
    public MetricMDNMNormalizedSSE(){
        super(true);
    }

    /**
     * Default constructor which treats all transformation methods equally.
     *
     * @param function
     */
    public MetricMDNMNormalizedSSE(AggregateFunction function){
        super(function, true);
    }
    
    /**
     * A constructor that allows to define a factor weighting generalization and suppression.
     *
     * @param gsFactor A factor [0,1] weighting generalization and suppression.
     *            The default value is 0.5, which means that generalization
     *            and suppression will be treated equally. A factor of 0
     *            will favor suppression, and a factor of 1 will favor
     *            generalization. The values in between can be used for
     *            balancing both methods.
     * @param function
     */
    public MetricMDNMNormalizedSSE(double gsFactor, AggregateFunction function){
        super(gsFactor, function, true);
    }
}
