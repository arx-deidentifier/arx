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

import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.criteria.RiskBasedThresholdAverageRisk;
import org.deidentifier.arx.criteria.RiskBasedThresholdPopulationUniques;
import org.deidentifier.arx.criteria.RiskBasedThresholdSampleUniques;
import org.deidentifier.arx.risk.RiskModelPopulationBasedUniquenessRisk.StatisticalModel;

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
	public PrivacyCriterion getCriterion(Model model) {
        switch (variant) {
        case VARIANT_AVERAGE_RISK:
            return new RiskBasedThresholdAverageRisk(threshold);
        case VARIANT_POPULATION_UNIQUES_DANKAR:
            return getPopulationBasedCriterion(StatisticalModel.DANKAR, model);
        case VARIANT_POPULATION_UNIQUES_PITMAN:
            return getPopulationBasedCriterion(StatisticalModel.PITMAN, model);
        case VARIANT_POPULATION_UNIQUES_SNB:
            return getPopulationBasedCriterion(StatisticalModel.SNB, model);
        case VARIANT_POPULATION_UNIQUES_ZAYATZ:
            return getPopulationBasedCriterion(StatisticalModel.ZAYATZ, model);
        case VARIANT_SAMPLE_UNIQUES:
            return new RiskBasedThresholdSampleUniques(threshold);
        default:
            throw new RuntimeException("Internal error: invalid variant of risk-based criterion");
        }
	}
	
	/**
	 * Returns a population-based criterion for the given models
	 * @param statisticalModel
	 * @param model
	 * @return
	 */
	private PrivacyCriterion getPopulationBasedCriterion(StatisticalModel statisticalModel, Model model) {
	    ModelRisk riskModel = model.getRiskModel();
	    return new RiskBasedThresholdPopulationUniques(threshold, 
	                                                   statisticalModel, 
	                                                   riskModel.getPopulationModel().clone(),
	                                                   riskModel.getAccuracy(),
	                                                   riskModel.getMaxIterations());
	}

	/**
     * Returns the threshold.
     *
     * @return
     */
	public double getThreshold() {
		return threshold;
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
        // TODO: Move to messages.properties
        switch (variant) {
        case VARIANT_AVERAGE_RISK:
            return "(" + threshold + ")-Average-Reidentification-Risk";
        case VARIANT_POPULATION_UNIQUES_DANKAR:
            return "(" + threshold + ")-Population-Uniqueness (Dankar)";
        case VARIANT_POPULATION_UNIQUES_PITMAN:
            return "(" + threshold + ")-Population-Uniqueness (Pitman)";
        case VARIANT_POPULATION_UNIQUES_SNB:
            return "(" + threshold + ")-Population-Uniqueness (SNB)";
        case VARIANT_POPULATION_UNIQUES_ZAYATZ:
            return "(" + threshold + ")-Population-Uniqueness (Zayatz)";
        case VARIANT_SAMPLE_UNIQUES:
            return "(" + threshold + ")-Sample-Uniqueness";
        default:
            throw new RuntimeException("Internal error: invalid variant of risk-based criterion");
        }
    }

    @Override
    public String getLabel() {
        switch (variant) {
        case VARIANT_AVERAGE_RISK:
            return "Average-Reidentification-Risk";
        case VARIANT_POPULATION_UNIQUES_DANKAR:
            return "Population-Uniqueness (Dankar)";
        case VARIANT_POPULATION_UNIQUES_PITMAN:
            return "Population-Uniqueness (Pitman)";
        case VARIANT_POPULATION_UNIQUES_SNB:
            return "Population-Uniqueness (SNB)";
        case VARIANT_POPULATION_UNIQUES_ZAYATZ:
            return "Population-Uniqueness (Zayatz)";
        case VARIANT_SAMPLE_UNIQUES:
            return "Sample-Uniqueness";
        default:
            throw new RuntimeException("Internal error: invalid variant of risk-based criterion");
        }
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
    public void parse(ModelCriterion criterion) {
        if (!(criterion instanceof ModelRiskBasedCriterion)) {
            return;
        }
        ModelRiskBasedCriterion other = (ModelRiskBasedCriterion)criterion;
        this.threshold = other.threshold;
        this.variant = other.variant;
        this.setEnabled(other.isEnabled());
    }
}
