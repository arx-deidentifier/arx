package org.deidentifier.arx.masking.functions;

import org.deidentifier.arx.DataType;

/**
 * Reforms the masking function for the given datacolumn under the context of a
 * given operator and datatype.
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
