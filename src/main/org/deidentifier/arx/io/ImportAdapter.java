/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2014 Karol Babioch <karol@babioch.de>
 * Copyright (C) 2014 Fabian Prasser
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

    /**
     * Array of datatypes describing the columns
     */
    protected DataType<?>[]     dataTypes;

    /**
     * Indexes of columns that should be imported
     * 
     * This keeps track of columns that should be imported, as not all columns
     * will necessarily be imported.
     */
    protected int[]             indexes;

    /**
     * Data source configuration used to import actual data
     */
    private ImportConfiguration config = null;

    /**
     * Creates a new instance of this object with given configuration
     * 
     * @param config
     *            {@link #config}
     */
    protected ImportAdapter(ImportConfiguration config) {

        this.config = config;
        if (config.getColumns().isEmpty()) {
            throw new IllegalArgumentException("No columns specified");
        }
    }

    /**
     * Returns the configuration used by the import adapter
     * 
     * @return {@link #config}
     */
    public ImportConfiguration getConfig() {
        return config;
    }

    /**
     * Returns the percentage of data has has already been imported
     * 
     * @return Percentage of data already imported, 0 - 100
     */
    public abstract int getProgress();

    /**
     * Returns an array with datatypes of columns that should be imported
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
