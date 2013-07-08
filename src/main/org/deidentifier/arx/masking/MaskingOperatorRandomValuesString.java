package org.deidentifier.arx.masking;

import org.deidentifier.arx.DataType.ARXString;

/**
 * Example for a non-generic operator on instance level 
 */
public class MaskingOperatorRandomValuesString extends MaskingOperatorRandomValues<ARXString>{

	public MaskingOperatorRandomValuesString(int[] distribution) {
		super(distribution);
	}

	@Override
	public String mask(String input) {
		/*
		 * Use distribution from superclass to generate random strings
		 */
		return null;
	}
}
