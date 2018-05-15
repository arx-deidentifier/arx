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
 * Represents a single CSV data column
 * 
 * CSV columns are referred to by an index (see {@link ImportColumnIndexed}).
 * 
 * @author Karol Babioch
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 * 
 */
public class ImportColumnCSV extends ImportColumnIndexed {

    /**
     * Creates a new instance of this object with the given parameters.
     *
     * @param index the index
     * @param datatype the datatype
     * @see {@link ImportColumnIndexed}
     */
    public ImportColumnCSV(int index, DataType<?> datatype) {
        super(index, datatype);
    }

    /**
     * Creates a new instance of this object with the given parameters.
     *
     * @param index the index
     * @param datatype the datatype
     * @param cleansing the cleansing
     */
    public ImportColumnCSV(int index, DataType<?> datatype, boolean cleansing) {
        super(index, datatype, cleansing);
    }

    /**
     * Creates a new instance of this object with the given parameters.
     *
     * @param index the index
     * @param aliasName the alias name
     * @param datatype the datatype
     * @see {@link ImportColumnIndexed}
     */
    public ImportColumnCSV(int index, String aliasName, DataType<?> datatype) {
        super(index, aliasName, datatype);
    }

    /**
     * Creates a new instance of this object with the given parameters.
     *
     * @param index the index
     * @param aliasName the alias name
     * @param datatype the datatype
     * @param cleansing the cleansing
     */
    public ImportColumnCSV(int index, String aliasName, DataType<?> datatype, boolean cleansing) {
        super(index, aliasName, datatype, cleansing);
    }

    /**
     * Creates a new instance of this object with the given parameters.
     *
     * @param name the name
     * @param datatype the datatype
     */
    public ImportColumnCSV(String name, DataType<?> datatype) {
        super(name, datatype);
    }

    /**
     * Creates a new instance of this object with the given parameters.
     *
     * @param name the name
     * @param datatype the datatype
     * @param cleansing the cleansing
     */
    public ImportColumnCSV(String name, DataType<?> datatype, boolean cleansing) {
        super(name, datatype, cleansing);
    }

    /**
     * Creates a new instance of this object with the given parameters.
     *
     * @param name the name
     * @param alias the alias
     * @param datatype the datatype
     */
    public ImportColumnCSV(String name, String alias, DataType<?> datatype) {
        super(name, alias, datatype);
    }

    /**
     * Creates a new instance of this object with the given parameters.
     *
     * @param name the name
     * @param alias the alias
     * @param datatype the datatype
     * @param cleansing the cleansing
     */
    public ImportColumnCSV(String name, String alias, DataType<?> datatype, boolean cleansing) {
        super(name, alias, datatype);
    }
}
