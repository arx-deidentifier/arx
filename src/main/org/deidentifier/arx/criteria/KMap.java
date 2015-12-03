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
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.DataManager;

/**
 * The k-map criterion as proposed by Latanya Sweeney
 * 
 * @author Fabian Prasser
 */
public class KMap extends ImplicitPrivacyCriterion{

    /** SVUID */
    private static final long serialVersionUID = -6966985761538810077L;

    /** K */
    private final int         k;

    /** A compressed representation of the research subset. */
    private DataSubset        subset;

    /**
     * Creates a new instance of the k-map criterion as proposed by Latanya Sweeney 
     * @param k
     * @param subset Research subset
     */
    public KMap(int k, DataSubset subset) {
        super(true, true);
        this.k = k;
        this.subset = subset;
    }
    
    /**
     * Returns k.
     *
     * @return
     */
    public int getK() {
        return k;
    }

    @Override
    public int getRequirements(){
        // Requires two counters
        return ARXConfiguration.REQUIREMENT_COUNTER |
               ARXConfiguration.REQUIREMENT_SECONDARY_COUNTER;
    }

    /**
     * Returns the research subset.
     *
     * @return
     */
    public DataSubset getSubset() {
        return this.subset;
    }

    @Override
    public void initialize(DataManager manager) {
        // Nothing to do
    }
    
    @Override
    public boolean isAnonymous(HashGroupifyEntry entry) {
        return entry.pcount >= k;
    }
    
	@Override
	public String toString() {
		return "("+k+")-map";
	}

    @Override
    public KMap clone() {
        return new KMap(this.getK(), this.getSubset().clone());
    }
}