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

import org.deidentifier.arx.criteria.DistinctLDiversity;
import org.deidentifier.arx.criteria.EntropyLDiversity;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.criteria.RecursiveCLDiversity;

/**
 * This class implements a model for the l-diversity criterion
 * @author Fabian Prasser
 *
 */
public class ModelLDiversityCriterion extends ModelExplicitCriterion{

    /** SVUID*/
    private static final long serialVersionUID = -9172448654255959945L;
    
    /** Variant*/
    public static final int VARIANT_DISTINCT = 0;
    /** Variant*/
    public static final int VARIANT_ENTROPY = 1;
    /** Variant*/
    public static final int VARIANT_RECURSIVE = 2;

	/** The variant to use*/
	private int variant = 0;
	/** L*/
	private int l = 2;
	/** C, if any*/
	private double c = 0.001d;

    /**
     * Creates a new instance
     * @param attribute
     */
    public ModelLDiversityCriterion(String attribute) {
        super(attribute);
    }
    
    /**
	 * Gets C
	 * @return
	 */
	public double getC() {
		return c;
	}
	
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
	 * Returns L
	 * @return
	 */
	public int getL() {
		return l;
	}
	
	/**
     * Returns the variant
     * @return
     */
	public int getVariant() {
		return variant;
	}
	
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
	 * Sets C
	 * @param c
	 */
	public void setC(double c) {
		this.c = c;
	}

	/**
	 * Sets L
	 * @param l
	 */
	public void setL(int l) {
		this.l = l;
	}
	
    /**
	 * Sets the variant
	 * @param variant
	 */
	public void setVariant(int variant) {
		this.variant = variant;
	}
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
