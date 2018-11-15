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

/**
 * This class is used to map attributes to their MaskingType and related configuration options, used in MaskingConfiguration.java
 * 
 * @author Sandro Schaeffler
 * @author Peter Bock
 */
public class AttributeParameters {

    /** Masking type */
    private MaskingType maskingType;

    /** 0 as default value represensts the "Identity" Distribution */
    private int         selectedDistributionIndex = 0;

    /** String length */
    private int         stringLength              = -1;

    /**
     * Creates an instance.
     * 
     * @param maskingType
     */
    public AttributeParameters(MaskingType maskingType) {
        this.maskingType = maskingType;
    }

    /**
     * Creates an instance.
     * 
     * @param maskingType
     * @param stringLength
     * @param selectedDistributionIndex
     */
    public AttributeParameters(MaskingType maskingType, int stringLength, int selectedDistributionIndex) {
        this.maskingType = maskingType;
        this.stringLength = stringLength;
        this.selectedDistributionIndex = selectedDistributionIndex;
    }

    /**
     * Returns the distribution index
     * @return
     */
    public int getDistributionIndex() {
        return selectedDistributionIndex;
    }

    /**
     * Returns the masking type
     * @return
     */
    public MaskingType getMaskingType() {
        return maskingType;
    }

    /**
     * Returns the string length.
     * @return
     */
    public int getStringLength() {
        return stringLength;
    }

    /**
     * Sets the distribution
     * @param distributionIndex
     */
    public void setDistribution(int distributionIndex) {
        this.selectedDistributionIndex = distributionIndex;
    }

    /**
     * Sets the string length
     * @param stringLength
     */
    public void setStringLength(int stringLength) {
        this.stringLength = stringLength;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "MaskingType: " + maskingType.getLabel() + ", Distribution: " + selectedDistributionIndex;
    }

}
