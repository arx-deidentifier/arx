package org.deidentifier.arx.masking;

import java.util.Date;

/**
 * Example for a non-generic masking operator on instance level
 */
public class MaskingOperatorRandomShiftDate extends AbstractInstanceMaskingDate{

	@Override
	protected Date maskInternal(Date input) {
		
		/*
		 * RETURN RANDOM SHIFTED DATE
		 */
		return null;
	}

}
