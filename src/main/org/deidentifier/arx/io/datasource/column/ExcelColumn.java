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
 * Represents a single Excel data column
 *
 * Excel columns are referred to by an index (see {@link IndexColumn}).
 */
public class ExcelColumn extends IndexColumn {

    /**
     * Creates a new instance of this object with the given parameters
     *
     * @see {@link IndexColumn}
     */
    public ExcelColumn(int index, DataType<?> datatype)
    {

        super(index, datatype);

    }

    /**
     * Creates a new instance of this object with the given parameters
     *
     * @see {@link IndexColumn}
     */
    public ExcelColumn(int index, String aliasName, DataType<?> datatype)
    {

        super(index, aliasName, datatype);

    }

}
