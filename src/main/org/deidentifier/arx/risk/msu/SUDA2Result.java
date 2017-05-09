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

    /** Num. columns */
    private final int        rows;
    /** Num. columns */
    private final int        columns;
    /** Maximal size of an MSU considered */
    private final int        maxK;
    /** The number of MSUs */
    private int              numMSUs   = 0;
    /** Contributions of each column */
    private final int[]      columnKeyContributions;
    /** Contributions of each column */
    private final int[]      columnKeyMinSize;
    /** Contributions of each column */
    private final int[]      columnKeyMaxSize;
    /** Contributions of each column */
    private final int[]      columnKeyAverageSize;
    /** Distribution of sizes of MSUs */
    private final int[]      sizeDistribution;
    /** Risk distribution */
    private final double[]   riskDistributionRow;
    /** Risk distribution */
    private final double[]   riskDistributionColumn;
    /** Risk distribution */
    private double           riskTotal = 0d;
    /** Contribution matrix */
    private final double[][] contributionPercent;
    
    /**
     * Creates a new instance
     * @param columns
     * @param maxK
     */
    SUDA2Result(int rows, int columns, int maxK) {
        this.columns = columns;
        this.rows = rows;
        this.riskDistributionRow = new double[this.rows];
        this.riskDistributionColumn = new double[this.columns];
        this.columnKeyContributions = new int[columns];
        this.columnKeyMinSize = new int[columns];
        Arrays.fill(columnKeyMinSize, columns);
        this.columnKeyMaxSize = new int[columns];
        this.columnKeyAverageSize = new int[columns];
        this.maxK = maxK;
        this.sizeDistribution = new int[maxK];
        this.contributionPercent = new double[columns][maxK];
        for (int i = 0; i < contributionPercent.length; i++) {
            this.contributionPercent[i] = new double[maxK];
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        SUDA2Result other = (SUDA2Result) obj;
        if (!Arrays.equals(columnKeyAverageSize, other.columnKeyAverageSize)) return false;
        if (!Arrays.equals(columnKeyContributions, other.columnKeyContributions)) return false;
        if (!Arrays.equals(columnKeyMaxSize, other.columnKeyMaxSize)) return false;
        if (!Arrays.equals(columnKeyMinSize, other.columnKeyMinSize)) return false;
        if (columns != other.columns) return false;
        if (maxK != other.maxK) return false;
        if (numMSUs != other.numMSUs) return false;
        if (!Arrays.equals(sizeDistribution, other.sizeDistribution)) return false;
        return true;
    }

    /**
     * @return the columnKeyAverageSize
     */
    public int[] getColumnKeyAverageSize() {
        return columnKeyAverageSize;
    }
    
    /**
     * Returns the contributions of each column
     * @return
     */
    public int[] getColumnKeyContributions() {
        return this.columnKeyContributions;
    }

    /**
     * @return the columnKeyMaxSize
     */
    public int[] getColumnKeyMaxSize() {
        return columnKeyMaxSize;
    }

    /**
     * @return the columnKeyMinSize
     */
    public int[] getColumnKeyMinSize() {
        return columnKeyMinSize;
    }

    /**
     * Returns the distribution of the sizes of MSUs
     * @return
     */
    public int[] getKeySizeDistribution() {
        return this.sizeDistribution;
    }
    
    /**
     * Returns the contributions of columns to record-level risks
     * @return
     */
    public double[] getColumnRisks() {
        double[] result = Arrays.copyOf(riskDistributionColumn, riskDistributionColumn.length);
        for (int i = 0; i < result.length; i++) {
            result[i] /= riskTotal;
        }
        return result;
    }

    /**
     * Returns the distribution of record-level risks.
     * First bucket is zero risk, second bucket 0% > risk <= 10%, 10% > risk <= 20%, 
     * @return
     */
    public double[] getRowRisksDistribution() {
        double[] result = new double[11];
//        for (double risk : this.riskDistributionRow) {
//            if (risk == 0d) {
//                result[0]++;
//            } else {
//                int index = (int) Math.floor(risk / 0.1d);
//                result[index + 1]++;
//            }
//        }
//        for (int i = 0; i < result.length; i++) {
//            result[i] /= (double)this.riskDistributionRow.length;
//        }
        return result;
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
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(columnKeyAverageSize);
        result = prime * result + Arrays.hashCode(columnKeyContributions);
        result = prime * result + Arrays.hashCode(columnKeyMaxSize);
        result = prime * result + Arrays.hashCode(columnKeyMinSize);
        result = prime * result + columns;
        result = prime * result + maxK;
        result = prime * result + numMSUs;
        result = prime * result + Arrays.hashCode(sizeDistribution);
        return result;
    }
    
    @Override
    public String toString() {
        
        int[] totalsContributions = new int[columns];
        Arrays.fill(totalsContributions, numMSUs);
        
        int[] totalAvgSize = new int[columns];
        for (int i = 0; i < totalAvgSize.length; i++) {
            totalAvgSize[i] = columnKeyContributions[i];
        }
        
        int[] totalsSize = new int[maxK];
        Arrays.fill(totalsSize, numMSUs);
        
        StringBuilder builder = new StringBuilder();
        builder.append("SUDA2 result\n");
        builder.append(" - Number of columns: ").append(this.columns).append("\n");
        builder.append(" - Number of MSUs: ").append(this.numMSUs).append("\n");
        builder.append(" - Column key contributions\n");
        builder.append(toString("     ", columnKeyContributions, totalsContributions, 0, false, true));
        builder.append(" - Column key min. size\n");
        builder.append(toString("     ", columnKeyMinSize, null, 0, true, false));
        builder.append(" - Column key max. size\n");
        builder.append(toString("     ", columnKeyMaxSize, null, 0, true, false));
        builder.append(" - Column key avg. size\n");
        builder.append(toString("     ", columnKeyAverageSize, totalAvgSize, 0, false, true));
        builder.append(" - Size distribution\n");
        builder.append(toString("     ", sizeDistribution, totalsSize, 1, true, true));
        builder.append(" - Risks (record-level)\n");
        builder.append(toString("     ", getRowRisksDistribution()));
        builder.append(" - Risks (column-level)\n");
        builder.append(toString("     ", riskDistributionColumn, riskTotal));
        builder.append(" - SUDA SCORE PER RECORD ***REMOVE***\n");
        builder.append(toString("     ", riskDistributionRow));
        return builder.toString();
    }

    /**
     * Registers an MSU with a column
     * @param column
     * @param size
     * @param score
     */
    private void registerColumn(int column, int size, double score) {
        this.columnKeyContributions[column]++;
        this.columnKeyMinSize[column] = Math.min(this.columnKeyMinSize[column], size);
        this.columnKeyMaxSize[column] = Math.max(this.columnKeyMaxSize[column], size);
        this.columnKeyAverageSize[column] += size;
        this.riskDistributionColumn[column] += score;
        this.contributionPercent[column][size]++;
    }
    
    /**
     * Returns the intermediate SUDA score for a MSU of a given size
     * @param size
     * @return
     */
    private double getIntermediateScore(int size) {
        // TODO: Improve
        double score = 1d;
        for (int i = size; i < maxK; i++) {
            score *= (double) (maxK - i); // 'maxK' in sdcMicro handbook, 'columns' in 2005 SUDA paper
        }
//        for (int i = 2; i <= columns; i++) {
//            score /= (double) i;
//        }
        return score;
    }

    /**
     * Registers the row to this result object
     * @param row
     * @param size
     * @param score
     */
    private void registerRow(int row, int size, double score) {
        System.out.println(row+"/"+size+"/"+score);
        riskDistributionRow[row] += score;
        riskTotal += score;
    }

    /**
     * Renders a distribution
     * @param intent
     * @param array
     * @return
     */
    private String toString(String intent, double[] array) {
        StringBuilder builder = new StringBuilder();
        DecimalFormat doubleFormat = new DecimalFormat("###.###");
        final int VALUE_WIDTH = 7;
        builder.append(intent).append("|");
        for (int index = 0; index < array.length; index++) {
            builder.append(toString(doubleFormat.format(index), VALUE_WIDTH)).append("|");
        }
        int width = builder.length() - intent.length();
        builder.append("\n");
        builder.append(intent);
        for (int i = 0; i < width; i++) {
            builder.append("-");
        }
        builder.append("\n");
        builder.append(intent).append("|");
        for (double val : array) {
            String value = doubleFormat.format(val);
            builder.append(toString(value, VALUE_WIDTH)).append("|");
        }
        builder.append("\n");

        return builder.toString();
    }

    /**
     * Renders a distribution
     * @param intent
     * @param array
     * @param total
     * @return
     */
    private String toString(String intent, double[] array, double total) {
        StringBuilder builder = new StringBuilder();
        DecimalFormat doubleFormat = new DecimalFormat("###.###");
        final int VALUE_WIDTH = 7;
        builder.append(intent).append("|");
        for (int index = 0; index < array.length; index++) {
            builder.append(toString(doubleFormat.format(index), VALUE_WIDTH)).append("|");
        }
        int width = builder.length() - intent.length();
        builder.append("\n");
        builder.append(intent);
        for (int i = 0; i < width; i++) {
            builder.append("-");
        }
        builder.append("\n");
        builder.append(intent).append("|");
        for (double val : array) {
            String value = doubleFormat.format(val / total);
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
            registerColumn(column, set.size(), 0d);
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
        double score = getIntermediateScore(size + 1);
        for (int i = 0; i < size; i++) {
            int column = set.get(i).getColumn();
            registerColumn(column, set.size() + 1, score);
        }

        registerColumn(item.getColumn(), set.size() + 1, score);
        
        // We still need to calculate the specific row for this MSU
        SUDA2Item temp = item;
        for (int i = 0; i < size; i++) {
            temp = temp.getProjection(set.get(i).getRows());
        }
        registerRow(temp.getRows().iterator().next().value, set.size() + 1, score);
    }

    /**
     * Registers an MSU
     * @param set
     */
    void registerMSU(SUDA2ItemSet set) {
        this.numMSUs++;
        this.sizeDistribution[set.size()-1]++;
        int size = set.size();
        double score = getIntermediateScore(size);
        for (int i = 0; i < size; i++) {
            int column = set.get(i).getColumn();
            registerColumn(column, set.size(), score);
        }
        // The one specific row has been calculated already
        registerRow(set.get(0).getRows().iterator().next().value, set.size(), score);
    }
}
