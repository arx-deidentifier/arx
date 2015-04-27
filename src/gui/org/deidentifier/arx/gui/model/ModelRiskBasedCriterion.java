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

import org.deidentifier.arx.criteria.AverageReidentificationRisk;
import org.deidentifier.arx.criteria.PopulationUniqueness;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.criteria.SampleUniqueness;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.risk.RiskModelPopulationUniqueness.PopulationUniquenessModel;

/**
 * This class implements a model for risk-based criteria
 *
 * @author Fabian Prasser
 */
public class ModelRiskBasedCriterion extends ModelImplicitCriterion{

    /** SVUID*/
    private static final long serialVersionUID = -3653781193588952725L;

    /** Threshold */
	private double threshold = 0.01d;

    /** Variant. */
    public static final int   VARIANT_AVERAGE_RISK              = 0;

    /** Variant. */
    public static final int   VARIANT_SAMPLE_UNIQUES            = 1;

    /** Variant. */
    public static final int   VARIANT_POPULATION_UNIQUES_DANKAR = 2;

    /** Variant. */
    public static final int   VARIANT_POPULATION_UNIQUES_PITMAN = 3;

    /** Variant. */
    public static final int   VARIANT_POPULATION_UNIQUES_ZAYATZ = 4;

    /** Variant. */
    public static final int   VARIANT_POPULATION_UNIQUES_SNB    = 5;

    /** The variant to use. */
    private int               variant                           = 0;

    /**
     * Creates a new instance
     * @param variant
     */
    public ModelRiskBasedCriterion(int variant) {
        this.variant = variant;
    }
    
	@Override
    public ModelRiskBasedCriterion clone() {
        ModelRiskBasedCriterion result = new ModelRiskBasedCriterion(this.variant);
        result.threshold = this.threshold;
        result.variant = this.variant;
        result.setEnabled(this.isEnabled());
        return result;
    }
	
	@Override
	public PrivacyCriterion getCriterion(Model model) {
        switch (variant) {
        case VARIANT_AVERAGE_RISK:
            return new AverageReidentificationRisk(threshold);
        case VARIANT_POPULATION_UNIQUES_DANKAR:
            return getPopulationBasedCriterion(PopulationUniquenessModel.DANKAR, model);
        case VARIANT_POPULATION_UNIQUES_PITMAN:
            return getPopulationBasedCriterion(PopulationUniquenessModel.PITMAN, model);
        case VARIANT_POPULATION_UNIQUES_SNB:
            return getPopulationBasedCriterion(PopulationUniquenessModel.SNB, model);
        case VARIANT_POPULATION_UNIQUES_ZAYATZ:
            return getPopulationBasedCriterion(PopulationUniquenessModel.ZAYATZ, model);
        case VARIANT_SAMPLE_UNIQUES:
            return new SampleUniqueness(threshold);
        default:
            throw new RuntimeException(Resources.getMessage("Model.0b")); //$NON-NLS-1$
        }
	}

	@Override
    public String getLabel() {
        switch (variant) {
        case VARIANT_AVERAGE_RISK:
            return Resources.getMessage("Model.1a"); //$NON-NLS-1$
        case VARIANT_POPULATION_UNIQUES_DANKAR:
            return Resources.getMessage("Model.2a"); //$NON-NLS-1$
        case VARIANT_POPULATION_UNIQUES_PITMAN:
            return Resources.getMessage("Model.3a"); //$NON-NLS-1$
        case VARIANT_POPULATION_UNIQUES_SNB:
            return Resources.getMessage("Model.4a"); //$NON-NLS-1$
        case VARIANT_POPULATION_UNIQUES_ZAYATZ:
            return Resources.getMessage("Model.5a"); //$NON-NLS-1$
        case VARIANT_SAMPLE_UNIQUES:
            return Resources.getMessage("Model.6a"); //$NON-NLS-1$
        default:
            throw new RuntimeException(Resources.getMessage("Model.7a")); //$NON-NLS-1$
        }
    }
	
	/**
     * Returns the threshold.
     *
     * @return
     */
	public double getThreshold() {
		return threshold;
	}

    @Override
    public void parse(ModelCriterion criterion) {
        if (!(criterion instanceof ModelRiskBasedCriterion)) {
            return;
        }
        ModelRiskBasedCriterion other = (ModelRiskBasedCriterion)criterion;
        this.threshold = other.threshold;
        this.variant = other.variant;
        this.setEnabled(other.isEnabled());
    }

    /**
     * Sets the threshold.
     *
     * @param k
     */
	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

    @Override
    public String toString() {
        switch (variant) {
        case VARIANT_AVERAGE_RISK:
            return Resources.getMessage("Model.8a") + threshold + Resources.getMessage("Model.9a"); //$NON-NLS-1$ //$NON-NLS-2$
        case VARIANT_POPULATION_UNIQUES_DANKAR:
            return Resources.getMessage("Model.10a") + threshold + Resources.getMessage("Model.11"); //$NON-NLS-1$ //$NON-NLS-2$
        case VARIANT_POPULATION_UNIQUES_PITMAN:
            return Resources.getMessage("Model.12") + threshold + Resources.getMessage("Model.13"); //$NON-NLS-1$ //$NON-NLS-2$
        case VARIANT_POPULATION_UNIQUES_SNB:
            return Resources.getMessage("Model.14") + threshold + Resources.getMessage("Model.15"); //$NON-NLS-1$ //$NON-NLS-2$
        case VARIANT_POPULATION_UNIQUES_ZAYATZ:
            return Resources.getMessage("Model.16") + threshold + Resources.getMessage("Model.17"); //$NON-NLS-1$ //$NON-NLS-2$
        case VARIANT_SAMPLE_UNIQUES:
            return Resources.getMessage("Model.18") + threshold + Resources.getMessage("Model.19"); //$NON-NLS-1$ //$NON-NLS-2$
        default:
            throw new RuntimeException(Resources.getMessage("Model.20")); //$NON-NLS-1$
        }
    }
    
    /**
	 * Returns a population-based criterion for the given models
	 * @param statisticalModel
	 * @param model
	 * @return
	 */
	private PrivacyCriterion getPopulationBasedCriterion(PopulationUniquenessModel statisticalModel, Model model) {
	    ModelRisk riskModel = model.getRiskModel();
	    return new PopulationUniqueness(threshold, 
	                                                   statisticalModel, 
	                                                   riskModel.getPopulationModel().clone(),
	                                                   riskModel.getSolverConfiguration());
	}
}
