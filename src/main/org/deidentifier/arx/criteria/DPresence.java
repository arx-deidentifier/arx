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
 * The d-presence criterion
 * Published in:
 * Nergiz M, Atzori M, Clifton C. 
 * Hiding the presence of individuals from shared databases. 
 * Proceedings of the 2007 ACM SIGMOD international conference on Management of data. 2007:665-676.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class DPresence extends ImplicitPrivacyCriterion{
    
    private static final long serialVersionUID = 8534004943055128797L;
    
    /** Delta min*/
    private final double dMin;
    /** Delta max*/
    private final double dMax;
    /** A compressed representation of the research subset*/
    private DataSubset subset;
    
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
        this.subset = subset;
    }
    
    /**
     * For building the inclusion criterion
     * @param subset
     */
    protected DPresence(DataSubset subset) {
        super(true);
        this.dMin = 0d;
        this.dMax = 1d;
        this.subset = subset;
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
        double delta = entry.count == 0 ? 0d : (double) entry.count / (double) entry.pcount;
        return (delta >= dMin) && (delta <= dMax);
    }

    /**
     * Returns the research subset
     * @return
     */
    public DataSubset getSubset() {
        return this.subset;
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