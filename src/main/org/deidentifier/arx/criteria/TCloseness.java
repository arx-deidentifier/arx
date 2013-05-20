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

/**
 * An abstract base class for t-closeness criteria
 * @author Fabian Prasser
 */
public abstract class TCloseness extends PrivacyCriterion {

    private static final long serialVersionUID = -139670758266526116L;
    
    /** The param t*/
    protected final double       t;
    
    /**
     * Creates a new instance
     * @param t
     */
    public TCloseness(double t) {
        super(false);
        this.t = t;
    }

    @Override
    public int getRequirements(){
        // Requires a distribution
        return ARXConfiguration.REQUIREMENT_DISTRIBUTION;
    }
}
