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
package org.deidentifier.arx.dp;

/**
 * Abstract base class for implementations of the exponential mechanism
 * 
 * @author Raffael Bild
 */
public abstract class AbstractExponentialMechanism<T,S> {
    
    /**
     * Returns a random sampled value
     * @return
     */
    public abstract T sample();
    
    /**
     * Sets the distribution to sample from.
     * The arrays values and scores have to have the same length.
     * @param values
     * @param scores
     */
    public void setDistribution(T[] values, S[]scores) {
        // Check arguments
        if (values.length == 0) {
            throw new RuntimeException("No values supplied");
        }
        if (values.length != scores.length) {
            throw new RuntimeException("Number of scores and values must be identical");
        }
    }
}
