package org.deidentifier.arx.masking.functions;

import java.security.SecureRandom;
import java.util.Random;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.ARXString;
import org.deidentifier.arx.framework.data.DataColumn;
import org.deidentifier.arx.masking.DataMaskingFunction;

/**
 * Generates a random alphanumeric string
 * 
 * @author Fabian Prasser
 */

public class DataMaskingFunctionRandomAlphanumericString extends DataMaskingFunction {

	/**
	 * Creates a random string
	 * 
	 * @param random
	 * @param buffer
	 * @return
	 */
	private static String getRandomAlphanumericString(char[] buffer, Random random) {
		for (int i = 0; i < buffer.length; i++) {
			buffer[i] = CHARACTERS[random.nextInt(CHARACTERS.length)];
		}
		return new String(buffer);
	}

	public static class AlphanumericString implements OperatorDataType {

		private boolean isIgnoreMissingData;
		private int length;

		public AlphanumericString(boolean isIgnoreMissingData, int length) {
			this.isIgnoreMissingData = isIgnoreMissingData;
			this.length = length;
		}

		@Override
		public void mask(DataColumn column) {
			// Prepare
			Random random = new SecureRandom();
			char[] buffer = new char[length];

			// Mask
			for (int row = 0; row < column.getNumRows(); row++) {

				// Leave null as is, if configured to not ignore missing data
				if (isIgnoreMissingData || !column.get(row).equals(DataType.NULL_VALUE)) {
					column.set(row, getRandomAlphanumericString(buffer, random));
				}
			}
		}

	}

	/** SVUID */
	private static final long serialVersionUID = 918401877743413029L;

	/** Characters */
	private static final char[] CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

	/** Length */
	private final int length;

	/**
	 * Creates a new instance
	 * 
	 * @param ignoreMissingData
	 * @param length
	 */
	public DataMaskingFunctionRandomAlphanumericString(boolean ignoreMissingData, int length) {
		super(ignoreMissingData, false);
		this.length = length;
	}

	@Override
	public void apply(DataColumn column, DataType<?> dataType) {
		getImplementationVersion(dataType).mask(column);
	}

	@Override
	public DataMaskingFunction clone() {
		return new DataMaskingFunctionRandomAlphanumericString(super.isIgnoreMissingData(), length);
	}

	@Override
	public OperatorDataType getImplementationVersion(DataType<?> type) {
		if (type instanceof ARXString) {
			return new AlphanumericString(super.isIgnoreMissingData(), length);
		} else {
			throw new UnsupportedOperationException();
		}
	}
}