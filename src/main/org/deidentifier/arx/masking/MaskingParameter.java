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
package org.deidentifier.arx.masking;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Set of classes describing all available masking parameters
 *
 * @author Karol Babioch
 */
abstract public class MaskingParameter<T> implements Serializable {

    /**
     * Class representing a boolean parameter.
     * 
     * @author Karol Babioch
     *
     */
    public static class ParameterBoolean extends MaskingParameter<Boolean> {

        /** Description */
        public static final ParameterDescription<Boolean> description      = new ParameterDescription<Boolean>(Boolean.class, "Boolean") {

                                                                               /** SVUID */
                                                                               private static final long serialVersionUID = 7248402123389578028L;

                                                                               /*
                                                                                * (non-Javadoc)
                                                                                * @see org.deidentifier.arx.masking.MaskingParameter.ParameterDescription#newInstance()
                                                                                */
                                                                               @Override
                                                                               public MaskingParameter<Boolean> newInstance() {
                                                                                   return BOOLEAN;
                                                                               }

                                                                               /*
                                                                                * (non-Javadoc)
                                                                                * @see org.deidentifier.arx.masking.MaskingParameter.ParameterDescription#newInstance(java.lang.String)
                                                                                */
                                                                               @Override
                                                                               public MaskingParameter<Boolean> newInstance(String name) {
                                                                                   return createBoolean(name);
                                                                               }

                                                                           };

        /** SVUID */
        private static final long                         serialVersionUID = 3591704008421371526L;

        /**
         * Creates an instance.
         */
        public ParameterBoolean() {
            this("");
        }

        /**
         * Creates an instance.
         * 
         * @param name
         */
        public ParameterBoolean(String name) {
            super(name);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.deidentifier.arx.masking.MaskingParameter#getDescription()
         */
        @Override
        public ParameterDescription<Boolean> getDescription() {
            return description;
        }
    }

    /**
     * Class representing descriptions for parameters.
     * @author Karol Babioch
     *
     * @param <T>
     */
    public static abstract class ParameterDescription<T> implements Serializable {

        /** SVUID */
        private static final long serialVersionUID = 7809765388058884669L;

        /** Class */
        private Class<?>          clazz;

        /** Label */
        private String            label;

        /**
         * Creates an instance.
         * 
         * @param clazz
         * @param label
         */
        private ParameterDescription(Class<T> clazz, String label) {
            this.clazz = clazz;
            this.label = label;
        }

        /**
         * Returns the label
         * @return
         */
        public String getLabel() {
            return this.label;
        }

        /**
         * Returns the class
         * @return
         */
        public Class<?> getWrappedClass() {
            return clazz;
        }

        /**
         * Returns new instance.
         * @return
         */
        public abstract MaskingParameter<T> newInstance();

        /**
         * Returns new instance with this name
         * @param name
         * @return
         */
        public abstract MaskingParameter<T> newInstance(String name);

    }

    /**
     * Class representing a double parameter.
     * 
     * @author Karol Babioch
     *
     */
    public static class ParameterDouble extends MaskingParameter<Double> {

        /** Description */
        public static final ParameterDescription<Double> description      = new ParameterDescription<Double>(Double.class, "Double") {

                                                                              /** SVUID */
                                                                              private static final long serialVersionUID = -4811531702128826389L;

                                                                              /*
                                                                               * (non-Javadoc)
                                                                               * @see org.deidentifier.arx.masking.MaskingParameter.ParameterDescription#newInstance()
                                                                               */
                                                                              @Override
                                                                              public MaskingParameter<Double> newInstance() {
                                                                                  return DOUBLE;
                                                                              }

                                                                              /*
                                                                               * (non-Javadoc)
                                                                               * @see org.deidentifier.arx.masking.MaskingParameter.ParameterDescription#newInstance(java.lang.String)
                                                                               */
                                                                              @Override
                                                                              public MaskingParameter<Double> newInstance(String name) {
                                                                                  return createDouble(name);
                                                                              }
                                                                          };

        /** SVUID */
        private static final long                        serialVersionUID = 4414119523100173055L;

        /**
         * Creates an instance.
         */
        public ParameterDouble() {
            this("");
        }

        /**
         * Creates an instance.
         * 
         * @param name
         */
        public ParameterDouble(String name) {
            super(name);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.deidentifier.arx.masking.MaskingParameter#getDescription()
         */
        @Override
        public ParameterDescription<Double> getDescription() {
            return description;
        }

    }

    /**
     * Class representing an integer parameter.
     * 
     * @author Karol Babioch
     *
     */
    public static class ParameterInteger extends MaskingParameter<Integer> {

        /** Description */
        public static final ParameterDescription<Integer> description      = new ParameterDescription<Integer>(Integer.class, "Integer") {

                                                                               /** SVUID */
                                                                               private static final long serialVersionUID = -2152324531271195806L;

                                                                               /*
                                                                                * (non-Javadoc)
                                                                                * @see org.deidentifier.arx.masking.MaskingParameter.ParameterDescription#newInstance()
                                                                                */
                                                                               @Override
                                                                               public MaskingParameter<Integer> newInstance() {
                                                                                   return INTEGER;
                                                                               }

                                                                               /*
                                                                                * (non-Javadoc)
                                                                                * @see org.deidentifier.arx.masking.MaskingParameter.ParameterDescription#newInstance(java.lang.String)
                                                                                */
                                                                               @Override
                                                                               public MaskingParameter<Integer> newInstance(String name) {
                                                                                   return createInteger(name);
                                                                               }

                                                                           };
        /** SVUID */
        private static final long                         serialVersionUID = 4414119523100173055L;

        /**
         * Creates an instance.
         */
        public ParameterInteger() {
            this("");
        }

        /**
         * Creates an instance.
         * 
         * @param name
         */
        public ParameterInteger(String name) {
            super(name);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.deidentifier.arx.masking.MaskingParameter#getDescription()
         */
        @Override
        public ParameterDescription<Integer> getDescription() {
            return description;
        }

    }

    /**
     * Class representing a string parameter.
     * 
     * @author Karol Babioch
     *
     */
    public static class ParameterString extends MaskingParameter<String> {

        /** Description */
        public static final ParameterDescription<String> description      = new ParameterDescription<String>(String.class, "String") {

                                                                              /** SVUID */
                                                                              private static final long serialVersionUID = -4475658340010476250L;

                                                                              /*
                                                                               * (non-Javadoc)
                                                                               * @see org.deidentifier.arx.masking.MaskingParameter.ParameterDescription#newInstance()
                                                                               */
                                                                              @Override
                                                                              public MaskingParameter<String> newInstance() {
                                                                                  return STRING;
                                                                              }

                                                                              /*
                                                                               * (non-Javadoc)
                                                                               * @see org.deidentifier.arx.masking.MaskingParameter.ParameterDescription#newInstance(java.lang.String)
                                                                               */
                                                                              @Override
                                                                              public MaskingParameter<String> newInstance(String name) {
                                                                                  return createString(name);
                                                                              }
                                                                          };

        /** SVUID */
        private static final long                        serialVersionUID = 4414119523100173055L;

        /**
         * Creates an instance.
         */
        public ParameterString() {
            this("");
        }

        /**
         * Creates an instance.
         * 
         * @param name
         */
        public ParameterString(String name) {
            super(name);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.deidentifier.arx.masking.MaskingParameter#getDescription()
         */
        @Override
        public ParameterDescription<String> getDescription() {
            return description;
        }

    }

    /** Boolean parameter */
    public static final MaskingParameter<Boolean> BOOLEAN          = new ParameterBoolean();

    /** Double parameter */
    public static final MaskingParameter<Double>  DOUBLE           = new ParameterDouble();

    /** Integer parameter */
    public static final MaskingParameter<Integer> INTEGER          = new ParameterInteger();

    /** SVUID */
    private static final long                     serialVersionUID = 3527116230970368219L;

    /** String parameter */
    public static final MaskingParameter<String>  STRING           = new ParameterString();

    /**
     * Creates a boolean parameter with this name.
     * 
     * @param name
     * @return
     */
    public static final MaskingParameter<Boolean> createBoolean(String name) {
        return new ParameterBoolean(name);
    }

    /**
     * Creates a double paramter with this name.
     * 
     * @param name
     * @return
     */
    public static final MaskingParameter<Double> createDouble(String name) {
        return new ParameterDouble(name);
    }

    /**
     * Creates an integer parameter with this name.
     * 
     * @param name
     * @return
     */
    public static final MaskingParameter<Integer> createInteger(String name) {
        return new ParameterInteger(name);
    }

    /**
     * Creates a string parameter with this name.
     * 
     * @param name
     * @return
     */
    public static final MaskingParameter<String> createString(String name) {
        return new ParameterString(name);
    }

    /**
     * Returns a list of parameters.
     * @return
     */
    public static final List<ParameterDescription<?>> list() {

        ArrayList<ParameterDescription<?>> list = new ArrayList<>();

        list.add(BOOLEAN.getDescription());
        list.add(STRING.getDescription());
        list.add(INTEGER.getDescription());
        list.add(DOUBLE.getDescription());

        return list;

    }

    /** Name */
    private String name;

    /**
     * Creates an instance.
     * 
     * @param name
     */
    public MaskingParameter(String name) {
        this.name = name;
    }

    /**
     * Returns the parameter description.
     * @return
     */
    abstract public ParameterDescription<T> getDescription();

    /**
     * Returns the name.
     * @return
     */
    public String getName() {

        return this.name;

    }

}
