package org.deidentifier.arx.masking;

import java.util.List;

/**
 * Performs data masking on a whole dictionary of data of type T.
 * @author Wesper
 *
 * @param <T> The type of data to be masked.
 */
public interface IDictionaryMasker<T> {
	
	/**
	 * Masks the dictionary of data in the input list. The original data is overwritten.
	 * 
	 * @param data The list of data on which the masking is performed and to which the masked
	 * output is written.
	 */
	public void maskList(List<T> data);
	
}
