package org.deidentifier.arx.risk;
//package de.tum.med.imedis.kettle.trans.steps.anonymization.indexing;
//
//import java.util.Arrays;
//import java.util.BitSet;
//import java.util.List;
//import java.util.function.BiFunction;
//import java.util.stream.Collectors;
//
//import org.deidentifier.arx.Data;
//import org.deidentifier.arx.DataHandleInput;
//import org.deidentifier.arx.DataType;
//import org.deidentifier.arx.framework.data.DataMatrix;
//
//
///** 
// * This class implements a partitioning of a dataset
// * based on the attributes of a certain column. The
// * column with the highest number of different values
// * is automatically picked.
// * 
// * @author Helmut Spengler (helmut.spengler@tum.de)
// *
// */
///**
// * @author work
// *
// */
//public class DataAnalyzer {
//	
//	public enum Mode {
//		WC,
//		OC,
//		WC_COMP,
//		OC_COMP,
//	}
//
//	/** TODO change to List<String[]> */
//	/** TODO change to List<String[]> */
//	/** The data */
//	private String[][] data;
//	
//	/** The ARX handle to the data */
//	private DataHandleInput handle;
//	
//	/** The compressed data */
//	DataMatrix compData;
//
//	/** Array of all indices */
//	private final Index[] indices;
//
//	/** The number of columns */
//	private final int numCols;
//	
//	/** The integer placeholders for the NULL value in the DataMatrix */
//	private int[] nullInts;
//
//	/** The number of rows */
//	private final int numDataRows;
//
//	/** The frequencies */
//	private int[] frequencies;
//
//	/** The optimization grade. 100% means perfect
//	 * optimization due to use of indices.
//	 */
//	private double statOptGrade = 0.0d;
//
//	/** Constructor.
//	 * @param data First row is expected to contain the headers. */
//	private DataAnalyzer(String[][] data) {
//
//		handle = (DataHandleInput) Data.create(data).getHandle();
//		compData = handle.getDataMatrix();
//		nullInts = getNullInts(handle);
//		
//		String[] header = data[0];
//		this.data = Arrays.copyOfRange(data, 1, data.length);
//		this.numDataRows = this.data.length;
//		String[][] dataTransp = transpose(this.data);
//
//		this.numCols = dataTransp.length;
//		indices = new Index[this.numCols];
//		for (int i = 0; i < this.numCols; i++) {
//			indices[i] = new Index(dataTransp[i], header[i]);
//		}
//	}
//	/** Constructor.
//	 * @param data First row is expected to contain the headers. */
//	public DataAnalyzer(String[][] data, Mode mode) {
//
//		this(data);
//		
//		switch (mode) {
//		case OC:
//			frequencies = countFrequencies(matchWithoutWildcards);
//			break;
//		case OC_COMP:
//			frequencies = countFrequencies(matchWithoutWildcardsCompressed);
//			break;
//		case WC:
//			frequencies = countFrequencies(matchWithWildcards);
//			break;
//		case WC_COMP:
//			frequencies = countFrequencies(matchWithWildcardsCompressed);
//			break;
//		default:
//			break;
//		
//		}
//	}
//	
//	public DataAnalyzer(List<String[]> data, Mode mode) {		
//		this(convertToArray(data), mode);
//	}
//
//	/** Get the highest risk.
//	 * @return */
//	public double getHighestRisk() {
//		double min = Double.MAX_VALUE;
//		for (int i = 0; i < frequencies.length; i++) {
//			min = Math.min(min, frequencies[i]);
//		}
//		return 1/min;
//	}
//
//	/** Get the records at risk.
//	 * @return */
//	public double getRecsAtRisk(double theta) {
//		double sum = 0.0d;	
//		for (int i = 0; i < frequencies.length; i++) {
//			if (1d/frequencies[i] > theta) sum++;
//		}
//		return sum / frequencies.length;
//
//	}
//
//	/** Get the average risk.
//	 * @return */
//	public double getAvgRisk() {
//		double sum = 0.0d;	
//		for (int i = 0; i < frequencies.length; i++) {
//			sum +=  1.0d / (frequencies[i]);
//		}
//		return sum / frequencies.length;
//
//	}
//
//
//
//	/**
//	 * Count the frequencies for all rows. 
//	 * Package private for testing reasons.
//	 * @param data
//	 * @param matcher matching algorithm
//	 * @return [minFreq, AvgFreq]
//	 */
//	private int[] countFrequencies(BiFunction<Integer, Integer, Boolean> matcher) {
//
//		int[]      frequencies = new int[numDataRows];
//
//		for (int i = 0; i < numDataRows; i++) {
//			frequencies[i] = 1;
//
//			List<Integer> relevantRows = get(this.data[i]);
//			statOptGrade += (relevantRows.size() + 1d) / (i + 1d);
//			for (int j : relevantRows) {
//				if (matcher.apply(i, j)) {
//					frequencies[i]++;
//					frequencies[j]++;
//				}
//			}
//			updateIndices(data[i], i);
//		}
//		statOptGrade /= (numDataRows);
//		statOptGrade = 1d - statOptGrade;
//
//		return frequencies;
//	}
//
//	/**
//	 * Sorts the row index of a row into the valueMap.
//	 * @param row
//	 * @param rowNumber
//	 */
//	private void updateIndices(String[] row, int rowNumber) {
//		for (int i = 0; i < this.numCols; i++) {
//			indices[i].updateIndex(row[i], rowNumber);
//		}
//	}
//
//	/**
//	 * Get the row indices matching certain values in the
//	 * hash column.
//	 * 
//	 * @param row contains the values to match against
//	 * @return
//	 */
//	private List<Integer> get(String[] row) {
//		BitSet bs = (BitSet)indices[0].getBitSet(row[0]).clone();
//		for (int i = 0; i < indices.length; i++) {
//			bs.and(indices[i].getBitSet(row[i]));
//		}
//		return bs.stream().boxed().collect(Collectors.toList());
//	}
//
//
//	/** Match two records without wild cards
//	 * Intentionally package private for testing reasons.
//	 */
//	private BiFunction<Integer, Integer, Boolean> matchWithoutWildcardsCompressed = (i, j) -> {
//
//		DataMatrix compData = handle.getDataMatrix();
//		for (int col = 0; col < handle.getNumColumns(); col++) {
//			if (compData.get(i, col) != compData.get(j, col)) return false;
//		}
//		
//		return true;
//	};
//
//
//	/** Match two records without wild cards
//	 * Intentionally package private for testing reasons.
//	 */
//	private BiFunction<Integer, Integer, Boolean> matchWithoutWildcards = (i, j) -> {
//
//			for (int col = 0; col < data[i].length; col++ ) {
//				if (data[i][col] == null) {
//					if (data[j][col] != null) {
//						return false;
//					}
//				} else {
//					if (!data[i][col].equals(data[j][col])) {
//						return false;
//					}
//				}
//			}
//		
//		return true;
//	};
//
//
//	/** Match two records with wildcards.
//	 * Intentionally package private for testing reasons.
//	 */
//	private BiFunction<Integer, Integer, Boolean> matchWithWildcardsCompressed = (i, j) -> {
//
//		for (int col = 0; col < handle.getNumColumns(); col++) {
//			int nullInt = nullInts[col];
//			if (compData.get(i, col) != nullInt && compData.get(j, col) != nullInt) {
//				if (compData.get(i, col) != compData.get(j, col)) {
//					return false;
//				}
//			}
//				
//		}
//		return true;
//	};
//
//
//	/** Match two records with wildcards.
//	 * Intentionally package private for testing reasons.
//	 */
//	private BiFunction<Integer, Integer, Boolean> matchWithWildcards = (i, j) -> {
//
//			for (int col = 0; col < data[i].length; col++ ) {
//				if (data[i][col] != null && data[j][col] != null && !data[i][col].equals(data[j][col])) {
//					return false; 
//				}
//			}
//
//		return true;
//	};
//
//	/** Get the absolute frequencies based on the matching algorithm supplied at initialization time.
//	 * @return
//	 */
//	public int[] getFrequencies() {
//		return frequencies;
//	}
//
//	/** Get the opimization grade. 100% means perfect
//	 * optimization due to use of indices.
//	 * @return
//	 */
//	public double getStatOptGrade() {
//		return statOptGrade;
//	}
//
//	private static int[] getNullInts(DataHandleInput handle) {
//		
//		int[] ret = new int[handle.getNumColumns()];
//		
//		for (int i = 0; i < handle.getNumColumns(); i++) {
//			String [] mapping = handle.getDictionary().getMapping()[i];
//			ret[i] = -1;
//			for (int j = 0; j < mapping.length; j++) {
//				if (DataType.NULL_VALUE.equals(mapping[j])) {
//					ret[i] = j;
//					break;
//				}
//			}
//		}
//		return ret;
//	}
//	private static String[][] convertToArray(List<String[]> data) {
//		String[][] ret = new String[data.size()][];
//		
//		for (int i = 0; i < data.size(); i++) {
//			ret[i] = data.get(i);
//		}
//		return ret;
//	}
//	/**
//	 * Transpose a matrix.
//	 * 
//	 * @param data
//	 * @return
//	 */
//	private String[][] transpose(String[][] data) {
//		int m = data.length;
//		int n = data[0].length;
//	
//		String[][] dataTransp = new String[n][m];
//	
//		for(int x = 0; x < n; x++)
//		{
//			for(int y = 0; y < m; y++)
//			{
//				dataTransp[x][y] = data[y][x];
//			}
//		}
//	
//		return dataTransp;
//	}
//
//}
