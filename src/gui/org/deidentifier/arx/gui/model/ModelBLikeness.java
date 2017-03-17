/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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

import org.deidentifier.arx.criteria.BasicBLikeness;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;

/**
 * This class implements a model for the b-likeness privacy model
 *
 * @author Fabian Prasser
 */
public class ModelBLikeness extends ModelExplicitCriterion{

    /** SVUID */
    private static final long serialVersionUID = 2102590973710328801L;

    /** Delta */
    private double            beta             = 1.0d;

    /**
     * Creates a new instance.
     *
     * @param attribute
     */
    public ModelBLikeness(String attribute) {
        super(attribute);
    }
    
    /**
     * Creates a new instance.
     *
     * @param attribute
     */
    public ModelBLikeness(String attribute, double beta) {
        super(attribute);
        this.beta = beta;
    }
    
    @Override
    public ModelBLikeness clone() {
        ModelBLikeness result = new ModelBLikeness(this.getAttribute());
        result.beta = this.beta;
        result.setEnabled(this.isEnabled());
        return result;
    }
	
	@Override
	public PrivacyCriterion getCriterion(Model model) {
	    return new BasicBLikeness(getAttribute(), beta);
	}
	
	/**
     * Returns B.
     *
     * @return
     */
	public double getB() {
		return beta;
	}
	
	@Override
    public String getLabel() {
        return  Resources.getMessage("Model.36"); //$NON-NLS-1$
    }

    @Override
    public void parse(ModelCriterion criterion, boolean _default) {
        if (!(criterion instanceof ModelBLikeness)) {
            return;
        }
        ModelBLikeness other = (ModelBLikeness)criterion;
        this.beta = other.beta;
        this.setEnabled(other.isEnabled());
    }
    
	@Override
    public void pull(ModelExplicitCriterion criterion) {
        if (!(criterion instanceof ModelBLikeness)) {
            throw new RuntimeException(Resources.getMessage("Model.2d")); //$NON-NLS-1$
        }
        ModelBLikeness other = (ModelBLikeness)criterion;
        this.beta = other.beta;
    }

    /**
     * Sets Beta.
     *
     * @param beta
     */
	public void setB(double beta) {
		this.beta = beta;
	}
    
    @Override
    public String toString() {
        return Resources.getMessage("Model.37") + SWTUtil.getPrettyString(beta) + Resources.getMessage("Model.38"); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
