package org.deidentifier.arx.masking;

import org.apache.commons.math3.distribution.IntegerDistribution;

/**
 * A masker that generates random integers in decimal format, using a given probability
 * distribution. A shift constant can be added to each sampled value to allow quick and simple
 * modification of the distribution.
 * 
 * @author Wesper
 *
 */
public class GenerateRandomIntegerDecimalMasker extends
				AbstractReplaceInstMasker<Double> {

	private IntegerDistribution	distribution;
	private int					shiftConstant = 0;
	
	/**
	 * Creates a new random generator based on the given distribution.
	 * @param distribution The integer distribution used to generate random decimals.
	 */
	public GenerateRandomIntegerDecimalMasker(IntegerDistribution distribution) {
		this(distribution, 0);
	}
	
	/**
	 * Creates a new random generator based on the given distribution. The sampled values can be
	 * shifted by a constant amount to easily modify the distribution.
	 * @param distribution The integer distribution used to generate random decimals.
	 * @param shiftConstant The shift constant added to the sampled integers.
	 */
	public GenerateRandomIntegerDecimalMasker(IntegerDistribution distribution,
												int shiftConstant) {
		this.distribution	= distribution;
		this.shiftConstant	= shiftConstant;
	}

	/**
	 * Generates a new replacement integer decimal.
	 */
	@Override
	public Double createReplacement() {
		return Double.valueOf(distribution.sample() + shiftConstant);
	}

}
