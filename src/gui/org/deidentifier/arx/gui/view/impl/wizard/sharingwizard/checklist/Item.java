package org.deidentifier.arx.gui.view.impl.wizard.sharingwizard.checklist;

/**
 * this is the base class for the Question as well as the Section.
 *
 */
public abstract class Item {
	private WeightConfiguration weightConfiguration;
	protected String identifier;
	protected String title;
		
	public Item(String line) {
		String components[] = line.split(":",2);
		if(components.length != 2) {
			System.err.println("Couldn't parse item! Original line: "+line);
			return;
		}
		this.identifier = components[0].trim();
		this.title = components[1].trim();
	}
	
	protected WeightConfiguration getWeightConfiguration() {
		return this.weightConfiguration;
	}
	
	public void setWeightConfiguration(WeightConfiguration weightConfiguration) {
		this.weightConfiguration = weightConfiguration;
		updateWeights();
	}
	
	public void updateWeights() {
		
	}
	
	public double getWeight() {
		if(weightConfiguration == null) {
			return 1.0;
		}
		return weightConfiguration.weightForIdentifier(this.getIdentifier());
	}
	
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
