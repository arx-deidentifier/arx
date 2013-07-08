package org.deidentifier.arx.masking;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.ARXString;

public abstract class AbstractDictionaryMaskingString extends AbstractDictionaryMasking<ARXString> {

	@Override
	public final String[] mask(String[] input) {
		
		// Convert input
		String[] array = new String[input.length];
		for (int i=0; i<input.length; i++){
			array[i] = DataType.STRING.fromString(input[i]);
		}
		
		// Call
		maskInternal(array); 
		
		// Convert input
		String[] out = new String[input.length];
		for (int i=0; i<array.length; i++){
			out[i] = DataType.STRING.toString(array[i]);
		}
				
		// Return
		return out;
	}
	
	protected abstract void maskInternal(String[] array);

}
