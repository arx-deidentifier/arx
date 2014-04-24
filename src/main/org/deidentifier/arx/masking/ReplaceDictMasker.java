package org.deidentifier.arx.masking;

import java.util.Date;
import java.util.List;

/**
 * Replaces the list of input data with the provided replacement list. 
 * @author Wesper
 *
 * @param <T> The type of data to be masked.
 */
public class ReplaceDictMasker<T> extends AbstractDictionaryMasker<T> {

	private List<T> replacementList;
	
	public ReplaceDictMasker(List<T> replacementList) {
		this.replacementList = replacementList;
	}
	
	/**
	 * Replaces the input list with this object's replacement list.
	 * <p>
	 * The input list must be of equal size or smaller than the replacement list, as the input
	 * could otherwise not be masked entirely. If the input is smaller, not all of the
	 * replacement values will be used.
	 * 
	 * @throws IllegalArgumentException if the replacement list is smaller than the input list.
	 */
	@Override
	public void maskList(List<T> data) {
		
		if(data.size() > replacementList.size()) throw
			new IllegalArgumentException("Argument input list is larger than replacement list.");
		
		for (int i = 0; i < data.size(); ++i)
			data.set(i, replacementList.get(i));

	}

	public List<T> getReplacementList() {
		return replacementList;
	}

	public void setReplacementList(List<T> replacementList) {
		this.replacementList = replacementList;
	}


	public static class ReplaceDecimalDictMasker extends ReplaceDictMasker<Double> {
		public ReplaceDecimalDictMasker(List<Double> replacementList) { super(replacementList); }
	};
	
	public static class ReplaceDateDictMasker extends ReplaceDictMasker<Date> {
		public ReplaceDateDictMasker(List<Date> replacementList) { super(replacementList); }
	};
	
	public static class ReplaceStringDictMasker extends ReplaceDictMasker<String> {
		public ReplaceStringDictMasker(List<String> replacementList) { super(replacementList); }
	};
}
