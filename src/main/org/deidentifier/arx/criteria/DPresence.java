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

import java.util.Set;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.framework.CompressedBitSet;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.DataManager;

/**
 * The d-presence criterion
 * @author Fabian Prasser
 */
public class DPresence extends PrivacyCriterion{
    
    private static final long serialVersionUID = 8534004943055128797L;
    
    /** Delta min*/
    private final double dMin;
    /** Delta max*/
    private final double dMax;
    /** The research subset, a set of tuple ids*/
    private final Set<Integer> subset;
    /** A compressed representation of the research subset*/
    private CompressedBitSet bitset;
    
    /**
     * Creates a new instance
     * @param dMin Delta min
     * @param dMax Delta max
     * @param subset Research subset
     */
    public DPresence(double dMin, double dMax, Set<Integer> subset) {
        super(false);
        this.dMin = dMin;
        this.dMax = dMax;
        this.subset = subset;
    }
        
    @Override
    public void initialize(DataManager manager) {
        bitset = new CompressedBitSet(manager.getDataQI().getDataLength());
        for (Integer line : subset) {
            bitset.set(line);
        }
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
    public CompressedBitSet getResearchSubset() {
        return this.bitset;
    }
}