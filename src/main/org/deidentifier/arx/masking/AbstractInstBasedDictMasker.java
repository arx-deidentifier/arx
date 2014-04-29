package org.deidentifier.arx.masking;

import java.util.List;

import org.deidentifier.arx.DataType;

/**
 * Performs data masking on a dictionary of values of type T by applying instance masking on
 * each value. Inheriting classes need only implement the {@link #mask} method for
 * instance-based data masking. {@link #maskList} is provided by this class and uses the
 * instance-based method on each element in the dictionary.
 * 
 * @author Wesper
 *
 * @param <T> The type of data to be masked.
 */
public abstract class AbstractInstBasedDictMasker<T>
					extends		AbstractDictionaryMasker<T>
					implements 	IInstanceMasker<T>, IDictionaryMasker<T> {
	
	/**
	 * Masks the given dictionary by calling the {@link #mask} method for each element of the
	 * input list. 
	 */
	@Override
	public void maskList(List<T> data) {
		for(int i = 0; i < data.size(); ++i)
			data.set(i, mask(data.get(i)));
	}
	
	/**
	 * Interprets the input string as a data instance of type T, performs data masking on it and
	 * returns the masked data, converted back to a string.
	 * @param input The string representing a data instance.
	 * @param type The parser used to interpret the string as data - usually a {@link
	 * org.deidentifier.arx.DataType DataType}.
	 * @return The string representing the masked data.
	 */
	public String maskString(String input, DataType<T> type) {
		return type.format(mask(type.parse(input)));
	}
}
