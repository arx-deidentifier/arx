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

/**
 * This class implements a base class for explicit privacy criteria,
 * i.e., ones that are associated to a specific attribute
 * @author Fabian Prasser
 */
public abstract class ModelExplicitCriterion extends ModelCriterion {

    /** SVUID*/
	private static final long serialVersionUID = 2140859935908452477L;
	
	/** The attribute*/
	private final String attribute;

	/**
	 * Creates a new instance
	 * @param attribute
	 */
	public ModelExplicitCriterion(String attribute) {
		super();
		this.attribute = attribute;
	}

	/**
	 * Returns the associated attribute
	 * @return
	 */
	public String getAttribute() {
		return attribute;
	}
	
	/**
	 * Implement this, to update data
	 * @param criterion
	 */
    public abstract void pull(ModelExplicitCriterion criterion);
}
