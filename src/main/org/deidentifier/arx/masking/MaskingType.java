package org.deidentifier.arx.masking;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.DataType;

/**
 * Set of classes describing all available masking types
 *
 * @author Karol Babioch
 */
abstract public class MaskingType implements Serializable {

    private static final long serialVersionUID = 485104089887474387L;

    abstract public MaskingTypeDescription getDescription();


    public static class Substitution extends MaskingType {

        private static final long serialVersionUID = -3408327861831396157L;

        public static final MaskingTypeDescription description = new MaskingTypeDescription("Substitution") {

            private static final long serialVersionUID = -7038871832896082645L;

            @Override
            public List<DataType<?>> getSupportedDataTypes() {

                List<DataType<?>> list = new ArrayList<>();
                list.add(DataType.STRING);
                list.add(DataType.INTEGER);

                return list;

            }

            @Override
            public List<MaskingParameter<?>> getParameters() {

                List<MaskingParameter<?>> list = new ArrayList<>();
                list.add(MaskingParameter.createBoolean("Parameter #1"));
                list.add(MaskingParameter.createString("Parameter #2"));

                return list;

            }

            @Override
            public MaskingType newInstance() {

                return new Substitution();

            }

        };

        @Override
        public MaskingTypeDescription getDescription() {

            return description;

        }

    }


    public static class Redaction extends MaskingType {

        private static final long serialVersionUID = -2544358188399389355L;

        public static final MaskingTypeDescription description = new MaskingTypeDescription("Redaction") {

            private static final long serialVersionUID = 8209429201843292494L;

            @Override
            public List<DataType<?>> getSupportedDataTypes() {

                List<DataType<?>> list = new ArrayList<>();
                list.add(DataType.STRING);
                list.add(DataType.INTEGER);

                return list;

            }

            @Override
            public List<MaskingParameter<?>> getParameters() {

                List<MaskingParameter<?>> list = new ArrayList<>();

                list.add(MaskingParameter.BOOLEAN);

                return list;

            }

            @Override
            public MaskingType newInstance() {

                return new Redaction();

            }


        };

        @Override
        public MaskingTypeDescription getDescription() {

            return description;

        }

    }


    public static abstract class MaskingTypeDescription implements Serializable {

        private static final long serialVersionUID = -3328298087202770639L;

        private String label;

        private MaskingTypeDescription(String label) {

            this.label = label;

        }

        public String getLabel() {

            return this.label;

        }

        abstract public List<DataType<?>> getSupportedDataTypes();

        abstract public List<MaskingParameter<?>> getParameters();

        abstract public MaskingType newInstance();

    }


    public static final MaskingType SUBSTITUTION = new Substitution();
    public static final MaskingType REDACTION = new Redaction();


    public static final List<MaskingTypeDescription> list() {

        ArrayList<MaskingTypeDescription> list = new ArrayList<>();

        list.add(SUBSTITUTION.getDescription());
        list.add(REDACTION.getDescription());

        return list;

    }

}
