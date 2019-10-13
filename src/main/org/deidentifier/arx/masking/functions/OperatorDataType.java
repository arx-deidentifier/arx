package org.deidentifier.arx.masking.functions;

import org.deidentifier.arx.framework.data.DataColumn;

/**
 * Reforms the masking function for the given datacolumn under the context of a
 * given operator and datatype..
 * 
 * @author Kieu-Mi Do
 *
 */
public interface OperatorDataType {
	void mask(DataColumn column);
}
