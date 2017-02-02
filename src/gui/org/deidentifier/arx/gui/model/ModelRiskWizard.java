package org.deidentifier.arx.gui.model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.deidentifier.arx.gui.resources.Resources;

import java.util.Properties;

/**
 * the weight configuration used when evaluating the checklist
 *
 */
public class ModelRiskWizard implements Serializable {
	/**
	 * the current serialization version
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * a map, mapping the item identifiers to a weight
	 */
	private Map<String,Double> weights;
	
	/**
	 * create a new configuration, preferably the previously used one
	 */
	public ModelRiskWizard() {
		weights = new HashMap<String,Double>();
	}
	
	/**
	 * create a new configuration from a file
	 * @param filename the file's filename
	 */
	public ModelRiskWizard(String filename) {
		this();
		loadProperties(filename);
	}
	
	/**
	 * get the weight for an item's identifier
	 * @param identifier the identifier
	 * @return the weight
	 */
	public double weightForIdentifier(String identifier) {
		if(weights.containsKey(identifier) == false) {
			return 1.0;
		}
		return weights.get(identifier);
	}
	
	/**
	 * sets the weight for an item's identifier 
	 * @param identifier the item identifier to change
	 * @param weight the weight to set
	 */
	public void setWeightForIdentifier(String identifier, double weight) {
		weights.put(identifier, weight);
	}
	
	/**
	 * try to load the weights from the specified filename
	 * @param filename the filename
	 */
	private void loadProperties(String filename) {
		Properties props = new Properties();
		try {
			FileInputStream in = new FileInputStream(filename);
			props.load(in);
			
			for(Entry<Object, Object> entry : props.entrySet()) {
				String key = (String) entry.getKey();
				String value = (String) entry.getValue();
				
				double weight = Double.parseDouble(value);
				weights.put(key, weight);
			}
			in.close();
		} catch (FileNotFoundException e) {
			//System.err.println("Couldn't open file: "+filename);
			return;
		} catch (IOException e) {
			//System.err.println("Couldn't parse file: "+filename);
			return;
		}
		
	}
	
	/**
	 * save the configuration to a new filename
	 * @param filename the filename to save to
	 */
	public void save(String filename) {
		if(filename == null) {
			//System.out.println("No file specified to save to");
			return;
		}
		
		Properties props = new Properties();		
		for(Entry<String, Double> entry: weights.entrySet()) {
			props.setProperty(entry.getKey(), Double.toString(entry.getValue()));
		}
		
		FileOutputStream out;
		try {
			out = new FileOutputStream(filename);
			props.store(out, null);
			out.close();
		} catch (FileNotFoundException e) {
			//System.err.println("Couldn't open file: "+filename);
			return;
		} catch (IOException e) {
			//System.err.println("Couldn't store file: "+filename);
			return;
		}
	}
	
}
