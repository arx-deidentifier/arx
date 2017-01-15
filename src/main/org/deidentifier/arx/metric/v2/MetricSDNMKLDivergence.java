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

package org.deidentifier.arx.metric.v2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.RowSet;
import org.deidentifier.arx.framework.check.groupify.HashGroupify;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.metric.MetricConfiguration;

/**
 * This class implements the KL Divergence metric.
 * Ashwin Machanavajjhala, Daniel Kifer, Johannes Gehrke, Muthuramakrishnan Venkitasubramaniam: 
 * L-diversity: Privacy beyond k-anonymity
 * ACM Transactions on Knowledge Discovery from Data (TKDD), Volume 1 Issue 1, March 2007 
 *
 * @author Fabian Prasser
 */
public class MetricSDNMKLDivergence extends AbstractMetricSingleDimensional {

    /** Tuple wrapper*/
    class TupleWrapper {
        
        /** Field*/
        private final int[] tuple;
        /** Field*/
        private final int hash;
        
        /**
         * Constructor
         * @param tuple
         */
        public TupleWrapper(int[] tuple) {
            this.tuple = tuple;
            this.hash = Arrays.hashCode(tuple);
        }

        @Override
        public boolean equals(Object other) {
            return Arrays.equals(this.tuple, ((TupleWrapper)other).tuple);
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }

    /** SUID. */
    private static final long serialVersionUID  = -4918601543733931921L;

    /**
     * Computes log 2.
     *
     * @param num
     * @return
     */
    static final double log2(final double num) {
        return Math.log(num) / LOG2;
    }

    /** Total number of tuples, depends on existence of research subset. */
    private Double              tuples            = null;

    /** Domain shares for each dimension. */
    private DomainShare[]       shares;

    /** Maximum value */
    private Double              max               = null;

    /** Tuple matcher */
    private TupleMatcher        matcher           = null;

    /** Distribution */
    private double[]            inputDistribution = null;

    /** Log 2. */
    private static final double LOG2              = Math.log(2);

    /** Maximal area */
    private double              maximalArea       = 0d;

    /**
     * Default constructor.
     */
    public MetricSDNMKLDivergence(){
        super(true, false, false);
    }
    
    @Override
    public ILSingleDimensional createMaxInformationLoss() {
        if (max == null) {
            throw new IllegalStateException("Metric must be initialized first");
        } else {
            return new ILSingleDimensional(max);
        }
    }

    @Override
    public ILSingleDimensional createMinInformationLoss() {
        return new ILSingleDimensional(0);
    }

    /**
     * Returns the configuration of this metric.
     *
     * @return
     */
    public MetricConfiguration getConfiguration() {
        return new MetricConfiguration(false,                      // monotonic
                                       0.5d,                       // gs-factor
                                       false,                      // precomputed
                                       0.0d,                       // precomputation threshold
                                       AggregateFunction.SUM       // aggregate function
                                       );
    }

    @Override
    public String getName() {
        return "KL-Divergence";
    }

    @Override
    public String toString() {
        return "KL-Divergence";
    }

    /**
     * Returns the area
     * @param output
     * @param generalization
     * @return
     */
    private double getArea(int[] output, int[] generalization) {
        
        double result = 1d;
        for (int dimension = 0; dimension < output.length; dimension++) {
            DomainShare share = this.shares[dimension];
            result *= share.getShare(output[dimension], generalization[dimension]) * share.getDomainSize();
        }
        return result;
    }
    
    @Override
    protected ILSingleDimensionalWithBound getInformationLossInternal(Transformation node, HashGroupify g) {
        
        // Obtain number of outliers
        double outliers = 0d;
        HashGroupifyEntry m = g.getFirstEquivalenceClass();
        while (m != null) {
            outliers += !m.isNotOutlier ? m.count : 0d;
            m = m.nextOrdered;
        }
        
        // Init
        double result = 0d;
        
        // For each tuple
        for (int row = 0; row < this.inputDistribution.length; row++) {
            
            // Obtain frequency
            double inputFrequency = inputDistribution[row];
            
            // Only if present
            if (inputFrequency != 0d) {
                
                int[] generalization = node.getGeneralization();
                HashGroupifyEntry entry = this.matcher.getEntry(row, generalization, g);
                double outputFrequency = entry.isNotOutlier ? entry.count : outliers;
                outputFrequency /= this.tuples;
                outputFrequency /= entry.isNotOutlier ? getArea(entry.key, generalization) : maximalArea;
                
                // Compute KL-Divergence
                result += inputFrequency * log2(inputFrequency / outputFrequency);
            }
        }
        
        // Return
        return new ILSingleDimensionalWithBound(result);
    }

    @Override
    protected ILSingleDimensionalWithBound getInformationLossInternal(Transformation node, HashGroupifyEntry entry) {
        return new ILSingleDimensionalWithBound(entry.count, entry.count);
    }
    
    @Override
    protected ILSingleDimensional getLowerBoundInternal(Transformation node) {
        return null;
    }

    @Override
    protected ILSingleDimensional getLowerBoundInternal(Transformation node,
                                                        HashGroupify g) {
        return null;
    }
    
    @Override
    protected void initializeInternal(final DataManager manager,
                                      final DataDefinition definition, 
                                      final Data input, 
                                      final GeneralizationHierarchy[] hierarchies, 
                                      final ARXConfiguration config) {
        
        // Prepare weights
        super.initializeInternal(manager, definition, input, hierarchies, config);

        // Compute domain shares
        this.shares = new DomainShare[hierarchies.length];
        for (int i = 0; i < shares.length; i++) {

            // Extract info
            String attribute = input.getHeader()[i];
            String[][] hierarchy = definition.getHierarchy(attribute);
            this.shares[i] = new DomainShareMaterialized(hierarchy,
                                                         input.getDictionary().getMapping()[i],
                                                         hierarchies[i].getArray());
        }

        // Determine total number of tuples
        this.tuples = (double)super.getNumRecords(config, input);
        RowSet subset = super.getSubset(config);
        
        // Tuple matcher
        this.matcher = new TupleMatcher(hierarchies, input.getArray());
       
        // Areamax
        this.maximalArea = 1d;
        for (int dimension = 0; dimension < this.shares.length; dimension++) {
            maximalArea *= this.shares[dimension].getDomainSize();
        }
        
        // Groupify
       Map<TupleWrapper, Integer> groupify = new HashMap<TupleWrapper, Integer>();
       for (int row = 0; row < input.getDataLength(); row++) {
           if (subset == null || subset.contains(row)) {
               TupleWrapper wrapper = new TupleWrapper(input.getArray()[row]);
               Integer count = groupify.get(wrapper);
               count = count == null ? 1 : count + 1;
               groupify.put(wrapper, count);
           }
       }
       
       // Build input distribution and compute max
       this.max = 0d;
       this.inputDistribution = new double[input.getArray().length];
       for (int row = 0; row < input.getDataLength(); row++) {
           if (subset == null || subset.contains(row)) {
               TupleWrapper wrapper = new TupleWrapper(input.getArray()[row]);
               double frequency = groupify.get(wrapper).doubleValue() / this.tuples;
               this.inputDistribution[row] = frequency ;
               max += frequency * log2(frequency * maximalArea);
           }
       }
    }
}
