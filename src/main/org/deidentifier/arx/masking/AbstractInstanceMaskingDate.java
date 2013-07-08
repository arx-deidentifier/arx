package org.deidentifier.arx.masking;

import java.util.Date;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.ARXDate;


public abstract class AbstractInstanceMaskingDate extends AbstractInstanceMasking<ARXDate>{

	@Override
	public final String mask(String input) {
		return DataType.DATE.toString(maskInternal(DataType.DATE.fromString(input)));
	}
	
	protected abstract Date maskInternal(Date input);

}
