package org.deidentifier.arx.masking;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.ARXDecimal;


public abstract class AbstractInstanceMaskingDecimal extends AbstractInstanceMasking<ARXDecimal>{

	@Override
	public final String mask(String input) {
		return DataType.DECIMAL.toString(maskInternal(DataType.DECIMAL.fromString(input)));
	}
	
	protected abstract double maskInternal(double input);

}
