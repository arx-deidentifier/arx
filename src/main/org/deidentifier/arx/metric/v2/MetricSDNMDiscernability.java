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

package org.deidentifier.arx.metric.v2;

import org.apache.commons.math3.fraction.BigFraction;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.certificate.elements.ElementData;
import org.deidentifier.arx.criteria.EDDifferentialPrivacy;
import org.deidentifier.arx.framework.check.groupify.HashGroupify;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.metric.MetricConfiguration;

/**
 * This class provides an implementation of the non-monotonic DM metric.
 * TODO: Add reference
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 * @author Raffael Bild
 */
public class MetricSDNMDiscernability extends AbstractMetricSingleDimensional {
    
    /** SVUID. */
    private static final long serialVersionUID = -8573084860566655278L;

    /** Total number of rows. */
    private long              numRows          = -1;

    /** Minimal size of equivalence classes enforced by the differential privacy model */
    private long              k                = -1;

    /**
     * Creates a new instance.
     */
    protected MetricSDNMDiscernability() {
        super(true, false, false);
    }

    /**
     * For subclasses.
     *
     * @param monotonicWithGeneralization
     * @param monotonicWithSuppression
     */
    MetricSDNMDiscernability(boolean monotonicWithGeneralization, boolean monotonicWithSuppression) {
        super(monotonicWithGeneralization, monotonicWithSuppression, false);
    }

    @Override
    public ILSingleDimensional createMaxInformationLoss() {
        Double rows = getNumTuples();
        if (rows == null) {
            throw new IllegalStateException("Metric must be initialized first");
        } else {
            return new ILSingleDimensional(rows * rows);
        }
    }
    
    @Override
    public ILSingleDimensional createMinInformationLoss() {
        Double rows = getNumTuples();
        if (rows == null) {
            throw new IllegalStateException("Metric must be initialized first");
        } else {
            return new ILSingleDimensional(rows);
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
    /**
     * Implements the score function described in Section 5.2 of the article
     * 
     * Bild R, Kuhn KA, Prasser F. SafePub: A Truthful Data Anonymization Algorithm With Strong Privacy Guarantees.
     * Proceedings on Privacy Enhancing Technologies. 2018(1):67-87.
     */
    public ILScore getScore(final Transformation<?> node, final HashGroupify groupify) {
        
        if (k < 0 || numRows < 0) {
            throw new RuntimeException("Parameters required for differential privacy have not been initialized yet");
        }
        
        // Prepare
        int numSuppressed = 0;
        BigFraction penaltyNotSuppressed = BigFraction.ZERO;
        
        // Sum up penalties. The casts to long are required to avoid integer overflows
        // when large numbers are being multiplied.
        HashGroupifyEntry m = groupify.getFirstEquivalenceClass();
        while (m != null) {
            if (m.isNotOutlier) {
                penaltyNotSuppressed = penaltyNotSuppressed.add(new BigFraction((long)m.count * (long)m.count));
            } else {
                numSuppressed += m.count;
            }
            numSuppressed += m.pcount - m.count;
            m = m.nextOrdered;
        }
        BigFraction penaltySuppressed = new BigFraction(numRows * (long)numSuppressed);
        
        // Adjust sensitivity and multiply with -1 so that higher values are better
        BigFraction score = BigFraction.MINUS_ONE.multiply(penaltySuppressed.add(penaltyNotSuppressed));
        score = score.divide(new BigFraction(numRows).multiply((k == 1) ? new BigFraction(5) : new BigFraction(k * k).divide(new BigFraction(k - 1)).add(BigFraction.ONE)));
        
        // Return score
        return new ILScore(score);
    }
    
    @Override
    public boolean isScoreFunctionSupported() {
        return true;
    }

    @Override
    public ElementData render(ARXConfiguration config) {
        ElementData result = new ElementData("Discernibility");
        result.addProperty("Monotonic", this.isMonotonic(config.getSuppressionLimit()));
        return result;
    }

    @Override
    public String toString() {
        return "Non-monotonic discernability";
    }
    
    @Override
    protected ILSingleDimensionalWithBound getInformationLossInternal(final Transformation<?> node, final HashGroupify g) {
        
        double rows = getNumTuples();
        double dm = 0;
        double dmStar = 0;
        HashGroupifyEntry m = g.getFirstEquivalenceClass();
        while (m != null) {
            if (m.count>0){
                double count = (double)m.count;
                double current = count * count;
                dmStar += current;
                dm += m.isNotOutlier ? current : rows * count;
            }
            m = m.nextOrdered;
        }
        return new ILSingleDimensionalWithBound(dm, dmStar);
    }

    @Override
    protected ILSingleDimensionalWithBound getInformationLossInternal(Transformation<?> node, HashGroupifyEntry entry) {
        return new ILSingleDimensionalWithBound(entry.count);
    }
    
    @Override
    protected ILSingleDimensional getLowerBoundInternal(Transformation<?> node) {
        return null;
    }

    @Override
    protected ILSingleDimensional getLowerBoundInternal(Transformation<?> node,
                                                        HashGroupify groupify) {
        double lowerBound = 0;
        HashGroupifyEntry m = groupify.getFirstEquivalenceClass();
        while (m != null) {
            lowerBound += (m.count>0) ? ((double) m.count * (double) m.count) : 0;
            m = m.nextOrdered;
        }
        return new ILSingleDimensional(lowerBound);
    }
    
    @Override
    protected void initializeInternal(final DataManager manager,
                                      final DataDefinition definition, 
                                      final Data input, 
                                      final GeneralizationHierarchy[] hierarchies, 
                                      final ARXConfiguration config) {
        
        super.initializeInternal(manager, definition, input, hierarchies, config);

        // Store minimal size of equivalence classes and total number of rows
        if (config.isPrivacyModelSpecified(EDDifferentialPrivacy.class)) {
            EDDifferentialPrivacy dpCriterion = config.getPrivacyModel(EDDifferentialPrivacy.class);
            numRows = input.getDataLength();
            k = dpCriterion.getMinimalClassSize();
        }
    }
}

