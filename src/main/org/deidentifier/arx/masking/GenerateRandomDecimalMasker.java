package org.deidentifier.arx.masking;

import org.apache.commons.math3.distribution.RealDistribution;

/**
 * A masker that generates random decimals by sampling from a given probability distribution.
 * Optionally, a shift constant can be supplied to easily modify the sampled values.
 * 
 * @author Wesper
 *
 */
public class GenerateRandomDecimalMasker extends AbstractReplaceInstMasker<Double> {

	protected RealDistribution	distribution;
	protected double			shiftConstant = 0.0d;
	
	/**
	 * Creates a new random decimal generator based on the given distribution.
	 * @param distribution The probability distribution from which decimals are sampled.
	 */
	public GenerateRandomDecimalMasker(RealDistribution distribution) {
		this(distribution, 0.0d);
	}
	
	/**
	 * Creates a new random decimal generator based on the given distribution. The sampled
	 * values can be shifted by a constant amount to easily modify the distribution.
	 * @param distribution The probability distribution from which decimals are sampled.
	 * @param shiftConstant The shift constant added to the sampled decimals.
	 */
	public GenerateRandomDecimalMasker(RealDistribution distribution, double shiftConstant) {
		this.distribution	= distribution;
		this.shiftConstant	= shiftConstant;
	}

	/**
	 * Generates a new replacement decimal.
	 */
	@Override
	public Double createReplacement() {
		return distribution.sample() + shiftConstant;
	}
	
	

}
