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
import java.util.List;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.gui.resources.Resources;

/**
 * Set of classes describing all available masking types
 *
 * previous MaskingTypes (commented out section):
 * @author Karol Babioch
 * 
 *         current MaskingTypes:
 * @author Sandro Schaeffler
 * @author Peter Bock
 */
public class MaskingType implements Serializable {

    /**
     * Class representing masking type descriptions.
     */
    public static abstract class MaskingTypeDescription implements Serializable {

        /** SVUID */
        private static final long serialVersionUID = -3328298087202770639L;

        /** Label */
        private String            label;

        /**
         * Creates an instance.
         * 
         * @param label
         */
        private MaskingTypeDescription(String label) {
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
         * Returns the supported data types.
         * @return
         */
        abstract public List<DataType<?>> getSupportedDataTypes();

        /**
         * Returns a new instance.
         * @return
         */
        abstract public MaskingType newInstance();

    }

    /** SVUID */
    private static final long       serialVersionUID          = 485104089887474387L;

    /** Code for suppressed */
    private static final int        MSK_TYPE_SUPPRESSED       = 0;
    /** Code for pseudonymization */
    private static final int        MSK_TYPE_PS               = 1;
    /** Code for noise addition */
    private static final int        MSK_TYPE_NA               = 2;
    /** Code for random shuffling */
    private static final int        MSK_TYPE_RS               = 3;
    /** Code for random generation */
    private static final int        MSK_TYPE_RG               = 4;

    /** Masking type */
    public static final MaskingType SUPPRESSED                = new MaskingType(MSK_TYPE_SUPPRESSED);
    /** Masking type */
    public static final MaskingType PSEUDONYMIZATION_MASKING  = new MaskingType(MSK_TYPE_PS);
    /** Masking type */
    public static final MaskingType NOISE_ADDITION_MASKING    = new MaskingType(MSK_TYPE_NA);
    /** Masking type */
    public static final MaskingType RANDOM_SHUFFLING_MASKING  = new MaskingType(MSK_TYPE_RS);
    /** Masking type */
    public static final MaskingType RANDOM_GENERATION_MASKING = new MaskingType(MSK_TYPE_RG);

    /** The type. */
    private int                     type                      = 0x0;

    /**
     * Instantiates a new type.
     * 
     * @param type the type
     */
    private MaskingType(final int type) {
        this.type = type;
    }

    /**
     * Returns the label
     * @return
     */
    public String getLabel() {
        switch (type) {
        case (MSK_TYPE_PS):
            return Resources.getMessage("MaskingConfigurationView.1"); //$NON-NLS-1$
        case (MSK_TYPE_NA):
            return Resources.getMessage("MaskingConfigurationView.2"); //$NON-NLS-1$
        case (MSK_TYPE_RS):
            return Resources.getMessage("MaskingConfigurationView.3"); //$NON-NLS-1$
        case (MSK_TYPE_RG):
            return Resources.getMessage("MaskingConfigurationView.4"); //$NON-NLS-1$
        default:
            return Resources.getMessage("MaskingConfigurationView.10"); //$NON-NLS-1$
        }
    }

    /**
     * Returns the type
     * @return
     */
    protected int getType() {
        return type;
    }

}
