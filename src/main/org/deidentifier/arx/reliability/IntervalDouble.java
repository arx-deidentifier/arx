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

package org.deidentifier.arx.reliability;

import java.io.Serializable;

/**
 * A basic double interval
 * 
 * @author Fabian Prasser
 */
public class IntervalDouble implements Serializable {

    /** SVUID */
    private static final long serialVersionUID = 6012504736748464073L;

    /** Lower */
    public final double       lower;
    /** Upper */
    public final double       upper;

    /**
     * Creates a new instance
     * @param lower
     * @param upper
     */
    IntervalDouble(double lower, double upper) {
        this.lower = lower;
        this.upper = upper;
    }
    
    /**
     * Returns whether this interval contains the given value
     * @param value
     * @return
     */
    public boolean contains(double value) {
        return this.lower <= value && this.upper >= value;
    }

    /**
     * Returns the range of this interval
     * @return
     */
    public double getError() {
        double result = upper - lower;
        return result + Math.ulp(result);
    }
    
    @Override
    public String toString() {
        return "[" + this.lower + ", " + this.upper + "]";
    }
}