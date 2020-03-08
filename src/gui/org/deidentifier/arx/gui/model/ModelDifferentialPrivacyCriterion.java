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

import org.deidentifier.arx.DataGeneralizationScheme;
import org.deidentifier.arx.criteria.EDDifferentialPrivacy;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;

/**
 * This class implements a model for the (e,d)-DP criterion.
 *
 * @author Fabian Prasser
 */
public class ModelDifferentialPrivacyCriterion extends ModelImplicitCriterion{

    /** SVUID */
    private static final long        serialVersionUID              = 1803345324372136700L;
    
    /** Delta */
    private double                   delta                         = 0.000001d;

    /** Epsilon */
    private double                   epsilon                       = 2d;

    /** Fraction of epsilon to use for automatic generalization */
    private Double                   epsilonGeneralizationFraction = 0.1d;

    /** Generalization scheme to be used or null in the case of data-dependent differential privacy */
    private DataGeneralizationScheme generalization                = null;

    /**
     * Creates a new instance
     */
    public ModelDifferentialPrivacyCriterion() {
        // Empty by design
    }
    
    /**
     * Creates a new instance
     * @param epsilon
     * @param delta
     */
	public ModelDifferentialPrivacyCriterion(double epsilon, double delta) {
        this.epsilon = epsilon;
        this.delta = delta;
    }

    @Override
    public ModelDifferentialPrivacyCriterion clone() {
        ModelDifferentialPrivacyCriterion result = new ModelDifferentialPrivacyCriterion();
        result.epsilon = this.epsilon;
        result.delta = this.delta;
        result.epsilonGeneralizationFraction = this.epsilonGeneralizationFraction;
        result.generalization = (this.generalization == null) ? null : this.generalization.clone();
        result.setEnabled(this.isEnabled());
        return result;
    }

	@Override
	public PrivacyCriterion getCriterion(Model model) {
		return new EDDifferentialPrivacy(epsilon, delta, generalization);
	}
	
    /**
     * Getter
     * @return
     */
    public double getDelta() {
        return delta;
    }

    /**
     * Getter
     * @return
     */
    public double getEpsilon() {
        return epsilon;
    }

    /**
     * Getter
     * @return
     */
    public DataGeneralizationScheme getGeneralization() {
        return generalization;
    }

    @Override
    public String getLabel() {
        return "(" + '\u03B5' + ", " + '\u03B4' + ")" + Resources.getMessage("ModelCriterion.3"); //$NON-NLS-1$
    }

    /**
     * Getter
     * @return
     */
    public double getEpsilonGeneralizationFraction() {
        if (epsilonGeneralizationFraction == null) {
            epsilonGeneralizationFraction = 0.1d;
        }
        return epsilonGeneralizationFraction;
    }

    @Override
    public void parse(ModelCriterion criterion, boolean _default) {
        if (!(criterion instanceof ModelDifferentialPrivacyCriterion)) {
            return;
        }
        ModelDifferentialPrivacyCriterion other = (ModelDifferentialPrivacyCriterion)criterion;
        this.epsilon = other.epsilon;
        this.delta = other.delta;
        this.epsilonGeneralizationFraction = other.epsilonGeneralizationFraction;
        if (!_default) {
            this.generalization = (other.generalization == null) ? null : other.generalization.clone();
        }
        if (!_default) {
            this.setEnabled(other.isEnabled());
        }
    }

    /**
     * Setter
     * @param delta
     */
    public void setDelta(double delta) {
        this.delta = delta;
    }

    /**
     * Setter
     * @param epsilon
     */
    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }

    /**
     * Setter
     * @param generalization
     */
    public void setGeneralization(DataGeneralizationScheme generalization) {
        this.generalization = generalization;
    }

    /**
     * Setter
     * @param epsilonGeneralizationFraction
     */
    public void setEpsilonGeneralizationFraction(double epsilonGeneralizationFraction) {
        this.epsilonGeneralizationFraction = epsilonGeneralizationFraction;
    }

    @Override
    public String toString() {
        return "(" + SWTUtil.getPrettyString(epsilon) + ", " + SWTUtil.getPrettyString(delta) + ")" + Resources.getMessage("ModelCriterion.2"); //$NON-NLS-1$
    }
}
