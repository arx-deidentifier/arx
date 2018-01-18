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

import org.deidentifier.arx.criteria.DDisclosurePrivacy;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;

/**
 * This class implements a model for the d-disclosure privacy criterion
 *
 * @author Fabian Prasser
 */
public class ModelDDisclosurePrivacyCriterion extends ModelExplicitCriterion{

    /** SVUID */
    private static final long serialVersionUID = 4708272194910927203L;
    /** Delta */
    private double            d                = 1.0d;

    /**
     * Creates a new instance.
     *
     * @param attribute
     */
    public ModelDDisclosurePrivacyCriterion(String attribute) {
        super(attribute);
    }
    
    /**
     * Creates a new instance.
     *
     * @param attribute
     */
    public ModelDDisclosurePrivacyCriterion(String attribute,
                                            double d) {
        super(attribute);
        this.d = d;
    }
    
    @Override
    public ModelDDisclosurePrivacyCriterion clone() {
        ModelDDisclosurePrivacyCriterion result = new ModelDDisclosurePrivacyCriterion(this.getAttribute());
        result.d = this.d;
        result.setEnabled(this.isEnabled());
        return result;
    }
	
	@Override
	public PrivacyCriterion getCriterion(Model model) {
	    return new DDisclosurePrivacy(getAttribute(), d);
	}
	
	/**
     * Returns D.
     *
     * @return
     */
	public double getD() {
		return d;
	}
	
	@Override
    public String getLabel() {
        return  '\u03B4' + Resources.getMessage("Model.31"); //$NON-NLS-1$
    }

    @Override
    public void parse(ModelCriterion criterion, boolean _default) {
        if (!(criterion instanceof ModelDDisclosurePrivacyCriterion)) {
            return;
        }
        ModelDDisclosurePrivacyCriterion other = (ModelDDisclosurePrivacyCriterion)criterion;
        this.d = other.d;
        this.setEnabled(other.isEnabled());
    }
    
	@Override
    public void pull(ModelExplicitCriterion criterion) {
        if (!(criterion instanceof ModelDDisclosurePrivacyCriterion)) {
            throw new RuntimeException(Resources.getMessage("Model.2d")); //$NON-NLS-1$
        }
        ModelDDisclosurePrivacyCriterion other = (ModelDDisclosurePrivacyCriterion)criterion;
        this.d = other.d;
    }

    /**
     * Sets D.
     *
     * @param d
     */
	public void setD(double d) {
		this.d = d;
	}
    
    @Override
    public String toString() {
        return SWTUtil.getPrettyString(d)+Resources.getMessage("Model.30"); //$NON-NLS-1$
    }
}
