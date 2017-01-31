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
import java.util.Properties;

public class WeightConfiguration {
	private String filename;
	private String name;
	private Map<String,Double> weights;
	
	private static final String lastUsedPath = "config/weights/last_used.txt";
	
	public WeightConfiguration() {
		this(lastUsedPath);
		this.name = "Last used"; 
	}
	
	public WeightConfiguration(String filename) {
		this.filename = filename;
		weights = new HashMap<String,Double>();
		loadProperties();
		
		updateName();
		
	}
	
	private void updateName() {
		Path p = Paths.get(filename);
		this.name = p.getFileName().toString();
	}
	
	public double weightForIdentifier(String identifier) {
		if(weights.containsKey(identifier) == false) {
			return 1.0;
		}
		return weights.get(identifier);
	}
	
	public void setWeightForIdentifier(String identifier, double weight) {
		weights.put(identifier, weight);
	}
	
	
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
			System.err.println("Couldn't open file: "+filename);
			return;
		} catch (IOException e) {
			System.err.println("Couldn't parse file: "+filename);
			return;
		}
		
	}
	
	public String getName() {
		return this.name;
	}
	
	
	public void saveLastUsed() {
		save(lastUsedPath);
	}
	
	
	public void save() {
		save(this.filename);
	}
	
	
	public void save(String filename) {
		if(filename == null) {
			System.out.println("No file specified to save to");
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
			System.err.println("Couldn't open file: "+filename);
			return;
		} catch (IOException e) {
			System.err.println("Couldn't store file: "+filename);
			return;
		}
		
		this.filename = filename;
		updateName();
	}
	
}
