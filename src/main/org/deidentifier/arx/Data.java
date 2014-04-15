/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
 * Copyright (C) 2014 Karol Babioch <karol@babioch.de>
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

package org.deidentifier.arx;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.deidentifier.arx.io.CSVDataInput;
import org.deidentifier.arx.io.ImportAdapter;
import org.deidentifier.arx.io.ImportColumn;
import org.deidentifier.arx.io.ImportConfiguration;

/**
 * Represents input data for the ARX framework
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public abstract class Data {

    /**
     * A data object for arrays
     * 
     * @author Fabian Prasser
 * @author Florian Kohlmayer
     */
    static class ArrayData extends Data {

        /** The array */
        private final String[][] array;

        /** Creates a new instance */
        private ArrayData(final String[][] array) {
            this.array = array;
        }

        @Override
        protected Iterator<String[]> iterator() {
            return new Iterator<String[]>() {

                private int pos = 0;

                @Override
                public boolean hasNext() {
                    return pos < array.length;
                }

                @Override
                public String[] next() throws NoSuchElementException {
                    if (hasNext()) {
                        return array[pos++];
                    } else {
                        throw new NoSuchElementException();
                    }
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

    }

    /**
     * The default implementation of a data object. It allows the user to
     * programmatically define its content.
     * 
     * @author Fabian Prasser
 * @author Florian Kohlmayer
     */
    public static class DefaultData extends Data {

        /** List of tuples */
        private final List<String[]> data = new ArrayList<String[]>();

        /**
         * Adds a row to this data object
         * 
         * @param row
         */
        public void add(final String... row) {
            data.add(row);
        }

        @Override
        protected Iterator<String[]> iterator() {
            return data.iterator();
        }

    }

    /**
     * A data object for iterators
     * 
     * @author Fabian Prasser
 * @author Florian Kohlmayer
     */
    static class IterableData extends Data {

        /** Iterator over tuples */
        private Iterator<String[]> iterator = null;

        /** Creates a new instance */
        private IterableData(final Iterator<String[]> iterator) {
            this.iterator = iterator;
        }

        @Override
        protected Iterator<String[]> iterator() {
            return iterator;
        }
    }

    /**
     * Creates a new default data object
     * 
     * @return A Data object
     */
    public static DefaultData create() {
        return new DefaultData();
    }

    /**
     * Creates a new data object from a CSV file
     * 
     * @param file
     *            A file
     * @param separator
     *            The utilized separator character
     * @return A Data object
     * @throws IOException
     */
    public static Data
            create(final File file, final char separator) throws IOException {
        return new IterableData(new CSVDataInput(file, separator).iterator());
    }

    /**
     * Creates a new data object from a CSV file
     * 
     * @param stream
     *            An input stream
     * @param separator
     *            The utilized separator character
     * @return A Data object
     * @throws IOException
     */
    public static Data create(final InputStream stream, final char separator) throws IOException {
        return new IterableData(new CSVDataInput(stream, separator).iterator());
    }

    /**
     * Creates a new data object from a given import adapter
     * 
     * @param adapter An adapter
     * @return A Data object
     */
    public static Data create(final ImportAdapter adapter) {

        Data data = new IterableData(adapter);
        
        // TODO: This is ugly
        Map<Integer, DataType<?>> types = new HashMap<Integer, DataType<?>>();
        List<ImportColumn> columns = adapter.getConfig().getColumns();
        for (int i=0; i<columns.size(); i++){
            types.put(i, columns.get(i).getDataType());
        }
        DataHandle handle = data.getHandle();
        for (int i=0; i<handle.getNumColumns(); i++) {
            String attribute = handle.getAttributeName(i);
            data.getDefinition().setDataType(attribute, types.get(i));
        }

        // Return
        return data;
    }

    /**
     * Creates a new data object from an iterator over tuples
     * 
     * @param iterator
     *            An iterator
     * @return A Data object
     */
    public static Data create(final Iterator<String[]> iterator) {
        return new IterableData(iterator);
    }

    /**
     * Creates a new data object from a list
     * 
     * @param list
     *            The list
     * @return A Data object
     */
    public static Data create(final List<String[]> list) {
        return new IterableData(list.iterator());
    }

    /**
     * Creates a new data object from a CSV file
     * 
     * @param path
     *            A path to the file
     * @param separator
     *            The utilized separator character
     * @return A Data object
     * @throws IOException
     */
    public static Data
            create(final String path, final char separator) throws IOException {
        return new IterableData(new CSVDataInput(path, separator).iterator());
    }

    /**
     * Creates a new data object from an import Adapter
     *
     * @param config The config that should be used to import data
     *
     * @return Data object as described by ImportAdapter
     *
     * @throws IOException
     */
    public static Data create(final ImportConfiguration config) throws IOException {

        final Data data = new IterableData(ImportAdapter.create(config));

        // TODO: This is ugly
        Map<Integer, DataType<?>> types = new HashMap<Integer, DataType<?>>();
        List<ImportColumn> columns = config.getColumns();
        for (int i=0; i<columns.size(); i++){
            types.put(i, columns.get(i).getDataType());
        }
        DataHandle handle = data.getHandle();
        for (int i=0; i<handle.getNumColumns(); i++) {
            String attribute = handle.getAttributeName(i);
            data.getDefinition().setDataType(attribute, types.get(i));
        }

        // Return
        return data;
    }

    /**
     * Creates a new data object from a two-dimensional string array
     * 
     * @param array
     *            The array
     * @return A Data object
     */
    public static Data create(final String[][] array) {
        return new ArrayData(array);
    }

    private DataHandleInput handle;

    private DataDefinition  definition = new DataDefinition();

    /**
     * Returns the data definition
     * 
     * @return
     */
    public DataDefinition getDefinition() {
        return definition;
    }

    /**
     * Returns a data handle
     * 
     * @return
     */
    public DataHandle getHandle() {
        if (handle == null) {
            handle = new DataHandleInput(this);
        } else {
            handle.update(this);
        }
        return handle;
    }

    protected abstract Iterator<String[]> iterator();
}
