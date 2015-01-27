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

import org.deidentifier.arx.criteria.EqualDistanceTCloseness;
import org.deidentifier.arx.criteria.HierarchicalDistanceTCloseness;
import org.deidentifier.arx.criteria.PrivacyCriterion;

/**
 * This class implements a model for the t-closeness criterion.
 *
 * @author Fabian Prasser
 */
public class ModelTClosenessCriterion extends ModelExplicitCriterion{

    /** SVUID. */
    private static final long serialVersionUID = 4901053938589514626L;
        
    /** Variant. */
    public static final int   VARIANT_EQUAL        = 0;
    
    /** Variant. */
    public static final int   VARIANT_HIERARCHICAL = 1;

	/** The variant. */
	private int variant = 0;
	
	/** T. */
	private double t = 0.001d;

    /**
     * Creates a new instance.
     *
     * @param attribute
     */
    public ModelTClosenessCriterion(String attribute) {
        super(attribute);
    }
    
    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.model.ModelCriterion#getCriterion(org.deidentifier.arx.gui.model.Model)
     */
    @Override
	public PrivacyCriterion getCriterion(Model model) {
	    switch (variant) {
            case VARIANT_EQUAL: return new EqualDistanceTCloseness(getAttribute(), t);
            case VARIANT_HIERARCHICAL: return new HierarchicalDistanceTCloseness(getAttribute(), t,
                                                                            model.getInputConfig().getHierarchy(getAttribute()));
            default: throw new RuntimeException("Internal error: invalid variant of t-closeness");
	    }
	}
	
	/**
     * Returns T.
     *
     * @return
     */
	public double getT() {
		return t;
	}
	
	/**
     * Returns the variant.
     *
     * @return
     */
	public int getVariant() {
		return variant;
	}
	
	/* (non-Javadoc)
	 * @see org.deidentifier.arx.gui.model.ModelExplicitCriterion#pull(org.deidentifier.arx.gui.model.ModelExplicitCriterion)
	 */
	@Override
    public void pull(ModelExplicitCriterion criterion) {
        if (!(criterion instanceof ModelTClosenessCriterion)) {
            throw new RuntimeException("Invalid type of criterion");
        }
        ModelTClosenessCriterion other = (ModelTClosenessCriterion)criterion;
        this.variant = other.variant;
        this.t = other.t;
    }

	/**
     * Sets T.
     *
     * @param t
     */
	public void setT(double t) {
		this.t = t;
	}

    /**
     * Sets the variant.
     *
     * @param variant
     */
	public void setVariant(int variant) {
		this.variant = variant;
	}
    
    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.model.ModelCriterion#toString()
     */
    @Override
    public String toString() {
        // TODO: Move to messages.properties
        switch (variant) {
            case VARIANT_EQUAL: return String.valueOf(t)+"-Closeness with equal-distance EMD";
            case VARIANT_HIERARCHICAL: return String.valueOf(t)+"-Closeness with hierarchical-distance EMD";
            default: throw new RuntimeException("Internal error: invalid variant of t-closeness");
        }
    }
}
