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

// TODO Extract class for describing a parameter (min, max, initial, description, validator, etc.)
/**
 * Set of classes representing all available masking parameters.
 * 
 * @author Karol Babioch
 * @author Sandro Schaeffler
 * @author Peter Bock
 * 
 * @param <T>
 */
abstract public class DistributionParameter<T> {

    /**
     * Class representing a double parameter.
     */
    public static class DoubleParameter extends DistributionParameter<Double> {

        /**
         * Creates an instance.
         */
        public DoubleParameter() {
            setType(Double.class);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.deidentifier.arx.masking.variable.DistributionParameter#isValid(java.lang.Object)
         */
        @Override
        public boolean isValid(Double value) {
            return true;
        }

    }

    /**
     * Class representing an integer parameter.
     */
    public static class IntegerParameter extends DistributionParameter<Integer> {

        /**
         * Creates an instance.
         */
        public IntegerParameter() {
            setType(Integer.class);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.deidentifier.arx.masking.variable.DistributionParameter#isValid(java.lang.Object)
         */
        @Override
        public boolean isValid(Integer value) {
            return true;
        }

    }

    /** Description */
    private String   description;

    /** Initial */
    private T        initial;

    /** Maxmimum */
    private T        max;

    /** Minimum */
    private T        min;

    /** Name */
    private String   name;

    /** Type */
    private Class<T> type;

    /** Value */
    private T        value;

    /**
     * Returns the description.
     * @return
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Returns the initial value.
     * @return
     */
    public T getInitial() {
        return this.initial;
    }

    /**
     * Returns the maximum .
     * @return
     */
    public T getMax() {
        return this.max;
    }

    /**
     * Returns the minimum.
     * @return
     */
    public T getMin() {
        return this.min;
    }

    /**
     * Returns the name.
     * @return
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the type.
     * @return
     */
    public Class<T> getType() {
        return this.type;
    }

    /**
     * Returns the value.
     * @return
     */
    public T getValue() {
        return this.value;
    }

    /**
     * Returns whether this value is valid.
     * @param value
     * @return
     */
    public abstract boolean isValid(T value);

    /**
     * Sets the description.
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the initial value.
     * @param initial
     */
    public void setInitial(T initial) {
        this.initial = initial;
    }

    /**
     * Sets the maximum.
     * @param max
     */
    public void setMax(T max) {
        this.max = max;
    }

    /**
     * Sets the minimum.
     * @param min
     */
    public void setMin(T min) {
        this.min = min;
    }

    /**
     * Sets the name.
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the type.
     * @param type
     */
    public void setType(Class<T> type) {
        this.type = type;
    }

    /**
     * Sets the value.
     * @param value
     */
    public void setValue(T value) {
        this.value = value;
    }
}
