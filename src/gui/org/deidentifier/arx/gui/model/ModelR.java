package org.deidentifier.arx.gui.model;

import java.io.Serializable;

import org.deidentifier.arx.r.OS;

public class ModelR implements Serializable {
	
	//TODO: bufferSize change has to have an effect, get default bufferSize from ViewStatisticsR, make it non-final there.
	
	private static final long serialVersionUID = -4456951642963734727L;
	private String path;
	private int bufferSize = 10000;
	private int eventDelay = 10;
	
	public ModelR() {
		String p = OS.getR();
		if (p == null) {
			this.path = "";
		} else {
			this.path = p;
		}
		//this.path = "";//Does not work?! OS.getR();
	}

	public int getBufferSize() {
		return this.bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}
	
	

	/**
     * @return the eventDelay
     */
    public int getTicksPerSecond() {
        return eventDelay;
    }

    /**
     * @param eventDelay the eventDelay to set
     */
    public void setTicksPerSecond(int eventDelay) {
        this.eventDelay = eventDelay;
    }

    public String getPath() {
		return this.path;
	}

	public void setPath(String path) {
		this.path = path;
	}

}
