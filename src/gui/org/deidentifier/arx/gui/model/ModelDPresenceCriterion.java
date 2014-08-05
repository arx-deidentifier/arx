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

import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.criteria.PrivacyCriterion;

public class ModelDPresenceCriterion extends ModelImplicitCriterion{

	private static final long serialVersionUID = -1765428286262869856L;
	private double dmin = 0.001d;
	private double dmax = 0.001d;
	
	public double getDmin() {
		return dmin;
	}
	public void setDmin(double dmin) {
		this.dmin = dmin;
	}
	public double getDmax() {
		return dmax;
	}
	public void setDmax(double dmax) {
		this.dmax = dmax;
	}
	
	@Override
	public PrivacyCriterion getCriterion(Model model) {
	    DataSubset subset = DataSubset.create(model.getInputConfig().getInput(), model.getInputConfig().getResearchSubset());
		return new DPresence(dmin, dmax, subset);
	}

    @Override
    public String toString() {
        // TODO: Move to messages.properties
        return "("+String.valueOf(dmin)+","+String.valueOf(dmax)+")-Presence";
    }

}
