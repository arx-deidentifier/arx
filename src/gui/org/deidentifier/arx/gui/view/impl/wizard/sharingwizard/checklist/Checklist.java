package org.deidentifier.arx.gui.view.impl.wizard.sharingwizard.checklist;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * the Checklist holds the sections and calculates the overall score
 *
 */
public class Checklist {
	private ArrayList<Section> sections;
	private double maximumWeight = 0.0;
	protected WeightConfiguration weightConfiguration;

	public Checklist() {
		this("config/khaled_el_emam.txt");
	}
	
	public Checklist(String filename) {
		super();
		weightConfiguration = new WeightConfiguration();
		try {
			// initialize reader
			BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
			loadChecklist(bufferedReader);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public Checklist(InputStream stream) {
		super();
		weightConfiguration = new WeightConfiguration();
		// initialize reader
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
		loadChecklist(bufferedReader);
	}
	
	private void loadChecklist(BufferedReader bufferedReader) {
		// create new and empty sections array
		sections = new ArrayList<Section>();
		
		try {
		    
		    // hold a reference to the current section
		    Section currentSection = null;
		    
		    // read first line, then iterate over the lines
		    String line = bufferedReader.readLine();
		    while (line != null) {
		    	line = line.trim(); // get rid of leading/trailing whitespaces
		    	if(line.length() == 0) {
		    		// do nothing
		    	} else if(line.startsWith("#") == true) {
		    		// current line is a section
		    		line = line.substring(1);
		    		currentSection = Section.sectionFromLine(weightConfiguration, line);
		    		sections.add(currentSection);
		    		maximumWeight += Math.abs(currentSection.getWeight());
		    	} else {
		    		// current line is an item
		    		if(currentSection == null) {
		    			System.out.println("Invalid syntax. Checklist item before section (section-lines start with #)");
		    		}
		    		
		    		// parse and add item
		    		Question newItem = Question.itemFromLine(currentSection, line);
		    		currentSection.addItem(newItem);
		    	}
		    	
		    	// read next line
		        line = bufferedReader.readLine();
		    }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public Section[] getSections() {
		return (sections.toArray(new Section[sections.size()]));
	}
	
	public double getMaximumWeight() {
		return maximumWeight;
	}
	
	public double getScore() {
		if(maximumWeight == 0.0) {
			System.out.println("This Checklist has a maximum weight of 0.0, it can't calculate a score: "+this);
			return 0.0;
		}
		double result = 0.0;
		for(Section s : sections) {
			result += s.getWeight() * s.getScore();
		}
		result /= maximumWeight;
		return result;
	}

	@Override
	public String toString() {
		return "Checklist [score="+this.getScore()+", sections=" + sections + "\n]";
	}
	
	public void saveWeightDefaults() {
		this.weightConfiguration.saveLastUsed();
	}
	
	public WeightConfiguration getWeightConfiguration() {
		return this.weightConfiguration;
	}
	
	public void setWeightConfiguration(WeightConfiguration weightConfiguration) {
		if(this.weightConfiguration == weightConfiguration) {
			return;
		}
		
		this.weightConfiguration = weightConfiguration;
		this.maximumWeight = 0.0;
		for(Section s : this.sections) {
			s.setWeightConfiguration(weightConfiguration);
			maximumWeight += Math.abs(s.getWeight());
		}
		
	}
	
}
