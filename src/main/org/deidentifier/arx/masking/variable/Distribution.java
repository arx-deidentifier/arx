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

package org.deidentifier.arx.masking.variable;

/**
 * Abstract superclass for all distributions
 *
 * @author Fabian Kloos
 * @author Fabian Prasser
 * @author Karol Babioch
 */
public abstract class Distribution<T> {

    /** Limit for y */
    private final double yLimit;

    /**
     * Creates an instance.
     * 
     * @param yLimit
     */
    public Distribution(double yLimit) {
        this.yLimit = yLimit;
    }

    /**
     * Creates an instance.
     * 
     * @return
     */
    public double getLimitY() {
        return yLimit;
    }

    /**
     * Returns the maximum.
     * @return
     */
    public abstract T getMaximum();

    /**
     * Returns the minimum.
     * @return
     */
    public abstract T getMinimum();

    /**
     * Returns the value.
     * @param value
     * @return
     */
    public abstract double getValue(T value);

}
