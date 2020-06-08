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
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.reliability.IntervalArithmeticDouble;
import org.deidentifier.arx.reliability.IntervalArithmeticException;

/**
 * (e,d)-Differential Privacy implemented with SafePub as proposed in:
 * 
 * Bild R, Kuhn KA, Prasser F. SafePub: A Truthful Data Anonymization Algorithm With Strong Privacy Guarantees.
 * Proceedings on Privacy Enhancing Technologies. 2018(1):67-87.
 * 
 * SafePub, in turn, is a practical implementation of (k,b)-SDGS which was originally proposed in:
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
     * Indicates if this instance was already initialized. An instance is initialized when it is used to anonymize a dataset.
     * When this model is used more than once, a new subset needs to be drawn before each use (i.e. when "initialized==true").
     * The field is transient, because we need to preserve the subset, when the model is loaded from a project file
     * (indicated by "initialized==false").
     */
    private transient boolean        initialized = false;

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
     * Note: *never* set deterministic to true in production. This parameterization is for testing purposes, only.
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
     * Returns the beta parameter of (k,b)-SDGS
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
    
    @Override
    public void initialize(DataManager manager, ARXConfiguration config){
        
        // Set beta and k if required
        if (beta < 0) {
            
            double epsilonAnon = epsilon;
            if (isDataDependent()) {
                try {
                    IntervalArithmeticDouble ia = new IntervalArithmeticDouble();
                    epsilonAnon = ia.sub(ia.createInterval(epsilon), ia.createInterval(config.getDPSearchBudget())).lower;
                } catch (IntervalArithmeticException e) {
                    throw new RuntimeException(e);
                }
            }
            
            ParameterCalculation pCalc = null;
            try {
                pCalc = new ParameterCalculation(epsilonAnon, delta);
            } catch (IntervalArithmeticException e) {
                throw new RuntimeException(e);
            }
            beta = pCalc.getBeta();
            k = pCalc.getK();
        }
        
        // Perform random sampling iff the model is used for the first time (subset == null)
        // or when it used again (initialized == true). We don't perform random sampling when
        // the model has been de-serialized (subset will be != null and initialized will be false).
        if (subset == null || initialized) {

            // Create RNG
            Random random = deterministic ? new Random(0xDEADBEEF) : new SecureRandom();

            // Create a data subset via sampling based on beta
            Set<Integer> subsetIndices = new HashSet<Integer>();
            int numRecords = manager.getDataGeneralized().getDataLength();
            for (int i = 0; i < numRecords; ++i) {
                if (random.nextDouble() < beta) {
                    subsetIndices.add(i);
                }
            }
            this.subset = DataSubset.create(numRecords, subsetIndices);

        }
        
        initialized = true;
    }

    @Override
    public boolean isAnonymous(Transformation<?> node, HashGroupifyEntry entry) {
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
    public boolean isHeuristicSearchSupported() {
        return isDataDependent();
    }
    
    @Override
    public boolean isHeuristicSearchWithTimeLimitSupported() {
        return false;
    }
    
    @Override
    public boolean isOptimalSearchSupported() {
        return false;
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
        
        try {
            result.addProperty("Uniqueness threshold (k)", getK());
            result.addProperty("Sampling probability (beta)", getBeta());
        } catch (Exception e) {
            // No harm is done if these properties can not be set
        }
        
        return result;
    }

    @Override
    public String toString() {
        return "("+epsilon+","+delta+")-DP";
    }
}
