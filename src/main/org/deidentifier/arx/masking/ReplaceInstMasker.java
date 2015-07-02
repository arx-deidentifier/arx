package org.deidentifier.arx.masking;

import java.util.Date;

/**
 * Replaces the input data with a constant replacement value.
 * @author Wesper
 *
 * @param <T> The type of data to be masked.
 */
public class ReplaceInstMasker<T> extends AbstractReplaceInstMasker<T> {

	private T replacementValue;
	
	public ReplaceInstMasker(T replacementValue) {
		this.replacementValue = replacementValue;
	}
	
	/**
	 * Returns the constant replacement value.
	 */
	@Override
	public T createReplacement() {
		return replacementValue;
	}

	public T getReplacementValue() {
		return replacementValue;
	}

	public void setReplacementValue(T replacementValue) {
		this.replacementValue = replacementValue;
	}


	public static class ReplaceDecimalInstMasker extends ReplaceInstMasker<Double> {
		public ReplaceDecimalInstMasker(Double replacementValue) { super(replacementValue); }
	};
	
	public static class ReplaceDateInstMasker extends ReplaceInstMasker<Date> {
		public ReplaceDateInstMasker(Date replacementValue) { super(replacementValue); }
	};
	
	public static class ReplaceStringInstMasker extends ReplaceInstMasker<String> { 
		public ReplaceStringInstMasker(String replacementValue) { super(replacementValue); }
	};
}
