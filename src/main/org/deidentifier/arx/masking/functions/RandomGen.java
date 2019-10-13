package org.deidentifier.arx.masking.functions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import org.apache.commons.lang.RandomStringUtils;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.ARXDate;
import org.deidentifier.arx.DataType.ARXDecimal;
import org.deidentifier.arx.DataType.ARXInteger;
import org.deidentifier.arx.DataType.ARXString;
import org.deidentifier.arx.framework.data.DataColumn;
import org.deidentifier.arx.masking.DataMaskingFunction;
import org.deidentifier.arx.masking.variable.Distribution;
import org.deidentifier.arx.masking.variable.DistributionParameter;
import org.deidentifier.arx.masking.variable.DistributionType;
import org.deidentifier.arx.masking.variable.RandomVariable;

public class RandomGen extends DataMaskingFunction {

	private static final long serialVersionUID = -7053376272682601068L;

	private static int numberFromDistribution(int min, double[] allY) {
		int[] ranges = new int[allY.length];
		HashMap<Integer, Integer> rangeToX = new HashMap<Integer, Integer>();
		int sum = 0;

		for (int i = 0; i < allY.length; i++) {
			sum += Math.round(1000 * (float) allY[i]);
			ranges[i] = sum;
			rangeToX.put(sum, min + i);
		}

		int randomNumber = new Random().nextInt(ranges[ranges.length - 1] + 1);
		int out = min;

		for (int i = 0; i < ranges.length; i++) {
			if (randomNumber <= ranges[i]) {
				out = rangeToX.get(ranges[i]);
				break;
			}
		}
		return out;
	}

	/**
	 * Chooses random object according to distribution.
	 * 
	 * @param allX
	 * @param allY
	 * @param values
	 * @return
	 */
	private static String fromDistribution(int min, double[] allY, Object[] values) {
		int len = allY.length;
		HashMap<Integer, String> rangeToString = new HashMap<Integer, String>();
		int[] ranges = new int[len];

		int sum = 0;
		for (int i = 0; i < len; i++) {
			sum += 1000 * Math.round((float) allY[i]);
			ranges[i] = sum;
			rangeToString.put(sum, values[i].toString());
		}

		int randomNumber = new Random().nextInt(ranges[len - 1] + 1);
		String out = values[0].toString();

		for (int i = 0; i < len; i++) {
			if (randomNumber <= ranges[i]) {
				out = rangeToString.get(ranges[i]);
				break;
			}
		}

		return out;
	}

	/**
	 * Replaces the values in the column with a randomly generated value. For
	 * columns of type integer, double, and ordinal, strings must be null.
	 * 
	 * @param allX
	 * @param allY
	 * @param strings
	 * @param column
	 */
	private static void generate(int min, double[] allY, String[] strings, DataColumn column,
			boolean isIgnoreMissingData) {
		for (int row = 0; row < column.getNumRows(); row++) {
			String replace = "";
			if (strings == null) {
				replace = "" + numberFromDistribution(min, allY);
			} else {
				replace = fromDistribution(min, allY, strings);
			}
			if (isIgnoreMissingData || !column.get(row).equals(DataType.NULL_VALUE)) {
				column.set(row, replace);
			}
		}
	}

	/**
	 * Checks a string-array for duplicates.
	 * 
	 * @param arr
	 * @return
	 */
	private static boolean checkForDuplicates(String[] arr) {
		HashSet<String> set = new HashSet<String>();

		for (int i = 0; i < arr.length; i++) {
			if (!set.add(arr[i]))
				return true;
		}
		return false;
	}

	private static int nonNullRow(DataColumn column) {
		for (int row = 0; row < column.getNumRows(); row++) {
			if (!column.get(row).equals(DataType.NULL_VALUE)) {
				return row;
			}
		}
		return -1;
	}

	public static class RandomGenNumeric implements OperatorDataType {

		private int min;
		private double[] allY;
		private boolean isIgnoreMissingData;

		public RandomGenNumeric(int min, double[] allY, boolean isIgnoreMissingData) {
			this.min = min;
			this.allY = allY;
			this.isIgnoreMissingData = isIgnoreMissingData;
		}

		@Override
		public void mask(DataColumn column) {
			generate(min, allY, null, column, isIgnoreMissingData);
		}
	}

	public static class RandomGenString implements OperatorDataType {

		private int min;
		private double[] allY;
		private boolean isIgnoreMissingData;

		public RandomGenString(int min, double[] allY, boolean isIgnoreMissingData) {
			this.min = min;
			this.allY = allY;
			this.isIgnoreMissingData = isIgnoreMissingData;
		}

		@Override
		public void mask(DataColumn column) {
			String[] strings = generateStrings(8, 16, allY.length);
			generate(min, allY, strings, column, isIgnoreMissingData);
		}

		/**
		 * Generates LEN random strings of length between MIN and MAX.
		 * Duplicates are not allowed.
		 * 
		 * @param min
		 * @param max
		 * @param len
		 * @return
		 */
		private String[] generateStrings(int min, int max, int len) {
			String[] random = new String[len];

			for (int i = 0; i < len; i++) {
				int length = min + new Random().nextInt(max - min + 1);
				random[i] = RandomStringUtils.randomAlphanumeric(length);
			}

			if (checkForDuplicates(random)) {
				return generateStrings(min, max, len);
			} else {
				return random;
			}
		}
	}

	public static class RandomGenDate implements OperatorDataType {

		private int min;
		private double[] allY;
		private boolean isIgnoreMissingData;

		public RandomGenDate(int min, double[] allY, boolean isIgnoreMissingData) {
			this.min = min;
			this.allY = allY;
			this.isIgnoreMissingData = isIgnoreMissingData;
		}

		@Override
		public void mask(DataColumn column) {
			if (nonNullRow(column) < 0) {
				return;
			}

			long earliest = new GregorianCalendar(1900, 1, 1).getTime().getTime() / (1000L * 60L * 60L * 24L);
			long latest = new GregorianCalendar(2050, 1, 1).getTime().getTime() / (1000L * 60L * 60L * 24L);

			String value = column.get(nonNullRow(column));
			SimpleDateFormat format = getDateFormat(value);

			String[] dates = generateDates(earliest, latest, allY.length, format);
			generate(min, allY, dates, column, isIgnoreMissingData);
		}

		/**
		 * Generates LEN random dates of length between EARLIEST and LATEST.
		 * Duplicates are not allowed
		 * 
		 * @param earliest
		 * @param latest
		 * @param len
		 * @return
		 */
		public String[] generateDates(long earliest, long latest, int len, SimpleDateFormat format) {
			String[] random = new String[len];

			for (int i = 0; i < len; i++) {
				long dateL = earliest + (long) (Math.random() * (latest - earliest));
				Date date = new Date(dateL);
				random[i] = format.format(date);
			}

			if (checkForDuplicates(random)) {
				return generateDates(earliest, latest, len, format);
			} else {
				return random;
			}
		}

		public SimpleDateFormat getDateFormat(String d) {
			if (d != null) {
				for (String parse : DataType.listDateFormats()) {
					SimpleDateFormat sdf = new SimpleDateFormat(parse);
					try {
						sdf.parse(d);
						return sdf;
					} catch (ParseException e) {

					}
				}
			}
			return null;
		}
	}

	private RandomVariable var;
	private DistributionMasking helper;

	/**
	 * Creates a new instance.
	 * 
	 * @param ignoreMissingData
	 * @param type
	 * @param handle
	 */
	public RandomGen(boolean ignoreMissingData, DistributionType type) {
		super(ignoreMissingData, false);
		var = new RandomVariable("XXX", type);
		helper = new DistributionMasking(type);
	}

	@Override
	public void apply(DataColumn column, DataType<?> dataType) {
		getImplementationVersion(dataType).mask(column);
	}

	private int getNameParam(ArrayList<DistributionParameter<?>> parameters) {
		int name = 0;
		for (int i = 0; i < parameters.size(); i++) {
			if (parameters.get(i).getName().equals("name")) {
				name = i;
			}
		}
		return name;
	}

	/*
	 * All y-values of the distribution.
	 * 
	 * @param dist
	 * 
	 * @param allX
	 * 
	 * @return
	 */
	private double[] allY(DistributionParameter<?> parameter, Distribution<Integer> dist) {
		int len = (int) parameter.getMax() - (int) parameter.getMin() + 1;

		double[] allY = new double[len];

		for (int i = 0; i < len; i++) {
			allY[i] = dist.getValue(i + (int) parameter.getMin());
		}
		return allY;
	}

	@Override
	public DataMaskingFunction clone() {
		return new RandomGen(super.isIgnoreMissingData(), var.getDistributionType());
	}

	@Override
	public OperatorDataType getImplementationVersion(DataType<?> type) {
		ArrayList<DistributionParameter<?>> parameters = helper.getParameters();
		helper.addAllParams(var, parameters);

		int name = getNameParam(parameters);

		DistributionParameter<?> param = parameters.get(name);

		if (type instanceof ARXInteger || type instanceof ARXDecimal) {
			return new RandomGenNumeric((int) param.getMin(), allY(param, var.getDistribution()),
					super.isIgnoreMissingData());
		} else if (type instanceof ARXString) {
			return new RandomGenString((int) param.getMin(), allY(param, var.getDistribution()),
					super.isIgnoreMissingData());
		} else if (type instanceof ARXDate) {
			return new RandomGenDate((int) param.getMin(), allY(param, var.getDistribution()),
					super.isIgnoreMissingData());
		} else {
			throw new UnsupportedOperationException();
		}
	}

}
