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
    
    /**  TODO */
    private static final long serialVersionUID = 8534004943055128797L;
    
    /** Delta min. */
    private final double dMin;
    
    /** Delta max. */
    private final double dMax;
    
    /** A compressed representation of the research subset. */
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
     * For building the inclusion criterion.
     *
     * @param subset
     */
    protected DPresence(DataSubset subset) {
        super(true);
        this.dMin = 0d;
        this.dMax = 1d;
        this.subset = subset;
    }
        
    /* (non-Javadoc)
     * @see org.deidentifier.arx.criteria.PrivacyCriterion#initialize(org.deidentifier.arx.framework.data.DataManager)
     */
    @Override
    public void initialize(DataManager manager) {
        // Nothing to do
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.criteria.PrivacyCriterion#getRequirements()
     */
    @Override
    public int getRequirements(){
        // Requires two counters
        return ARXConfiguration.REQUIREMENT_COUNTER |
               ARXConfiguration.REQUIREMENT_SECONDARY_COUNTER;
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.criteria.PrivacyCriterion#isAnonymous(org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry)
     */
    @Override
    public boolean isAnonymous(HashGroupifyEntry entry) {
        double delta = entry.count == 0 ? 0d : (double) entry.count / (double) entry.pcount;
        return (delta >= dMin) && (delta <= dMax);
    }

    /**
     * Returns the research subset.
     *
     * @return
     */
    public DataSubset getSubset() {
        return this.subset;
    }

    /**
     * Returns dMin.
     *
     * @return
     */
    public double getDMin() {
        return dMin;
    }
    

    /**
     * Returns dMax.
     *
     * @return
     */
    public double getDMax() {
        return dMax;
    }
    
	/* (non-Javadoc)
	 * @see org.deidentifier.arx.criteria.PrivacyCriterion#toString()
	 */
	@Override
	public String toString() {
		return "("+dMin+","+dMax+")-presence";
	}
}