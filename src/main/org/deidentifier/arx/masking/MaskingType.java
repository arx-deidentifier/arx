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
import org.deidentifier.arx.gui.resources.Resources;

/**
 * Set of classes describing all available masking types
 *
 * previous MaskingTypes (commented out section):
 * @author Karol Babioch
 * 
 * current MaskingTypes:
 * @author Sandro Schaeffler
 * @author Peter Bock
 */
public class MaskingType implements Serializable {
	
    private static final long serialVersionUID = 485104089887474387L;

/*
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
*/

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


//    public static final MaskingType MatchAndReplaceString = new MatchAndReplaceString();
//    public static final MaskingType SplitAndReplaceString = new SplitAndReplaceString();
//    public static final MaskingType ConstantShiftDecimal = new ConstantShiftDecimal();
//    public static final MaskingType ConstantShiftDate = new ConstantShiftDate();
//    public static final MaskingType RandomShiftDecimal = new RandomShiftDecimal();
    
	/** The type. */
	private int type = 0x0;
	
	/**
	 * Instatiates a new type.
	 * 
	 * @param type the type
	 */
	private MaskingType(final int type) {
		this.type=type;
	}
	
	protected int getType()
	{
		return type;
	}
	
	private static final int MSK_TYPE_SUPPRESSED = 0;
	private static final int MSK_TYPE_PS = 1;
	private static final int MSK_TYPE_NA = 2;
	private static final int MSK_TYPE_RS = 3;
	private static final int MSK_TYPE_RG = 4;
	public static final MaskingType SUPPRESSED = new MaskingType(MSK_TYPE_SUPPRESSED);
    public static final MaskingType PSEUDONYMIZATION_MASKING = new MaskingType(MSK_TYPE_PS);
    public static final MaskingType NOISE_ADDITION_MASKING = new MaskingType(MSK_TYPE_NA);	
    public static final MaskingType RANDOM_SHUFFLING_MASKING = new MaskingType(MSK_TYPE_RS);
    public static final MaskingType RANDOM_GENERATION_MASKING= new MaskingType(MSK_TYPE_RG);
    
    public String getLabel()
    {
    	switch (type)
    	{
	    	case (MSK_TYPE_PS): return Resources.getMessage("MaskingConfigurationView.1");
	    	case (MSK_TYPE_NA): return Resources.getMessage("MaskingConfigurationView.2");
	    	case (MSK_TYPE_RS): return Resources.getMessage("MaskingConfigurationView.3");
	    	case (MSK_TYPE_RG): return Resources.getMessage("MaskingConfigurationView.4");
	    	default: return "Suppressed";
    	}
    }

//    public static final List<MaskingTypeDescription> list() {
//
//        ArrayList<MaskingTypeDescription> list = new ArrayList<>();
//
//        list.add(MatchAndReplaceString.getDescription());
//        list.add(SplitAndReplaceString.getDescription());
//        list.add(ConstantShiftDecimal.getDescription());
//        list.add(ConstantShiftDate.getDescription());
//        list.add(RandomShiftDecimal.getDescription());
//
//        return list;
//
//    }

}
