package org.deidentifier.arx.gui.model;

import java.io.Serializable;

import org.deidentifier.arx.criteria.PrivacyCriterion;

public abstract class ModelCriterion implements Serializable {

	private static final long serialVersionUID = 8097643412538848066L;
	private boolean enabled = false;
	private boolean active = false;

	public boolean isActive() {
		return active;
	}
	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}

	public abstract PrivacyCriterion getCriterion(Model model);
    public abstract String toString();
}
