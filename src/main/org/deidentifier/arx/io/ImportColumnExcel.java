/*
 * ARX: Powerful Data Anonymization
 * Copyright 2014 Karol Babioch <karol@babioch.de>
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
     * Creates a new instance of this object with the given parameters.
     *
     * @param index
     * @param datatype
     * @see {@link ImportColumnIndexed}
     */
    public ImportColumnExcel(int index, DataType<?> datatype) {
        super(index, datatype);
    }

    /**
     * Creates a new instance of this object with the given parameters.
     *
     * @param index
     * @param aliasName
     * @param datatype
     * @see {@link ImportColumnIndexed}
     */
    public ImportColumnExcel(int index, String aliasName, DataType<?> datatype) {
        super(index, aliasName, datatype);
    }

    /**
     * Creates a new instance of this object with the given parameters.
     *
     * @param name
     * @param datatype
     */
    public ImportColumnExcel(String name, DataType<?> datatype) {
        super(name, datatype);
    }

    /**
     * Creates a new instance of this object with the given parameters.
     *
     * @param name
     * @param alias
     * @param datatype
     */
    public ImportColumnExcel(String name, String alias, DataType<?> datatype) {
        super(name, alias, datatype);
    }
}
