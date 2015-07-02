package org.deidentifier.arx.masking;

/**
 * Masks instances of decimal data by adding a constant value.
 * 
 * @author Wesper
 *
 */
public class ConstantShiftDecimalMasker extends AbstractInstBasedDictMasker<Double> {

	protected double shiftDistance;
	
	/**
	 * Creates a constant shift masker that shifts the input decimals by the specified constant.
	 * @param shiftDistance The shift added to the input decimals.
	 */
	public ConstantShiftDecimalMasker(double shiftDistance) {
		this.shiftDistance = shiftDistance;
	}
	
	/**
	 * Shifts the input decimal by a constant.
	 */
	@Override
	public Double mask(Double input) {
		return input + shiftDistance;
	}

	public double getShiftDistance() {
		return shiftDistance;
	}

	public void setShiftDistance(double shiftDistance) {
		this.shiftDistance = shiftDistance;
	}

}
