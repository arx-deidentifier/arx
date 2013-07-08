package org.deidentifier.arx.masking;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.ARXDecimal;

public abstract class AbstractDictionaryMaskingDecimal extends AbstractDictionaryMasking<ARXDecimal> {

	@Override
	public final String[] mask(String[] input) {
		
		// Convert input
		double[] array = new double[input.length];
		for (int i=0; i<input.length; i++){
			array[i] = DataType.DECIMAL.fromString(input[i]);
		}
		
		// Call
		maskInternal(array); 
		
		// Convert input
		String[] out = new String[input.length];
		for (int i=0; i<array.length; i++){
			out[i] = DataType.DECIMAL.toString(array[i]);
		}
				
		// Return
		return out;
	}
	
	protected abstract void maskInternal(double[] array);

}
