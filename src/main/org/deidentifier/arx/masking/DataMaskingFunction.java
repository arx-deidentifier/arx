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
import java.util.Random;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.framework.data.DataColumn;

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
        private static final long   serialVersionUID = 918401877743413029L;

        /** Characters */
        private static final char[] CHARACTERS       = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

        /** Length */
        private final int           length;
        
        /**
         * Creates a new instance
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
    
    public static class PermutationFunctionColumns extends DataMaskingFunction {
    	
        /** SVUID */
		private static final long serialVersionUID = 1470074649699937850L;
    	
    	public PermutationFunctionColumns (boolean ignoreMissingData) {
            super(ignoreMissingData, false);
        }

		@Override
		public void apply(DataColumn column) {
			
			FisherYatesKnuthYao(column);
			
		}

		@Override
		public DataMaskingFunction clone() {
			// TODO Auto-generated method stub
			return null;
		}
		
		
		private void FisherYatesKnuthYao(DataColumn column) {
			
			int j = 0;
			int lengthColumn = column.getNumRows()-1;
			
			for(int i = lengthColumn; i>=2; i--) {
				j = KnuthYao(i)+1;
				swap(column, i, j);
			}
		}
		
		private int KnuthYao(int n) {
			
			Random rand = new SecureRandom();
			// 0 to 1 inclusive.
			int randBit = 0;
			
			int u = 1;
			int x = 0;
			int d = 0;
			
			while (true) {
				while (u < n) {
					randBit = rand.nextInt(2);
					u = 2*u;
					x = 2*x + randBit;
				}
				d = u-n;
				if (x >= d) return x - d;
				else u = d;
			}
		}
		
		private void swap(DataColumn column, int i, int j) {
			String tmp = column.get(i);
			column.set(i, column.get(j));
			column.set(j, tmp);
		}
    	
    }

    /** SVUID */
    private static final long serialVersionUID = -5605460206017591293L;

    /** Ignore missing data */
    private final boolean     ignoreMissingData;

    /** Preserves data types */
    private final boolean     typePreserving;

    /**
     * Creates a new instance
     * @param ignoreMissingData
     * @param typePreserving
     */
    private DataMaskingFunction(boolean ignoreMissingData, boolean typePreserving) {
        this.ignoreMissingData = ignoreMissingData;
        this.typePreserving = typePreserving;
    }
    
    /**
     * Applies the function to the given column
     * @param column
     */
    public abstract void apply(DataColumn column);

    /** Clone*/
    public abstract DataMaskingFunction clone();

    /**
     * Returns whether the function ignores missing data
     * @return
     */
    public boolean isIgnoreMissingData() {
        return this.ignoreMissingData;
    }

    /**
     * Returns whether the function is type preserving
     * @return
     */
    public boolean isTypePreserving() {
        return this.typePreserving;
    }
}
