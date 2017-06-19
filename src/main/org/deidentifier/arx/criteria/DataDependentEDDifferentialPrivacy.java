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
 * Data-dependent (e,d)-Differential Privacy implemented with (k,b)-SDGS as proposed in:
 * 
 * Ninghui Li, Wahbeh H. Qardaji, Dong Su:
 * On sampling, anonymization, and differential privacy or, k-anonymization meets differential privacy. 
 * Proceedings of ASIACCS 2012. pp. 32-33
 * 
 * @author Raffael Bild
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class DataDependentEDDifferentialPrivacy extends AbstractEDDifferentialPrivacy {
    
    /** SVUID */
    private static final long        serialVersionUID = 242579895476272606L;

    /** Parameter */
    private final double             epsilonAnon;
    /** Parameter */
    private final double             epsilonSearch;
    /** Parameter */
    private final int                steps;
    /** Parameter */
    private final double             delta;
    /** Parameter */
    private final int                k;
    /** Parameter */
    private final double             beta;

    /**
     * Creates a new instance
     * @param epsilonAnon
     * @param epsilonSearch
     * @param delta
     * @param steps
     */
    public DataDependentEDDifferentialPrivacy(double epsilonAnon, double epsilonSearch, double delta, int steps) {
        super(delta);
        this.epsilonAnon = epsilonAnon;
        this.epsilonSearch = epsilonSearch;
        this.beta = calculateBeta(epsilonAnon);
        this.k = calculateK(getDelta(), epsilonAnon, this.beta);
        this.steps = steps;
        this.delta = delta;
    }
    
    /**
     * Creates a new instance which may be configured to produce deterministic output.
     * Note: *never* use this in production. It is implemented for testing purposes, only.
     * 
     * @param epsilonAnon
     * @param epsilonSearch
     * @param delta
     * @param steps
     * @param deterministic
     */
    public DataDependentEDDifferentialPrivacy(double epsilonAnon, double epsilonSearch,
                                              double delta, int steps, boolean deterministic) {
        super(delta, deterministic);
        this.epsilonAnon = epsilonAnon;
        this.epsilonSearch = epsilonSearch;
        this.beta = calculateBeta(epsilonAnon);
        this.k = calculateK(getDelta(), epsilonAnon, this.beta);
        this.steps = steps;
        this.delta = delta;
    }
    

    @Override
    public DataDependentEDDifferentialPrivacy clone() {
        return new DataDependentEDDifferentialPrivacy(this.getEpsilonAnon(), this.getEpsilonSearch(),
                                                      this.getDelta(), this.getSteps(), this.isDeterministic());
    }
    
    @Override
    public double getBeta() {
        return beta;
    }
    
    /**
     * Returns the epsilon anon parameter
     * @return
     */
    public double getEpsilonAnon() {
        return epsilonAnon;
    }
    
    /**
     * Returns the epsilon search parameter
     * @return
     */
    public double getEpsilonSearch() {
        return epsilonAnon;
    }
    
    /**
     * Returns the delta parameter
     * @return
     */
    public double getDelta() {
        return delta;
    }
    
    @Override
    public double getEpsilon() {
        return epsilonAnon + epsilonSearch;
    }
    
    /**
     * Returns the steps parameter
     * @return
     */
    public int getSteps() {
        return steps;
    }
    
    @Override
    public int getK() {
        return k;
    }
}
