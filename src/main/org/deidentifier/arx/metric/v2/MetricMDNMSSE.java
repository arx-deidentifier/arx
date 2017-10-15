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

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.certificate.elements.ElementData;
import org.deidentifier.arx.framework.check.groupify.HashGroupify;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.DataCentroidDistances;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.metric.MetricConfiguration;

/**
 * This class implements a variant of the SSE metric.
 *
 * @author Fabian Prasser
 */
public class MetricMDNMSSE extends AbstractMetricMultiDimensional {

    /** SUID. */
    private static final long          serialVersionUID = -3523080409527197055L;

    /** Distances for each dimension. */
    private DataCentroidDistances<?>[] distances;

    /** We must override this for backward compatibility. Remove, when re-implemented. */
    private final double               gFactor;

    /** We must override this for backward compatibility. Remove, when re-implemented. */
    private final double               gsFactor;

    /** We must override this for backward compatibility. Remove, when re-implemented. */
    private final double               sFactor;

    /** Whether this instance is normalized*/
    private final boolean              normalized;

    /**
     * Default constructor which treats all transformation methods equally.
     */
    public MetricMDNMSSE(){
        // TODO: This metric should be enhanced to
        // (1) support attribute weights
        // (1) support generalized values
        this(false);
    }

    /**
     * Default constructor which treats all transformation methods equally.
     *
     * @param function
     */
    public MetricMDNMSSE(AggregateFunction function){
        this(function, false);
    }
    
    /**
     * A constructor that allows to define a factor weighting generalization and suppression.
     *
     * @param gsFactor A factor [0,1] weighting generalization and suppression.
     *            The default value is 0.5, which means that generalization
     *            and suppression will be treated equally. A factor of 0
     *            will favor suppression, and a factor of 1 will favor
     *            generalization. The values in between can be used for
     *            balancing both methods.
     * @param function
     */
    public MetricMDNMSSE(double gsFactor, AggregateFunction function){
        this(gsFactor, function, false);
    }
    
    /**
     * For internal use only. Default constructor which treats all transformation methods equally.
     * @param normalized
     */
    protected MetricMDNMSSE(boolean normalized){
        this(0.5d, AggregateFunction.ARITHMETIC_MEAN, normalized);
    }

    /**
     * For internal use only. Default constructor which treats all transformation methods equally.
     * @param function
     * @param normalized
     */
    protected MetricMDNMSSE(AggregateFunction function, boolean normalized){
        this(0.5d, function, normalized);
    }
    
    /**
     * For internal use only. A constructor that allows to define a factor weighting generalization and suppression.
     *
     * @param gsFactor A factor [0,1] weighting generalization and suppression.
     *            The default value is 0.5, which means that generalization
     *            and suppression will be treated equally. A factor of 0
     *            will favor suppression, and a factor of 1 will favor
     *            generalization. The values in between can be used for
     *            balancing both methods.
     * @param function
     * @param normalized
     */
    protected MetricMDNMSSE(double gsFactor, AggregateFunction function, boolean normalized){
        super(true, false, false, function);
        if (gsFactor < 0d || gsFactor > 1d) {
            throw new IllegalArgumentException("Parameter must be in [0, 1]");
        }
        this.gsFactor = gsFactor;
        this.sFactor = gsFactor <  0.5d ? 2d * gsFactor : 1d;
        this.gFactor = gsFactor <= 0.5d ? 1d            : 1d - 2d * (gsFactor - 0.5d);
        this.normalized = normalized;
    }
    
    /**
     * Returns the configuration of this metric.
     *
     * @return
     */
    public MetricConfiguration getConfiguration() {
        return new MetricConfiguration(false,                        // monotonic
                                       gsFactor,                     // gs-factor
                                       false,                        // precomputed
                                       0.0d,                         // precomputation threshold
                                       this.getAggregateFunction()   // aggregate function
                                       );
    }
    
    @Override
    public double getGeneralizationFactor() {
        return gFactor;
    }
    
    @Override
    public double getGeneralizationSuppressionFactor() {
        return gsFactor;
    }

    @Override
    public String getName() {
        return (normalized ? "Normalized " : "") + "SSE";
    }
    
    @Override
    public double getSuppressionFactor() {
        return sFactor;
    }

    @Override
    public boolean isAbleToHandleMicroaggregation() {
        return true;
    }
    
    @Override
    public boolean isAbleToHandleClusteredMicroaggregation() {
        return true;
    }

    @Override
    public boolean isGSFactorSupported() {
        return true;
    }

    @Override
    public ElementData render(ARXConfiguration config) {
        ElementData result = new ElementData((normalized ? "Normalized " : "") + "SSE");
        result.addProperty("Aggregate function", super.getAggregateFunction().toString());
        result.addProperty("Monotonic", this.isMonotonic(config.getSuppressionLimit()));
        result.addProperty("Generalization factor", this.getGeneralizationFactor());
        result.addProperty("Suppression factor", this.getSuppressionFactor());
        return result;
    }
    
    @Override
    public String toString() {
        return (normalized ? "Normalized " : "") + "SSE ("+gsFactor+"/"+gFactor+"/"+sFactor+")";
    }

    @Override
    protected ILMultiDimensionalWithBound getInformationLossInternal(Transformation node, HashGroupify g) {
        
        // Prepare
        int dimensions = getDimensions();
        int dimensionsGeneralized = getDimensionsGeneralized();
        
        int[] transformation = node.getGeneralization();
        double[] result = new double[dimensions];
        double[] bound = new double[dimensions];

        // Compute information loss and lower bound
        HashGroupifyEntry m = g.getFirstEquivalenceClass();
        while (m != null) {
            if (m.count>0) {
                m.read();
                for (int dimension=0; dimension<dimensionsGeneralized; dimension++){
                    int value = m.next();
                    int level = transformation[dimension];
                    double distance = (double)m.count * distances[dimension].getDistance(value, level);
                    double average = (double)m.count * distances[dimension].getAverageDistance();
                    result[dimension] += m.isNotOutlier ? distance * gFactor : average * sFactor;
                    bound[dimension] += distance * gFactor;
                }
            }
            m = m.nextOrdered;
        }
        
        // Return information loss and lower bound
        return new ILMultiDimensionalWithBound(super.createInformationLoss(result),
                                               super.createInformationLoss(bound));
        
    }

    @Override
    protected ILMultiDimensionalWithBound getInformationLossInternal(Transformation node, HashGroupifyEntry entry) {

        // Init
        int dimensions = getDimensions();
        int dimensionsGeneralized = getDimensionsGeneralized();
        
        double[] result = new double[dimensions];
        int[] transformation = node.getGeneralization();

        // Compute
        entry.read();
        for (int dimension = 0; dimension < dimensionsGeneralized; dimension++) {
            int value = entry.next();
            int level = transformation[dimension];
            double distance = (double)entry.count * distances[dimension].getDistance(value, level);
            result[dimension] = distance;
        }

        // Return
        return new ILMultiDimensionalWithBound(super.createInformationLoss(result));
    }
    
    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Transformation node) {
        return null;
    }
    
    @Override
    protected AbstractILMultiDimensional getLowerBoundInternal(Transformation node, HashGroupify g) {
        
        // Prepare
        int dimensions = getDimensions();
        int dimensionsGeneralized = getDimensionsGeneralized();
        int[] transformation = node.getGeneralization();
        double[] bound = new double[dimensions];

        // Compute lower bound
        HashGroupifyEntry m = g.getFirstEquivalenceClass();
        while (m != null) {
            if (m.count>0) {
                m.read();
                for (int dimension=0; dimension<dimensionsGeneralized; dimension++){
                    int value = m.next();
                    int level = transformation[dimension];
                    double distance = (double)m.count * distances[dimension].getDistance(value, level);
                    bound[dimension] += distance * gFactor;
                }
            }
            m = m.nextOrdered;
        }
        
        // Return
        return super.createInformationLoss(bound);
    }

    @Override
    protected void initializeInternal(final DataManager manager,
                                      final DataDefinition definition, 
                                      final Data input, 
                                      final GeneralizationHierarchy[] hierarchies, 
                                      final ARXConfiguration config) {
        
        // Prepare weights
        super.initializeInternal(manager, definition, input, hierarchies, config);

        // Save domain shares
        this.distances = manager.getCentroidDistances(normalized);
        
        // Min and max
        double[] min = new double[getDimensions()];
        Arrays.fill(min, 0d);
        double[] max = new double[getDimensions()];
        for (int i=0; i<distances.length; i++) {
            max[i] = this.distances[i].getAverageDistance() * super.getNumRecords(config, input);
        }
        super.setMin(min);
        super.setMax(max);
    }
}
