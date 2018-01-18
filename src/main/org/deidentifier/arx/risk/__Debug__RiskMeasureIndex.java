package org.deidentifier.arx.risk;
//package de.tum.med.imedis.kettle.trans.steps.anonymization.indexing;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.BitSet;
//import java.util.HashMap;
//import java.util.List;
//import java.util.stream.Collectors;
//
///** 
// * This class implements an index for a single column
// * of a dataset.
// * 
// * @author Helmut Spengler (helmut.spengler@tum.de)
// *
// */
//public class Index {
//	
//	/** The column that the index relates to */
//	private final String colName;
//
//	/** The data */
//	private final String[] data;
//	
//	/** number of null values in hash column */
//	private final long numNullValues;
//
//	/** Contains for each possible value of this column
//	 * all rows that contain the respective value. */
//	private final HashMap<String, List<Integer>> valueMapListInt;
//
//	/** Contains for each possible value of this column
//	 * all rows that contain the respective value. */
//	private final HashMap<String, BitSet> valueMapBitSet;
//
//	/**
//	 * Constructor.
//	 * 
//	 * @param data
//	 * @param colName
//	 */
//	public Index(String[] data, String colName) {
//		this.data = data;
//		this.colName = colName;
//		
//		this.numNullValues = Arrays.stream(this.data).filter(v -> v == null).count();
//		List<String> distinctValues = Arrays.stream(this.data).distinct().collect(Collectors.toList());
//
//		valueMapListInt = new HashMap<>();
//		valueMapBitSet = new HashMap<>();
//		for (String val : distinctValues) {
//			valueMapListInt.put(val, new ArrayList<>());
//			valueMapBitSet.put(val, new BitSet(data.length));
//		}
//	}
//
//	/**
//	 * Sorts the row index of a row into the valueMap.
//	 * @param row
//	 * @param rowNum
//	 */
//	public void updateIndex(String hashKey, int rowNum) {
//		
//		if (null == hashKey) {
//			for (String attrVal : valueMapListInt.keySet()) {
//				valueMapListInt.get(attrVal).add(rowNum);
//				valueMapBitSet.get(attrVal).set(rowNum);
//			}
//			return;
//		}
//
//		valueMapListInt.get(hashKey).add(rowNum);
//		valueMapBitSet.get(hashKey).set(rowNum);
//		
//		if (this.numNullValues > 0) {
//			valueMapListInt.get(null).add(rowNum);
//			valueMapBitSet.get(null).set(rowNum);
//		}
//	}
//
//	/** Get the row indices matching a certain value in the
//	 * hash column.
//	 * @param value
//	 * @return
//	 */
//	public List<Integer> get(String value) {
//		
//		
//		return valueMapListInt.get(value);
//		
//	}
//	/** Get the row indices matching a certain value in the
//	 * hash column.
//	 * @param value
//	 * @return
//	 */
//	public BitSet getBitSet(String value) {
//		
//		return valueMapBitSet.get(value);
//		
//	}
//
//	public String getColName() {
//		return colName;
//	}
//}
