package org.deidentifier.arx.gui.view.impl.wizard.sharingwizard.checklist;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
public class WeightConfiguration {
	/**
	 * the current configurations filename
	 */
	private String filename;
	
	/**
	 * the current configurations name (last path component)
	 */
	private String name;
	
	/**
	 * a map, mapping the item identifiers to a weight
	 */
	private Map<String,Double> weights;
	
	/**
	 * path to the last used configuration file
	 */
	private static final String lastUsedPath = "config/weights/last_used.txt";
	
	/**
	 * create a new configuration, preferably the previously used one
	 */
	public WeightConfiguration() {
		this(lastUsedPath);
		this.name = Resources.getMessage("RiskWizard.14"); 
	}
	
	/**
	 * create a new configuration from a file
	 * @param filename the file's filename
	 */
	public WeightConfiguration(String filename) {
		this.filename = filename;
		weights = new HashMap<String,Double>();
		loadProperties();
		
		updateName();
		
	}
	
	/**
	 * update the name, based on the filename's path filename
	 */
	private void updateName() {
		Path p = Paths.get(filename);
		this.name = p.getFileName().toString();
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
	 */
	private void loadProperties() {
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
	
	public String getName() {
		return this.name;
	}
	
	/**
	 * tries to save the weight configuration to the lastUsedPath
	 */
	public void saveLastUsed() {
		save(lastUsedPath);
	}
	
	/**
	 * save the configuration to the current filename
	 */
	public void save() {
		save(this.filename);
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
		
		this.filename = filename;
		updateName();
	}
	
}
