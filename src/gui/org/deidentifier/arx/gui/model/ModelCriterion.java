/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

	/**
     * Clone
     */
    public abstract ModelCriterion clone();
	
	/**
     * Implement this to return the criterion.
     *
     * @param model
     * @return
     */
	public abstract PrivacyCriterion getCriterion(Model model);

	/**
     * Implement this to return a string representation.
     *
     * @return
     */
    public abstract String getLabel();

	/**
     * Is this criterion active.
     *
     * @return
     */
	public boolean isEnabled() {
		return enabled;
	}

    /**
     * Parse
     * @param other
     * @param _default Defines whether the model represents a typical parameter configuration for the criterion
     */
    public abstract void parse(ModelCriterion other, boolean _default);
    
    /**
     * Sets the criterion active/inactive.
     *
     * @param active
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
