package org.deidentifier.arx.examples;

import org.deidentifier.arx.framework.data.DataColumn;
import org.deidentifier.arx.framework.data.DataMatrix;
import org.deidentifier.arx.framework.data.Dictionary;
import org.deidentifier.arx.masking.DataMaskingFunction.PermutationFunctionColumns;

public class Example59 {
	
	public static void main (String[] args) {
		int rows = 1000;
		int columns = 2;
		
		DataMatrix data = new DataMatrix(rows, columns);
		String[] header = new String[]{"column1","column2"};
		int column = 0;
		Dictionary dictionary = new Dictionary(rows);

		int[] tuple = new int[columns];
		
		for (int i=0;i<rows;i++) {
			
			for(int j=0;j<columns;j++) {
				tuple[j] = dictionary.register(j, Integer.toString(j+1) + "_ColumnStringTest_" + Integer.toString(i+1)+"row");
			}
			data.setRow(i, tuple);
		}
		
		
		dictionary.finalizeAll();
		DataColumn c = new DataColumn(data, header, column, dictionary);
		
		PermutationFunctionColumns pfc = new PermutationFunctionColumns(true);
		for (int i=0; i<c.getNumRows();i++) {
			System.out.println(c.get(i));
		}
		System.out.println();
		pfc.apply(c);
		
		for (int i=0; i<c.getNumRows();i++) {
			System.out.println(c.get(i));
		}
	}
}
