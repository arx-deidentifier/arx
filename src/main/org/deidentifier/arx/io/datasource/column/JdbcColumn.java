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

package org.deidentifier.arx.io.datasource.column;

import org.deidentifier.arx.DataType;


/**
 * Represents a single JDBC data column
 *
 * JDBC columns can be referred to by both an index ({@link IIndexColumn})
 * and by name ({@link INamedColumn}. Use the appropriate constructor that
 * suits your need.
 *
 * @note For now only the index based addressing works
 *
 * @warning Don't mix name based and index based addressing
 *
 * @todo Implement name based addressing
 */
public class JdbcColumn extends Column implements IIndexedColumn, INamedColumn {

    /**
     * Index this column refers to
     *
     * @note Counting starts usually at 0
     */
    private int index;

    /**
     * Name this column refers to
     */
    private String name;


    /**
     * Creates a new instance of this object with the given parameters
     */
    public JdbcColumn(int index, DataType<?> datatype) {

        this(index, null, datatype);

    }

    /**
     * Creates a new instance of this object with the given parameters
     */
    public JdbcColumn(int index, String aliasName, DataType<?> datatype)
    {

        super(aliasName, datatype);
        setIndex(index);

    }

    /**
     * Creates a new instance of this object with the given parameters
     */
    public JdbcColumn(String aliasName, DataType<?> datatype)
    {

        super(aliasName, datatype);

    }

    /**
     * Creates a new instance of this object with the given parameters
     */
    public JdbcColumn(String name, String aliasName, DataType<?> datatype)
    {

        super(aliasName, datatype);
        setName(name);

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

    /**
     * @return {@link #name}
     */
    @Override
    public String getName()
    {

        return name;

    }

    /**
     * @param name {@link #name}
     */
    @Override
    public void setName(String name)
    {

        this.name = name;

    }

}
