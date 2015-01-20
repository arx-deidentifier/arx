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
 * Represents a single JDBC data column
 * 
 * JDBC columns can be referred to by both an index ({@link IIndexColumn}) and
 * by name ({@link IImportColumnNamed}. Use the appropriate constructor that
 * suits your need.
 *
 * @author Karol Babioch
 * @author Fabian Prasser
 * @note For now only the index based addressing works
 * @warning Don't mix name based and index based addressing
 */
public class ImportColumnJDBC extends ImportColumn implements
        IImportColumnIndexed, IImportColumnNamed {

    /**
     * Index this column refers to.
     *
     * @note Counting starts usually at 0
     */
    private int    index = -1;

    /** Name this column refers to. */
    private String name;

    /**
     * Creates a new instance of this object with the given parameters.
     *
     * @param index
     * @param datatype
     */
    public ImportColumnJDBC(int index, DataType<?> datatype) {
        this(index, null, datatype);
    }

    /**
     * Creates a new instance of this object with the given parameters.
     *
     * @param index
     * @param aliasName
     * @param datatype
     */
    public ImportColumnJDBC(int index, String aliasName, DataType<?> datatype) {
        super(aliasName, datatype);
        setIndex(index);
    }

    /**
     * Creates a new instance of this object with the given parameters.
     *
     * @param name
     * @param datatype
     */
    public ImportColumnJDBC(String name, DataType<?> datatype) {
        super(name, datatype);
        setName(name);
    }

    /**
     * Creates a new instance of this object with the given parameters.
     *
     * @param name
     * @param aliasName
     * @param datatype
     */
    public ImportColumnJDBC(String name, String aliasName, DataType<?> datatype) {
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
     * @return {@link #name}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * @param index
     *            {@link #index}
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * @param name
     *            {@link #name}
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }
}
