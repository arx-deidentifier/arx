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

package org.deidentifier.arx.gui.model;

import java.io.Serializable;

/**
 * A model for local recoding
 *
 * @author Fabian Prasser
 */
public class ModelLocalRecoding implements Serializable {
    
    /**
     * Possible modes for local recoding
     * 
     * @author Fabian Prasser
     */
    public static enum LocalRecodingMode {
        SINGLE_PASS,
        ITERATIVE,
        MULTI_PASS,
        FIXPOINT,
        FIXPOINT_ADAPTIVE
    }

    /** SVUID. */
    private static final long serialVersionUID       = -5333464333997155970L;

    /** GS-Factor */
    private double            gsFactor               = 0.05d;

    /** The number of iterations to perform */
    private int               numIterations          = 100;

    /** Is the GS-Factor adaptive */
    private double            adaptionFactor         = 0.05d;

    /** The type of recoding to perform */
    private LocalRecodingMode mode                   = LocalRecodingMode.ITERATIVE;

    /**
     * Getter
     * @return
     */
    public double getAdaptionFactor() {
        return adaptionFactor;
    }

    /**
     * Getter
     * @return
     */
    public double getGsFactor() {
        return gsFactor;
    }

    /**
     * Getter
     * @return
     */
    public LocalRecodingMode getMode() {
        return mode;
    }

    /**
     * Getter
     * @return
     */
    public int getNumIterations() {
        return numIterations;
    }

    /**
     * Setter
     * @param mode
     */
    public void setAdaptionFactor(double adaptionFactor) {
        this.adaptionFactor = adaptionFactor;
    }

    /**
     * Setter
     * @param gsFactor
     */
    public void setGsFactor(double gsFactor) {
        this.gsFactor = gsFactor;
    }

    /**
     * Setter
     * @param mode
     */
    public void setMode(LocalRecodingMode mode) {
        this.mode = mode;
    }
    
    /**
     * Setter
     * @param numIterations
     */
    public void setNumIterations(int numIterations) {
        this.numIterations = numIterations;
    }
}
