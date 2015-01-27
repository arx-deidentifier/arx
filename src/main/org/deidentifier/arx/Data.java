/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.arx;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.deidentifier.arx.io.CSVDataInput;
import org.deidentifier.arx.io.ImportAdapter;
import org.deidentifier.arx.io.ImportConfiguration;

/**
 * Represents input data for the ARX framework.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public abstract class Data {

    /**
     * The default implementation of a data object. It allows the user to
     * programmatically define its content.
     * 
     * @author Fabian Prasser
 * @author Florian Kohlmayer
     */
    public static class DefaultData extends Data {

        /** List of tuples. */
        private final List<String[]> data = new ArrayList<String[]>();

        /**
         * Adds a row to this data object.
         *
         * @param row
         */
        public void add(final String... row) {
            data.add(row);
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.Data#iterator()
         */
        @Override
        protected Iterator<String[]> iterator() {
            return data.iterator();
        }

    }

    /**
     * A data object for arrays.
     *
     * @author Fabian Prasser
     * @author Florian Kohlmayer
     */
    static class ArrayData extends Data {

        /** The array. */
        private final String[][] array;

        /**
         * Creates a new instance.
         *
         * @param array
         */
        private ArrayData(final String[][] array) {
            this.array = array;
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.Data#iterator()
         */
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
     * A data object for iterators.
     *
     * @author Fabian Prasser
     * @author Florian Kohlmayer
     */
    static class IterableData extends Data {

        /** Iterator over tuples. */
        private Iterator<String[]> iterator = null;

        /**
         * Creates a new instance.
         *
         * @param iterator
         */
        private IterableData(final Iterator<String[]> iterator) {
            this.iterator = iterator;
        }

        /* (non-Javadoc)
         * @see org.deidentifier.arx.Data#iterator()
         */
        @Override
        protected Iterator<String[]> iterator() {
            return iterator;
        }
    }

    /**
     * Creates a new default data object.
     *
     * @return A Data object
     */
    public static DefaultData create() {
        return new DefaultData();
    }

    /**
     * Creates a new data object from the given data source specification.
     *
     * @param source The source that should be used to import data
     * @return Data object as described by the data source
     * @throws IOException
     */
    public static Data create(final DataSource source) throws IOException {

        ImportConfiguration config = source.getConfiguration();
        ImportAdapter adapter = ImportAdapter.create(config);
        return create(adapter);
    }

    /**
     * Creates a new data object from a CSV file.
     *
     * @param file A file
     * @param separator The utilized separator character
     * @return A Data object
     * @throws IOException
     */
    public static Data
            create(final File file, final char separator) throws IOException {
        return new IterableData(new CSVDataInput(file, separator).iterator());
    }

    /**
     * Creates a new data object from a CSV file.
     *
     * @param stream An input stream
     * @param separator The utilized separator character
     * @return A Data object
     * @throws IOException
     */
    public static Data create(final InputStream stream, final char separator) throws IOException {
        return new IterableData(new CSVDataInput(stream, separator).iterator());
    }

    /**
     * Creates a new data object from an iterator over tuples.
     *
     * @param iterator An iterator
     * @return A Data object
     */
    public static Data create(final Iterator<String[]> iterator) {
        
        // Obtain data
        IterableData result = new IterableData(iterator);

        // Update definition, if needed
        if (iterator instanceof ImportAdapter){
            result.getDefinition().parse((ImportAdapter)iterator);
        }
        
        // Return
        return result;
    }

    /**
     * Creates a new data object from a list.
     *
     * @param list The list
     * @return A Data object
     */
    public static Data create(final List<String[]> list) {
        return new IterableData(list.iterator());
    }

    /**
     * Creates a new data object from a CSV file.
     *
     * @param path A path to the file
     * @param separator The utilized separator character
     * @return A Data object
     * @throws IOException
     */
    public static Data
            create(final String path, final char separator) throws IOException {
        return new IterableData(new CSVDataInput(path, separator).iterator());
    }

    /**
     * Creates a new data object from a two-dimensional string array.
     *
     * @param array The array
     * @return A Data object
     */
    public static Data create(final String[][] array) {
        return new ArrayData(array);
    }

    /**  TODO */
    private DataHandleInput handle;

    /**  TODO */
    private DataDefinition  definition = new DataDefinition();

    /**
     * Returns the data definition.
     *
     * @return
     */
    public DataDefinition getDefinition() {
        return definition;
    }

    /**
     * Returns a data handle.
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

    /**
     * 
     *
     * @return
     */
    protected abstract Iterator<String[]> iterator();
}
