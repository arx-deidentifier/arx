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
	/**
	 * the array containing the sections of the checklist
	 */
	private ArrayList<Section> sections;
	
	/**
	 * the maximum achievable weight
	 */
	private double maximumWeight = 0.0;
	
	/**
	 * the current weight configuration
	 */
	protected WeightConfiguration weightConfiguration;

	/**
	 * create a checklist from a file
	 * @param filename the filename
	 */
	public Checklist(String filename) {
		super();
		weightConfiguration = new WeightConfiguration();
		try {
			// initialize reader
			BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
			loadChecklist(bufferedReader);
		} catch(IOException e) {
			//e.printStackTrace();
		}
	}
	
	/**
	 * create a checklist from an input stream
	 * @param stream the input stream
	 */
	public Checklist(InputStream stream) {
		super();
		weightConfiguration = new WeightConfiguration();
		// initialize reader
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
		loadChecklist(bufferedReader);
	}
	
	/**
	 * loads the checklist using a buffered reader
	 * @param bufferedReader the buffered reader used to parse the file
	 */
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
		    			//System.out.println("Invalid syntax. Checklist item before section (section-lines start with #)");
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
					//e.printStackTrace();
				}
			}
		}
	}

	/**
	 * get all sections
	 * @return the sections
	 */
	public Section[] getSections() {
		return (sections.toArray(new Section[sections.size()]));
	}
	
	/**
	 * get the maximum weight
	 * @return the maximum weight
	 */
	public double getMaximumWeight() {
		return maximumWeight;
	}
	
	/**
	 * get the current score of the complete checklist
	 * @return the score
	 */
	public double getScore() {
		if(maximumWeight == 0.0) {
			//System.out.println("This Checklist has a maximum weight of 0.0, it can't calculate a score: "+this);
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
	
	/**
	 * saves the current weights to the last used filename
	 */
	public void saveWeightDefaults() {
		this.weightConfiguration.saveLastUsed();
	}
	
	/**
	 * gets the current weight configuration
	 * @return the weight configuration
	 */
	public WeightConfiguration getWeightConfiguration() {
		return this.weightConfiguration;
	}
	
	/**
	 * sets the weight configuration and updates the values
	 * @param weightConfiguration the weight configuration
	 */
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
