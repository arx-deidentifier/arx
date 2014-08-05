/*
 * ARX: Powerful Data Anonymization
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
 * Represents a single Excel data column
 * 
 * Excel columns are referred to by an index (see {@link ImportColumnIndexed}).
 * 
 * 
 * @author Karol Babioch
 * @author Fabian Prasser
 */
public class ImportColumnExcel extends ImportColumnIndexed {

    /**
     * Creates a new instance of this object with the given parameters
     * 
     * @see {@link ImportColumnIndexed}
     */
    public ImportColumnExcel(int index, DataType<?> datatype) {
        super(index, datatype);
    }

    /**
     * Creates a new instance of this object with the given parameters
     * 
     * @see {@link ImportColumnIndexed}
     */
    public ImportColumnExcel(int index, String aliasName, DataType<?> datatype) {
        super(index, aliasName, datatype);
    }

    /**
     * Creates a new instance of this object with the given parameters
     * @param name
     * @param datatype
     */
    public ImportColumnExcel(String name, DataType<?> datatype) {
        super(name, datatype);
    }

    /**
     * Creates a new instance of this object with the given parameters
     * @param name
     * @param alias
     * @param datatype
     */
    public ImportColumnExcel(String name, String alias, DataType<?> datatype) {
        super(name, alias, datatype);
    }
}
