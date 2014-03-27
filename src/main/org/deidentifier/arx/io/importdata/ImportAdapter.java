package org.deidentifier.arx.io.importdata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deidentifier.arx.DataType;

/**
 * Base adapter that all implementations need to extend
 */
abstract public class ImportAdapter implements Iterator<String[]> {

    /**
     * @param columns List of columns selected to import from
     */
    public abstract void setColumns(List<Column> columns) throws Exception;

    /**
     * @return List of columns selected to import from
     */
    public abstract List<Column> getColumns();

    /**
     * @return Array of datatypes for columns that are selected to import from
     *
     * @throws Exception In case no columns are selected
     */
    public DataType<?>[] getColumnDatatypes() throws Exception {

        if (getColumns().size() == 0) {

            throw new Exception("No columns selected");

        }

        List<DataType<?>> result = new ArrayList<DataType<?>>();

        for (Column column : getColumns()) {

                result.add(column.getDatatype());

        }

        return result.toArray(new DataType[result.size()]);

    }

}
