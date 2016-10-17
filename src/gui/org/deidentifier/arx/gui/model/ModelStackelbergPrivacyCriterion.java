/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2016 Fabian Prasser, Florian Kohlmayer and contributors
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

import org.deidentifier.arx.ARXStackelbergConfiguration;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.criteria.StackelbergPrivacyModel;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;

/**
 * This class provides a model for the game-theoretic privacy model
 * 
 * @author James Gaupp
 * @author Fabian Prasser
 */
public class ModelStackelbergPrivacyCriterion extends ModelImplicitCriterion {
    
    /**
     * The attacker model used by the privacy model
     * @author Fabian Prasser
     *
     */
    public static enum AttackerModel {
        PROSECUTOR,
        JOURNALIST
    }

    /** SVUID */
    private static final long serialVersionUID = -4305859036159393453L;

    /** Benefit */
    private double            publisherBenefit = 0d;
    /** Loss */
    private double            publisherLoss    = 0d;
    /** Gain */
    private double            adversaryGain    = 0d;
    /** Cost */
    private double            adversaryCost    = 0d;
    /** Prosecutor model */
    private AttackerModel     attackerModel    = AttackerModel.PROSECUTOR;

    /**
     * Creates a new instance
     */
    public ModelStackelbergPrivacyCriterion() {
    	// Empty by design
    }

    /**
     * Creates a new instance
     * @param publisherBenefit
     * @param publisherLoss
     * @param adversaryGain
     * @param adversaryCost
     * @param attackerModel
     */
    public ModelStackelbergPrivacyCriterion(double publisherBenefit,
                                            double publisherLoss,
                                            double adversaryGain,
                                            double adversaryCost,
                                            AttackerModel attackerModel) {
    	this.publisherBenefit = publisherBenefit;
    	this.publisherLoss = publisherLoss;
    	this.adversaryGain = adversaryGain;
    	this.adversaryCost = adversaryCost;
    }

	@Override
	public ModelCriterion clone() {
		ModelStackelbergPrivacyCriterion  result = new ModelStackelbergPrivacyCriterion();
		result.setAdversaryCost(adversaryCost);
		result.setAdversaryGain(adversaryGain);
		result.setPublisherBenefit(publisherBenefit);
		result.setPublisherLoss(publisherLoss);
		result.setAttackerModel(attackerModel);
		return result;
	}
	
    /**
     * Returns the adversary cost
     * @return
     */
    public double getAdversaryCost() {
		return adversaryCost;
	}

    /**
     * Returns the adversary gain
     * @return
     */
    public double getAdversaryGain() {
		return adversaryGain;
	}

    /**
     * @return the attackerModel
     */
    public AttackerModel getAttackerModel() {
        return attackerModel;
    }
    
    @Override
	public PrivacyCriterion getCriterion(Model model) {

	    // Create config
        ARXStackelbergConfiguration config = ARXStackelbergConfiguration.create();
        
        // Configure benefits and costs
        config.setAdversaryCost(adversaryCost)
              .setAdversaryGain(adversaryGain)
              .setPublisherLoss(publisherLoss)
              .setPublisherBenefit(publisherBenefit);
        
        // Configure attacker model
        if (this.attackerModel == AttackerModel.PROSECUTOR) {
            config.setProsecutorAttackerModel();
        } else {
            config.setJournalistAttackerModel(DataSubset.create(model.getInputConfig().getInput(),
                                                                model.getInputConfig()
                                                                     .getResearchSubset()));
        }
        
        // Return
        return new StackelbergPrivacyModel(config);
	}

	@Override
	public String getLabel() {
		return Resources.getMessage("ModelCriterion.4");
	}

	/**
	 * Returns the publisher benefit
	 * @return
	 */
    public double getPublisherBenefit() {
		return publisherBenefit;
	}
	
	/**
     * Returns the publisher loss
     * @return
     */
    public double getPublisherLoss() {
		return publisherLoss;
	}
    
    @Override
	public void parse(ModelCriterion criterion, boolean _default) {
        if (!(criterion instanceof ModelStackelbergPrivacyCriterion)) {
            return;
        }
        ModelStackelbergPrivacyCriterion other = (ModelStackelbergPrivacyCriterion)criterion;
        this.adversaryCost = other.adversaryCost;
        this.adversaryGain = other.adversaryGain;
        this.publisherBenefit = other.publisherBenefit;
        this.publisherLoss = other.publisherLoss;
        this.attackerModel = other.attackerModel;
        if (!_default) {
            this.setEnabled(other.isEnabled());
        }

	}
    
	/**
	 * Sets the adversary cost
	 * @param adversaryCost
	 */
	public void setAdversaryCost(double adversaryCost) {
		this.adversaryCost = adversaryCost;
	}
	
	/**
	 * Sets the adversary gain
	 * @param adversaryGain
	 */
	public void setAdversaryGain(double adversaryGain) {
		this.adversaryGain = adversaryGain;
	}
	
	/**
     * @param attackerModel the attackerModel to set
     */
    public void setAttackerModel(AttackerModel attackerModel) {
        this.attackerModel = attackerModel;
    }

	/**
	 * Sets the publisher benefit
	 * @param publisherBenefit
	 */
	public void setPublisherBenefit(double publisherBenefit) {
		this.publisherBenefit = publisherBenefit;
	}
	
	/**
	 * Sets the publisher loss
	 * @param publisherLoss
	 */
	public void setPublisherLoss(double publisherLoss) {
		this.publisherLoss = publisherLoss;
	}
	
	@Override
	public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("("); //$NON-NLS-1$
        builder.append(SWTUtil.getPrettyString(this.publisherBenefit)).append(", "); //$NON-NLS-1$
        builder.append(SWTUtil.getPrettyString(this.publisherLoss)).append(", "); //$NON-NLS-1$
        builder.append(SWTUtil.getPrettyString(this.adversaryGain)).append(", "); //$NON-NLS-1$
        builder.append(SWTUtil.getPrettyString(this.adversaryCost)).append(")-"); //$NON-NLS-1$
        builder.append(Resources.getMessage("ModelCriterion.4")); //$NON-NLS-1$
        builder.append(" (");
        builder.append(this.attackerModel == AttackerModel.PROSECUTOR ? Resources.getMessage("ModelCriterion.5") //$NON-NLS-1$
                : Resources.getMessage("ModelCriterion.6")); //$NON-NLS-1$
        builder.append(")");
        return builder.toString();
	}
}
