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
