/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2016 Fabian Prasser, Florian Kohlmayer and contributors
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
package org.deidentifier.arx.risk.msu;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Set;

/**
 * The result of executing SUDA2
 * 
 * @author Fabian Prasser
 */
public class SUDA2Result {

    /** Num columns */
    private final int   columns;
    /** Maximal size of an MSU considered */
    private final int   maxK;
    /** The number of MSUs */
    private int         numMSUs = 0;
    /** Contributions of each column */
    private final int[] columnKeyContributions;
    /** Contributions of each column */
    private final int[] columnKeyMinSize;
    /** Contributions of each column */
    private final int[] columnKeyMaxSize;
    /** Contributions of each column */
    private final int[] columnKeyAverageSize;
    /** Distribution of sizes of MSUs */
    private final int[] sizeDistribution;
    
    /**
     * Creates a new instance
     * @param columns
     * @param maxK
     */
    SUDA2Result(int columns, int maxK) {
        this.columns = columns;
        this.columnKeyContributions = new int[columns];
        this.columnKeyMinSize = new int[columns];
        Arrays.fill(columnKeyMinSize, columns);
        this.columnKeyMaxSize = new int[columns];
        this.columnKeyAverageSize = new int[columns];
        this.maxK = maxK;
        this.sizeDistribution = new int[maxK];
    }
    
    /**
     * Returns the contributions of each column
     * @return
     */
    public int[] getColumnKeyContributions() {
        return this.columnKeyContributions;
    }

    /**
     * Returns the maximal size which has been searched for
     * @return
     */
    public int getMaxK() {
        return this.maxK;
    }
    
    /**
     * Returns the number of columns considered
     * @return
     */
    public int getNumColumns() {
        return this.columns;
    }
    
    /**
     * Returns the number of MSUs found
     * @return
     */
    public int getNumMSUs() {
        return this.numMSUs;
    }
    
    /**
     * Returns the distribution of the sizes of MSUs
     * @return
     */
    public int[] getKeySizeDistribution() {
        return this.sizeDistribution;
    }
    
    @Override
    public String toString() {
        
        int[] totals1 = new int[columns];
        Arrays.fill(totals1, numMSUs);
        
        int[] totals2 = new int[columns];
        for (int i = 0; i < totals2.length; i++) {
            totals2[i] = columnKeyContributions[i];
        }
        
        int[] totals3 = new int[maxK];
        Arrays.fill(totals3, numMSUs);
        
        StringBuilder builder = new StringBuilder();
        builder.append("SUDA2 result\n");
        builder.append(" - Number of columns: ").append(this.columns).append("\n");
        builder.append(" - Number of MSUs: ").append(this.numMSUs).append("\n");
        builder.append(" - Column key contributions\n");
        builder.append(toString("     ", columnKeyContributions, totals1, 0, false, true));
        builder.append(" - Column key min. size\n");
        builder.append(toString("     ", columnKeyMinSize, null, 0, true, false));
        builder.append(" - Column key max. size\n");
        builder.append(toString("     ", columnKeyMaxSize, null, 0, true, false));
        builder.append(" - Column key avg. size\n");
        builder.append(toString("     ", columnKeyAverageSize, totals2, 0, false, true));
        builder.append(" - Size distribution\n");
        builder.append(toString("     ", sizeDistribution, totals3, 1, true, true));
        return builder.toString();
    }
    
    /**
     * Makes sure that the value has the given number of characters
     * @param value
     * @param width
     * @return
     */
    private String toString(String value, int width) {
        while (value.length() < width) {
            value = " " + value;
        }
        return value;
    }
    
    /**
     * Renders a distribution
     * @param intent
     * @param array
     * @param offset
     * @param absolute
     * @return
     */
    private String toString(String intent, int[] array, int[] totals, int offset, boolean absolute, boolean relative) {
        
        StringBuilder builder = new StringBuilder();
        DecimalFormat integerFormat = new DecimalFormat("#######");
        DecimalFormat doubleFormat = new DecimalFormat("###.###");
        final int VALUE_WIDTH = 7;
        builder.append(intent).append("|");
        for (int index = 0; index < array.length; index++) {
            builder.append(toString(integerFormat.format(index + offset), VALUE_WIDTH)).append("|");
        }
        int width = builder.length() - intent.length();
        builder.append("\n");
        if (absolute) {
            builder.append(intent);
            for (int i = 0; i < width; i++) {
                builder.append("-");
            }
            builder.append("\n");
            builder.append(intent).append("|");
            for (int val : array) {
                String value = integerFormat.format(val);
                builder.append(toString(value, VALUE_WIDTH)).append("|");
            }
            builder.append("\n");
        }
        if (relative) {
            builder.append(intent);
            for (int i = 0; i < width; i++) {
                builder.append("-");
            }
            builder.append("\n");
            builder.append(intent).append("|");
            for (int i = 0; i < array.length; i++) {
                int _value = array[i];
                int _total = totals[i];
                String value = doubleFormat.format((double)_value / (double)_total).replace(',', '.');
                if (value.equals("0") && _value > 0) value = "~0";
                builder.append(toString(value, VALUE_WIDTH)).append("|");
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    /**
     * Registers an MSU
     * @param set
     */
    void registerMSU(Set<SUDA2Item> set) {
        this.numMSUs++;
        this.sizeDistribution[set.size() - 1]++;
        for (SUDA2Item item : set) {
            int column = item.getColumn();
            registerColumn(column, set.size());
        }
    }

    /**
     * Registers an MSU
     * @param item
     * @param set
     */
    void registerMSU(SUDA2Item item, SUDA2ItemSet set) {
        this.numMSUs++;
        this.sizeDistribution[set.size()]++;
        int size = set.size();
        for (int i = 0; i < size; i++) {
            int column = set.get(i).getColumn();
            registerColumn(column, set.size() + 1);
        }
        registerColumn(item.getColumn(), set.size() + 1);
    }

    /**
     * Registers an MSU
     * @param set
     */
    void registerMSU(SUDA2ItemSet set) {
        this.numMSUs++;
        this.sizeDistribution[set.size()-1]++;
        int size = set.size();
        for (int i = 0; i < size; i++) {
            int column = set.get(i).getColumn();
            registerColumn(column, set.size());
        }
    }

    /**
     * Registers an MSU with a column
     * @param column
     * @param size
     */
    private void registerColumn(int column, int size) {
        this.columnKeyContributions[column]++;
        this.columnKeyMinSize[column] = Math.min(this.columnKeyMinSize[column], size);
        this.columnKeyMaxSize[column] = Math.max(this.columnKeyMaxSize[column], size);
        this.columnKeyAverageSize[column] += size;
    }
}
