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

import java.util.ArrayList;
import java.util.List;


/**
 * Abstract base configuration
 *
 * This abstract superclass defines properties that all configurations have
 * in common, i.e. a notion of columns, which can be added and retrieved.
 * 
 * @author Karol Babioch
 * @author Fabian Prasser
 */
public abstract class ImportConfiguration {

    /**
     * List of columns to be imported
     *
     * Each element of this list represents a single column to import from.
     * Refer to {@link ImportColumn} for details. Columns can be added by invoking
     * {@link #addColumn(ImportColumn)} and retrieved by {@link #getColumns()}.
     *
     * @note Only columns that are part of this list will be imported from,
     * any other column will simply be ignored.
     */
    protected List<ImportColumn> columns = new ArrayList<ImportColumn>();

    /**
     * Optimized loading, if supported
     */
    private boolean optimizedLoading = false;

    /**
     * Adds a single column to import from.
     *
     * @param column A single column to import from
     * @note This needs to be implemented by the specific configuration class,
     *       as {@link ImportColumn} is only an abstract superclass for various kind of
     *       columns.
     */
    abstract public void addColumn(ImportColumn column);

    /**
     * Returns all added columns.
     *
     * @return {@link #columns}
     */
    public List<ImportColumn> getColumns() {
        return columns;
    }

    /**
     * Tries to enable more efficient loading, if supported
     * @return the optimizedLoading
     */
    public boolean isOptimizedLoading() {
        return optimizedLoading;
    }

    /**
     * Tries to enable more efficient loading, if supported
     * @param optimizedLoading
     */
    public void setOptimizedLoading(boolean optimizedLoading) {
        this.optimizedLoading = optimizedLoading;
    }
}
