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

package org.deidentifier.arx.masking.variable;

// TODO Extract class for describing a parameter (min, max, initial, description, validator, etc.)
abstract public class DistributionParameter<T> {

    private String name;
    private String description;
    private T value;
    private T initial;
    private T min;
    private T max;
    private Class<T> type;

    public void setName(String name) {

        this.name = name;

    }

    public String getName() {

        return this.name;

    }

    public void setValue(T value) {

        this.value = value;

    }

    public T getValue() {

        return this.value;

    }

    public void setMin(T min) {

        this.min = min;

    }

    public void setMax(T max) {

        this.max = max;

    }

    public T getMin() {

        return this.min;

    }

    public T getMax() {

        return this.max;

    }

    public void setInitial(T initial) {

        this.initial = initial;

    }

    public T getInitial() {

        return this.initial;

    }

    public void setDescription(String description) {

        this.description = description;

    }

    public String getDescription() {

        return this.description;

    }

    public Class<T> getType() {

        return this.type;

    }

    public void setType(Class<T> type) {

        this.type = type;

    }

    public abstract boolean isValid(T value);

    // TODO: Rename
    public static class Int extends DistributionParameter<Integer> {

        public Int() {

            setType(Integer.class);

        }

        @Override
        public boolean isValid(Integer value) {

            return true;

        }

    }

    // TODO: Rename
    public static class Dou extends DistributionParameter<Double> {

        public Dou() {

            setType(Double.class);

        }

        @Override
        public boolean isValid(Double value) {

            return true;

        }

    }

}
