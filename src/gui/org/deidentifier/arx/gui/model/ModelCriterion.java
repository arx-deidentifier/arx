/*
 * ARX: Powerful Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.arx.gui.model;

import java.io.Serializable;

import org.deidentifier.arx.criteria.PrivacyCriterion;

/**
 * A base class for models for criteria.
 *
 * @author Fabian Prasser
 */
public abstract class ModelCriterion implements Serializable {

    /** SVUID. */
	private static final long serialVersionUID = 8097643412538848066L;
	
	/** Is this criterion enabled. */
	private boolean enabled = false;
	
	/** Is this criterion active. */
	private boolean active = false;

	/**
     * Implement this to return the criterion.
     *
     * @param model
     * @return
     */
	public abstract PrivacyCriterion getCriterion(Model model);
	
	/**
     * Is this criterion active.
     *
     * @return
     */
	public boolean isActive() {
		return active;
	}

	/**
     * Is this criterion enabled.
     *
     * @return
     */
	public boolean isEnabled() {
		return enabled;
	}
	
	/**
     * Sets the criterion active/inactive.
     *
     * @param active
     */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
     * Sets the criterion enabled/disabled.
     *
     * @param enabled
     */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	/**
     * Implement this to return a string representation.
     *
     * @return
     */
    public abstract String toString();
}
