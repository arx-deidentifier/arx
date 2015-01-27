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

import org.deidentifier.arx.criteria.DistinctLDiversity;
import org.deidentifier.arx.criteria.EntropyLDiversity;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.criteria.RecursiveCLDiversity;

/**
 * This class implements a model for the l-diversity criterion.
 *
 * @author Fabian Prasser
 */
public class ModelLDiversityCriterion extends ModelExplicitCriterion{

    /** SVUID. */
    private static final long serialVersionUID = -9172448654255959945L;
    
    /** Variant. */
    public static final int VARIANT_DISTINCT = 0;
    
    /** Variant. */
    public static final int VARIANT_ENTROPY = 1;
    
    /** Variant. */
    public static final int VARIANT_RECURSIVE = 2;

	/** The variant to use. */
	private int variant = 0;
	
	/** L. */
	private int l = 2;
	
	/** C, if any. */
	private double c = 0.001d;

    /**
     * Creates a new instance.
     *
     * @param attribute
     */
    public ModelLDiversityCriterion(String attribute) {
        super(attribute);
    }
    
    /**
     * Gets C.
     *
     * @return
     */
	public double getC() {
		return c;
	}
	
	/* (non-Javadoc)
	 * @see org.deidentifier.arx.gui.model.ModelCriterion#getCriterion(org.deidentifier.arx.gui.model.Model)
	 */
	@Override
	public PrivacyCriterion getCriterion(Model model) {
	    switch (variant) {
    	    case VARIANT_DISTINCT: return new DistinctLDiversity(getAttribute(), l);
    	    case VARIANT_ENTROPY: return new EntropyLDiversity(getAttribute(), l); 
    	    case VARIANT_RECURSIVE: return new RecursiveCLDiversity(getAttribute(), c, l);
	        default: throw new RuntimeException("Internal error: invalid variant of l-diversity");
	    }
	}
	
	/**
     * Returns L.
     *
     * @return
     */
	public int getL() {
		return l;
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
        if (!(criterion instanceof ModelLDiversityCriterion)) {
            throw new RuntimeException("Invalid type of criterion");
        }
        ModelLDiversityCriterion other = (ModelLDiversityCriterion)criterion;
        this.variant = other.variant;
        this.l = other.l;
        this.c = other.c;
    }
	
	/**
     * Sets C.
     *
     * @param c
     */
	public void setC(double c) {
		this.c = c;
	}

	/**
     * Sets L.
     *
     * @param l
     */
	public void setL(int l) {
		this.l = l;
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
            case VARIANT_DISTINCT: return "Distinct-"+l+"-diversity";
            case VARIANT_ENTROPY: return "Entropy-"+l+"-diversity"; 
            case VARIANT_RECURSIVE: return "Recursive-("+String.valueOf(c)+","+l+")-diversity";
            default: throw new RuntimeException("Internal error: invalid variant of l-diversity");
        }
    }
}
