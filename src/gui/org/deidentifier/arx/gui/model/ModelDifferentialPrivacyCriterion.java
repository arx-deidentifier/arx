/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2016 Fabian Prasser, Florian Kohlmayer and contributors
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

import org.deidentifier.arx.DataGeneralizationScheme;
import org.deidentifier.arx.DataGeneralizationScheme.GeneralizationDegree;
import org.deidentifier.arx.criteria.EDDifferentialPrivacy;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;

/**
 * This class implements a model for the (e,d)-DP criterion.
 *
 * @author Fabian Prasser
 * @author Raffael Bild
 */
public class ModelDifferentialPrivacyCriterion extends ModelImplicitCriterion{

    /** SVUID */
    private static final long        serialVersionUID = 1803345324372136700L;

    /** Epsilon anon */
    private double                   epsilonAnon      = 2d;
    
    /** Epsilon search */
    private double                   epsilonSearch    = 0d;

    /** Delta */
    private double                   delta            = 0.000001d;

    /** Generalization scheme */
    private DataGeneralizationScheme generalization   = DataGeneralizationScheme.create(GeneralizationDegree.MEDIUM);
    
    /** Steps */
    private int                      steps            = 100;

    /**
     * Creates a new instance
     */
    public ModelDifferentialPrivacyCriterion() {
        // Empty by design
    }
    
    /**
     * Creates a new instance
     * @param epsilonAnon
     * @param delta
     */
	public ModelDifferentialPrivacyCriterion(double epsilonAnon, double delta) {
        this.epsilonAnon = epsilonAnon;
        this.delta = delta;
    }

    @Override
    public ModelDifferentialPrivacyCriterion clone() {
        ModelDifferentialPrivacyCriterion result = new ModelDifferentialPrivacyCriterion();
        result.epsilonAnon = this.epsilonAnon;
        result.epsilonSearch = this.epsilonSearch;
        result.delta = this.delta;
        result.generalization = this.generalization.clone();
        result.steps = this.steps;
        result.setEnabled(this.isEnabled());
        return result;
    }

	@Override
	public PrivacyCriterion getCriterion(Model model) {
		return epsilonSearch > 0d ? new EDDifferentialPrivacy(epsilonAnon, delta, epsilonSearch, steps) :
		    new EDDifferentialPrivacy(epsilonAnon, delta, generalization);
	}
	
    /**
     * Getter
     * @return
     */
    public double getDelta() {
        return delta;
    }

    /**
     * Getter
     * @return
     */
    public double getEpsilonAnon() {
        return epsilonAnon;
    }
    
    /**
     * Getter
     * @return
     */
    public double getEpsilonSearch() {
        return epsilonSearch;
    }

    /**
     * Getter
     * @return
     */
    public DataGeneralizationScheme getGeneralization() {
        return generalization;
    }
    
    /**
     * Getter
     * @return
     */
    public int getSteps() {
        return steps;
    }

    @Override
    public String getLabel() {
        return Resources.getMessage("ModelCriterion.3"); //$NON-NLS-1$
    }

    @Override
    public void parse(ModelCriterion criterion, boolean _default) {
        if (!(criterion instanceof ModelDifferentialPrivacyCriterion)) {
            return;
        }
        ModelDifferentialPrivacyCriterion other = (ModelDifferentialPrivacyCriterion)criterion;
        this.epsilonAnon = other.epsilonAnon;
        this.epsilonSearch = other.epsilonSearch;
        this.delta = other.delta;
        this.steps = other.steps;
        if (!_default) {
            this.generalization = other.generalization.clone();
        }
        if (!_default) {
            this.setEnabled(other.isEnabled());
        }
    }

    /**
     * Setter
     * @param delta
     */
    public void setDelta(double delta) {
        this.delta = delta;
    }

    /**
     * Setter
     * @param epsilonAnon
     */
    public void setEpsilonAnon(double epsilonAnon) {
        this.epsilonAnon = epsilonAnon;
    }
    
    /**
     * Setter
     * @param epsilonSearch
     */
    public void setEpsilonSearch(double epsilonSearch) {
        this.epsilonSearch = epsilonSearch;
    }

    /**
     * Setter
     * @param generalization
     */
    public void setGeneralization(DataGeneralizationScheme generalization) {
        this.generalization = generalization;
    }
    
    /**
     * Setter
     * @param steps
     */
    public void setSteps(int steps) {
        this.steps = steps;
    }

    @Override
    public String toString() {
        return "(" + SWTUtil.getPrettyString(epsilonAnon + epsilonSearch) + ", " + SWTUtil.getPrettyString(delta) + ")" + Resources.getMessage("ModelCriterion.2"); //$NON-NLS-1$
    }
}
