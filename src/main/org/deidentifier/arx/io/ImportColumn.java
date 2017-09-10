/*
 * ARX: Powerful Data Anonymization
 * Copyright 2014 - 2015 Karol Babioch, Fabian Prasser, Florian Kohlmayer
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

package org.deidentifier.arx.io;

import org.deidentifier.arx.DataType;

/**
 * Represents a single data column
 * 
 * This represents a single column that will be imported from. Each column at
 * least consists of an {@link #aliasName} and {@link #dataType}.
 * 
 * @author Karol Babioch
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 * 
 */
abstract public class ImportColumn { // NO_UCD

    /**
     * Alias name of column.
     *
     * @note Note that this is alias name of the column. The original names
     *       might be different in case of {@link IImportColumnNamed}.
     */
    private String        aliasName;

    /** Datatype of column. */
    private DataType<?>   dataType;

    /** Indicates if non-matching values should be replaced with NULL values. */
    private boolean cleansing;

    /**
     * Creates a new instance of this object with the given parameters.
     *
     * @param aliasName {@link #aliasName}
     * @param dataType {@link #dataType}
     */
    public ImportColumn(String aliasName, DataType<?> dataType) {

        setAliasName(aliasName);
        setDataType(dataType);
        cleansing = false;
    }

    /**
     * Creates a new instance of this object with the given parameters.
     *
     * @param aliasName {@link #aliasName}
     * @param dataType {@link #dataType}
     * @param cleansing the cleansing
     */
    public ImportColumn(String aliasName, DataType<?> dataType, boolean cleansing) {

        setAliasName(aliasName);
        setDataType(dataType);
        this.cleansing = cleansing;
    }

    /**
     * Gets the alias name.
     *
     * @return {@link #aliasName}
     */
    public String getAliasName() {
        return aliasName;
    }

    /**
     * Gets the data type.
     *
     * @return {@link #dataType}
     */
    public DataType<?> getDataType() {
        return dataType;
    }

    /**
     * Should non-matching values be replaced with NULL values.
     *
     * @return true, if cleansing is enabled
     */
    public boolean isCleansing() {
        return cleansing;
    }

    /**
     * Sets the alias name.
     *
     * @param aliasName {@link #aliasName}
     */
    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    /**
     * Should we perform cleansing
     * @param cleansing
     */
    public void setCleansing(boolean cleansing) {
        this.cleansing = cleansing;
    }

    /**
     * Sets the data type.
     *
     * @param dataType {@link #dataType}
     */
    public void setDataType(DataType<?> dataType) {
        this.dataType = dataType;
    }
}
