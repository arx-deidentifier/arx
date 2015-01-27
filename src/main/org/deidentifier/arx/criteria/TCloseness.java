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

package org.deidentifier.arx.criteria;

import org.deidentifier.arx.ARXConfiguration;

/**
 * An abstract base class for t-closeness criteria as proposed in:
 * Li N, Li T, Venkatasubramanian S. 
 * t-Closeness: Privacy beyond k-anonymity and l-diversity. 
 * 23rd International Conference on Data Engineering. 2007:106-115. 
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public abstract class TCloseness extends ExplicitPrivacyCriterion {

    /**  TODO */
    private static final long serialVersionUID = -139670758266526116L;
    
    /** The param t. */
    protected final double       t;
    
    /**
     * Creates a new instance.
     *
     * @param attribute
     * @param t
     */
    public TCloseness(String attribute, double t) {
        super(attribute, false);
        this.t = t;
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.criteria.PrivacyCriterion#getRequirements()
     */
    @Override
    public int getRequirements(){
        // Requires a distribution
        return ARXConfiguration.REQUIREMENT_DISTRIBUTION;
    }
    
    /**
     * Returns the parameter t.
     *
     * @return
     */
    public double getT(){
        return t;
    }
}
