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
 * Superclass for column types that are only referred to by an index.
 *
 * @author Karol Babioch
 * @author Fabian Prasser
 * @see {@link ImportColumnExcel}
 * @see {@link ImportColumnCSV}
 */
abstract public class ImportColumnIndexed extends ImportColumn implements
        IImportColumnIndexed {

    /**
     * Index this column refers to.
     *
     * @note Counting starts usually at 0
     */
    private int index;

    /** Column name. */
    private String name;
    
    /**
     * Creates a new instance of this object with the given parameters.
     *
     * @param index {@link #index}
     * @param datatype {@link #dataType}
     * @note This does not assign a name to the column explicitly. The name will
     *       be assigned later on either from a header {@link IImportConfigurationWithHeader} or by some automatic naming
     *       scheme.
     */
    public ImportColumnIndexed(int index, DataType<?> datatype) {
        this(index, null, datatype);
    }

    /**
     * Creates a new instance of this object with the given parameters.
     *
     * @param index {@link #index}
     * @param aliasName {@link ImportColumn#setAliasName(String)}
     * @param datatype {@link #dataType}
     * @note This does assign an alias name to the column explicitly.
     */
    public ImportColumnIndexed(int index, String aliasName, DataType<?> datatype) {
        super(aliasName, datatype);
        setIndex(index);
    }
    
    /**
     * Creates a new instance of this object with the given parameters.
     *
     * @param name
     * @param datatype
     */
    public ImportColumnIndexed(String name, DataType<?> datatype) {
        this(name, null, datatype);
    }

    /**
     * Creates a new instance of this object with the given parameters.
     *
     * @param name
     * @param aliasName
     * @param datatype
     */
    public ImportColumnIndexed(String name, String aliasName, DataType<?> datatype) {
        super(aliasName, datatype);
        setIndex(Integer.MIN_VALUE);
        setName(name);
    }

    /**
     * @return {@link #index}
     */
    public int getIndex() {
        return index;
    }

    /**
     * @param index
     *            {@link #index}
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Gets the name.
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Returns whether an index was given.
     *
     * @return
     */
    public boolean isIndexSpecified() {
        return this.index != Integer.MIN_VALUE;
    }
}
