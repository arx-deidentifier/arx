package org.deidentifier.arx.masking;

/**
 * Performs data masking on single instances of data of type T.
 * 
 * @author Wesper
 *
 * @param <T> The type of data to be masked.
 */
public interface IInstanceMasker<T> {

	/**
	 * Masks the input data instance.
	 * 
	 * @param input The data instance to be masked.
	 * @return The masked data.
	 */
	public T mask(T input);
	
}
