package org.deidentifier.arx.gui.view.impl.wizard.sharingwizard.checklist;

import java.util.ArrayList;

public class Section extends Item {
	private ArrayList<Question> items;
	private double maximumWeight = 0.0;
	
	
	public Section(WeightConfiguration weightConfiguration, String line) {
		super(line);
		this.items = new ArrayList<Question>();
		setWeightConfiguration(weightConfiguration);
	}
	
	public static Section sectionFromLine(WeightConfiguration weightConfiguration, String line) {
		line = line.trim();
		Section section = new Section(weightConfiguration, line);
		return section;
	}
	
	public Question[] getItems() {
		return items.toArray(new Question[items.size()]);
	}
	
	public void addItem(Question item) {
		item.setWeightConfiguration(this.getWeightConfiguration());
		items.add(item);
		maximumWeight += Math.abs(item.getWeight());
	}
	
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
	
	public double getScore() {
		if(maximumWeight == 0.0) {
			System.out.println("This Section has a maximum weight of 0.0, it can't calculate a score: "+this);
			return 0.0;
		}
		double result = 0.0;
		for(Question q : items) {
			result += q.getScore();
		}
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
