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
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.RowSet;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.DataManager;

/**
 * The d-presence criterion
 * Published in:
 * Nergiz M, Atzori M, Clifton C. 
 * Hiding the presence of individuals from shared databases. 
 * Proceedings of the 2007 ACM SIGMOD international conference on Management of data. 2007:665-676.
 * 
 * @author Prasser, Kohlmayer
 */
public class DPresence extends ImplicitPrivacyCriterion{
    
    private static final long serialVersionUID = 8534004943055128797L;
    
    /** Delta min*/
    private final double dMin;
    /** Delta max*/
    private final double dMax;
    /** A compressed representation of the research subset*/
    private RowSet bitset;
    /** A sorted array representation of the research subset*/
    private int[] array;
    
    /**
     * Creates a new instance of the d-presence criterion as proposed in:
     * Nergiz M, Atzori M, Clifton C. 
     * Hiding the presence of individuals from shared databases. 
     * Proceedings of the 2007 ACM SIGMOD international conference on Management of data. 2007:665-676.
     * @param dMin Delta min
     * @param dMax Delta max
     * @param subset Research subset
     */
    public DPresence(double dMin, double dMax, DataSubset subset) {
        super(false);
        this.dMin = dMin;
        this.dMax = dMax;
        this.bitset = subset.getRowSet();
        this.array = subset.getArray();
    }
    
    /**
     * For building the enclosure criterion
     * @param subset
     */
    protected DPresence(DataSubset subset) {
        super(true);
        this.dMin = 0d;
        this.dMax = 1d;
        this.bitset = subset.getRowSet();
        this.array = subset.getArray();
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
        if (entry.count > 0) {
            double dCurrent = (double) entry.count / (double) entry.pcount;
            // current_delta has to be between delta_min and delta_max
            return (dCurrent >= dMin) && (dCurrent <= dMax);
        } else {
            return true;
        }
    }

    /**
     * Returns the research subset
     * @return
     */
    public RowSet getBitSet() {
        return this.bitset;
    }
    
    /**
     * Returns the size of the research subset
     * @return
     */
    public int getSize() {
        return this.array.length;
    }
    

    /**
     * Returns the research subset
     * @return
     */
    public int[] getArray() {
        return this.array;
    }

    /**
     * Returns dMin
     * @return
     */
    public double getDMin() {
        return dMin;
    }
    

    /**
     * Returns dMax
     * @return
     */
    public double getDMax() {
        return dMax;
    }
    
    
	@Override
	public String toString() {
		return "("+dMin+","+dMax+")-presence";
	}
}