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

public class ModelLDiversityCriterion extends ModelExplicitCriterion{

	private static final long serialVersionUID = -9172448654255959945L;

	public ModelLDiversityCriterion(String attribute) {
		super(attribute);
	}
	private int variant = 0;
	private int l = 2;
	private double c = 0.001d;
	public int getVariant() {
		return variant;
	}
	public void setVariant(int variant) {
		this.variant = variant;
	}
	public int getL() {
		return l;
	}
	public void setL(int l) {
		this.l = l;
	}
	public double getC() {
		return c;
	}
	public void setC(double c) {
		this.c = c;
	}

	@Override
	public PrivacyCriterion getCriterion(Model model) {
		
		if (variant==0){
			return new DistinctLDiversity(getAttribute(), l);
		} else if (variant==1){
			return new EntropyLDiversity(getAttribute(), l);
		} else if (variant==2){
			return new RecursiveCLDiversity(getAttribute(), c, l);
		} else {
			throw new RuntimeException("Internal error: invalid variant of l-diversity");
		}
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
    @Override
    public String toString() {
        // TODO: Move to messages.properties
        if (variant==0){
            return "Distinct-"+l+"-diversity";
        } else if (variant==1){
            return "Entropy-"+l+"-diversity";
        } else if (variant==2){
            return "Recursive-("+String.valueOf(c)+","+l+")-diversity";
        } else {
            throw new RuntimeException("Internal error: invalid variant of l-diversity");
        }
    }
}
