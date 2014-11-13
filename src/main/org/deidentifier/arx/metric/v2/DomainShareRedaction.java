/*
 * ARX: Powerful Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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
