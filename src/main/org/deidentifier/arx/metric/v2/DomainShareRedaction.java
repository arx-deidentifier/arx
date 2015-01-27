/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
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

import org.deidentifier.arx.aggregates.HierarchyBuilderRedactionBased;

/**
 * This class represents a set of domain shares for an attribute. The shares are derived from a functional
 * redaction-based generalization hierarchy
 * 
 * @author Fabian Prasser
 */
public class DomainShareRedaction implements DomainShare {

    /** SVUID. */
    private static final long serialVersionUID = 2015677962393713964L;

    /** Meta-data about the nature of the domain of the attribute. */
    private double            maxValueLength;

    /** Meta-data about the nature of the domain of the attribute. */
    private double            domainSize;

    /** Meta-data about the nature of the domain of the attribute. */
    private double            alphabetSize;

    /** For interpolating linearly from input to output range. */
    private double            minInput;

    /** For interpolating linearly from input to output range. */
    private double            maxInput;

    /** For interpolating linearly from input to output range. */
    private double            minOutput;

    /** For interpolating linearly from input to output range. */
    private double            maxOutput;

    /**
     * Creates a new set of domain shares derived from the given functional redaction-based hierarchy.
     *
     * @param builder
     */
    public DomainShareRedaction(HierarchyBuilderRedactionBased<?> builder) {

        // Store base data
        this.domainSize = builder.getDomainSize();
        this.alphabetSize = builder.getAlphabetSize();
        this.maxValueLength = builder.getMaxValueLength();
        
        // Prepare values for interpolation
        this.minInput = 1d / Math.pow(alphabetSize, maxValueLength);
        this.maxInput = 1d;
        this.minOutput = 1d / domainSize;
        this.maxOutput = 1d;
    }

    /**
     * Returns the size of the domain.
     *
     * @return
     */
    @Override
    public double getDomainSize() {
        return domainSize;
    }

    /**
     * Returns the share of the given value.
     *
     * @param value
     * @param level
     * @return
     */
    @Override
    public double getShare(int value, int level) {
        
        // Compute and interpolate
        double input = Math.pow(alphabetSize, (double)level - maxValueLength);
        return (input - minInput) / (maxInput - minInput) * (maxOutput - minOutput) + minOutput;
        
    }
}
