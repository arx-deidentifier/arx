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

package org.deidentifier.arx.io.importdata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.io.datasource.CSVFileConfiguration;
import org.deidentifier.arx.io.datasource.Configuration;
import org.deidentifier.arx.io.datasource.ExcelFileConfiguration;
import org.deidentifier.arx.io.datasource.JdbcConfiguration;
import org.deidentifier.arx.io.datasource.column.Column;


/**
 * Base adapter for all data sources
 *
 * This defines properties and methods that all data source import adapters
 * have in common. Data sources itself are described by
 * {@link Configuration}.
 */
abstract public class ImportAdapter implements Iterator<String[]> {

    /**
     * Array of datatypes describing the columns
     */
    protected DataType<?>[] dataTypes;

    /**
     * Indexes of columns that should be imported
     *
     * This keeps track of columns that should be imported, as not all columns
     * will necessarily be imported.
     */
    protected int[] indexes;

    /**
     * Data source configuration used to import actual data
     */
    private Configuration config = null;


    /**
     * Factory method
     *
     * This will return an appropriate ImportAdapter for each implemented
     * data source {@link ImportAdapter}. Refer to the specific
     * ImportAdapter itself for details.
     *
     * @param config {@link #config}
     *
     * @return Specific ImportAdapter for given configuration
     *
     * @throws IOException
     */
    public static ImportAdapter create(Configuration config) throws IOException {

        if (config instanceof CSVFileConfiguration) {

            return new CSVFileImportAdapter((CSVFileConfiguration)config);

        } else if (config instanceof ExcelFileConfiguration) {

            return new ExcelFileImportAdapter((ExcelFileConfiguration)config);

        } else if (config instanceof JdbcConfiguration) {

            return new JdbcImportAdapter((JdbcConfiguration)config);

        } else {

            throw new IllegalArgumentException("No ImportAdapter defined for this type of configuration");

        }

    }

    /**
     * Creates a new instance of this object with given configuration
     *
     * @param config {@link #config}
     */
    protected ImportAdapter(Configuration config) {

        this.config = config;

        if (config.getColumns().isEmpty()) {

            throw new IllegalArgumentException("No columns specified");

        }

    }

    /**
     * Returns an array with datatypes of columns that should be imported
     *
     * @return Array containing datatypes of columns that should be imported
     */
    protected DataType<?>[] getColumnDatatypes() {

        List<DataType<?>> result = new ArrayList<DataType<?>>();
        for (Column column : config.getColumns()) {

            result.add(column.getDataType());

        }

        return result.toArray(new DataType[result.size()]);

    }

    /**
     * Returns the percentage of data has has already been imported
     *
     * @return Percentage of data already imported, 0 - 100
     */
    public abstract int getProgress();

    /**
     * Returns the configuration used by the import adapter
     *
     * @return {@link #config}
     */
    public Configuration getConfig() {

        return config;

    }

}
