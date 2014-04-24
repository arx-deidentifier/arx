package org.deidentifier.arx.masking;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.ReadablePeriod;

/**
 * Masks date instances by adding a constant time period.
 * 
 * @author Wesper
 *
 */
public class ConstantShiftDateMasker extends AbstractInstBasedDictMasker<Date> {

	/** The amount of time the input date shall be shifted. */
	private ReadablePeriod shiftPeriod;
	
	/**
	 * Creates a constant shift masker that shifts the input date by the specified amount of
	 * time.
	 * 
	 * @param shiftDistance The shift in milliseconds.
	 */
	public ConstantShiftDateMasker(int shiftDistance) {
		setShiftDistance(shiftDistance);
	}
	
	/**
	 * Creates a constant shift masker that shifts the input by the given period of time.
	 * 
	 * @param shiftPeriod The amount of time the dates are shifted.
	 */
	public ConstantShiftDateMasker(ReadablePeriod shiftPeriod) {
		setShiftPeriod(shiftPeriod);
	}
	
	/**
	 * Shifts the given date by a constant time period.
	 * 
	 * @param input The date to be shifted.
	 * @return The shifted date.
	 */
	@Override
	public Date mask(Date input) {
		DateTime date = new DateTime(input);
		DateTime shiftedDate = date.plus(shiftPeriod);
		return shiftedDate.toDate();
	}

	public int getShiftDistance() {
		return shiftPeriod.toPeriod().getMillis();
	}

	public void setShiftDistance(int shiftDistance) {
		shiftPeriod = new Period(shiftDistance);
	}

	public ReadablePeriod getShiftPeriod() {
		return shiftPeriod;
	}

	/**
	 * Sets the time interval by which the masker will shift dates.
	 * 
	 * @param shiftPeriod The time period to be added to dates.
	 */
	public void setShiftPeriod(ReadablePeriod shiftPeriod) {
		this.shiftPeriod = shiftPeriod;
	}

}
