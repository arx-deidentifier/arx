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

import org.deidentifier.arx.DataGeneralizationScheme;

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
public class EDDifferentialPrivacy extends EDDifferentialPrivacyAbstract {
    
    /** SVUID */
    private static final long        serialVersionUID = 242579895476272606L;

    /** Parameter */
    private final double             epsilon;
    /** Parameter */
    private final int                k;
    /** Parameter */
    private final double             beta;
    /** Parameter */
    private DataGeneralizationScheme generalization;

    /**
     * Creates a new instance
     * 
     * @param epsilon
     * @param delta
     * @param generalization
     */
    public EDDifferentialPrivacy(double epsilon, double delta, 
                                 DataGeneralizationScheme generalization) {
        this(epsilon, delta, generalization, false);
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
        super(delta, deterministic);
        this.epsilon = epsilon;
        this.generalization = generalization;
        this.beta = calculateBeta(epsilon);
        this.k = calculateK(getDelta(), epsilon, this.beta);
    }
    
    /**
     * Private constructor used for cloning instances.
     * 
     * @param epsilon
     * @param delta
     * @param beta
     * @param k
     * @param generalization
     * @param deterministic
     */
    private EDDifferentialPrivacy(double epsilon, double delta,
                                  double beta, int k,
                                  DataGeneralizationScheme generalization,
                                  boolean deterministic) {
        super(delta, deterministic);
        this.epsilon = epsilon;
        this.generalization = generalization;
        this.beta = beta;
        this.k = k;
    }

    @Override
    public EDDifferentialPrivacy clone() {
        return new EDDifferentialPrivacy(this.getEpsilon(), this.getDelta(), this.getBeta(), this.getK(),
                                         this.getGeneralizationScheme(), this.isDeterministic());
    }
    
    @Override
    public double getBeta() {
        return beta;
    }
    
    @Override
    public double getEpsilon() {
        return epsilon;
    }
    
    @Override
    public int getK() {
        return k;
    }

    /**
     * Returns the defined generalization scheme
     * @return
     */
    public DataGeneralizationScheme getGeneralizationScheme() {
        return this.generalization;
    }
}
