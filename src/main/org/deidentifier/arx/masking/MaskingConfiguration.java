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

import java.util.HashMap;
import java.util.Map;

/**
 * Class containing the masking configuration, i.e. a map of attributes, MaskingTypes related configuration options
 *
 * @author Sandro Schaeffler
 * @author Peter Bock
 */
public class MaskingConfiguration {

    /** Maskings */
    private static Map<String, AttributeParameters> maskings = new HashMap<String, AttributeParameters>();

    /**
     * Adds a masking.
     * @param attribute
     * @param attributeParameters
     */
    public static void addMasking(String attribute, AttributeParameters attributeParameters) {
        maskings.put(attribute, attributeParameters);
    }

    /**
     * Adds a masking.
     * @param attribute
     * @param maskingType
     */
    public static void addMasking(String attribute, MaskingType maskingType) {
        maskings.put(attribute, new AttributeParameters(maskingType));
    }

    /**
     * Returns the masking mapping.
     * @return
     */
    public static Map<String, AttributeParameters> getMapping() {
        return maskings;
    }

    /**
     * Adds a distribution.
     * @param attribute
     * @param distributionIndex
     */
    public static void addDistribution(String attribute, int distributionIndex) {
        maskings.get(attribute).setDistribution(distributionIndex);
        System.out.println(maskings);
    }

    /**
     * Removes a masking configuration.
     * @param attribute
     */
    public static void removeMasking(String attribute) {
        maskings.remove(attribute);
    }

    /**
     * Returns the masking type for this attribute.
     * @param attribute
     * @return
     */
    public static MaskingType getMaskingType(String attribute) {

        AttributeParameters parameter;
        parameter = maskings.get(attribute);
        if (parameter == null)
            return MaskingType.SUPPRESSED;
        else
            return parameter.getMaskingType();

    }

    /**
     * Returns the distribution index for this attribute.
     * @param attribute
     * @return
     */
    public static int getDistributionIndex(String attribute) {

        AttributeParameters parameter;
        parameter = maskings.get(attribute);
        if (parameter == null) {
            return 0; // 0 equals "Identity" Distribution
        } else
            return parameter.getDistributionIndex();
    }

}
