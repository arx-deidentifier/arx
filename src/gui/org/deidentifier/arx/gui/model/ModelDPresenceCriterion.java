package org.deidentifier.arx.gui.model;

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
		return new DPresence(dmin, dmax, model.getInputConfig().getResearchSubset());
	}

}
