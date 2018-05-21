/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;

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
	
	/**
	 * Creates a new instance
	 */
    public ModelDPresenceCriterion() {
        // Empty by design
    }
	
    /**
     * Creates a new instance
     * @param dmin
     * @param dmax
     */
	public ModelDPresenceCriterion(double dmin, double dmax) {
        super();
        this.dmin = dmin;
        this.dmax = dmax;
    }

    @Override
    public ModelDPresenceCriterion clone() {
        ModelDPresenceCriterion result = new ModelDPresenceCriterion();
        result.dmax = this.dmax;
        result.dmin = this.dmin;
        result.setEnabled(this.isEnabled());
        return result;
    }
	
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
	
	@Override
    public String getLabel() {
        return Resources.getMessage("Model.0d") + '\u03B4' + Resources.getMessage("Model.1c"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public void parse(ModelCriterion criterion, boolean _default) {
        if (!(criterion instanceof ModelDPresenceCriterion)) {
            return;
        }
        ModelDPresenceCriterion other = (ModelDPresenceCriterion)criterion;
        this.dmax = other.dmax;
        this.dmin = other.dmin;
        if (!_default) {
            this.setEnabled(other.isEnabled());
        }
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
    
    @Override
    public String toString() {
        return Resources.getMessage("Model.2c") + //$NON-NLS-1$
               SWTUtil.getPrettyString(dmin) +
               Resources.getMessage("Model.3c") + //$NON-NLS-1$
               SWTUtil.getPrettyString(dmax) +
               Resources.getMessage("Model.4c"); //$NON-NLS-1$
    }
}
