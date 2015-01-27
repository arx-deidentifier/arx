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
 * This is a special criterion that does not enforce any privacy guarantees
 * but allows to define a data subset.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class Inclusion extends DPresence {
    
    /**  TODO */
    private static final long serialVersionUID = -3984193225980793775L;
    
    /**
     * Creates a new instance of the enclosure criterion.
     *
     * @param subset Research subset
     */
    public Inclusion(DataSubset subset) {
        super(subset);
    }
        
    /* (non-Javadoc)
     * @see org.deidentifier.arx.criteria.DPresence#initialize(org.deidentifier.arx.framework.data.DataManager)
     */
    @Override
    public void initialize(DataManager manager) {
        // Nothing to do
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.criteria.DPresence#getRequirements()
     */
    @Override
    public int getRequirements(){
        // Requires two counters
        return ARXConfiguration.REQUIREMENT_COUNTER |
               ARXConfiguration.REQUIREMENT_SECONDARY_COUNTER;
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.criteria.DPresence#isAnonymous(org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry)
     */
    @Override
    public boolean isAnonymous(HashGroupifyEntry entry) {
        return true;
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.criteria.DPresence#toString()
     */
    @Override
    public String toString() {
        return "Inclusion";
    }
}