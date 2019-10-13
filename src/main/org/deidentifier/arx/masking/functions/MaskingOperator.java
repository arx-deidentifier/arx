package org.deidentifier.arx.masking.functions;

import org.deidentifier.arx.DataType;

/**
 * Returns an operator datatype.
 * 
 * @author Kieu-Mi Do
 *
 */
public interface MaskingOperator {
	/**
	 * Returns the implementation version.
	 * 
	 * @param type
	 * @return
	 */
	OperatorDataType getImplementationVersion(DataType<?> type);
}
