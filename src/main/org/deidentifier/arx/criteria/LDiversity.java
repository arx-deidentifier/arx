/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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

/**
 * An abstract base class for l-diversity criteria
 * Published in:
 * Machanavajjhala A, Kifer D, Gehrke J. 
 * l-diversity: Privacy beyond k-anonymity. 
 * Transactions on Knowledge Discovery from Data (TKDD). 2007;1(1):3. 
 * 
 * @author Prasser, Kohlmayer
 */
public abstract class LDiversity extends ExplicitPrivacyCriterion {

    private static final long serialVersionUID = 6429149925699964530L;

    /** The parameter l*/
    protected final double    l;
    protected final int       minSize;

    /** 
     * Creates a new instance
     * @param l
     */
    public LDiversity(String attribute, double l) {
        super(attribute, false);
        this.l = l;
        this.minSize = (int) Math.ceil(l);
    }

    @Override
    public int getRequirements() {

        // Requires a distribution, but nothing else
        return ARXConfiguration.REQUIREMENT_DISTRIBUTION;
    }

    /**
     * Returns the parameter l
     * @return
     */
    public double getL() {
        return l;
    }
    
    /**
     * Returns the minimal group size required to fulfill this criterion
     * @return
     */
    public int getMinimalGroupSize(){
        return minSize;
    }
}
