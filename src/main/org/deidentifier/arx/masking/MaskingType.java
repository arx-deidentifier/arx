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

import org.deidentifier.arx.DataType;

/**
 * Set of classes describing all available masking types
 *
 * @author Karol Babioch
 */
abstract public class MaskingType implements Serializable {

    private static final long serialVersionUID = 485104089887474387L;

    abstract public MaskingTypeDescription getDescription();


    public static class MatchAndReplaceString extends MaskingType {

        private static final long serialVersionUID = -3408327861831396157L;

        public static final MaskingTypeDescription description = new MaskingTypeDescription("MatchAndReplaceString") {

            private static final long serialVersionUID = -7038871832896082645L;

            @Override
            public List<DataType<?>> getSupportedDataTypes() {

                List<DataType<?>> list = new ArrayList<>();
                list.add(DataType.STRING);

                return list;

            }

            @Override
            public MaskingType newInstance() {

                return new MatchAndReplaceString();

            }

        };

        @Override
        public MaskingTypeDescription getDescription() {

            return description;

        }

    }


    public static class SplitAndReplaceString extends MaskingType {

        private static final long serialVersionUID = -3408327861831396157L;

        public static final MaskingTypeDescription description = new MaskingTypeDescription("SplitAndReplaceString") {

            private static final long serialVersionUID = -7038871832896082645L;

            @Override
            public List<DataType<?>> getSupportedDataTypes() {

                List<DataType<?>> list = new ArrayList<>();
                list.add(DataType.STRING);

                return list;

            }

            @Override
            public MaskingType newInstance() {

                return new SplitAndReplaceString();

            }

        };

        @Override
        public MaskingTypeDescription getDescription() {

            return description;

        }

    }

    public static class ConstantShiftDate extends MaskingType {

        private static final long serialVersionUID = -2544358188399389355L;

        public static final MaskingTypeDescription description = new MaskingTypeDescription("ConstantShiftDate") {

            private static final long serialVersionUID = 8209429201843292494L;

            @Override
            public List<DataType<?>> getSupportedDataTypes() {

                List<DataType<?>> list = new ArrayList<>();
                list.add(DataType.DATE);

                return list;

            }

            @Override
            public MaskingType newInstance() {

                return new ConstantShiftDate();

            }

        };

        @Override
        public MaskingTypeDescription getDescription() {

            return description;

        }

    }

    public static class ConstantShiftDecimal extends MaskingType {

        private static final long serialVersionUID = -3408327861831396157L;

        public static final MaskingTypeDescription description = new MaskingTypeDescription("ConstantShiftDecimal") {

            private static final long serialVersionUID = -7038871832896082645L;

            @Override
            public List<DataType<?>> getSupportedDataTypes() {

                List<DataType<?>> list = new ArrayList<>();
                list.add(DataType.INTEGER);
                list.add(DataType.DECIMAL);

                return list;

            }

            @Override
            public MaskingType newInstance() {

                return new ConstantShiftDecimal();

            }

        };

        @Override
        public MaskingTypeDescription getDescription() {

            return description;

        }

    }


    public static class RandomShiftDecimal extends MaskingType {

        private static final long serialVersionUID = -476644133411909846L;

        public static final MaskingTypeDescription description = new MaskingTypeDescription("RandomShiftDecimal") {

            private static final long serialVersionUID = -4414489925882607507L;

            @Override
            public List<DataType<?>> getSupportedDataTypes() {

                List<DataType<?>> list = new ArrayList<>();
                list.add(DataType.INTEGER);
                list.add(DataType.DECIMAL);

                return list;

            }

            @Override
            public MaskingType newInstance() {

                return new RandomShiftDecimal();

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

        abstract public MaskingType newInstance();

    }


    public static final MaskingType MatchAndReplaceString = new MatchAndReplaceString();
    public static final MaskingType SplitAndReplaceString = new SplitAndReplaceString();
    public static final MaskingType ConstantShiftDecimal = new ConstantShiftDecimal();
    public static final MaskingType ConstantShiftDate = new ConstantShiftDate();
    public static final MaskingType RandomShiftDecimal = new RandomShiftDecimal();


    public static final List<MaskingTypeDescription> list() {

        ArrayList<MaskingTypeDescription> list = new ArrayList<>();

        list.add(MatchAndReplaceString.getDescription());
        list.add(SplitAndReplaceString.getDescription());
        list.add(ConstantShiftDecimal.getDescription());
        list.add(ConstantShiftDate.getDescription());
        list.add(RandomShiftDecimal.getDescription());

        return list;

    }

}
