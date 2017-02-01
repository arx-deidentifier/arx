package org.deidentifier.arx.gui.view.impl.wizard.sharingwizard.checklist;

import java.util.ArrayList;

/**
 * represents a section from the checklist
 *
 */
public class Section extends Item {
	/**
	 * the items this section contains
	 */
	private ArrayList<Question> items;
	
	/**
	 * the maximum weight possible
	 */
	private double maximumWeight = 0.0;
	
	/**
	 * create a new section from a line
	 * @param weightConfiguration the current weight configuration
	 * @param line the line to parse
	 */
	public Section(WeightConfiguration weightConfiguration, String line) {
		super(line);
		this.items = new ArrayList<Question>();
		setWeightConfiguration(weightConfiguration);
	}

	/**
	 * create a new section from a line
	 * @param weightConfiguration the current weight configuration
	 * @param line the line to parse
	 * @return the section item
	 */
	public static Section sectionFromLine(WeightConfiguration weightConfiguration, String line) {
		line = line.trim();
		Section section = new Section(weightConfiguration, line);
		return section;
	}
	
	/**
	 * get all questions in this section
	 * @return
	 */
	public Question[] getItems() {
		return items.toArray(new Question[items.size()]);
	}
	
	/**
	 * add a question to the section and updates the maximumWeight
	 * @param item the question to add
	 */
	public void addItem(Question item) {
		item.setWeightConfiguration(this.getWeightConfiguration());
		items.add(item);
		maximumWeight += Math.abs(item.getWeight());
	}
	
	/**
	 * updates the weights and calculates the maximum weight
	 */
	public void updateWeights() {
		super.updateWeights();
		
		maximumWeight = 0.0;
		for(Question item : items) {
			item.setWeightConfiguration(this.getWeightConfiguration());
			maximumWeight += Math.abs(item.getWeight());
		}
	}
	
	public double getMaximumWeight() {
		return maximumWeight;
	}
	
	/**
	 * calculates the score based on the section's question's scores
	 * @return
	 */
	public double getScore() {
		if(maximumWeight == 0.0) {
			//System.out.println("This Section has a maximum weight of 0.0, it can't calculate a score: "+this);
			return 0.0;
		}
		double result = 0.0;
		for(Question q : items) {
			result += q.getScore();
		}
		// if the weight is negative, 'flip' the answers rating (yes will be -1, no will be 1)
		if(this.getWeight() < 0.0) {
			result = (result * (-1.0));
		}
		result /= maximumWeight;
		return result;
	}
	
	@Override
	public String toString() {
		return "\n\tSection [id=" + this.getIdentifier() + ", title=" + this.getTitle() + ", weight=" + this.getWeight() + ", score="+ this.getScore() +", items=" + items + ", config="+this.getWeightConfiguration().getName()+"\n\t]";
	}
	
}
