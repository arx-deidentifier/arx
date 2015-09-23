/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.arx.risk.hipaa;

/**
 * Represents the HIPPA identifiers
 * @author David Gaﬂmann
 */
public enum Category {
    NAME('A'),
    GEOGRAPHIC_SUBDIVISION('B'),
    DATE('C'),
    TELEPHONE_NUMBER('D'),
    FAX_NUMBER('E'),
    EMAIL_ADDRESS('F'),
    SOCIAL_SECURITY_NUMBER('G'),
    MEDICAL_RECORD_NUMBER('H'),
    HEALTH_PLAN_BENEFICIARY_NUMBER('I'),
    ACCOUNT_NUMBER('J'),
    CERTIFICATE_NUMBER('K'),
    VEHICLE_IDENTIFIER('L'),
    DEVICE_IDENTIFIER('M'),
    URL('N'),
    IP('O'),
    BIOMETRIC_IDENTIFIER('P'),
    PHOTOGRAPH('Q'),
    OTHER('R');

    private final char category;

    Category(char category) {
        this.category = category;
    }

    public int getCategory() {
        return this.category;
    }
}
