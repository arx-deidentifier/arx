package org.deidentifier.arx.masking.functions;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Random;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.framework.data.DataColumn;
import org.deidentifier.arx.masking.DataMaskingFunction;

/**
 * Generates a random permutation column's rows
 * 
 * @author giupardeb
 * 
 */

public class PermutationFunctionColumns extends DataMaskingFunction {

	public static class FYKY implements OperatorDataType {

		/**
		 * Implementation of Fisher-Yates Knuth-Yao algorithm is based on the
		 * paper: Axel Bacher, Olivier Bodini, Hsien-Kuei Hwang, and Tsung-Hsi
		 * Tsai. Generating random permutations by coin-tossing: classical
		 * algorithms, new analysis and modern implementation
		 * 
		 * @param column
		 */
		@Override
		public void mask(DataColumn column) {
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
	}

	public static class RS implements OperatorDataType {

		/**
		 * Implementation of Rao-Sandelius algorithm is based on the paper: Axel
		 * Bacher, Olivier Bodini, Hsien-Kuei Hwang, and Tsung-Hsi Tsai.
		 * Generating random permutations by coin-tossing: classical algorithms,
		 * new analysis and modern implementation
		 * 
		 * @param column
		 */
		@Override
		public void mask(DataColumn column) {
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
	public void apply(DataColumn column, DataType<?> dataType) {
		getImplementationVersion(dataType).mask(column);
	}

	@Override
	public DataMaskingFunction clone() {
		return new PermutationFunctionColumns(super.isIgnoreMissingData(), typePermutation);
	}

	@Override
	public OperatorDataType getImplementationVersion(DataType<?> type) {
		switch (typePermutation) {
		case FYKY:
			return new FYKY();
		case RS:
			return new RS();
		default:
			throw new UnsupportedOperationException();
		}
	}
}