package org.deidentifier.arx.masking.functions;

import org.deidentifier.arx.framework.data.DataColumn;

/**
 * Returns an operator datatype.
 * 
 * @author Kieu-Mi Do
 *
 */
public interface OperatorDataType {
	void mask(DataColumn column);
}
