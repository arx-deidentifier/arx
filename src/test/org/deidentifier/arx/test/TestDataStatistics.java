/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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

package org.deidentifier.arx.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.StatisticsContingencyTable;
import org.deidentifier.arx.aggregates.StatisticsContingencyTable.Entry;
import org.deidentifier.arx.aggregates.StatisticsFrequencyDistribution;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.criteria.KAnonymity;
import org.junit.Test;

/**
 * Test class for data statistics
 * 
 * @author Fabian Prasser
 */
public class TestDataStatistics extends AbstractTest {
    
    /**
     * Helper class
     * 
     * @author Fabian Prasser
     */
    class DoubleArrayWrapper {
        
        /** Double array*/
        double[] values;
        
        /**
         * Creates a new instance
         * @param values
         */
        public DoubleArrayWrapper(double[] values) {
            this.values = values;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            DoubleArrayWrapper other = (DoubleArrayWrapper) obj;
            if (!getOuterType().equals(other.getOuterType())) {
                return false;
            }
            if (!Arrays.equals(this.values, other.values)) {
                return false;
            }
            return true;
        }
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + getOuterType().hashCode();
            result = (prime * result) + Arrays.hashCode(this.values);
            return result;
        }
        
        private TestDataStatistics getOuterType() {
            return TestDataStatistics.this;
        }
        
    }
    
    /**
     * Performs a test.
     *
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @Test
    public void testContingency1() throws IllegalArgumentException, IOException {
        
        this.provider.createDataDefinition();
        this.provider.getData().getDefinition().setDataType("age", DataType.INTEGER);
        
        final ARXConfiguration config = ARXConfiguration.create();
        config.addPrivacyModel(new KAnonymity(2));
        config.setSuppressionLimit(0d);
        
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXResult result = anonymizer.anonymize(this.provider.getData(), config);
        
        // Define
        StatisticsContingencyTable contingency;
        String[] values1;
        String[] values2;
        double[][] frequencies;
        
        // Check input
        contingency = this.provider.getData()
                                   .getHandle()
                                   .getStatistics()
                                   .getContingencyTable(0, true, 2, true);
                                   
        values1 = new String[] { "34", "45", "66", "70" };
        values2 = new String[] { "81667", "81675", "81925", "81931" };
        assertTrue(Arrays.equals(values1, contingency.values1));
        assertTrue(Arrays.equals(values2, contingency.values2));
        
        frequencies = new double[][] { { 0, 0, 0.14285714285714285 },
                                       { 1, 1, 0.14285714285714285 },
                                       { 2, 2, 0.14285714285714285 },
                                       { 3, 3, 0.2857142857142857 },
                                       { 1, 3, 0.14285714285714285 },
                                       { 0, 3, 0.14285714285714285 } };
                           
        assertTrue("Unexpected result", deepEquals(toArray(contingency), frequencies));
        
        // Check output
        contingency = result.getOutput(false)
                            .getStatistics()
                            .getContingencyTable(0, true, 2, true);
                            
        values1 = new String[] { "<50", ">=50" };
        values2 = new String[] { "816**", "819**" };
        assertTrue(Arrays.equals(values1, contingency.values1));
        assertTrue(Arrays.equals(values2, contingency.values2));
        
        frequencies = new double[][] { { 0, 0, 0.2857142857142857 },
                                       { 1, 1, 0.42857142857142855 },
                                       { 0, 1, 0.2857142857142857 } };
        
        assertTrue("Unexpected result", deepEquals(toArray(contingency), frequencies));
    }
    
    /**
     * Performs a test.
     *
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @Test
    public void testContingency2() throws IllegalArgumentException, IOException {
        
        this.provider.createDataDefinition();
        this.provider.getData().getDefinition().setDataType("age", DataType.INTEGER);
        
        // Subset
        Set<Integer> set = new HashSet<Integer>();
        set.add(0);
        set.add(6);
        DataSubset subset = DataSubset.create(this.provider.getData(), set);
        
        final ARXConfiguration config = ARXConfiguration.create();
        config.addPrivacyModel(new KAnonymity(2));
        config.addPrivacyModel(new DPresence(0.0d, 1.0d, subset));
        config.setSuppressionLimit(0d);
        
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXResult result = anonymizer.anonymize(this.provider.getData(), config);
        
        // Define
        StatisticsContingencyTable contingency;
        String[] values1;
        String[] values2;
        double[][] frequencies;
        
        // Check input
        contingency = this.provider.getData().getHandle().getView().getStatistics().getContingencyTable(0, true, 2, true);
        
        values1 = new String[] { "34", "45" };
        values2 = new String[] { "81667", "81931" };
        assertTrue(Arrays.equals(values1, contingency.values1));
        assertTrue(Arrays.equals(values2, contingency.values2));
        
        frequencies = new double[][] { { 0, 0, 0.5 },
                                       { 1, 1, 0.5 } };
        
        assertTrue("Unexpected result", deepEquals(toArray(contingency), frequencies));
        
        // Check output
        contingency = result.getOutput(false).getView().getStatistics().getContingencyTable(0, true, 2, true);
        
        values1 = new String[] { "<50" };
        values2 = new String[] { "81***" };
        assertTrue(Arrays.equals(values1, contingency.values1));
        assertTrue(Arrays.equals(values2, contingency.values2));
        
        frequencies = new double[][] { { 0, 0, 1.0 } };
        assertTrue("Unexpected result", deepEquals(toArray(contingency), frequencies));
    }
    
    /**
     * Performs a test.
     *
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @Test
    public void testDistribution1() throws IllegalArgumentException, IOException {
        
        this.provider.createDataDefinition();
        this.provider.getData().getDefinition().setDataType("age", DataType.INTEGER);
        
        final ARXConfiguration config = ARXConfiguration.create();
        config.addPrivacyModel(new KAnonymity(2));
        config.setSuppressionLimit(0d);
        
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXResult result = anonymizer.anonymize(this.provider.getData(), config);
        
        // Define
        StatisticsFrequencyDistribution distribution;
        String[] values;
        double[] frequency;
        
        // Check input
        distribution = this.provider.getData().getHandle().getStatistics().getFrequencyDistribution(0, true);
        values = new String[] { "34", "45", "66", "70" };
        frequency = new double[] { 0.2857142857142857, 0.2857142857142857, 0.14285714285714285, 0.2857142857142857 };
        assertTrue(Arrays.equals(values, distribution.values));
        assertTrue(Arrays.equals(frequency, distribution.frequency));
        
        // Check output
        distribution = result.getOutput(false).getStatistics().getFrequencyDistribution(0, true);
        values = new String[] { "<50", ">=50" };
        frequency = new double[] { 0.5714285714285714, 0.42857142857142855 };
        assertTrue(Arrays.equals(values, distribution.values));
        assertTrue(Arrays.equals(frequency, distribution.frequency));
    }
    
    /**
     * Performs a test.
     *
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @Test
    public void testDistribution2() throws IllegalArgumentException, IOException {
        
        this.provider.createDataDefinition();
        this.provider.getData().getDefinition().setDataType("age", DataType.INTEGER);
        
        final ARXConfiguration config = ARXConfiguration.create();
        config.addPrivacyModel(new KAnonymity(2));
        config.setSuppressionLimit(0d);
        
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXResult result = anonymizer.anonymize(this.provider.getData(), config);
        
        // Define
        StatisticsFrequencyDistribution distribution;
        String[] values;
        double[] frequency;
        
        // Check input
        distribution = this.provider.getData().getHandle().getStatistics().getFrequencyDistribution(1, false);
        values = new String[] { "female", "male" };
        frequency = new double[] { 0.42857142857142855, 0.5714285714285714 };
        assertTrue(Arrays.equals(values, distribution.values));
        assertTrue(Arrays.equals(frequency, distribution.frequency));
        
        // Check output
        distribution = result.getOutput(false).getStatistics().getFrequencyDistribution(1, true);
        values = new String[] { "*" };
        frequency = new double[] { 1.0 };
        assertTrue(Arrays.equals(values, distribution.values));
        assertTrue(Arrays.equals(frequency, distribution.frequency));
    }
    
    /**
     * Checks the two arrays regarding equality, treating a double[][]
     * as a set of comparable double[]'s
     * 
     * @param set1
     * @param set2
     */
    private boolean deepEquals(double[][] set1, double[][] set2) {
        
        if (set1.length != set2.length) {
            return false;
        }
        
        Set<DoubleArrayWrapper> frequencies = new HashSet<DoubleArrayWrapper>();
        for (int i = 0; i < set1.length; i++) {
            frequencies.add(new DoubleArrayWrapper(set1[i]));
        }
        
        // Check
        for (int j = 0; j < set2.length; j++) {
            if (!frequencies.contains(new DoubleArrayWrapper(set2[j]))) {
                return false;
            }
        }
        
        // They are equal
        return true;
    }
    
    /**
     * Converts a contigency table to an array
     * 
     * @param contingency
     * @return
     */
    private double[][] toArray(StatisticsContingencyTable contingency) {
        
        List<double[]> list = new ArrayList<double[]>();
        while (contingency.iterator.hasNext()) {
            Entry e = contingency.iterator.next();
            list.add(new double[] { e.value1, e.value2, e.frequency });
        }
        return list.toArray(new double[][] {});
    }
}
