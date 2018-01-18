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
    
    /**
     * Default value. 
     * @return
     */
    public static double getDefaultAccuracy() {
        return 1e-6;
    }
    
    /**
     * Default value. 
     * @return
     */
    public static boolean getDefaultDeterministic() {
        return true;
    }

    /**
     * Default value. 
     * @return
     */
    public static int getDefaultIterationsPerTry() {
        return 1000;
    }

    /**
     * Default value. 
     * @return
     */
    public static int getDefaultIterationsTotal() {
        return 10000;
    }

    /**
     * Default value. Returns a set of start values for the solver in range [0,1][0,1]
     * @return
     */
    public static double[][] getDefaultStartValues() {
        double[][] result = new double[16][];
        int index = 0;
        for (double d1 = 0d; d1 < 1d; d1 += 0.33d) {
            for (double d2 = 0d; d2 < 1d; d2 += 0.33d) {
                result[index++] = new double[] { d1, d2 };
            }
        }
        return result;
    }

    /**
     * Default value. 
     * @return
     */
    public static int getDefaultTimePerTry() {
        return 100;
    }

    /**
     * Default value. 
     * @return
     */
    public static int getDefaultTimeTotal() {
        return 1000;
    }

    /** Modified*/
    private boolean modified = false;
    
    /**
     * Creates a new instance
     */
    private ARXSolverConfiguration() {
        
        // Set default values
        this.accuracy(getDefaultAccuracy());
        this.iterationsPerTry(getDefaultIterationsPerTry());
        this.iterationsTotal(getDefaultIterationsTotal());
        this.timePerTry(getDefaultTimePerTry());
        this.timeTotal(getDefaultTimeTotal());
        this.setDeterministic(getDefaultDeterministic());
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
     * Returns whether the solving process is deterministic
     * @return
     */
    public boolean isDeterministic() {
        return super.getStartValues() != null;
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
        if ((super.getStartValues() == null && values != null) || 
            (values == null && super.getStartValues() != null) ||
            (values != null && !Arrays.equals(super.getStartValues(), values))) {
            modified = true;
        }
        return super.preparedStartValues(values);
    }
    
    /**
     * Sets the solving process to be deterministic
     * @param deterministic
     * @return
     */
    public ARXSolverConfiguration setDeterministic(boolean deterministic) {
        return preparedStartValues(deterministic ? getDefaultStartValues() : null);
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
