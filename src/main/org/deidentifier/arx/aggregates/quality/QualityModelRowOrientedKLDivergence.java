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

package org.deidentifier.arx.aggregates.quality;

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
public class QualityModelRowOrientedKLDivergence extends QualityModel<QualityMeasureRowOriented> {

    /**
     * Creates a new instance
     * 
     * @param interrupt
     * @param input
     * @param output
     * @param groupedInput
     * @param groupedOutput
     * @param hierarchies
     * @param shares
     * @param indices
     * @param config
     */
    public QualityModelRowOrientedKLDivergence(WrappedBoolean interrupt,
                                               DataHandleInternal input,
                                               DataHandleInternal output,
                                               Groupify<TupleWrapper> groupedInput,
                                               Groupify<TupleWrapper> groupedOutput,
                                               String[][][] hierarchies,
                                               QualityDomainShare[] shares,
                                               int[] indices,
                                               QualityConfiguration config) {
        
        super(interrupt,
              input,
              output,
              groupedInput,
              groupedOutput,
              hierarchies,
              shares,
              indices,
              config);
    }

    @Override
    public QualityMeasureRowOriented evaluate() {

        try {

            // Prepare
            DataHandleInternal input = getInput();
            DataHandleInternal output = getOutput();
            int rows = input.getNumRows();
            int[] indices = getIndices();
            QualityDomainShare[] shares = getDomainShares();
            double[] inputDistribution = getDistribution(getGroupedInput(), input, indices, shares, rows);

            // Min and max
            double min = getKLDivergence(input, inputDistribution, inputDistribution, indices, shares, rows);
            double _max = 1d;
            for (QualityDomainShare share : shares) {
                _max *= share.getDomainSize();
            }
            double max = (double) rows * log2(_max);

            // Output distribution
            double[] outputDistribution = getDistribution(getGroupedOutput(), output, indices, shares, rows);
    
            // KL divergence
            double result = getKLDivergence(output,
                                            inputDistribution,
                                            outputDistribution, 
                                            indices, 
                                            shares, 
                                            rows);
            
            // Return
            return new QualityMeasureRowOriented(min, result, max);
            
        } catch (Exception e) {
            // Silently catch exceptions
            return new QualityMeasureRowOriented();
        }
    }
    
    /**
     * Returns the area
     * @param handle
     * @param row
     * @param indices
     * @param shares
     * @return
     */
    private double getArea(DataHandleInternal handle, 
                           int row,
                           int[] indices,
                           QualityDomainShare[] shares) {
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
     * @param indices
     * @param shares
     * @param rows
     * @return
     */
    private double[] getDistribution(Groupify<TupleWrapper> groupify,
                                     DataHandleInternal handle,
                                     int[] indices,
                                     QualityDomainShare[] shares,
                                     int rows) {
        
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
     * @param indices
     * @param shares
     * @return
     */
    private double getKLDivergence(DataHandleInternal handle,
                                   double[] inputDistribution,
                                   double[] outputDistribution,
                                   int[] indices,
                                   QualityDomainShare[] shares,
                                   int rows) {

        // Init
        double result = 0d;
        
        // For each tuple
        for (int row = 0; row < rows; row++) {
            
            // Obtain frequency
            double inputFrequency = inputDistribution[row];
            double outputFrequency = outputDistribution[row];
            outputFrequency /= getArea(handle, row, indices, shares);
            
            // Compute KL-Divergence
            result += inputFrequency * log2(inputFrequency / outputFrequency);
            
            // TODO: Something seems to be wrong with this implementation
            // The value should never be <0, but it is

            // Check
            checkInterrupt();
        }
        
        return result;
    }
}
