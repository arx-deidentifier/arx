package org.deidentifier.arx.gui.model;

import org.deidentifier.arx.ARXStackelbergConfiguration;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.criteria.StackelbergPrivacyModel;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;

public class ModelStackelbergPrivacyCriterion extends ModelImplicitCriterion {

	private static final long serialVersionUID = -3632131196984677525L;
	
    private double             publisherBenefit  = 0d;
    private double             publisherLoss     = 0d;
    private double             adversaryGain     = 0d;
    private double             adversaryCost     = 0d;

	/** Parameter for the journalist model */
    private DataSubset         subset            = null;
    
    public ModelStackelbergPrivacyCriterion() {
    	// Empty by design
    }

	public ModelStackelbergPrivacyCriterion(double publisherBenefit, double publisherLoss, double adversaryGain, double adversaryCost, DataSubset subset) {
    	this.publisherBenefit = publisherBenefit;
    	this.publisherLoss = publisherLoss;
    	this.adversaryGain = adversaryGain;
    	this.adversaryCost = adversaryCost;
    	this.subset = subset;
    }

	@Override
	public ModelCriterion clone() {
		ModelStackelbergPrivacyCriterion  result = new ModelStackelbergPrivacyCriterion();
		result.setAdversaryCost(adversaryCost);
		result.setAdversaryGain(adversaryGain);
		result.setPublisherBenefit(publisherBenefit);
		result.setPublisherLoss(publisherLoss);
		result.setSubset(subset.clone());
		
		return result;
	}
	
    public double getAdversaryCost() {
		return adversaryCost;
	}
    
    public double getAdversaryGain() {
		return adversaryGain;
	}

	@Override
	public PrivacyCriterion getCriterion(Model model) {
		
		ARXStackelbergConfiguration config = ARXStackelbergConfiguration.create();
		config.setAdversaryCost(adversaryCost).setAdversaryGain(adversaryGain).setPublisherLoss(publisherLoss).setPublisherBenefit(publisherBenefit).setJournalistAttackerModel(subset);
        
		return new StackelbergPrivacyModel(config);
	}

	@Override
	public String getLabel() {
		return Resources.getMessage("ModelCriterion.4");
	}
	
    public double getPublisherBenefit() {
		return publisherBenefit;
	}
    
    public double getPublisherLoss() {
		return publisherLoss;
	}
    
    public DataSubset getSubset() {
		return subset;
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
        this.subset = other.subset.clone();
        this.setEnabled(other.isEnabled());

	}
	
	public void setAdversaryCost(double adversaryCost) {
		this.adversaryCost = adversaryCost;
	}
	
	public void setAdversaryGain(double adversaryGain) {
		this.adversaryGain = adversaryGain;
	}
	
	public void setPublisherBenefit(double publisherBenefit) {
		this.publisherBenefit = publisherBenefit;
	}
	
	public void setPublisherLoss(double publisherLoss) {
		this.publisherLoss = publisherLoss;
	}
	
	public void setSubset(DataSubset subset) {
		this.subset = subset;
	}

	@Override
	public String toString() {
		String params = "(" + SWTUtil.getPrettyString(this.publisherBenefit) + ", " + SWTUtil.getPrettyString(this.publisherLoss)
		+ ", " + SWTUtil.getPrettyString(this.adversaryGain) + ", " + SWTUtil.getPrettyString(this.adversaryCost) + ") - ";
		return params + Resources.getMessage("ModelCriterion.4");
	}

}
