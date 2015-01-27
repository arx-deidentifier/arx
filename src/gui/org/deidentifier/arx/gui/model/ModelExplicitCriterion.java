/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
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

/**
 * This class implements a base class for explicit privacy criteria,
 * i.e., ones that are associated to a specific attribute
 * @author Fabian Prasser
 */
public abstract class ModelExplicitCriterion extends ModelCriterion {

    /** SVUID. */
	private static final long serialVersionUID = 2140859935908452477L;
	
	/** The attribute. */
	private final String attribute;

	/**
     * Creates a new instance.
     *
     * @param attribute
     */
	public ModelExplicitCriterion(String attribute) {
		super();
		this.attribute = attribute;
	}

	/**
     * Returns the associated attribute.
     *
     * @return
     */
	public String getAttribute() {
		return attribute;
	}
	
	/**
     * Implement this, to update data.
     *
     * @param criterion
     */
    public abstract void pull(ModelExplicitCriterion criterion);
}
