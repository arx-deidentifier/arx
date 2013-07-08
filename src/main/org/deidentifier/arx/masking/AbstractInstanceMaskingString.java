package org.deidentifier.arx.masking;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.ARXString;


public abstract class AbstractInstanceMaskingString extends AbstractInstanceMasking<ARXString>{

	@Override
	public final String mask(String input) {
		return DataType.STRING.toString(maskInternal(DataType.STRING.fromString(input)));
	}
	
	protected abstract String maskInternal(String input);

}
