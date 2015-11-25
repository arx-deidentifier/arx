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
package org.deidentifier.arx;

import java.util.Arrays;

import de.linearbits.newtonraphson.NewtonRaphsonConfiguration;

/**
 * Runtime configuration for the solver
 * @author prasser
 *
 */
public class ARXSolverConfiguration extends NewtonRaphsonConfiguration<ARXSolverConfiguration>{

    /** SVUID*/
    private static final long serialVersionUID = -7122709349147064168L;

    /**
     * Creates a new instance
     * @return
     */
    public static ARXSolverConfiguration create() {
        return new ARXSolverConfiguration();
    }
    
    /** Modified*/
    private boolean modified = false;
    
    /**
     * Creates a new instance
     */
    private ARXSolverConfiguration() {
        
        // Set default values
        this.accuracy(1e-6);
        this.iterationsPerTry(1000);
        this.iterationsTotal(10000);
        this.timePerTry(100);
        this.timeTotal(1000);        
    }

    @Override
    public ARXSolverConfiguration accuracy(double arg0) {
        if (arg0 != super.getAccuracy()) {
            modified = true;
        }
        return super.accuracy(arg0);
    }

    /**
     * Clones this config
     */
    public ARXSolverConfiguration clone() {
        ARXSolverConfiguration result = ARXSolverConfiguration.create();
        result.accuracy(this.getAccuracy());
        result.iterationsPerTry(this.getIterationsPerTry());
        result.iterationsTotal(this.getIterationsTotal());
        result.timePerTry(this.getTimePerTry());
        result.timeTotal(this.getTimeTotal());
        result.preparedStartValues(this.getStartValues().clone());
        return result;
    }

    /**
     * Modified
     * @return
     */
    public boolean isModified() {
        return this.modified;
    }

    @Override
    public ARXSolverConfiguration iterationsPerTry(int arg0) {
        if (arg0 != super.getIterationsPerTry()) {
            modified = true;
        }
        return super.iterationsPerTry(arg0);
    }

    @Override
    public ARXSolverConfiguration iterationsTotal(int arg0) {
        if (arg0 != super.getIterationsTotal()) {
            modified = true;
        }
        return super.iterationsTotal(arg0);
    }

    @Override
    public ARXSolverConfiguration preparedStartValues(double[][] values) {
        if (super.getStartValues() == null || (values != null && !Arrays.equals(super.getStartValues(), values))) {
            modified = true;
        }
        return super.preparedStartValues(values);
    }

    /**
     * Modified
     */
    public void setUnmodified() {
        this.modified = false;
    }

    @Override
    public ARXSolverConfiguration timePerTry(int arg0) {
        if (arg0 != super.getTimePerTry()) {
            modified = true;
        }
        return super.timePerTry(arg0);
    }
    
    @Override
    public ARXSolverConfiguration timeTotal(int arg0) {
        if (arg0 != super.getTimeTotal()) {
            modified = true;
        }
        return super.timeTotal(arg0);
    }
}
