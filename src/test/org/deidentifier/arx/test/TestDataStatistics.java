/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
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
 * 
 */
public class TestDataStatistics extends AbstractTest {

    /**
     * 
     *
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @Test
    public void testDistribution1() throws IllegalArgumentException, IOException {

        provider.createDataDefinition();
        provider.getData().getDefinition().setDataType("age", DataType.INTEGER);

        final ARXConfiguration config = ARXConfiguration.create();
        config.addCriterion(new KAnonymity(2));
        config.setMaxOutliers(0d);

        ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXResult result = anonymizer.anonymize(provider.getData(), config);
        
        // Define
        StatisticsFrequencyDistribution distribution;
        String[] values;
        double[] frequency;
        
        // Check input
        distribution = provider.getData().getHandle().getStatistics().getFrequencyDistribution(0, true);
        values = new String[]{"34", "45", "66", "70"};
        frequency = new double[]{0.2857142857142857, 0.2857142857142857, 0.14285714285714285, 0.2857142857142857};
        assertTrue(Arrays.equals(values, distribution.values));
        assertTrue(Arrays.equals(frequency, distribution.frequency));

        // Check output
        distribution = result.getOutput(false).getStatistics().getFrequencyDistribution(0, true);
        values = new String[]{"<50", ">=50"};
        frequency = new double[]{0.5714285714285714, 0.42857142857142855};
        assertTrue(Arrays.equals(values, distribution.values));
        assertTrue(Arrays.equals(frequency, distribution.frequency));
    }

    /**
     * 
     *
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @Test
    public void testDistribution2() throws IllegalArgumentException, IOException {

        provider.createDataDefinition();
        provider.getData().getDefinition().setDataType("age", DataType.INTEGER);

        final ARXConfiguration config = ARXConfiguration.create();
        config.addCriterion(new KAnonymity(2));
        config.setMaxOutliers(0d);

        ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXResult result = anonymizer.anonymize(provider.getData(), config);
        
        // Define
        StatisticsFrequencyDistribution distribution;
        String[] values;
        double[] frequency;
        
        // Check input
        distribution = provider.getData().getHandle().getStatistics().getFrequencyDistribution(1, false);
        values = new String[]{"female", "male"};
        frequency = new double[]{0.42857142857142855, 0.5714285714285714};
        assertTrue(Arrays.equals(values, distribution.values));
        assertTrue(Arrays.equals(frequency, distribution.frequency));

        // Check output
        distribution = result.getOutput(false).getStatistics().getFrequencyDistribution(1, true);
        values = new String[]{"*"};
        frequency = new double[]{1.0};
        assertTrue(Arrays.equals(values, distribution.values));
        assertTrue(Arrays.equals(frequency, distribution.frequency));
    }

    /**
     * 
     *
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @Test
    public void testContingency1() throws IllegalArgumentException, IOException {

        provider.createDataDefinition();
        provider.getData().getDefinition().setDataType("age", DataType.INTEGER);

        final ARXConfiguration config = ARXConfiguration.create();
        config.addCriterion(new KAnonymity(2));
        config.setMaxOutliers(0d);

        ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXResult result = anonymizer.anonymize(provider.getData(), config);
        
        // Define
        StatisticsContingencyTable contingency;
        String[] values1;
        String[] values2;
        double[][] frequencies;

        // Check input
        contingency = provider.getData()
                              .getHandle()
                              .getStatistics()
                              .getContingencyTable(0, true, 2, true);
        
        values1 = new String[]{"34", "45", "66", "70"};
        values2 = new String[]{"81667", "81675", "81925", "81931"};
        assertTrue(Arrays.equals(values1, contingency.values1));
        assertTrue(Arrays.equals(values2, contingency.values2));
        
        frequencies = new double[][] {  { 1, 1, 0.14285714285714285 },
                                        { 2, 2, 0.14285714285714285 },
                                        { 1, 3, 0.14285714285714285 },
                                        { 0, 0, 0.14285714285714285 },
                                        { 3, 3, 0.2857142857142857 },
                                        { 0, 3, 0.14285714285714285 } };
                
        assertTrue(Arrays.deepEquals(contingencyToArray(contingency), frequencies));

        // Check output
        contingency = result.getOutput(false)
                            .getStatistics()
                            .getContingencyTable(0, true, 2, true);
                
        values1 = new String[]{"<50", ">=50"};
        values2 = new String[]{"816**", "819**"};
        assertTrue(Arrays.equals(values1, contingency.values1));
        assertTrue(Arrays.equals(values2, contingency.values2));
        
        frequencies = new double[][] {  { 1, 1, 0.42857142857142855 },
                                        { 0, 1, 0.2857142857142857 },
                                        { 0, 0, 0.2857142857142857 } };
          
        assertTrue(Arrays.deepEquals(contingencyToArray(contingency), frequencies));
    }

    /**
     * 
     *
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @Test
    public void testContingency2() throws IllegalArgumentException, IOException {

        provider.createDataDefinition();
        provider.getData().getDefinition().setDataType("age", DataType.INTEGER);
       
        // Subset
        Set<Integer> set = new HashSet<Integer>();
        set.add(0);
        set.add(6);
        DataSubset subset = DataSubset.create(provider.getData(), set);

        final ARXConfiguration config = ARXConfiguration.create();
        config.addCriterion(new KAnonymity(2));
        config.addCriterion(new DPresence(0.0d, 1.0d, subset));
        config.setMaxOutliers(0d);

        ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXResult result = anonymizer.anonymize(provider.getData(), config);
        
        // Define
        StatisticsContingencyTable contingency;
        String[] values1;
        String[] values2;
        double[][] frequencies;

        // Check input
        contingency = provider.getData().getHandle().getView().getStatistics().getContingencyTable(0, true, 2, true);
        
        values1 = new String[]{"34", "45"};
        values2 = new String[]{"81667", "81931"};
        assertTrue(Arrays.equals(values1, contingency.values1));
        assertTrue(Arrays.equals(values2, contingency.values2));
        
        frequencies = new double[][] {  { 1, 1, 0.5 },
                                        { 0, 0, 0.5 } };
                
        assertTrue(Arrays.deepEquals(contingencyToArray(contingency), frequencies));

        // Check output
        contingency = result.getOutput(false).getView().getStatistics().getContingencyTable(0, true, 2, true);

        values1 = new String[]{"<50"};
        values2 = new String[]{"81***"};
        assertTrue(Arrays.equals(values1, contingency.values1));
        assertTrue(Arrays.equals(values2, contingency.values2));
        
        frequencies = new double[][] {  { 0, 0, 1.0 } };
                
        assertTrue(Arrays.deepEquals(contingencyToArray(contingency), frequencies));
    }

    /**
     * 
     *
     * @param contingency
     * @return
     */
    private double[][] contingencyToArray(StatisticsContingencyTable contingency) {

        List<double[]> list = new ArrayList<double[]>();
        while (contingency.iterator.hasNext()) {
            Entry e = contingency.iterator.next();
            list.add(new double[]{e.value1, e.value2, e.frequency});
        }
        return list.toArray(new double[][]{});
    }
}
