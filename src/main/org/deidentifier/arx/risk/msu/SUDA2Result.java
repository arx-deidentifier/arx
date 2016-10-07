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
    private final int[] columnContributions;
    /** Distribution of sizes of MSUs */
    private final int[] sizeDistribution;
    
    /**
     * Creates a new instance
     * @param columns
     * @param maxK
     */
    SUDA2Result(int columns, int maxK) {
        this.columns = columns;
        this.columnContributions = new int[columns];
        this.maxK = maxK;
        this.sizeDistribution = new int[maxK];
    }
    
    /**
     * Returns the contributions of each column
     * @return
     */
    public int[] getColumnContributions() {
        return this.columnContributions;
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
    public int[] getSizeDistribution() {
        return this.sizeDistribution;
    }
    
    /**
     * Registers an MSU
     * @param set
     */
    void registerMSU(Set<SUDA2Item> set) {
        this.numMSUs++;
        this.sizeDistribution[set.size()]++;
        for(SUDA2Item item : set) {
            columnContributions[item.getColumn()]++;
        }
    }
    
    /**
     * Registers an MSU
     * @param set
     */
    void registerMSU(SUDA2ItemSet set) {
        this.numMSUs++;
        this.sizeDistribution[set.getItems().size()-1]++;
        for(SUDA2Item item : set.getItems()) {
            columnContributions[item.getColumn()]++;
        }
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SUDA2 result\n");
        builder.append(" - Number of columns: ").append(this.columns).append("\n");
        builder.append(" - Number of MSUs: ").append(this.numMSUs).append("\n");
        builder.append(" - Column contributions\n");
        builder.append(toString("     ", columnContributions, 0));
        builder.append(" - Size distribution\n");
        builder.append(toString("     ", sizeDistribution, 1));
        return builder.toString();
    }

    /**
     * Renders a distribution
     * @param intent
     * @param array
     * @param offset
     * @return
     */
    private String toString(String intent, int[] array, int offset) {
        
        StringBuilder builder = new StringBuilder();
        DecimalFormat integerFormat = new DecimalFormat("#######");
        DecimalFormat doubleFormat = new DecimalFormat("###.###");
        final int VALUE_WIDTH = 7;
        builder.append(intent).append("|");
        for (int index = 0; index < array.length; index++) {
            builder.append(toString(integerFormat.format(index + offset), VALUE_WIDTH)).append("|");
        }
        builder.append("\n").append(intent);
        int width = builder.length();
        for (int i = 0; i < width; i++) {
            builder.append("-");
        }
        builder.append("\n").append(intent).append("|");
        for (int val : array) {
            String value = integerFormat.format(val);
            builder.append(toString(value, VALUE_WIDTH)).append("|");
        }
        builder.append("\n").append(intent);
        for (int i = 0; i < width; i++) {
            builder.append("-");
        }
        builder.append("\n").append(intent).append("|");
        for (double val : array) {
            String value = doubleFormat.format(val / numMSUs).replace(',', '.');
            if (value.equals("0") && val > 0) value = "~0";
            builder.append(toString(value, VALUE_WIDTH)).append("|");
        }
        builder.append("\n");
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
}
