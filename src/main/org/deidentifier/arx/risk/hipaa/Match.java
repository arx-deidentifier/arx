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
 * Provides information about the occurrence of an HIPPA identifier
 * @author David Gaﬂmann
 * @author Florian Kohlmayer
 */
public class Match {
    
    /**
     * Represents the classifier for the HIPAA identifier.
     * @author Florian Kohlmayer
     */
    public enum Classifier {
                            COLUMN_NAME,
                            ATTRIBUTE_VALUE
    }
    
    /**
     * Represents the HIPPA identifiers
     * @author David Gaﬂmann
     */
    public enum HIPAAIdentifier {
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
        
        HIPAAIdentifier(char category) {
            this.category = category;
        }
        
        public int getCategory() {
            return category;
        }
    }
    
    private final String          column;
    private final HIPAAIdentifier identifier;
    private final Classifier      classifiedBy;
    private final String          value;
    
    /**
     * Constructor.
     * 
     * @param columnName
     * @param category
     * @param classifiedBy
     * @param value
     */
    public Match(String columnName, HIPAAIdentifier category, Classifier classifiedBy, String value) {
        this.column = columnName;
        this.classifiedBy = classifiedBy;
        this.identifier = category;
        this.value = value;
    }
    
    /**
     * The classifier (column name or instance)
     * @return
     */
    public Classifier getClassifier() {
        return classifiedBy;
    }
    
    /**
     * The column name where the identifier was found
     * @return The column name
     */
    public String getColumn() {
        return column;
    }
    
    /**
     * The identifier which was found
     * @return The found identifier
     */
    public HIPAAIdentifier getIdentifier() {
        return identifier;
    }
    
    /**
     * The value which caused the identification
     * @return The value which caused the warning
     */
    public String getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return String.format("Column: %s\tIdentifier: %s\tClassifier: %s\tValue: %s", getColumn(), getIdentifier(), getClassifier(), getValue());
    }
}
