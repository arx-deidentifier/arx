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

    private static final long serialVersionUID = 3527116230970368219L;

    private String name;

    abstract public ParameterDescription<T> getDescription();

    public MaskingParameter(String name) {

        this.name = name;

    }

    public String getName() {

        return this.name;

    }

    public static class ParameterBoolean extends MaskingParameter<Boolean> {

        public ParameterBoolean() {

            this("");

        }

        public ParameterBoolean(String name) {

            super(name);

        }

        private static final long serialVersionUID = 3591704008421371526L;

        public static final ParameterDescription<Boolean> description = new ParameterDescription<Boolean>(Boolean.class, "Boolean") {

            private static final long serialVersionUID = 7248402123389578028L;

            @Override
            public MaskingParameter<Boolean> newInstance() {

                return BOOLEAN;

            }

            @Override
            public MaskingParameter<Boolean> newInstance(String name) {

                return createBoolean(name);

            }

        };

        @Override
        public ParameterDescription<Boolean> getDescription() {

            return description;

        }

    }


    public static class ParameterString extends MaskingParameter<String> {

        public ParameterString() {

            this("");

        }

        public ParameterString(String name) {

            super(name);

        }

        private static final long serialVersionUID = 4414119523100173055L;

        public static final ParameterDescription<String> description = new ParameterDescription<String>(String.class, "String") {

            private static final long serialVersionUID = -4475658340010476250L;

            @Override
            public MaskingParameter<String> newInstance() {

                return STRING;

            }

            @Override
            public MaskingParameter<String> newInstance(String name) {

                return createString(name);

            }


        };

        @Override
        public ParameterDescription<String> getDescription() {

            return description;

        }

    }


    public static class ParameterInteger extends MaskingParameter<Integer> {

        public ParameterInteger() {

            this("");

        }

        public ParameterInteger(String name) {

            super(name);

        }

        private static final long serialVersionUID = 4414119523100173055L;

        public static final ParameterDescription<Integer> description = new ParameterDescription<Integer>(Integer.class, "Integer") {

            private static final long serialVersionUID = -2152324531271195806L;

            @Override
            public MaskingParameter<Integer> newInstance() {

                return INTEGER;

            }

            @Override
            public MaskingParameter<Integer> newInstance(String name) {

                return createInteger(name);

            }

        };

        @Override
        public ParameterDescription<Integer> getDescription() {

            return description;

        }

    }


    public static class ParameterDouble extends MaskingParameter<Double> {

        public ParameterDouble() {

            this("");

        }

        public ParameterDouble(String name) {

            super(name);

        }

        private static final long serialVersionUID = 4414119523100173055L;

        public static final ParameterDescription<Double> description = new ParameterDescription<Double>(Double.class, "Double") {

            private static final long serialVersionUID = -4811531702128826389L;

            @Override
            public MaskingParameter<Double> newInstance() {

                return DOUBLE;

            }

            @Override
            public MaskingParameter<Double> newInstance(String name) {

                return createDouble(name);

            }

        };

        @Override
        public ParameterDescription<Double> getDescription() {

            return description;

        }

    }


    public static final MaskingParameter<Boolean> createBoolean(String name) {

        return new ParameterBoolean(name);

    }


    public static final MaskingParameter<String> createString(String name) {

        return new ParameterString(name);

    }


    public static final MaskingParameter<Integer> createInteger(String name) {

        return new ParameterInteger(name);

    }


    public static final MaskingParameter<Double> createDouble(String name) {

        return new ParameterDouble(name);

    }



    public static abstract class ParameterDescription<T> implements Serializable {

        private static final long serialVersionUID = 7809765388058884669L;

        private Class<?> clazz;

        private String label;

        private ParameterDescription(Class<T> clazz, String label) {

            this.clazz = clazz;
            this.label = label;

        }

        public Class<?> getWrappedClass() {

            return clazz;

        }

        public String getLabel() {

            return this.label;

        }

        public abstract MaskingParameter<T> newInstance();

        public abstract MaskingParameter<T> newInstance(String name);


    }


    public static final MaskingParameter<Boolean> BOOLEAN = new ParameterBoolean();
    public static final MaskingParameter<String> STRING = new ParameterString();
    public static final MaskingParameter<Integer> INTEGER = new ParameterInteger();
    public static final MaskingParameter<Double> DOUBLE = new ParameterDouble();


    public static final List<ParameterDescription<?>> list() {

        ArrayList<ParameterDescription<?>> list = new ArrayList<>();

        list.add(BOOLEAN.getDescription());
        list.add(STRING.getDescription());
        list.add(INTEGER.getDescription());
        list.add(DOUBLE.getDescription());

        return list;

    }

}
