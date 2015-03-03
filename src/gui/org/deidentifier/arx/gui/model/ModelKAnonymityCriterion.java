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

import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.criteria.PrivacyCriterion;

/**
 * This class implements a model for the k-anonymity criterion.
 *
 * @author Fabian Prasser
 */
public class ModelKAnonymityCriterion extends ModelImplicitCriterion{

    /** SVUID. */
	private static final long serialVersionUID = 6393748805356545958L;
	
	/** K. */
	private int k = 2;
	
	/* (non-Javadoc)
	 * @see org.deidentifier.arx.gui.model.ModelCriterion#getCriterion(org.deidentifier.arx.gui.model.Model)
	 */
	@Override
	public PrivacyCriterion getCriterion(Model model) {
		return new KAnonymity(k);
	}

	/**
     * Returns K.
     *
     * @return
     */
	public int getK() {
		return k;
	}
	
	/**
     * Sets K.
     *
     * @param k
     */
	public void setK(int k) {
		this.k = k;
	}

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.model.ModelCriterion#toString()
     */
    @Override
    public String toString() {
        // TODO: Move to messages.properties
        return k+"-Anonymity";
    }
	
}
