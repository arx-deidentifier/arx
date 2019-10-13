package org.deidentifier.arx.masking.functions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.ARXDate;
import org.deidentifier.arx.DataType.ARXDecimal;
import org.deidentifier.arx.DataType.ARXInteger;
import org.deidentifier.arx.framework.data.DataColumn;
import org.deidentifier.arx.masking.DataMaskingFunction;
import org.deidentifier.arx.masking.variable.Distribution;
import org.deidentifier.arx.masking.variable.DistributionParameter;
import org.deidentifier.arx.masking.variable.DistributionType;
import org.deidentifier.arx.masking.variable.RandomVariable;

public class NoiseAddition extends DataMaskingFunction {

	private static final long serialVersionUID = 8893507615419634072L;

	private static void maskDataColumn(DataColumn column, int min, double[] allY, NoiseFunctionHandler nHandler,
			boolean isIgnoreMissingData) {
		for (int row = 0; row < column.getNumRows(); row++) {
			int noise = numberFromDistribution(min, allY);
			if (isIgnoreMissingData || !column.get(row).equals(DataType.NULL_VALUE)) {
				String value = column.get(row);
				column.set(row, nHandler.handle(value, noise));
			}
		}
	}

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

	private interface NoiseFunctionHandler {
		String handle(String value, int noise);
	}

	public static class NoiseAdditionInteger implements OperatorDataType {

		private int min;
		private double[] allY;
		private boolean isIgnoreMissingData;

		public NoiseAdditionInteger(int min, double[] allY, boolean isIgnoreMissingData) {
			this.min = min;
			this.allY = allY;
			this.isIgnoreMissingData = isIgnoreMissingData;
		}

		@Override
		public void mask(DataColumn column) {
			maskDataColumn(column, min, allY, (value, noise) -> "" + (Integer.valueOf(value) + noise),
					isIgnoreMissingData);
		}
	}

	public static class NoiseAdditionDecimal implements OperatorDataType {

		private int min;
		private double[] allY;
		private boolean isIgnoreMissingData;

		public NoiseAdditionDecimal(int min, double[] allY, boolean isIgnoreMissingData) {
			this.min = min;
			this.allY = allY;
			this.isIgnoreMissingData = isIgnoreMissingData;
		}

		@Override
		public void mask(DataColumn column) {
			maskDataColumn(column, min, allY, (value, noise) -> "" + (Double.valueOf(value) + noise),
					isIgnoreMissingData);
		}
	}

	public static class NoiseAdditionDate implements OperatorDataType {

		private int min;
		private double[] allY;
		private boolean isIgnoreMissingData;

		public NoiseAdditionDate(int min, double[] allY, boolean isIgnoreMissingData) {
			this.min = min;
			this.allY = allY;
			this.isIgnoreMissingData = isIgnoreMissingData;
		}

		@Override
		public void mask(DataColumn column) {
			maskDataColumn(column, min, allY, (value, noise) -> {
				SimpleDateFormat format = getDateFormat(value);
				try {
					Date date = format.parse(value);
					long ms = date.getTime();
					ms += noise * 1000L * 60L * 60L * 24L;
					date.setTime(ms);
					return format.format(date);
				} catch (ParseException e) {
					e.printStackTrace();
					return "0";
				}
			}, isIgnoreMissingData);
		}

		private SimpleDateFormat getDateFormat(String d) {
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
	public NoiseAddition(boolean ignoreMissingData, DistributionType type) {
		super(ignoreMissingData, false);
		var = new RandomVariable("XXX", type);
		helper = new DistributionMasking(type);
	}

	@Override
	public void apply(DataColumn column, DataType<?> dataType) {
		getImplementationVersion(dataType).mask(column);
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

	private int getNameParam(ArrayList<DistributionParameter<?>> parameters) {
		int name = 0;
		for (int i = 0; i < parameters.size(); i++) {
			if (parameters.get(i).getName().equals("name")) {
				name = i;
			}
		}
		return name;
	}

	@Override
	public DataMaskingFunction clone() {
		return new NoiseAddition(super.isIgnoreMissingData(), var.getDistributionType());
	}

	@Override
	public OperatorDataType getImplementationVersion(DataType<?> type) {
		ArrayList<DistributionParameter<?>> parameters = helper.getParameters();
		helper.addAllParams(var, parameters);

		int name = getNameParam(parameters);

		DistributionParameter<?> param = parameters.get(name);

		if (type instanceof ARXInteger) {
			return new NoiseAdditionInteger((int) param.getMin(), allY(param, var.getDistribution()),
					super.isIgnoreMissingData());
		} else if (type instanceof ARXDecimal) {
			return new NoiseAdditionDecimal((int) param.getMin(), allY(param, var.getDistribution()),
					super.isIgnoreMissingData());
		} else if (type instanceof ARXDate) {
			return new NoiseAdditionDate((int) param.getMin(), allY(param, var.getDistribution()),
					super.isIgnoreMissingData());
		} else {
			throw new UnsupportedOperationException();
		}
	}
}