package org.deidentifier.arx.masking;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Masks a dictionary of data by shuffling its values around.
 * 
 * @author Wesper
 *
 * @param <T> The type of the data to be masked.
 */
public class ShuffleMasker<T> extends AbstractDictionaryMasker<T> {

	/**
	 * Masks the input list by shuffling its entries.
	 */
	@Override
	public void maskList(List<T> data) {
		Collections.shuffle(data, Random.staticInstance);
	}
	
	public static class ShuffleDecimalMasker	extends ShuffleMasker<Double>	{ };
	public static class ShuffleDateMasker		extends ShuffleMasker<Date>		{ };
	public static class ShuffleStringMasker		extends ShuffleMasker<String>	{ };
}