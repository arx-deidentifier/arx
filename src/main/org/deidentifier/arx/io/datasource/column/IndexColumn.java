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


abstract public class IndexColumn extends Column implements IIndexedColumn {

    /**
     * Index of column
     *
     * @note Counting starts at 0, which would be the first column
     */
    private int index;

    /**
     * Creates a new instance of this object with the given parameters
     *
     * This does not assign a name to the column.
     *
     * @param index {@link #index}
     * @param datatype {@link #dataType}
     */
    public IndexColumn(int index, DataType<?> datatype) {

        this(index, null, datatype);

    }

    public IndexColumn(int index, String aliasName, DataType<?> datatype)
    {

        super(aliasName, datatype);
        setIndex(index);

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
