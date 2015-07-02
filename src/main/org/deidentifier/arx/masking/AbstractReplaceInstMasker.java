package org.deidentifier.arx.masking;

/**
 * Performs data masking by replacing the input data with new values for each data instance,
 * generated independently from the input. The implementation of the data generator must be
 * provided in the {@link #createReplacement}
 * method.
 * @author Wesper
 *
 * @param <T> The type of data to be masked
 */
public abstract class AbstractReplaceInstMasker<T> extends AbstractInstBasedDictMasker<T> {

	/**
	 * Generates a replacement independently from the given input. This method is present to
	 * specify the {@link IInstanceMasker} interface.
	 * @param input Unused - can be {@code null}. 
	 */
	@Override
	public T mask(T input) {
		return createReplacement();
	}
	
	/**
	 * Creates a new replacement value.
	 * @return The generated value.
	 */
	public abstract T createReplacement();

}
