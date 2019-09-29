/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.deidentifier.arx.masking;

import java.io.Serializable;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import org.apache.commons.lang.RandomStringUtils;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.framework.data.DataColumn;
import org.deidentifier.arx.masking.variable.Distribution;
import org.deidentifier.arx.masking.variable.DistributionParameter;
import org.deidentifier.arx.masking.variable.DistributionType;
import org.deidentifier.arx.masking.variable.RandomVariable;

/**
 * This class implements data masking functions
 * 
 * @author Fabian Prasser
 */
public abstract class DataMaskingFunction implements Serializable {

	/**
	 * Generates a random alphanumeric string
	 * 
	 * @author Fabian Prasser
	 */
	public static class DataMaskingFunctionRandomAlphanumericString extends DataMaskingFunction {

		/** SVUID */
		private static final long serialVersionUID = 918401877743413029L;

		/** Characters */
		private static final char[] CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

		/** Length */
		private final int length;

		/**
		 * Creates a new instance
		 * 
		 * @param ignoreMissingData
		 * @param length
		 */
		public DataMaskingFunctionRandomAlphanumericString(boolean ignoreMissingData, int length) {
			super(ignoreMissingData, false);
			this.length = length;
		}

		@Override
		public void apply(DataColumn column) {

			// Prepare
			Random random = new SecureRandom();
			char[] buffer = new char[length];

			// Mask
			for (int row = 0; row < column.getNumRows(); row++) {

				// Leave null as is, if configured to not ignore missing data
				if (super.isIgnoreMissingData() || !column.get(row).equals(DataType.NULL_VALUE)) {
					column.set(row, getRandomAlphanumericString(buffer, random));
				}
			}
		}

		@Override
		public DataMaskingFunction clone() {
			return new DataMaskingFunctionRandomAlphanumericString(super.isIgnoreMissingData(), length);
		}

		/**
		 * Creates a random string
		 * 
		 * @param random
		 * @param buffer
		 * @return
		 */
		private String getRandomAlphanumericString(char[] buffer, Random random) {
			for (int i = 0; i < buffer.length; i++) {
				buffer[i] = CHARACTERS[random.nextInt(CHARACTERS.length)];
			}
			return new String(buffer);
		}
	}

	/**
	 * Generates a random permutation column's rows
	 * 
	 * @author giupardeb
	 * 
	 */

	public static class PermutationFunctionColumns extends DataMaskingFunction {

		/** SVUID */
		private static final long serialVersionUID = 1470074649699937850L;

		private final PermutationType typePermutation;

		/**
		 * create a type enum to define permutation type
		 * 
		 * @author giupardeb
		 *
		 */
		public enum PermutationType {
			FYKY, RS
		}

		/**
		 * Creates a new instance
		 * 
		 * @param ignoreMissingData
		 * @param typePermutation
		 */
		public PermutationFunctionColumns(boolean ignoreMissingData, PermutationType typePermutation) {
			super(ignoreMissingData, false);
			this.typePermutation = typePermutation;
		}

		@Override
		public void apply(DataColumn column) {
			switch (typePermutation) {
			case FYKY:
				fisherYatesKnuthYao(column);
				break;
			case RS:
				raoSandelius(column);
				break;
			}
		}

		/**
		 * Implementation of Rao-Sandelius algorithm is based on the paper: Axel
		 * Bacher, Olivier Bodini, Hsien-Kuei Hwang, and Tsung-Hsi Tsai.
		 * Generating random permutations by coin-tossing: classical algorithms,
		 * new analysis and modern implementation
		 * 
		 * @param column
		 */
		private void raoSandelius(DataColumn column) {

			int lengthColumn = column.getNumRows();
			ArrayList<String> col = new ArrayList<String>();
			ArrayList<String> colout = new ArrayList<String>();

			for (int i = 0; i < lengthColumn; i++)
				col.add(column.get(i));

			colout = rs(lengthColumn, col);

			for (int i = 0; i < lengthColumn; i++)
				column.set(i, colout.get(i));
		}

		/**
		 * Implementation of the core Rao Sandelius function
		 * 
		 * @param lengthColumn
		 * @param column
		 * @return permuted ArrayList
		 */
		private ArrayList<String> rs(int lengthColumn, ArrayList<String> column) {

			Random rand = new SecureRandom();
			if (lengthColumn <= 1)
				return column;

			if (lengthColumn == 2) {
				if (rand.nextInt(2) == 1) {
					swap(column, 0, 1);
					return column;
				} else
					return column;
			}

			ArrayList<String> tmp1 = new ArrayList<String>();
			ArrayList<String> tmp2 = new ArrayList<String>();

			for (int i = 0; i < lengthColumn; i++) {

				if (rand.nextInt(2) == 1)
					tmp1.add(column.get(i));
				else
					tmp2.add(column.get(i));
			}

			ArrayList<String> array1 = rs(tmp1.size(), tmp1);
			ArrayList<String> array2 = rs(tmp2.size(), tmp2);
			array1.addAll(array2);

			return array1;
		}

		@Override
		public DataMaskingFunction clone() {
			return new PermutationFunctionColumns(super.isIgnoreMissingData(), typePermutation);
		}

		/**
		 * Implementation of Fisher-Yates Knuth-Yao algorithm is based on the
		 * paper: Axel Bacher, Olivier Bodini, Hsien-Kuei Hwang, and Tsung-Hsi
		 * Tsai. Generating random permutations by coin-tossing: classical
		 * algorithms, new analysis and modern implementation
		 * 
		 * @param column
		 */
		private void fisherYatesKnuthYao(DataColumn column) {

			int j = 0;
			int lengthColumn = column.getNumRows() - 1;

			for (int i = lengthColumn; i >= 2; i--) {
				j = knuthYao(i) + 1;
				swap(column, i, j);
			}
		}

		/**
		 * Implementation knuth Yao function
		 * 
		 * @param n
		 * @return
		 */
		private int knuthYao(int n) {

			Random rand = new SecureRandom();
			// 0 to 1 inclusive.
			int randBit = 0;

			int u = 1;
			int x = 0;
			int d = 0;

			while (true) {
				while (u < n) {
					randBit = rand.nextInt(2);
					u = 2 * u;
					x = 2 * x + randBit;
				}
				d = u - n;
				if (x >= d)
					return x - d;
				else
					u = d;
			}
		}

		/**
		 * swapping rows
		 * 
		 * @param column
		 * @param i
		 * @param j
		 */
		private void swap(DataColumn column, int i, int j) {
			String tmp = column.get(i);
			column.set(i, column.get(j));
			column.set(j, tmp);
		}

		/**
		 * swapping rows
		 * 
		 * @param column
		 * @param i
		 * @param j
		 */
		private void swap(ArrayList<String> column, int i, int j) {
			String tmp = column.get(i);
			column.set(i, column.get(j));
			column.set(j, tmp);
		}

	}

	public static class NoiseAddition extends DataMaskingFunction {
		private RandomVariable var;
		private DistributionMasking helper;

		/**
		 * Creates a new instance.
		 * 
		 * @param ignoreMissingData
		 * @param type
		 * @param handle
		 */
		public NoiseAddition(boolean ignoreMissingData, DistributionType type, DataHandle handle) {
			super(ignoreMissingData, false);
			var = new RandomVariable("XXX", type);
			helper = new DistributionMasking(type, handle);
		}

		@Override
		public void apply(DataColumn column) {
			String dataType = helper.getDataType(column);
			ArrayList<DistributionParameter<?>> parameters = helper.getParameters();
			helper.addAllParams(var, parameters);

			int name = helper.getNameParam(parameters);
			int[] allX = helper.allX(parameters.get(name));
			double[] allY = helper.allY(var.getDistribution(), allX);

			switch (dataType) {
			case "Decimal":
			case "Integer":
				addNoise(column, allX, allY, dataType);
				break;
			case "Date":
				addDateNoise(column, allX, allY, dataType);
			default:
				System.out.println("Datatype not fit for noise addition.");
				break;
			}
		}

		/**
		 * Adds noise to the column given the distribution and the datatype.
		 * 
		 * @param column
		 * @param allX
		 * @param allY
		 * @param dataType
		 */
		private void addNoise(DataColumn column, int[] allX, double[] allY, String dataType) {
			for (int row = 0; row < column.getNumRows(); row++) {
				int noise = helper.numberFromDistribution(allX, allY);
				if (super.isIgnoreMissingData() || !column.get(row).equals(DataType.NULL_VALUE)) {
					if (dataType.equals("Integer")) {
						column.set(row, "" + (Integer.valueOf(column.get(row)) + noise));
					} else {
						column.set(row, "" + (Double.valueOf(column.get(row)) + noise));
					}
				}
			}
		}

		private void addDateNoise(DataColumn column, int[] allX, double[] allY, String dataType) {
			for (int row = 0; row < column.getNumRows(); row++) {
				int noise = helper.numberFromDistribution(allX, allY);
				if (super.isIgnoreMissingData() || !column.get(row).equals(DataType.NULL_VALUE)) {
					String value = column.get(row);
					SimpleDateFormat format = helper.getDateFormat(value);
					try {
						Date date = format.parse(value);
						long ms = date.getTime();
						ms += noise * 1000L * 60L * 60L * 24L;
						date.setTime(ms);
						column.set(row, format.format(date));
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			}
		}

		@Override
		public DataMaskingFunction clone() {
			return this;
		}
	}

	public static class RandomGen extends DataMaskingFunction {
		private RandomVariable var;
		private DistributionMasking helper;

		/**
		 * Creates a new instance.
		 * 
		 * @param ignoreMissingData
		 * @param type
		 * @param handle
		 */
		public RandomGen(boolean ignoreMissingData, DistributionType type, DataHandle handle) {
			super(ignoreMissingData, false);
			var = new RandomVariable("XXX", type);
			helper = new DistributionMasking(type, handle);
		}

		@Override
		public void apply(DataColumn column) {
			String dataType = helper.getDataType(column);
			ArrayList<DistributionParameter<?>> parameters = helper.getParameters();

			helper.addAllParams(var, parameters);
			int name = helper.getNameParam(parameters);
			int[] allX = helper.allX(parameters.get(name));
			double[] allY = helper.allY(var.getDistribution(), allX);

			switch (dataType) {
			case "Integer":
			case "Decimal":
			case "Ordinal":
				generate(allX, allY, null, column);
				break;
			case "String":
				String[] strings = helper.generateStrings(8, 16, allX.length);
				generate(allX, allY, strings, column);
			case "Date":
				if (nonNullRow(column) < 0)
					break;
				long earliest = new GregorianCalendar(1900, 1, 1).getTime().getTime() / (1000L * 60L * 60L * 24L);
				long latest = new GregorianCalendar(2050, 1, 1).getTime().getTime() / (1000L * 60L * 60L * 24L);
				String value = column.get(nonNullRow(column));
				SimpleDateFormat format = helper.getDateFormat(value);
				String[] dates = helper.generateDates(earliest, latest, allX.length, format);
				generate(allX, allY, dates, column);
			default:
				break;
			}
		}

		private int nonNullRow(DataColumn column) {
			for (int row = 0; row < column.getNumRows(); row++) {
				if (!column.get(row).equals(DataType.NULL_VALUE)) {
					return row;
				}
			}
			return -1;
		}

		/**
		 * Replaces the values in the column with a randomly generated value.
		 * For columns of type integer, double, and ordinal, strings must be
		 * null.
		 * 
		 * @param allX
		 * @param allY
		 * @param strings
		 * @param column
		 */
		private void generate(int[] allX, double[] allY, String[] strings, DataColumn column) {
			for (int row = 0; row < column.getNumRows(); row++) {
				String replace = "";
				if (strings == null) {
					replace = "" + helper.numberFromDistribution(allX, allY);
				} else {
					replace = helper.fromDistribution(allX, allY, strings);
				}
				if (super.isIgnoreMissingData() || !column.get(row).equals(DataType.NULL_VALUE)) {
					column.set(row, replace);
				}
			}
		}

		@Override
		public DataMaskingFunction clone() {
			return this;
		}
	}

	public static class DistributionMasking {
		private DistributionType type;
		private DataHandle handle;
		private RandomVariable var;

		public DistributionMasking(DistributionType type, DataHandle handle) {
			this.type = type;
			this.handle = handle;
			var = new RandomVariable("XXX", type);
		}

		public String getDataType(DataColumn column) {
			return handle.getDataType(column.getAttribute()).toString();
		}

		public ArrayList<DistributionParameter<?>> getParameters() {
			ArrayList<DistributionParameter<?>> parameters = (ArrayList<DistributionParameter<?>>) type.getDescription()
					.getParameters();
			for (int i = 0; i < parameters.size(); i++) {
				var.addParameter(parameters.get(i));
			}
			return parameters;
		}

		public void addAllParams(RandomVariable var, ArrayList<DistributionParameter<?>> parameters) {
			for (DistributionParameter p : parameters) {
				var.addParameter(p);
			}

		}

		public int getNameParam(ArrayList<DistributionParameter<?>> parameters) {
			int name = 0;
			for (int i = 0; i < parameters.size(); i++) {
				if (parameters.get(i).getName().equals("name")) {
					name = i;
				}
			}
			return name;
		}

		/**
		 * All x-values of the distribution.
		 * 
		 * @param parameter
		 * @return
		 */
		private int[] allX(DistributionParameter<?> parameter) {
			int[] allX = new int[(int) parameter.getMax() - (int) parameter.getMin() + 1];

			for (int i = 0; i < allX.length; i++) {
				allX[i] = (int) parameter.getMin() + i;
			}
			return allX;
		}

		/**
		 * All y-values of the distribution.
		 * 
		 * @param dist
		 * @param allX
		 * @return
		 */
		private double[] allY(Distribution<Integer> dist, int[] allX) {
			double[] allY = new double[allX.length];

			for (int i = 0; i < allX.length; i++) {
				allY[i] = dist.getValue(allX[i]);
			}
			return allY;
		}

		/**
		 * Chooses a random number given the distribution.
		 * 
		 * @param allX
		 * @param allY
		 * @return
		 */
		private int numberFromDistribution(int[] allX, double[] allY) {
			int[] ranges = new int[allX.length];
			HashMap<Integer, Integer> rangeToX = new HashMap<Integer, Integer>();
			int sum = 0;

			for (int i = 0; i < allX.length; i++) {
				sum += 1000 * Math.round((float) allY[i]);
				ranges[i] = sum;
				rangeToX.put(sum, allX[i]);
			}

			int randomNumber = new Random().nextInt(ranges[ranges.length - 1] + 1);
			int out = allX[0];

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
		private String fromDistribution(int[] allX, double[] allY, Object[] values) {
			int len = allX.length;
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

		/**
		 * Checks a string-array for duplicates.
		 * 
		 * @param arr
		 * @return
		 */
		private boolean checkForDuplicates(String[] arr) {
			HashSet<String> set = new HashSet<String>();

			for (int i = 0; i < arr.length; i++) {
				if (!set.add(arr[i]))
					return true;
			}
			return false;
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

	/** SVUID */
	private static final long serialVersionUID = -5605460206017591293L;

	/** Ignore missing data */
	private final boolean ignoreMissingData;

	/** Preserves data types */
	private final boolean typePreserving;

	/**
	 * Creates a new instance
	 * 
	 * @param ignoreMissingData
	 * @param typePreserving
	 */
	private DataMaskingFunction(boolean ignoreMissingData, boolean typePreserving) {
		this.ignoreMissingData = ignoreMissingData;
		this.typePreserving = typePreserving;
	}

	/**
	 * Applies the function to the given column
	 * 
	 * @param column
	 */
	public abstract void apply(DataColumn column);

	/** Clone */
	@Override
	public abstract DataMaskingFunction clone();

	/**
	 * Returns whether the function ignores missing data
	 * 
	 * @return
	 */
	public boolean isIgnoreMissingData() {
		return this.ignoreMissingData;
	}

	/**
	 * Returns whether the function is type preserving
	 * 
	 * @return
	 */
	public boolean isTypePreserving() {
		return this.typePreserving;
	}
}
