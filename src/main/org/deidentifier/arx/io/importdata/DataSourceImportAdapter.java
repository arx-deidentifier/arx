package org.deidentifier.arx.io.importdata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deidentifier.arx.DataType;

/**
 * Base adapter that all implementations need to extend
 */
abstract public class DataSourceImportAdapter implements Iterator<String[]> {
    
    /**
     * Factory method
     * @param config
     * @return
     * @throws IOException 
     */
    public static DataSourceImportAdapter create(DataSourceConfiguration config) throws IOException{
        if (config instanceof CSVConfiguration){
            return new CSVImportAdapter((CSVConfiguration)config);
        } else {
            // TODO: Implement
            return null;
        }
    }

    /** The config*/
    private List<Column> columns = null;
    
    /** The config*/
    private DataSourceConfiguration config = null;
    
    /**
     * Creates a new instance
     * @param config
     */
    protected DataSourceImportAdapter(DataSourceConfiguration config){
        this.columns = config.getColumns();
        this.config = config;
        if (this.columns.isEmpty()) {
            throw new IllegalArgumentException("No columns specified");
        }
    }
    
    /**
     * @return Array of datatypes for columns that are selected to import from
     */
    protected DataType<?>[] getColumnDatatypes() {

        List<DataType<?>> result = new ArrayList<DataType<?>>();
        for (Column column : columns) {
            result.add(column.getDatatype());
        }
        return result.toArray(new DataType[result.size()]);
    }

    /**
     * Returns the indexes to import
     * @return
     */
    protected int[] getIndexesToImport(){

        /* Get indexes to import from {@see #indexesToImport} */
        ArrayList<Integer> list = new ArrayList<Integer>();
        for(Column column : columns) {
            list.add(column.getIndex());
        }
        
        int[] result = new int[list.size()];
        for (int i=0; i<result.length; i++){
            result[i] = list.get(i);
        }
        return result;
    }
    
    /**
     * Returns the progress of the import process in percent
     * @return
     */
    public abstract int getProgress();

    /**
     * Returns the configuration
     * @return
     */
    public DataSourceConfiguration getConfig() {
        return config;
    }
}
