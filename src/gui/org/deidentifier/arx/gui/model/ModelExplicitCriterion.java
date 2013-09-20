package org.deidentifier.arx.gui.model;


public abstract class ModelExplicitCriterion extends ModelCriterion {

	private static final long serialVersionUID = 2140859935908452477L;
	private final String attribute;

	public ModelExplicitCriterion(String attribute) {
		super();
		this.attribute = attribute;
	}

	public String getAttribute() {
		return attribute;
	}
	
    public abstract void pull(ModelExplicitCriterion criterion);
}
