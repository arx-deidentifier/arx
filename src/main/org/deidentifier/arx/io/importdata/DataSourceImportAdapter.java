package org.deidentifier.arx.io.importdata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.io.CSVFileConfiguration;
import org.deidentifier.arx.io.DataSourceConfiguration;
import org.deidentifier.arx.io.ExcelFileConfiguration;


/**
 * Base adapter for all data sources
 *
 * This defines properties and methods that all data source import adapters
 * have in common. Data sources itself are described by
 * {@link DataSourceConfiguration}.
 */
abstract public class DataSourceImportAdapter implements Iterator<String[]> {

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
    private DataSourceConfiguration config = null;


    /**
     * Factory method
     *
     * This will return an appropriate ImportAdapter for each implemented
     * data source {@link DataSourceImportAdapter}. Refer to the specific
     * ImportAdapter itself for details.
     *
     * @param config {@link #config}
     *
     * @return Specific ImportAdapter for given configuration
     *
     * @throws IOException 
     */
    public static DataSourceImportAdapter create(DataSourceConfiguration config) throws IOException {

        if (config instanceof CSVFileConfiguration) {

            return new CSVFileImportAdapter((CSVFileConfiguration)config);

        } else if (config instanceof ExcelFileConfiguration) {

            return new ExcelFileImportAdapter((ExcelFileConfiguration)config);

        } else {

            throw new IllegalArgumentException("No ImportAdapter defined for this type of configuration");

        }

    }

    /**
     * Creates a new instance of this object with given configuration
     *
     * @param config {@link #config}
     */
    protected DataSourceImportAdapter(DataSourceConfiguration config) {

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
     * Returns an array with indexes of columns that should be imported
     *
     * Only columns listed within {@link #columns} will be imported. This
     * iterates over the list of columns and returns an array with indexes
     * of columns that should be imported.
     *
     * @return Array containing indexes of columns that should be imported
     */
    protected int[] getIndexesToImport(){

        /* Get indexes to import from */
        ArrayList<Integer> indexes = new ArrayList<Integer>();
        for(Column column : config.getColumns()) {

            indexes.add(column.getIndex());

        }

        int[] result = new int[indexes.size()];
        for (int i = 0; i < result.length; i++) {

            result[i] = indexes.get(i);

        }

        return result;

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
    public DataSourceConfiguration getConfig() {

        return config;

    }

}
