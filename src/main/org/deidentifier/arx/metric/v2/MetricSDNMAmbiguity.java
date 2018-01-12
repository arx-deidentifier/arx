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

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.certificate.elements.ElementData;
import org.deidentifier.arx.framework.check.groupify.HashGroupify;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.metric.MetricConfiguration;

/**
 * This class implements a variant of the Ambiguity metric.
 * See Jacob Goldberger, Tamir Tassa: Efficient Anonymizations with Enhanced Utility.
 * TRANSACTIONS ON DATA PRIVACY. 3. (2010). 149-175.
 *
 * @author Fabian Prasser
 */
public class MetricSDNMAmbiguity extends AbstractMetricSingleDimensional {

    /** SUID. */
    private static final long serialVersionUID = -4376770864891280340L;

    /** Total number of tuples, depends on existence of research subset. */
    private Double            tuples = null;

    /** Domain shares for each dimension. */
    private DomainShare[]     shares;

    /** Maximum value */
    private Double            max              = null;
    
    /**
     * Default constructor.
     */
    public MetricSDNMAmbiguity(){
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
        if (tuples == null) {
            throw new IllegalStateException("Metric must be initialized first");
        } else {
            return new ILSingleDimensional(tuples);
        }
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
        return "Ambiguity";
    }

    @Override
    public ElementData render(ARXConfiguration config) {
        ElementData result = new ElementData("Ambiguity");
        result.addProperty("Monotonic", this.isMonotonic(config.getSuppressionLimit()));
        return result;
    }

    @Override
    public String toString() {
        return "Ambiguity";
    }
    
    @Override
    protected ILSingleDimensionalWithBound getInformationLossInternal(Transformation node, HashGroupify g) {

        // Init
        int[] transformation = node.getGeneralization();
        double result = 0d;
        double bound = 0d;

        // Compute loss and lower bound
        HashGroupifyEntry m = g.getFirstEquivalenceClass();
        while (m != null) {
            if (m.count>0) {
                double classResult = 1d;
                double classBound = 1d;
                // Compute
                m.read();
                for (int dimension = 0; dimension < transformation.length; dimension++) {
                    int value = m.next();
                    int level = transformation[dimension];
                    double share = shares[dimension].getShare(value, level);
                    classResult *= (m.isNotOutlier ? share : 1d) * shares[dimension].getDomainSize();
                    classBound *= share * shares[dimension].getDomainSize();
                }
                classResult *= m.count;
                classBound *= m.count;
                result += classResult;
                bound += classBound;
            }
            m = m.nextOrdered;
        }
                
        // Return
        return new ILSingleDimensionalWithBound(result, bound);
    }
    
    @Override
    protected ILSingleDimensionalWithBound getInformationLossInternal(Transformation node, HashGroupifyEntry entry) {

        // Init
        int[] transformation = node.getGeneralization();
        double result = 1d;

        // Compute
        entry.read();
        for (int dimension = 0; dimension < transformation.length; dimension++) {
            int value = entry.next();
            int level = transformation[dimension];
            result *= shares[dimension].getShare(value, level) * shares[dimension].getDomainSize();
        }
        result *= entry.count;
        
        // Return
        return new ILSingleDimensionalWithBound(result, result);
    }

    @Override
    protected ILSingleDimensional getLowerBoundInternal(Transformation node) {
        return null;
    }

    @Override
    protected ILSingleDimensional getLowerBoundInternal(Transformation node,
                                                               HashGroupify g) {
        

        // Init
        int[] transformation = node.getGeneralization();
        double result = 0d;

        // Compute loss and lower bound
        HashGroupifyEntry m = g.getFirstEquivalenceClass();
        while (m != null) {
            if (m.count>0) {
                double classResult = 1d;
                // Compute
                m.read();
                for (int dimension = 0; dimension < transformation.length; dimension++) {
                    int value = m.next();
                    int level = transformation[dimension];
                    double share = shares[dimension].getShare(value, level);
                    classResult *= share * shares[dimension].getDomainSize();
                }
                classResult *= m.count;
                result += classResult;
            }
            m = m.nextOrdered;
        }
        
        // Return
        return new ILSingleDimensional(result);
    }
    
    /**
     * For subclasses.
     *
     * @return
     */
    protected DomainShare[] getShares(){
        return this.shares;
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
        this.max = 1d;
        this.shares = new DomainShare[hierarchies.length];
        for (int i = 0; i < shares.length; i++) {

            // Extract info
            String attribute = input.getHeader()[i];
            String[][] hierarchy = definition.getHierarchy(attribute);
            this.shares[i] = new DomainShareMaterialized(hierarchy,
                                                         input.getDictionary().getMapping()[i],
                                                         hierarchies[i].getArray());
            this.max *= hierarchy.length;
        }

        // Determine total number of tuples
        this.tuples = (double)super.getNumRecords(config, input);
        this.max *= this.tuples;
    }
}
