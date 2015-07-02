package org.deidentifier.arx.masking;

import java.util.Date;

import org.apache.commons.math3.distribution.IntegerDistribution;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.ReadablePeriod;

/**
 * A masker that shifts dates randomly according to a given probability distribution.
 * <p>
 * The shift distance is calculated by sampling an integer from the distribution and multiplying
 * it with the supplied base period. Optionally, a constant shift is applied to the sampled
 * integer. (This makes it easy to shift e.g. the center of a binomial distribution around
 * P(X = 0).)
 * <p>
 * <b>Example:</b><pre>
 * // A masker that shifts dates randomly in a range from -3 to +3 days:
 * int n = 6;	// Range of [0...6] = 7 days
 * double p = 0.5;	// Coefficent for binomial distribution
 * int shift = -3;	// Center distribution around P(X = 0).
 * RandomShiftDateMasker masker = new RandomShiftDateMasker(
 * 		new {@link org.apache.commons.math3.distribution.BinomialDistribution BinomialDistribution}(n, p),
 * 		shift,
 * 		{@link org.joda.time.Days Days}.ONE );</pre>
 * 
 * @author Wesper
 *
 */
public class RandomShiftDateMasker extends AbstractInstBasedDictMasker<Date> {
	
	private IntegerDistribution	distribution;
	private int					shiftConstant = 0;
	private ReadablePeriod		basePeriod = Days.ONE;
	
	public RandomShiftDateMasker(IntegerDistribution distribution) {
		this(distribution, Days.ONE);
	}
	
	public RandomShiftDateMasker(IntegerDistribution distribution,
									ReadablePeriod basePeriod) {
		this(distribution, 0, basePeriod);
	}
	
	public RandomShiftDateMasker(IntegerDistribution distribution, int shiftConstant,
									ReadablePeriod basePeriod) {
		this.distribution	= distribution;
		this.shiftConstant	= shiftConstant;
		this.basePeriod		= basePeriod;
	}
	
	@Override
	public Date mask(Date input) {
		DateTime inputDate = new DateTime(input);
		
		// Determine shift scalar, multiply with base period and add to input:
		int shift = distribution.sample() + shiftConstant;
		ReadablePeriod shiftPeriod = basePeriod.toPeriod().multipliedBy(shift);
		DateTime shiftedDate = inputDate.plus(shiftPeriod);
		
		return shiftedDate.toDate();
		
	}

}
