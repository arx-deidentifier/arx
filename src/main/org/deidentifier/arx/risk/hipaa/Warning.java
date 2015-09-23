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
public class Warning {
    private final String     columnName;
    private final Identifier identifier;
    private final Classifier classifiedBy;
    private final String     value;
    
    public Warning(String columnName, Identifier identifier, Classifier classifiedBy, String value) {
        this.columnName = columnName;
        this.classifiedBy = classifiedBy;
        this.identifier = identifier;
        this.value = value;
    }
    
    /**
     * The column name where the identifier was found
     * @return The column number
     */
    public String getColumn() {
        return this.columnName;
    }
    
    /**
     * The identifier which was found
     * @return The found identifier
     */
    public Identifier getIdentifier() {
        return this.identifier;
    }
    
    /**
     * The value which caused the identification
     * @return The value which caused the warning
     */
    public String getValue() {
        return this.value;
    }
    
    /**
     * The classifier (column name or instance)
     * @return
     */
    public Classifier getClassifier() {
        return this.classifiedBy;
    }
    
    @Override
    public String toString() {
        return String.format("Column: %s\tType: %s\tClassifier: %s\tValue: %s", this.getColumn(), this.getIdentifier(), this.getClassifier(), this.getValue());
    }
}
