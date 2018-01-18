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

import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;

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
	/**
     * Creates a new instance
     */
    public ModelKAnonymityCriterion() {
        // Empty by design
    }
    
	/**
	 * Creates a new instance
	 * @param k
	 */
	public ModelKAnonymityCriterion(int k) {
        this.k = k;
    }

    @Override
    public ModelKAnonymityCriterion clone() {
        ModelKAnonymityCriterion result = new ModelKAnonymityCriterion();
        result.k = this.k;
        result.setEnabled(this.isEnabled());
        return result;
    }

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

    @Override
    public String getLabel() {
        return Resources.getMessage("ModelCriterion.0"); //$NON-NLS-1$
    }

    @Override
    public void parse(ModelCriterion criterion, boolean _default) {
        if (!(criterion instanceof ModelKAnonymityCriterion)) {
            return;
        }
        ModelKAnonymityCriterion other = (ModelKAnonymityCriterion)criterion;
        this.k = other.k;
        if (!_default) {
            this.setEnabled(other.isEnabled());
        }
    }

    /**
     * Sets K.
     *
     * @param k
     */
	public void setK(int k) {
		this.k = k;
	}
    
    @Override
    public String toString() {
        return SWTUtil.getPrettyString(k) + Resources.getMessage("ModelCriterion.1"); //$NON-NLS-1$
    }
}
