package org.deidentifier.arx.gui.view.impl.wizard.sharingwizard.checklist;

import org.deidentifier.arx.gui.model.ModelRiskWizard;

/**
 * this is the base class for the Question as well as the Section.
 *
 */
public abstract class Item {
	/**
	 * the current weight configuration
	 */
	private ModelRiskWizard weightConfiguration;
	
	/**
	 * the identifier (used for the weight configuration)
	 */
	protected String identifier;
	
	/**
	 * the item's title
	 */
	protected String title;
	
	/**
	 * create a new item from a line
	 * @param line the line to parse
	 */
	public Item(String line) {
		String components[] = line.split(":",2);
		if(components.length != 2) {
			//System.err.println("Couldn't parse item! Original line: "+line);
			return;
		}
		this.identifier = components[0].trim();
		this.title = components[1].trim();
	}
	
	protected ModelRiskWizard getWeightConfiguration() {
		return this.weightConfiguration;
	}
	
	/**
	 * set weight configuration and update weights
	 * @param weightConfiguration
	 */
	public void setWeightConfiguration(ModelRiskWizard weightConfiguration) {
		this.weightConfiguration = weightConfiguration;
		updateWeights();
	}
	
	/**
	 * update the weights, used by subclasses to calculate the new score accordingly
	 */
	public void updateWeights() {
		
	}
	
	/**
	 * gets the current weight for this item according to the current weight configuration
	 * @return
	 */
	public double getWeight() {
		if(weightConfiguration == null) {
			return 1.0;
		}
		return weightConfiguration.weightForIdentifier(this.getIdentifier());
	}
	
	/**
	 * updates the weight configurations weight for this item
	 * @param weight the new weight for this item
	 */
	public void setWeight(double weight) {
		weightConfiguration.setWeightForIdentifier(this.getIdentifier(), weight);
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
}
