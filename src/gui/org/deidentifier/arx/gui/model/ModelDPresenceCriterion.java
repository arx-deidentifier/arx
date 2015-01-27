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

package org.deidentifier.arx.gui.model;

import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.criteria.PrivacyCriterion;

/**
 * This class implements a model for the d-presence criterion.
 *
 * @author Fabian Prasser
 */
public class ModelDPresenceCriterion extends ModelImplicitCriterion{

    /** SVUID. */
	private static final long serialVersionUID = -1765428286262869856L;
	
	/** Dmin. */
	private double dmin = 0.0d;
	
	/** Dmax. */
	private double dmax = 0.0d;
	
	/* (non-Javadoc)
	 * @see org.deidentifier.arx.gui.model.ModelCriterion#getCriterion(org.deidentifier.arx.gui.model.Model)
	 */
	@Override
	public PrivacyCriterion getCriterion(Model model) {
	    DataSubset subset = DataSubset.create(model.getInputConfig().getInput(), model.getInputConfig().getResearchSubset());
		return new DPresence(dmin, dmax, subset);
	}
	
	/**
     * Returns dmax.
     *
     * @return
     */
	public double getDmax() {
		return dmax;
	}
	
	/**
     * Returns dmin.
     *
     * @return
     */
	public double getDmin() {
		return dmin;
	}
	
	/**
     * Sets dmax.
     *
     * @param dmax
     */
	public void setDmax(double dmax) {
		this.dmax = dmax;
	}
	
	/**
     * Sets dmin.
     *
     * @param dmin
     */
	public void setDmin(double dmin) {
		this.dmin = dmin;
	}

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.model.ModelCriterion#toString()
     */
    @Override
    public String toString() {
        // TODO: Move to messages.properties
        return "("+String.valueOf(dmin)+","+String.valueOf(dmax)+")-Presence";
    }
}
