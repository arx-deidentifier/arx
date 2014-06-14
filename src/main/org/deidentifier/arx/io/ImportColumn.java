/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2014 Karol Babioch <karol@babioch.de>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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
 */
abstract public class ImportColumn {

    /**
     * Alias name of column
     * 
     * @note Note that this is alias name of the column. The original names
     *       might be different in case of {@link IImportColumnNamed}.
     */
    private String      aliasName;

    /**
     * Datatype of column
     */
    private DataType<?> dataType;

    /**
     * Creates a new instance of this object with the given parameters
     * 
     * @param aliasName
     *            {@link #aliasName}
     * @param dataType
     *            {@link #dataType}
     */
    public ImportColumn(String aliasName, DataType<?> dataType) {

        setAliasName(aliasName);
        setDataType(dataType);
    }

    /**
     * @return {@link #aliasName}
     */
    public String getAliasName() {
        return aliasName;
    }

    /**
     * @return {@link #dataType}
     */
    public DataType<?> getDataType() {
        return dataType;
    }

    /**
     * @param aliasName
     *            {@link #aliasName}
     */
    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    /**
     * @param dataType
     *            {@link #dataType}
     */
    public void setDataType(DataType<?> dataType) {
        this.dataType = dataType;
    }
}
