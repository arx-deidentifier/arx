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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deidentifier.arx.DataType;

/**
 * Base adapter for all data sources
 * 
 * This defines properties and methods that all data source import adapters have
 * in common. Data sources itself are described by {@link ImportConfiguration}.
 * 
 * @author Karol Babioch
 * @author Fabian Prasser
 */
abstract public class ImportAdapter implements Iterator<String[]> {
    
    /**
     * Factory method
     * 
     * This will return an appropriate ImportAdapter for each implemented data
     * source {@link ImportAdapter}. Refer to the specific ImportAdapter itself
     * for details.
     * 
     * @param config {@link #config}
     * 
     * @return Specific ImportAdapter for given configuration
     * 
     * @throws IOException
     */
    public static ImportAdapter create(ImportConfiguration config) throws IOException {

        if (config instanceof ImportConfigurationCSV) {
            return new ImportAdapterCSV((ImportConfigurationCSV) config);
        } else if (config instanceof ImportConfigurationExcel) {
            return new ImportAdapterExcel((ImportConfigurationExcel) config);
        } else if (config instanceof ImportConfigurationJDBC) {
            return new ImportAdapterJDBC((ImportConfigurationJDBC) config);
        } else {
            throw new IllegalArgumentException("No adapter defined for this type of configuration");
        }
    }

    /** The header. */
    protected String[] header;

    /** Array of datatypes describing the columns. */
    protected DataType<?>[]     dataTypes;
    
    /**
     * Indexes of columns that should be imported
     * 
     * This keeps track of columns that should be imported, as not all columns
     * will necessarily be imported.
     */
    protected int[]             indexes;

    /** Data source configuration used to import actual data. */
    private ImportConfiguration config = null;

    /**
     * Creates a new instance of this object with given configuration.
     *
     * @param config {@link #config}
     */
    protected ImportAdapter(ImportConfiguration config) {

        this.config = config;
        if (config.getColumns().isEmpty()) {
            throw new IllegalArgumentException("No columns specified");
        }
    }

    /**
     * Returns the configuration used by the import adapter.
     *
     * @return {@link #config}
     */
    public ImportConfiguration getConfig() {
        return config;
    }

    /**
     * Returns the header.
     *
     * @return
     */
    public String[] getHeader() {
        return header;
    }
    
    /**
     * Returns the number of records, if available, null otherwise
     * @return
     */
    public Integer getLength() {
        return null;
    }

    /**
     * Returns the percentage of data has has already been imported.
     *
     * @return Percentage of data already imported, 0 - 100
     */
    public abstract int getProgress();
    
    /**
     * Returns an array with datatypes of columns that should be imported.
     *
     * @return Array containing datatypes of columns that should be imported
     */
    protected DataType<?>[] getColumnDatatypes() {

        List<DataType<?>> result = new ArrayList<DataType<?>>();
        for (ImportColumn column : config.getColumns()) {
            result.add(column.getDataType());
        }
        return result.toArray(new DataType[result.size()]);

    }
}
