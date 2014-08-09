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

import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.criteria.PrivacyCriterion;

/**
 * This class implements a model for the k-anonymity criterion
 * @author Fabian Prasser
 */
public class ModelKAnonymityCriterion extends ModelImplicitCriterion{

    /** SVUID*/
	private static final long serialVersionUID = 6393748805356545958L;
	/** K*/
	private int k = 2;
	
	@Override
	public PrivacyCriterion getCriterion(Model model) {
		return new KAnonymity(k);
	}

	/**
	 * Returns K
	 * @return
	 */
	public int getK() {
		return k;
	}
	
	/**
	 * Sets K
	 * @param k
	 */
	public void setK(int k) {
		this.k = k;
	}

    @Override
    public String toString() {
        // TODO: Move to messages.properties
        return k+"-Anonymity";
    }
	
}
