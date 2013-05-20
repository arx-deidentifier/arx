/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
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
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;

/**
 * The k-anonymity criterion
 * @author Prasser, Kohlmayer
 */
public class KAnonymity extends PrivacyCriterion{

    private static final long serialVersionUID = -8370928677928140572L;
    
    /** The parameter k*/
    private final int k;
    
    /**
     * Creates a new instance
     * @param k
     */
    public KAnonymity(int k){
        super(true);
        this.k = k;
    }

    @Override
    public int getRequirements(){
        // Requires only one counter
        return ARXConfiguration.REQUIREMENT_COUNTER;
    }

    @Override
    public boolean isAnonymous(HashGroupifyEntry entry) {
        return entry.count >= k;
    }

    /**
     * Returns the parameter k
     * @return
     */
    public int getK() {
        return k;
    }
}