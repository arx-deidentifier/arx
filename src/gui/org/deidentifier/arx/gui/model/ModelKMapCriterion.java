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
import org.deidentifier.arx.criteria.KMap;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;

/**
 * This class implements a model for the d-presence criterion.
 *
 * @author Fabian Prasser
 */
public class ModelKMapCriterion extends ModelImplicitCriterion{

    /** SVUID. */
    private static final long serialVersionUID = 2268947734419591705L;
    
    /** Dmin. */
	private int k = 5;
	
	/**
	 * Creates a new instance
	 */
    public ModelKMapCriterion() {
        // Empty by design
    }
	
    /**
     * Creates a new instance
     * @param k
     */
	public ModelKMapCriterion(int k) {
        super();
        this.k = k;
    }

    @Override
    public ModelKMapCriterion clone() {
        ModelKMapCriterion result = new ModelKMapCriterion();
        result.k = this.k;
        result.setEnabled(this.isEnabled());
        return result;
    }
	
	@Override
	public PrivacyCriterion getCriterion(Model model) {
	    DataSubset subset = DataSubset.create(model.getInputConfig().getInput(), model.getInputConfig().getResearchSubset());
		return new KMap(k, subset);
	}
	
	/**
     * Returns k.
     *
     * @return
     */
	public int getK() {
		return k;
	}
	
	@Override
    public String getLabel() {
        return Resources.getMessage("Model.32"); //$NON-NLS-1$
    }

    @Override
    public void parse(ModelCriterion criterion, boolean _default) {
        if (!(criterion instanceof ModelKMapCriterion)) {
            return;
        }
        ModelKMapCriterion other = (ModelKMapCriterion)criterion;
        this.k = other.k;
        this.setEnabled(other.isEnabled());
    }

    /**
     * Sets k.
     *
     * @param k
     */
	public void setK(int k) {
		this.k = k;
	}

    @Override
    public String toString() {
        return SWTUtil.getPrettyString(k) + Resources.getMessage("Model.33"); //$NON-NLS-1$
    }
}
