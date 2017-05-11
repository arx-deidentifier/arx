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

import org.deidentifier.arx.certificate.elements.ElementData;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyMatrix;
import org.deidentifier.arx.risk.msu.SUDA2;

/**
 * This criterion ensures that all keys are at least of a given size
 * 
 * @author Fabian Prasser
 */
public class MinimalKeySize extends KeyBasedCriterion{

    /** SVUID */
    private static final long serialVersionUID = 4317638260873614586L;

    /** Parameter */
    private final int         minKeySize;

    /**
     * Creates a new instance of this criterion.
     *  
     * @param minKeySize
     */
    public MinimalKeySize(int minKeySize){
        super(false, true);
        this.minKeySize = minKeySize;
    }
    
    @Override
    public MinimalKeySize clone() {
        return new MinimalKeySize(this.minKeySize);
    }

    @Override
    public boolean isLocalRecodingSupported() {
        return true;
    }

    @Override
    public ElementData render() {
        ElementData result = new ElementData("Minimal key size");
        result.addProperty("Threshold", minKeySize);
        return result;
    }

    @Override
    public String toString() {
        return "("+minKeySize+")-minimal-key-size";
    }

    @Override
    protected boolean isFulfilled(HashGroupifyMatrix matrix) {
        return new SUDA2(matrix.getMatrix().getMatrix()).isKeyPresent(minKeySize);
    }
}