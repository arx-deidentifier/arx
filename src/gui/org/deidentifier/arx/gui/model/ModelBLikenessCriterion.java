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

import org.deidentifier.arx.criteria.BasicBLikeness;
import org.deidentifier.arx.criteria.EnhancedBLikeness;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;

/**
 * This class implements a model for the b-likeness privacy model
 *
 * @author Fabian Prasser
 */
public class ModelBLikenessCriterion extends ModelExplicitCriterion{

    /** SVUID */
    private static final long serialVersionUID = 2269238032187539934L;

    /** Is this the enhanced variant */
    private boolean           enhanced         = false;

    /** Delta */
    private double            beta             = 1.0d;

    /**
     * Creates a new instance.
     *
     * @param attribute
     */
    public ModelBLikenessCriterion(String attribute) {
        super(attribute);
    }
    
    /**
     * Creates a new instance.
     *
     * @param attribute
     * @param beta
     * @param enhanced
     */
    public ModelBLikenessCriterion(String attribute, double beta, boolean enhanced) {
        super(attribute);
        this.beta = beta;
        this.enhanced = enhanced;
    }
    
    @Override
    public ModelBLikenessCriterion clone() {
        ModelBLikenessCriterion result = new ModelBLikenessCriterion(this.getAttribute());
        result.beta = this.beta;
        result.enhanced = this.enhanced;
        result.setEnabled(this.isEnabled());
        return result;
    }
	
	@Override
	public PrivacyCriterion getCriterion(Model model) {
	    if (enhanced) {
	        return new EnhancedBLikeness(getAttribute(), beta);
	    } else {
	        return new BasicBLikeness(getAttribute(), beta);
	    }
	}
	
	/**
     * Returns B.
     *
     * @return
     */
	public double getB() {
		return beta;
	}
	
	/**
	 * Returns whether this is the enhanced variant
	 * @return
	 */
	public boolean isEnhanced() {
	    return enhanced;
	}
	
	@Override
    public String getLabel() {
        return '\u03B2' + Resources.getMessage("Model.36"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public void parse(ModelCriterion criterion, boolean _default) {
        if (!(criterion instanceof ModelBLikenessCriterion)) {
            return;
        }
        ModelBLikenessCriterion other = (ModelBLikenessCriterion)criterion;
        this.beta = other.beta;
        this.enhanced = other.enhanced;
        this.setEnabled(other.isEnabled());
    }
    
	@Override
    public void pull(ModelExplicitCriterion criterion) {
        if (!(criterion instanceof ModelBLikenessCriterion)) {
            throw new RuntimeException(Resources.getMessage("Model.2d")); //$NON-NLS-1$
        }
        ModelBLikenessCriterion other = (ModelBLikenessCriterion)criterion;
        this.beta = other.beta;
        this.enhanced = other.enhanced;
    }

    /**
     * Sets Beta.
     *
     * @param beta
     */
	public void setB(double beta) {
		this.beta = beta;
	}
	
	/**
	 * Sets whether or not this is the enhanced variant
	 * @param enhanced
	 */
	public void setEnhanced(boolean enhanced) {
	    this.enhanced = enhanced;
	}
    
    @Override
    public String toString() {
        return (this.enhanced ? Resources.getMessage("Model.39") : Resources.getMessage("Model.37")) + //$NON-NLS-1$ //$NON-NLS-2$
                SWTUtil.getPrettyString(beta) + Resources.getMessage("Model.38"); //$NON-NLS-1$
    }
}
