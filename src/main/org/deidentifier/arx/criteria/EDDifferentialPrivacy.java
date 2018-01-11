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
package org.deidentifier.arx.criteria;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataGeneralizationScheme;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.certificate.elements.ElementData;
import org.deidentifier.arx.dp.ParameterCalculation;
import org.deidentifier.arx.dp.ParameterCalculationDouble;
import org.deidentifier.arx.dp.ParameterCalculationIntervalDouble;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.reliability.IntervalArithmeticException;

/**
 * (e,d)-Differential Privacy implemented with (k,b)-SDGS as proposed in:
 * 
 * Ninghui Li, Wahbeh H. Qardaji, Dong Su:
 * On sampling, anonymization, and differential privacy or, k-anonymization meets differential privacy. 
 * Proceedings of ASIACCS 2012. pp. 32-33
 * 
 * @author Raffael Bild
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class EDDifferentialPrivacy extends ImplicitPrivacyCriterion {
    
    /** SVUID */
    private static final long        serialVersionUID = 242579895476272606L;

    /** Parameter */
    private final double             epsilon;
    /** Parameter */
    private final double             delta;
    /** Parameter */
    private int                      k;
    /** Parameter */
    private double                   beta;
    /** Parameter */
    private DataSubset               subset;
    /** Parameter */
    private transient boolean        deterministic    = false;
    /** Parameter */
    private DataGeneralizationScheme generalization;

    /**
     * Creates a new instance which is data-independent iff generalization is not null
     * @param epsilon
     * @param delta
     * @param generalization
     */
    public EDDifferentialPrivacy(double epsilon, double delta, 
                                 DataGeneralizationScheme generalization) {
        this(epsilon, delta, generalization, false);
    }
    
    /**
     * Creates a new data-dependent instance
     * @param epsilon
     * @param delta
     */
    public EDDifferentialPrivacy(double epsilon, double delta) {
        this(epsilon, delta, null, false);
    }
    
    /**
     * Creates a new instance which may be configured to produce deterministic output.
     * Note: *never* use this in production. It is implemented for testing purposes, only.
     * 
     * @param epsilon
     * @param delta
     * @param generalization
     * @param deterministic
     */
    public EDDifferentialPrivacy(double epsilon, double delta, 
                                 DataGeneralizationScheme generalization,
                                 boolean deterministic) {
        super(false, false);
        this.epsilon = epsilon;
        this.delta = delta;
        this.generalization = generalization;
        this.beta = -1d;
        this.k = -1;
        this.deterministic = deterministic;
    }
    

    @Override
    public EDDifferentialPrivacy clone() {
        return new EDDifferentialPrivacy(this.getEpsilon(), this.getDelta(), this.getGeneralizationScheme());
    }

    /**
     * Returns the k parameter of (k,b)-SDGS
     * @return
     */
    public double getBeta() {
        if (beta < 0d) { throw new RuntimeException("This instance has not been initialized yet"); }
        return beta;
    }
    
    @Override
    public DataSubset getDataSubset() {
        return subset;
    }

    /**
     * Returns the delta parameter of (e,d)-DP
     * @return
     */
    public double getDelta() {
        return delta;
    }

    /**
     * Returns the epsilon parameter of (e,d)-DP
     * @return
     */
    public double getEpsilon() {
        return epsilon;
    }

    /**
     * Returns the defined generalization scheme
     * @return
     */
    public DataGeneralizationScheme getGeneralizationScheme() {
        return this.generalization;
    }

    /**
     * Returns the k parameter of (k,b)-SDGS
     * @return
     */
    public int getK() {
        if (k < 0) { throw new RuntimeException("This instance has not been initialized yet"); }
        return k;
    }

    @Override
    public int getMinimalClassSize() {
        return getK();
    }
    
    @Override
    public int getRequirements(){
        // Requires two counters
        return ARXConfiguration.REQUIREMENT_COUNTER |
               ARXConfiguration.REQUIREMENT_SECONDARY_COUNTER;
    }

    /**
     * Sets k and beta and creates a random sample based on beta if required
     *
     * @param manager
     * @param config
     */
    public void initialize(DataManager manager, ARXConfiguration config){
        
        // Set beta and k if required
        if (beta < 0) {
            
            ParameterCalculation pCalc = null;
            double epsilonAnon = epsilon - (isDataDependent() ? config.getDPSearchBudget() : 0d);
            if (config.isReliableAnonymizationEnabled()) {
                try {
                    pCalc = new ParameterCalculationIntervalDouble(epsilonAnon, delta);
                } catch (IntervalArithmeticException e) {
                    throw new RuntimeException(e);
                }
            } else {
                pCalc = new ParameterCalculationDouble(epsilonAnon, delta);
            }
                    
            beta = pCalc.getBeta();
            k = pCalc.getK();
        }
        
        // If the subset has already been created
        if (subset != null) {
            return;
        }

        // Create RNG
        Random random;
        if (deterministic) {
            random = new Random(0xDEADBEEF);
        } else {
            random = new SecureRandom();
        }

        // Create a data subset via sampling based on beta
        Set<Integer> subsetIndices = new HashSet<Integer>();
        int records = manager.getDataGeneralized().getDataLength();
        for (int i = 0; i < records; ++i) {
            if (random.nextDouble() < beta) {
                subsetIndices.add(i);
            }
        }
        this.subset = DataSubset.create(records, subsetIndices);
    }

    @Override
    public boolean isAnonymous(Transformation node, HashGroupifyEntry entry) {
        return entry.count >= getK();
    }
    
    /**
     * Returns whether this instance is data-dependent
     * @return
     */
    public boolean isDataDependent() {
        return this.generalization == null;
    }
    
    /**
     * Returns whether this instance is deterministic
     * @return
     */
    public boolean isDeterministic() {
        return deterministic;
    }

    @Override
    public boolean isLocalRecodingSupported() {
        return false;
    }
    
    @Override
    public boolean isMinimalClassSizeAvailable() {
        return true;
    }

    @Override
    public boolean isSubsetAvailable() {
        return subset != null;
    }
    
    @Override
    public ElementData render() {
        ElementData result = new ElementData("Differential privacy");
        result.addProperty("Epsilon", epsilon);
        result.addProperty("Delta", delta);
        result.addProperty("Uniqueness threshold (k)", getK());
        result.addProperty("Sampling probability (beta)", getBeta());
        return result;
    }

    @Override
    public String toString() {
        return "(" + epsilon + "," + delta + ")-DP";
    }
}
