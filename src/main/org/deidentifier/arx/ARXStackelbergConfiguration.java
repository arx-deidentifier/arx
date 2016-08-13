package org.deidentifier.arx;



/**
 * Basic configuration for the Stackelberg game
 * @author Fabian Prasser
 *
 */
public class ARXStackelbergConfiguration {
    
    /**
     * Creates a new instance
     * @return
     */
    public static ARXStackelbergConfiguration create() {
        return new ARXStackelbergConfiguration();
    }

    /** Basic parameters */
    private double             publisherBenefit  = 1200d;
    /** Basic parameters */
    private double             publisherLoss     = 300d;
    /** Basic parameters */
    private double             adversaryGain     = 300d;
    /** Basic parameters */
    private double             adversaryCost     = 4d;
    /** Basic parameters */
    private boolean            journalist        = false;

    /** Parameter for the journalist model */
    private DataSubset         subset            = null;
    /** Parameter for the journalist model */
    private double             significanceLevel = 0d;
    /** Parameter for the journalist model */
    private ARXPopulationModel population        = null;
    
    /**
     * Hide constructor
     */
    private ARXStackelbergConfiguration() {
        // Empty by design
    }

    @Override
    public ARXStackelbergConfiguration clone() {
        ARXStackelbergConfiguration result = new ARXStackelbergConfiguration();
        result.publisherBenefit = this.publisherBenefit;
        result.publisherLoss = this.publisherLoss;
        result.adversaryGain = this.adversaryGain;
        result.adversaryCost = this.adversaryCost;
        result.journalist = this.journalist;
        result.subset = this.subset.clone();
        result.significanceLevel = this.significanceLevel;
        result.population = this.population.clone();
        return result;
    }

    /**
     * @return the adversaryCost
     */
    public double getAdversaryCost() {
        return adversaryCost;
    }

    /**
     * @return the adversaryGain
     */
    public double getAdversaryGain() {
        return adversaryGain;
    }
    
    /**
     * @return the subset
     */
    public DataSubset getDataSubset() {
        return subset;
    }
    /**
     * @return the population
     */
    public ARXPopulationModel getPopulationModel() {
        return population;
    }

    /**
     * @return the publisherBenefit
     */
    public double getPublisherBenefit() {
        return publisherBenefit;
    }

    /**
     * @return the publisherLoss
     */
    public double getPublisherLoss() {
        return publisherLoss;
    }

    /**
     * @return the significanceLevel
     */
    public double getSignificanceLevel() {
        return significanceLevel;
    }

    /**
     * @return whether we assume the journalist attacker model
     */
    public boolean isJournalistAttackerModel() {
        return journalist;
    }

    /**
     * @return whether we assume the journalist attacker model
     */
    public boolean isProsecutorAttackerModel() {
        return !journalist;
    }

    /**
     * Returns whether explicit sub-/supersets are available or whether we need to use population estimates
     * @return
     */
    public boolean isSubsetAvailable() {
        return this.subset != null;
    }
    /**
     * @param adversaryCost the adversaryCost to set
     */
    public ARXStackelbergConfiguration setAdversaryCost(double adversaryCost) {
        this.adversaryCost = adversaryCost;
        return this;
    }

    /**
     * @param adversaryGain the adversaryGain to set
     */
    public ARXStackelbergConfiguration setAdversaryGain(double adversaryGain) {
        this.adversaryGain = adversaryGain;
        return this;
    }

    /**
     * Set the journalist attacker model using population estimates using the Zero-Truncated Poisson distribution
     * @param population
     * @param significanceLevel
     * @return
     */
    public ARXStackelbergConfiguration setJournalistAttackerModel(ARXPopulationModel population, double significanceLevel){
        this.journalist = true;
        this.subset = null;
        this.significanceLevel = significanceLevel;
        this.population = population;
        return this;
    }

    /**
     * Set the journalist attacker model with an explicit sub-/superset
     * @param subset
     * @return
     */
    public ARXStackelbergConfiguration setJournalistAttackerModel(DataSubset subset){
        this.journalist = true;
        this.subset = subset;
        this.significanceLevel = 0d;
        this.population = null;
        return this;
    }

    /**
     * Set the prosecutor attacker model
     * @return
     */
    public ARXStackelbergConfiguration setProsecutorAttackerModel(){
        this.journalist = false;
        this.subset = null;
        this.significanceLevel = 0d;
        this.population = null;
        return this;
    }
    /**
     * @param publisherBenefit the publisherBenefit to set
     */
    public ARXStackelbergConfiguration setPublisherBenefit(double publisherBenefit) {
        this.publisherBenefit = publisherBenefit;
        return this;
    }
    
    /**
     * @param publisherLoss the publisherLoss to set
     */
    public ARXStackelbergConfiguration setPublisherLoss(double publisherLoss) {
        this.publisherLoss = publisherLoss;
        return this;
    }
    
    @Override
    public String toString() {
        
        StringBuilder builder = new StringBuilder();
        if (isProsecutorAttackerModel()) {
            builder.append("[prosecutor, ");   
        } else {
            builder.append("[journalist, ").append(isSubsetAvailable() ? "explicit, " : "estimated, ");
        }
        builder.append("benefit=").append(publisherBenefit).append(", loss=");
        builder.append(publisherLoss).append(", gain=").append(adversaryGain).append(", cost=").append(adversaryCost).append("]");
        return builder.toString();
    }
}
