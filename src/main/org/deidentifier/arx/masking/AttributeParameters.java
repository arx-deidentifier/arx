package org.deidentifier.arx.masking;

import org.deidentifier.arx.masking.variable.RandomVariable;

public class AttributeParameters{
	
	private MaskingType maskingType;
	
	private int stringLength=-1;
	
	private RandomVariable selectedDistribution;
	
	public AttributeParameters(MaskingType maskingType, RandomVariable selectedDistribution)
	{
		this.maskingType=maskingType;
		this.selectedDistribution=selectedDistribution;
	}
	public AttributeParameters(MaskingType maskingType, int stringLength)
	{
		this.maskingType=maskingType;
		this.stringLength=stringLength;
	}
	
}