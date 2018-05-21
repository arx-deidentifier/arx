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

    /**  SVUID */
    private static final long serialVersionUID = 6429149925699964530L;

    /** The parameter l. */
    protected final double    l;
    
    /**  The derived minimal size of a class */
    protected final int       minSize;

    /**
     * 
     * Creates a new instance.
     *
     * @param attribute
     * @param l
     * @param monotonicWithSuppression
     * @param monotonicWithGeneralization
     */
    public LDiversity(String attribute, double l, boolean monotonicWithSuppression, boolean monotonicWithGeneralization) {
        super(attribute, monotonicWithSuppression, monotonicWithGeneralization);
        this.l = l;
        this.minSize = (int) Math.ceil(l);
    }

    /**
     * Returns the parameter l.
     *
     * @return
     */
    public double getL() {
        return l;
    }

    @Override
    public int getMinimalClassSize() {
        return this.minSize;
    }

    @Override
    public int getRequirements() {

        // Requires a distribution, but nothing else
        return ARXConfiguration.REQUIREMENT_DISTRIBUTION;
    }

    /**
     * Return journalist risk threshold, 1 if there is none
     * @return
     */
    public double getRiskThresholdJournalist() {
        return getRiskThresholdProsecutor();
    }

    /**
     * Return marketer risk threshold, 1 if there is none
     * @return
     */
    public double getRiskThresholdMarketer() {
        return getRiskThresholdProsecutor();
    }

    /**
     * Return prosecutor risk threshold, 1 if there is none
     * @return
     */
    public double getRiskThresholdProsecutor() {
        return 1d / minSize;
    }

    @Override
    public boolean isMinimalClassSizeAvailable() {
        return true;
    }
}