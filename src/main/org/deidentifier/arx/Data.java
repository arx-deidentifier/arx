/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.deidentifier.arx.io.CSVDataInput;
import org.deidentifier.arx.io.CSVOptions;
import org.deidentifier.arx.io.CSVSyntax;
import org.deidentifier.arx.io.ImportAdapter;
import org.deidentifier.arx.io.ImportConfiguration;

/**
 * Represents input data for the ARX framework.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public abstract class Data { // NO_UCD

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
         * @param row the row
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
         * @param array the array
         */
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
     * A data object for iterators.
     *
     * @author Fabian Prasser
     * @author Florian Kohlmayer
     */
    static class IterableData extends Data {

        /** Iterator over tuples. */
        private Iterator<String[]> iterator = null;
        
        /** Length*/
        private Integer length;

        /**
         * Creates a new instance.
         *
         * @param iterator the iterator
         */
        private IterableData(final Iterator<String[]> iterator) {
            this.iterator = iterator;
            this.length = null;
        }

        /**
         * Creates a new instance.
         *
         * @param iterator the iterator
         */
        private IterableData(final Iterator<String[]> iterator, Integer length) {
            this.iterator = iterator;
            this.length = length;
        }

        @Override
        protected Integer getLength() {
            return length;
        }
        
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
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Data create(final DataSource source) throws IOException {

        ImportConfiguration config = source.getConfiguration();
        ImportAdapter adapter = ImportAdapter.create(config);
        return create(adapter, adapter.getLength());
    }

    /**
     * Creates a new data object from a CSV file.
     *
     * @param file the file
     * @return the data
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Data create(final File file, final Charset charset) throws IOException {
        return new IterableData(new CSVDataInput(file, charset).iterator());
    }

    /**
     * Creates a new data object from a CSV file.
     *
     * @param file A file
     * @param delimiter The utilized separator character
     * @return A Data object
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Data create(final File file, final Charset charset, final char delimiter) throws IOException {
        return new IterableData(new CSVDataInput(file, charset, delimiter).iterator());
    }

    /**
     * Creates a new data object from a CSV file.
     *
     * @param file A file
     * @param delimiter The utilized separator character
     * @param quote The delimiter for strings
     * @return A Data object
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Data create(final File file, final Charset charset, final char delimiter, final char quote) throws IOException {
        return new IterableData(new CSVDataInput(file, charset, delimiter, quote).iterator());
    }

    /**
     * Creates a new data object from a CSV file.
     *
     * @param file the file
     * @param delimiter the delimiter
     * @param quote the quote
     * @param escape the escape
     * @return the data
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Data create(final File file, final Charset charset, final char delimiter, final char quote, final char escape) throws IOException {
        return new IterableData(new CSVDataInput(file, charset, delimiter, quote, escape).iterator());
    }

    /**
     * Creates a new data object from a CSV file.
     *
     * @param file the file
     * @param delimiter the delimiter
     * @param quote the quote
     * @param escape the escape
     * @param linebreak the linebreak
     * @return the data
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Data create(final File file, final Charset charset, final char delimiter, final char quote, final char escape, final char[] linebreak) throws IOException {
        return new IterableData(new CSVDataInput(file, charset, delimiter, quote, escape, linebreak).iterator());
    }

    /**
     * Creates a new data object from a CSV file.
     *
     * @param file the file
     * @param config the config
     * @return the data
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Data create(final File file, final Charset charset, final CSVSyntax config) throws IOException {
        return new IterableData(new CSVDataInput(file, charset, config).iterator());
    }

    /**
     * Creates a new data object from a CSV file.
     *
     * @param file the file
     * @param config the config
     * @param options the options
     * @return the data
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Data create(final File file, final Charset charset, final CSVSyntax config, final CSVOptions options) throws IOException {
        return new IterableData(new CSVDataInput(file, charset, config, options).iterator());
    }

    /**
     * Creates a new data object from a CSV file.
     *
     * @param file the file
     * @param config the config
     * @param datatypes the datatypes
     * @return the data
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Data create(final File file, final Charset charset, final CSVSyntax config, final DataType<?>[] datatypes) throws IOException {
        return new IterableData(new CSVDataInput(file, charset, config, datatypes).iterator());
    }
    /**
     * Creates a new data object from a CSV file.
     *
     * @param stream the stream
     * @return the data
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Data create(final InputStream stream, final Charset charset) throws IOException {
        return new IterableData(new CSVDataInput(stream, charset).iterator());
    }

    /**
     * Creates a new data object from a CSV file.
     *
     * @param stream An input stream
     * @param delimiter The utilized separator character
     * @return A Data object
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Data create(final InputStream stream, final Charset charset, final char delimiter) throws IOException {
        return new IterableData(new CSVDataInput(stream, charset, delimiter).iterator());
    }

    /**
     * Creates a new data object from a CSV file.
     *
     * @param stream An input stream
     * @param delimiter The utilized separator character
     * @param length For improved memory requirements
     * @return A Data object
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Data create(final InputStream stream, final Charset charset, final char delimiter, final int length) throws IOException {
        return new IterableData(new CSVDataInput(stream, charset, delimiter).iterator(), length);
    }
    
    /**
     * Creates a new data object from a CSV file.
     *
     * @param stream An input stream
     * @param delimiter The utilized separator character
     * @param quote The delimiter for strings
     * @return A Data object
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Data create(final InputStream stream, final Charset charset, final char delimiter, final char quote) throws IOException {
        return new IterableData(new CSVDataInput(stream, charset, delimiter, quote).iterator());
    }

    /**
     * Creates a new data object from a CSV file.
     *
     * @param stream the stream
     * @param delimiter the delimiter
     * @param quote the quote
     * @param escape the escape
     * @return the data
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Data create(final InputStream stream, final Charset charset, final char delimiter, final char quote, final char escape) throws IOException {
        return new IterableData(new CSVDataInput(stream, charset, delimiter, quote, escape).iterator());
    }

    /**
     * Creates a new data object from a CSV file.
     *
     * @param stream the stream
     * @param delimiter the delimiter
     * @param quote the quote
     * @param escape the escape
     * @param linebreak the linebreak
     * @return the data
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Data create(final InputStream stream, final Charset charset, final char delimiter, final char quote, final char escape, final char[] linebreak) throws IOException {
        return new IterableData(new CSVDataInput(stream, charset, delimiter, quote, escape, linebreak).iterator());
    }

    /**
     * Creates a new data object from a CSV file.
     *
     * @param stream the stream
     * @param config the config
     * @return the data
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Data create(final InputStream stream, final Charset charset, final CSVSyntax config) throws IOException {
        return new IterableData(new CSVDataInput(stream, charset, config).iterator());
    }

    /**
     * Creates a new data object from a CSV file.
     *
     * @param stream the stream
     * @param config the config
     * @param datatypes the datatypes
     * @return the data
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Data create(final InputStream stream, final Charset charset, final CSVSyntax config, final DataType<?>[] datatypes) throws IOException {
        return new IterableData(new CSVDataInput(stream, charset, config, datatypes).iterator());
    }

    /**
     * Creates a new data object from an iterator over tuples.
     *
     * @param iterator An iterator
     * @return A Data object
     */
    public static Data create(final Iterator<String[]> iterator) {
        
        // Prepare
        IterableData result = null;
        
        // Optimize, if possible
        if (iterator instanceof ImportAdapter) {
            
            // Obtain data
            result = new IterableData(iterator, ((ImportAdapter)iterator).getLength());
            
        } else {

            // Obtain data
            result = new IterableData(iterator);
        }

        // Update definition, if needed
        if (iterator instanceof ImportAdapter) {
            result.getDefinition().parse((ImportAdapter) iterator);
        }

        // Return
        return result;
    }

    /**
     * Creates a new data object from an iterator over tuples.
     *
     * @param iterator An iterator
     * @param length number of records to load
     * @return A data object
     */
    public static Data create(final Iterator<String[]> iterator, Integer length) {

        // Obtain data
        IterableData result = new IterableData(iterator, length);

        // Update definition, if needed
        if (iterator instanceof ImportAdapter) {
            result.getDefinition().parse((ImportAdapter) iterator);
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
     * @param path the path
     * @return the data
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Data create(final String path, final Charset charset) throws IOException {
        return new IterableData(new CSVDataInput(path, charset).iterator());
    }

    /**
     * Creates a new data object from a CSV file.
     *
     * @param path A path to the file
     * @param delimiter The utilized separator character
     * @return A Data object
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Data create(final String path, final Charset charset, final char delimiter) throws IOException {
        return new IterableData(new CSVDataInput(path, charset, delimiter).iterator());
    }

    /**
     * Creates a new data object from a CSV file.
     *
     * @param path A path to the file
     * @param delimiter The utilized separator character
     * @param quote The delimiter for strings
     * @return A Data object
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Data create(final String path, final Charset charset, final char delimiter, final char quote) throws IOException {
        return new IterableData(new CSVDataInput(path, charset, delimiter, quote).iterator());
    }

    /**
     * Creates a new data object from a CSV file.
     *
     * @param path the path
     * @param delimiter the delimiter
     * @param quote the quote
     * @param escape the escape
     * @return the data
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Data create(final String path, final Charset charset, final char delimiter, final char quote, final char escape) throws IOException {
        return new IterableData(new CSVDataInput(path, charset, delimiter, quote, escape).iterator());
    }

    /**
     * Creates a new data object from a CSV file.
     *
     * @param path the path
     * @param delimiter the delimiter
     * @param quote the quote
     * @param escape the escape
     * @param linebreak the linebreak
     * @return the data
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Data create(final String path, final Charset charset, final char delimiter, final char quote, final char escape, final char[] linebreak) throws IOException {
        return new IterableData(new CSVDataInput(path, charset, delimiter, quote, escape, linebreak).iterator());
    }

    /**
     * Creates a new data object from a CSV file.
     *
     * @param path the path
     * @param config the config
     * @param options the options
     * @return the data
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Data create(final String path, final Charset charset, final CSVSyntax config, final CSVOptions options) throws IOException {
        return new IterableData(new CSVDataInput(path, charset, config, options).iterator());
    }

    /**
     * Creates a new data object from a CSV file.
     *
     * @param path the path
     * @param config the config
     * @param datatypes the datatypes
     * @return the data
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Data create(final String path, final Charset charset, final CSVSyntax config, final DataType<?>[] datatypes) throws IOException {
        return new IterableData(new CSVDataInput(path, charset, config, datatypes).iterator());
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

    /** The data handle. */
    private DataHandleInput handle;

    /** The data definition. */
    private DataDefinition  definition = new DataDefinition();

    /**
     * Returns the data definition.
     *
     * @return the definition
     */
    public DataDefinition getDefinition() {
        return definition;
    }

    /**
     * Returns a data handle.
     *
     * @return the handle
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
     * Override to return a length to improve loading
     * @return
     */
    protected Integer getLength() {
        return null;
    }

    /**
     * Iterator.
     *
     * @return the iterator
     */
    protected abstract Iterator<String[]> iterator();
}
