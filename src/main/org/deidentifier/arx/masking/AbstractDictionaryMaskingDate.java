package org.deidentifier.arx.masking;

import java.util.Date;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.ARXDate;

public abstract class AbstractDictionaryMaskingDate extends AbstractDictionaryMasking<ARXDate> {

	@Override
	public final String[] mask(String[] input) {
		
		// Convert input
		Date[] array = new Date[input.length];
		for (int i=0; i<input.length; i++){
			array[i] = DataType.DATE.fromString(input[i]);
		}
		
		// Call
		maskInternal(array); 
		
		// Convert input
		String[] out = new String[input.length];
		for (int i=0; i<array.length; i++){
			out[i] = DataType.DATE.toString(array[i]);
		}
				
		// Return
		return out;
	}
	
	protected abstract void maskInternal(Date[] array);

}
