/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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

public class ModelTClosenessCriterion extends ModelExplicitCriterion{

	private static final long serialVersionUID = 4901053938589514626L;
	public ModelTClosenessCriterion(String attribute) {
		super(attribute);
	}
	private int variant = 0;
	private double t = 0.001d;
	public int getVariant() {
		return variant;
	}
	public void setVariant(int variant) {
		this.variant = variant;
	}
	public double getT() {
		return t;
	}
	public void setT(double t) {
		this.t = t;
	}

	@Override
	public PrivacyCriterion getCriterion(Model model) {
		if (variant == 0) {
			return new EqualDistanceTCloseness(getAttribute(), t);
		} else if (variant == 1) {
			return new HierarchicalDistanceTCloseness(getAttribute(), t,
					model.getInputConfig().getHierarchy(getAttribute()));
		} else {
			throw new RuntimeException(
					"Internal error: invalid variant of t-closeness");
		}
	}

    @Override
    public void pull(ModelExplicitCriterion criterion) {
        if (!(criterion instanceof ModelTClosenessCriterion)) {
            throw new RuntimeException("Invalid type of criterion");
        }
        ModelTClosenessCriterion other = (ModelTClosenessCriterion)criterion;
        this.variant = other.variant;
        this.t = other.t;
    }
    @Override
    public String toString() {
        // TODO: Move to messages.properties
        if (variant==0){
            return String.valueOf(t)+"-Closeness with equal-distance EMD";
        } else if (variant==1){
            return String.valueOf(t)+"-Closeness with hierarchical-distance EMD";
        } else {
            throw new RuntimeException("Internal error: invalid variant of l-diversity");
        }
    }
}
