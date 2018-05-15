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
 * Represents a single JDBC data column
 * 
 * JDBC columns can be referred to by both an index ({@link IIndexColumn}) and
 * by name ({@link IImportColumnNamed}. Use the appropriate constructor that
 * suits your need.
 *
 * @author Karol Babioch
 * @author Fabian Prasser
 * @author Florian Kohlmayer
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
        this(index, null, datatype, false);
    }

    /**
     * Creates a new instance of this object with the given parameters.
     * @param index
     * @param datatype
     * @param cleansing
     */
    public ImportColumnJDBC(int index, DataType<?> datatype, boolean cleansing) {
        this(index, null, datatype, cleansing);
    }

    /**
     * Creates a new instance of this object with the given parameters.
     *
     * @param index
     * @param aliasName
     * @param datatype
     */
    public ImportColumnJDBC(int index, String aliasName, DataType<?> datatype) {
        this(index, aliasName, datatype, false);
    }

    /**
     * Creates a new instance of this object with the given parameters.
     * 
     * @param index
     * @param aliasName
     * @param datatype
     * @param cleansing
     */
    public ImportColumnJDBC(int index, String aliasName, DataType<?> datatype, boolean cleansing) {
        super(aliasName, datatype, cleansing);
        setIndex(index);
    }

    /**
     * Creates a new instance of this object with the given parameters.
     *
     * @param name
     * @param datatype
     */
    public ImportColumnJDBC(String name, DataType<?> datatype) {
        this(name, datatype, false);
    }

    /**
     * Creates a new instance of this object with the given parameters.
     * 
     * @param name
     * @param datatype
     * @param cleansing
     */
    public ImportColumnJDBC(String name, DataType<?> datatype, boolean cleansing) {
        super(name, datatype, cleansing);
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
        this(name, aliasName, datatype, false);
    }

    /**
     * Creates a new instance of this object with the given parameters.
     * 
     * @param name
     * @param aliasName
     * @param datatype
     * @param cleansing
     */
    public ImportColumnJDBC(String name, String aliasName, DataType<?> datatype, boolean cleansing) {
        super(aliasName, datatype, cleansing);
        setName(name);
    }

    /**
     * @return {@link #index}
     */
    @Override
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
    @Override
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
