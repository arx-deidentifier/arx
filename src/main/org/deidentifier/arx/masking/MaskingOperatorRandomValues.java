package org.deidentifier.arx.masking;

import org.deidentifier.arx.DataType;

/**
 * Example for a generic abstract operator on instance level 
 */
public abstract class MaskingOperatorRandomValues<T extends DataType<?>> extends AbstractInstanceMasking<T>{

	protected final int[] distribution;
	
	public MaskingOperatorRandomValues(int[] distribution) {
		this.distribution = distribution;
	}
}
