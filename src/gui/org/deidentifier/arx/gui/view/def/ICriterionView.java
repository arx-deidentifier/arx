package org.deidentifier.arx.gui.view.def;

import org.deidentifier.arx.criteria.PrivacyCriterion;

public interface ICriterionView extends IView {

	public abstract PrivacyCriterion getCriterion();
}
