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
import org.deidentifier.arx.gui.resources.Resources;

/**
 * This class implements a model for the l-diversity criterion.
 *
 * @author Fabian Prasser
 */
public class ModelLDiversityCriterion extends ModelExplicitCriterion{

    /** SVUID. */
    private static final long serialVersionUID  = -9172448654255959945L;

    /** Variant. */
    public static final int   VARIANT_DISTINCT  = 0;

    /** Variant. */
    public static final int   VARIANT_ENTROPY   = 1;

    /** Variant. */
    public static final int   VARIANT_RECURSIVE = 2;

    /** The variant to use. */
    private int               variant           = 0;

    /** L. */
    private int               l                 = 2;

    /** C, if any. */
    private double            c                 = 0.001d;

    /**
     * Creates a new instance.
     *
     * @param attribute
     */
    public ModelLDiversityCriterion(String attribute) {
        super(attribute);
    }
    
    @Override
    public ModelLDiversityCriterion clone() {
        ModelLDiversityCriterion result = new ModelLDiversityCriterion(this.getAttribute());
        result.l = this.l;
        result.c = this.c;
        result.variant = this.variant;
        result.setEnabled(this.isEnabled());
        return result;
    }
	
	/**
     * Gets C.
     *
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
	        default: throw new RuntimeException(Resources.getMessage("Model.0e")); //$NON-NLS-1$
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
	
	@Override
    public String getLabel() {
        return Resources.getMessage("Model.1d"); //$NON-NLS-1$
    }
	
	/**
     * Returns the variant.
     *
     * @return
     */
	public int getVariant() {
		return variant;
	}

	@Override
    public void parse(ModelCriterion criterion) {
        if (!(criterion instanceof ModelLDiversityCriterion)) {
            return;
        }
        ModelLDiversityCriterion other = (ModelLDiversityCriterion)criterion;
        this.l = other.l;
        this.c = other.c;
        this.variant = other.variant;
        this.setEnabled(other.isEnabled());
    }
	
    @Override
    public void pull(ModelExplicitCriterion criterion) {
        if (!(criterion instanceof ModelLDiversityCriterion)) {
            throw new RuntimeException(Resources.getMessage("Model.2b")); //$NON-NLS-1$
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
    
    @Override
    public String toString() {
        switch (variant) {
            case VARIANT_DISTINCT: return Resources.getMessage("Model.3")+l+Resources.getMessage("Model.4"); //$NON-NLS-1$ //$NON-NLS-2$
            case VARIANT_ENTROPY: return Resources.getMessage("Model.5")+l+Resources.getMessage("Model.6");  //$NON-NLS-1$ //$NON-NLS-2$
            case VARIANT_RECURSIVE: return Resources.getMessage("Model.7")+String.valueOf(c)+Resources.getMessage("Model.8")+l+Resources.getMessage("Model.9"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            default: throw new RuntimeException(Resources.getMessage("Model.10")); //$NON-NLS-1$
        }
    }
}
