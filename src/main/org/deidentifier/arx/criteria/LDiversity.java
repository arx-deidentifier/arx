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
 * An abstract base class for l-diversity criteria
 * Published in:
 * Machanavajjhala A, Kifer D, Gehrke J. 
 * l-diversity: Privacy beyond k-anonymity. 
 * Transactions on Knowledge Discovery from Data (TKDD). 2007;1(1):3. 
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public abstract class LDiversity extends ExplicitPrivacyCriterion {

    /**  TODO */
    private static final long serialVersionUID = 6429149925699964530L;

    /** The parameter l. */
    protected final double    l;
    
    /**  TODO */
    protected final int       minSize;

    /**
     * 
     * Creates a new instance.
     *
     * @param attribute
     * @param l
     * @param monotonic
     */
    public LDiversity(String attribute, double l, boolean monotonic) {
        super(attribute, monotonic);
        this.l = l;
        this.minSize = (int) Math.ceil(l);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.criteria.PrivacyCriterion#getRequirements()
     */
    @Override
    public int getRequirements() {

        // Requires a distribution, but nothing else
        return ARXConfiguration.REQUIREMENT_DISTRIBUTION;
    }

    /**
     * Returns the parameter l.
     *
     * @return
     */
    public double getL() {
        return l;
    }
    
    /**
     * Returns the minimal group size required to fulfill this criterion.
     *
     * @return
     */
    public int getMinimalGroupSize(){
        return minSize;
    }
}
