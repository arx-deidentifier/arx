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

package org.deidentifier.arx.test;

import static org.junit.Assert.assertEquals;

import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.risk.RiskEstimateBuilder;
import org.deidentifier.arx.risk.RiskModelSampleWildcard;
import org.junit.Test;

/**
 * Test for wild card matching of missing values.
 *
 * @author Helmut Spengler
 */
public class TestWildcardMatching {

    /** The string representing an empty value */
    private String _AV = DataType.ANY_VALUE;    
    /** The precision to be used when comparing double values */
    private double precision = 1e-16;
    
    ////////////////////////////////////
    // Test data
    ////////////////////////////////////
    private Data testData1 = Data.create(new String[][] {
        {  "sex", "zip"  },  // header
        {  "M",   "4711" }, 
        {  "M",   "4711" }, 
        {  "M",   "4711" }, 
        {  _AV,   "4711" }, 
        {  _AV,   "4711" },        
        {  "F",   "4712" },        
        {  "F",     _AV  }, 
        {  "M",     _AV  }  
    });
    private int[] expectedWildcardFrequencies1 = { 6, 6, 6, 7, 7, 2, 4, 6 };

    private Data testData2 = Data.create(new String[][] {
        { "age", "sex", "Region"},  // header
        {  _AV,  "F",   "North" }, 
        { "68",  "F",   "North" }, 
        { "68",   _AV,  "North" }, 
        {  _AV,   _AV,  "North" }, 
        {  _AV,   _AV,  "North" }, 
        {  _AV,   _AV,  "North" }, 
        { "68", "F",      _AV   }, 
        { "68",  _AV,   "South" }, 
        { "68", "F",    "South" }, 
        { "68", "F",    "South" }  
    });
    private int[] expectedWildcardFrequencies2 = { 7, 7, 7, 7, 7, 7, 10, 4, 4, 4 };

    private Data testData3 = Data.create(new String[][] {
        { "weight", "icd",  },  // header
        { "73",     "C18.7" },
        { "73",     "C18.7" },
        {  _AV,     "C18.7" },
        {  _AV,     "C18.7" },
        {  _AV,     "C18.2" },
        { "67",     "C18.2" },
        { "67",     "C18.2" },
        { "67",     "C18.7" },
        { "67",     "C18.7" },
        { "67",     "C18.7" } 
    });
    private int[] expectedWildcardFrequencies3 = { 4, 4, 7, 7, 3, 3, 3, 5, 5, 5 };
    
    ////////////////////////////////////
    // Helper and setup methods
    ////////////////////////////////////    
    /**
     * Compare ARX results for highest risk and average risk with results obtained via manual determination of frequencies.
     * @param data
     * @param frequencies
     */
    private void compareRiskValues(Data data, int[] frequencies) {
        
        // Declare all attributes as QIs
        for (int i = 0; i < data.getHandle().getNumColumns(); i++) {
            data.getDefinition().setAttributeType(data.getHandle().getAttributeName(i), AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);            
        }
        
        // Get risk model
        RiskEstimateBuilder builder = data.getHandle().getRiskEstimator();
        RiskModelSampleWildcard riskModel = builder.getSampleBasedRiskSummaryWildcard(0.0d,  _AV);
        
        // Perform assertions
        assertEquals(getHighestRiskFromFrequencies(frequencies), riskModel.getHighestRisk(), precision);
        assertEquals(getAverageRiskFromFrequencies(frequencies), riskModel.getAverageRisk(), precision);
    }
    
    /**
     * Return the highest risk based on a vector of frequencies.
     * @param frequencies
     * @return
     */
    private double getHighestRiskFromFrequencies(int[] frequencies) {
        int min = Integer.MAX_VALUE;
        
        for (int v : frequencies) {
            min = Math.min(min, v);
        }
        
        return 1d/(double)min;
    }
    
    /**
     * Return the average risk based on a vector of frequencies.
     * @param frequencies
     * @return
     */
    private double getAverageRiskFromFrequencies(int[] frequencies) {
        double result = 0.0d;
    
        for (int v : frequencies) {
            result += 1d / (double) v;
        }
        result /= ((double) frequencies.length);
        
        return result;        
    }

    ////////////////////////////////////
    // The actual tests
    ////////////////////////////////////
    @Test
    public void testRiskValues1() {
        compareRiskValues(testData1, expectedWildcardFrequencies1);
    }
    
    @Test
    public void testRiskValues2() {
        compareRiskValues(testData2, expectedWildcardFrequencies2);
    }
    
    @Test
    public void testRiskValues3() {
        compareRiskValues(testData3, expectedWildcardFrequencies3);
    }
}
