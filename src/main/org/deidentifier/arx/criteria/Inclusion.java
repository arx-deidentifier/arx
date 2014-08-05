/*
 * ARX: Powerful Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.arx.criteria;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.DataManager;

/**
 * This is a special criterion that does not enforce any privacy guarantees
 * but allows to define a data subset
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class Inclusion extends DPresence {
    
    private static final long serialVersionUID = -3984193225980793775L;
    
    /**
     * Creates a new instance of the enclosure criterion
     * @param subset Research subset
     */
    public Inclusion(DataSubset subset) {
        super(subset);
    }
        
    @Override
    public void initialize(DataManager manager) {
        // Nothing to do
    }

    @Override
    public int getRequirements(){
        // Requires two counters
        return ARXConfiguration.REQUIREMENT_COUNTER |
               ARXConfiguration.REQUIREMENT_SECONDARY_COUNTER;
    }

    @Override
    public boolean isAnonymous(HashGroupifyEntry entry) {
        return true;
    }

    @Override
    public String toString() {
        return "Inclusion";
    }
}