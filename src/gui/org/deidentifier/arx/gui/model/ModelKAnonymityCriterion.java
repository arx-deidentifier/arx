package org.deidentifier.arx.gui.model;

import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.criteria.PrivacyCriterion;

public class ModelKAnonymityCriterion extends ModelImplicitCriterion{

	private static final long serialVersionUID = 6393748805356545958L;
	private int k = 2;
	
	public int getK() {
		return k;
	}

	public void setK(int k) {
		this.k = k;
	}
	
	@Override
	public PrivacyCriterion getCriterion(Model model) {
		return new KAnonymity(k);
	}
}
