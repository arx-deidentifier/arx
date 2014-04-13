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

package org.deidentifier.arx.io.datasource;

import org.deidentifier.arx.DataType;


/**
 * Represents a single data column
 *
 * This represents a single column that will be imported from. Each column
 * consists of an {@link #index}, {@link #aliasName} and {@link #dataType}.
 */
public class Column {

    /**
     * Index of column
     *
     * @note Counting starts at 0, which would be the first column
     */
    private int index;

    /**
     * Name of column
     */
    private String aliasName;

    /**
     * Datatype of column
     */
    private DataType<?> dataType;


    /**
     * Creates a new instance of this object with the given parameters
     *
     * This does not assign a name to the column.
     *
     * @param index {@link #index}
     * @param datatype {@link #dataType}
     */
    public Column(int index, DataType<?> datatype) {

        this(index, null, datatype);

    }

    /**
     * Creates a new instance of this object with the given parameters
     *
     * @param index {@link #index}
     * @param aliasName {@link #aliasName}
     * @param dataType {@link #dataType}
     */
    public Column(int index, String aliasName, DataType<?> dataType) {

        setIndex(index);
        setAliasName(aliasName);
        setDataType(dataType);

    }

    /**
     * @return {@link #aliasName}
     */
    public String getAliasName()
    {

        return aliasName;

    }

    /**
     * @param aliasName {@link #aliasName}
     */
    public void setAliasName(String aliasName)
    {

        this.aliasName = aliasName;

    }

    /**
     * @return {@link #dataType}
     */
    public DataType<?> getDataType()
    {

        return dataType;

    }

    /**
     * @param dataType {@link #dataType}
     */
    public void setDataType(DataType<?> dataType)
    {

        this.dataType = dataType;

    }

    /**
     * @return {@link #index}
     */
    public int getIndex() {

        return index;

    }

    /**
     * @param index {@link #index}
     */
    public void setIndex(int index) {

        this.index = index;

    }

}
