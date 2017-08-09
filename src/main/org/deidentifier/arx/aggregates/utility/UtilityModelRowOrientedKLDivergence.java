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

package org.deidentifier.arx.aggregates.utility;

import org.deidentifier.arx.DataHandleInternal;
import org.deidentifier.arx.common.Groupify;
import org.deidentifier.arx.common.TupleWrapper;
import org.deidentifier.arx.common.WrappedBoolean;

/**
 * This class implements the KL Divergence metric.<br>
 * Ashwin Machanavajjhala, Daniel Kifer, Johannes Gehrke, Muthuramakrishnan Venkitasubramaniam: <br>
 * L-diversity: Privacy beyond k-anonymity<br>
 * ACM Transactions on Knowledge Discovery from Data (TKDD), Volume 1 Issue 1, March 2007
 * 
 * @author Fabian Prasser
 */
class UtilityModelRowOrientedKLDivergence extends UtilityModel<UtilityMeasureRowOriented> {

    /** Domain shares */
    private final UtilityDomainShare[] shares;
    /** Header */
    private final int[]                indices;
    /** Rows */
    private final int                  rows;
    /** Distribution */
    private double[]                   inputDistribution = null;
    /** Minimum */
    private final double               min;
    /** Maximum */
    private final double               max;

    /**
     * Creates a new instance
     * @param interrupt
     * @param input
     * @param config
     */
    UtilityModelRowOrientedKLDivergence(WrappedBoolean interrupt,
                                        DataHandleInternal input,
                                        UtilityConfiguration config) {
        super(interrupt, input, config);
        this.rows = input.getNumRows();
        this.indices = getHelper().getIndicesOfQuasiIdentifiers(input);
        this.shares = getHelper().getDomainShares(input, indices);
        this.inputDistribution = getDistribution(getHelper().getGroupify(input, indices), input);
        this.min = getKLDivergence(input, inputDistribution, inputDistribution);
        double _max = 1d;
        for (UtilityDomainShare share : shares) {
            _max *= share.getDomainSize();
        }
        this.max = (double)this.rows * log2(_max);
    }
    
    /**
     * Returns the area
     * @param handle
     * @param row
     * @return
     */
    private double getArea(DataHandleInternal handle, int row) {
        double area = 1d;
        for (int i = 0; i < indices.length; i++) {
            int column = indices[i];
            double loss = 1d;
            if (!isSuppressed(handle, row, column)) {
                loss = shares[i].getShare(handle.getValue(row, column), 0);
            }
            area *= loss * shares[i].getDomainSize();
        }
        return area;
    }
    
    /**
     * Returns the distribution per record
     * @param groupify
     * @param handle
     * @return
     */
    private double[] getDistribution(Groupify<TupleWrapper> groupify,
                                     DataHandleInternal handle) {
        
        // Build input distribution
        double[] result = new double[rows];
        for (int row = 0; row < rows; row++) {
            TupleWrapper tuple = new TupleWrapper(handle, indices, row, false);
            double frequency = (double)groupify.get(tuple).getCount() / (double)rows;
            result[row] = frequency;

            // Check
            checkInterrupt();
        }
        
        // Return
        return result;
    }

    /**
     * Calculates KL-Divergence
     * @param handle
     * @param inputDistribution
     * @param outputDistribution
     * @return
     */
    private double getKLDivergence(DataHandleInternal handle,
                                   double[] inputDistribution,
                                   double[] outputDistribution) {

        // Init
        double result = 0d;
        
        // For each tuple
        for (int row = 0; row < rows; row++) {
            
            // Obtain frequency
            double inputFrequency = inputDistribution[row];
            double outputFrequency = outputDistribution[row];
            outputFrequency /= getArea(handle, row);
            
            // Compute KL-Divergence
            result += inputFrequency * log2(inputFrequency / outputFrequency); 

            // Check
            checkInterrupt();
        }
        
        return result;
    }
    
    @Override
    UtilityMeasureRowOriented evaluate(DataHandleInternal output) {
        
        try {
    
            // Output distribution
            double[] outputDistribution = getDistribution(getHelper().getGroupify(output, indices), output);
    
            // KL divergence
            double result = getKLDivergence(output,
                                            inputDistribution,
                                            outputDistribution);
            
            // Return
            return new UtilityMeasureRowOriented(min, result, max);
            
        } catch (Exception e) {
            // Silently catch exceptions
            return new UtilityMeasureRowOriented(min, Double.NaN, max);
        }
    }
}
