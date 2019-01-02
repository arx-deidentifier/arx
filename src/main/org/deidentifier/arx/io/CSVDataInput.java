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

package org.deidentifier.arx.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Iterator;

import org.deidentifier.arx.DataType;

import com.univocity.parsers.csv.CsvFormat;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

/**
 * This class implements a reader for CSV encoded information.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class CSVDataInput {
    
    /**
     * Static helper class for lazy initialization of a read
     * 
     * @author Fabian Prasser
     * @author Florian Kohlmayer
     */
    private static class LazyFileReader extends Reader {

        /** Reader */
        private InputStreamReader reader = null;
        /** File */
        private final File file;
        /** Charset */
        private final Charset charset;

        /**
         * Creates a new instance
         * 
         * @param file
         */
        public LazyFileReader(File file, Charset charset) {
            this.file = file;
            this.charset = charset;
        }

        @Override
        public void close() throws IOException {
            if (reader != null) {
                reader.close();
            }
        }

        @Override
        @SuppressWarnings("resource")
        public int read(char[] cbuf, int off, int len) throws IOException {
            reader = reader != null ? reader : new InputStreamReader(new FileInputStream(file), charset);
            return reader.read(cbuf, off, len);
        }
    }

    /** A reader. */
    private final Reader            reader;

    /** Settings. */
    private final CsvParserSettings settings;

    /** Cleanisng enabled */
    private final boolean           cleansing;

    /** The data type for each column */
    private final DataType<?>[]     datatypes;
    
    /**
     * Instantiate.
     *
     * @param file the file
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final File file, final Charset charset) throws IOException {
        this(file, charset, CSVSyntax.DEFAULT_DELIMITER);
    }

    /**
     * Instantiate.
     *
     * @param file the file
     * @param delimiter the delimiter
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final File file, final Charset charset, final char delimiter) throws IOException {
        this(file, charset, delimiter, CSVSyntax.DEFAULT_QUOTE);
    }

    /**
     * Instantiate.
     *
     * @param file the file
     * @param delimiter the delimiter
     * @param quote the quote
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final File file, final Charset charset, final char delimiter, final char quote) throws IOException {
        this(file, charset, delimiter, quote, CSVSyntax.DEFAULT_ESCAPE);
    }

    /**
     * Instantiate.
     *
     * @param file the file
     * @param delimiter the delimiter
     * @param quote the quote
     * @param escape the escape
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final File file, final Charset charset, final char delimiter, final char quote, final char escape) throws IOException {
        this(file, charset, delimiter, quote, escape, CSVSyntax.DEFAULT_LINEBREAK);
    }

    /**
     * Instantiate.
     *
     * @param file the file
     * @param delimiter the delimiter
     * @param quote the quote
     * @param escape the escape
     * @param linebreak the linebreak
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final File file, final Charset charset, final char delimiter, final char quote, final char escape, final char[] linebreak) throws IOException {
        this(new LazyFileReader(file, charset), delimiter, quote, escape, linebreak, null);
    }
    
    /**
     * Instantiate.
     *
     * @param file the file
     * @param config the config
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final File file, final Charset charset,  final CSVSyntax config) throws IOException {
        this(file, charset, config, (DataType<?>[])null);
    }

    /**
     * Instantiate.
     *
     * @param file the file
     * @param config the config
     * @param options the options
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final File file, final Charset charset,  final CSVSyntax config, final CSVOptions options) throws IOException {
        this(new FileReader(file),
             config.getDelimiter(),
             config.getQuote(),
             config.getEscape(),
             config.getLinebreak(),
             null,
             options);
    }
    
    /**
     * Instatiate.
     * 
     * @param file
     * @param config
     * @param datatype
     * @throws IOException
     */
    public CSVDataInput(final File file, final Charset charset, final CSVSyntax config, final DataType<?>[] datatype) throws IOException {
        this(new LazyFileReader(file, charset), config.getDelimiter(), config.getQuote(), config.getEscape(), config.getLinebreak(), datatype);
    }

    /**
     * Instantiate.
     *
     * @param stream the stream
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final InputStream stream, final Charset charset) throws IOException {
        this(stream, charset, CSVSyntax.DEFAULT_DELIMITER);
    }

    /**
     * Instantiate.
     *
     * @param stream the stream
     * @param delimiter the delimiter
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final InputStream stream, final Charset charset, final char delimiter) throws IOException {
        this(stream, charset, delimiter, CSVSyntax.DEFAULT_QUOTE);
    }

    /**
     * Instantiate.
     *
     * @param stream the stream
     * @param delimiter the delimiter
     * @param quote the quote
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final InputStream stream, final Charset charset, final char delimiter, final char quote) throws IOException {
        this(stream, charset, delimiter, quote, CSVSyntax.DEFAULT_ESCAPE);
    }

    /**
     * Instantiate.
     *
     * @param stream the stream
     * @param delimiter the delimiter
     * @param quote the quote
     * @param escape the escape
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final InputStream stream, final Charset charset, final char delimiter, final char quote, final char escape) throws IOException {
        this(stream, charset, delimiter, quote, escape, CSVSyntax.DEFAULT_LINEBREAK);
    }

    /**
     * Instantiate.
     *
     * @param stream the stream
     * @param delimiter the delimiter
     * @param quote the quote
     * @param escape the escape
     * @param linebreak the linebreak
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final InputStream stream, final Charset charset, final char delimiter, final char quote, final char escape, final char[] linebreak) throws IOException {
        this(new InputStreamReader(stream, charset), delimiter, quote, escape, linebreak, null);
    }

    /**
     * Instantiate.
     *
     * @param stream the stream
     * @param config the config
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final InputStream stream, final Charset charset, final CSVSyntax config) throws IOException {
        this(stream, charset, config, (DataType<?>[])null);
    }

    /**
     * Instantiate.
     *
     * @param stream the stream
     * @param config the config
     * @param options the options
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final InputStream stream, final Charset charset, final CSVSyntax config, final CSVOptions options) throws IOException {
        this(stream, charset, config, (DataType<?>[])null, options);
    }

    /**
     * Instantiate.
     * 
     * @param stream
     * @param config
     * @param datatypes
     * @throws IOException
     */
    public CSVDataInput(final InputStream stream, final Charset charset, final CSVSyntax config, final DataType<?>[] datatypes) throws IOException {
        this(new InputStreamReader(stream, charset), config.getDelimiter(), config.getQuote(), config.getEscape(), config.getLinebreak(), datatypes);
    }

    /**
     * Instantiate.
     * 
     * @param stream
     * @param config
     * @param datatypes
     * @param options
     * @throws IOException
     */
    public CSVDataInput(final InputStream stream, final Charset charset, final CSVSyntax config, final DataType<?>[] datatypes, CSVOptions options) throws IOException {
        this(new InputStreamReader(stream, charset), config.getDelimiter(), config.getQuote(), config.getEscape(), config.getLinebreak(), datatypes, options);
    }
    
    /**
     * Instantiate.
     *
     * @param reader the reader
     * @param delimiter the delimiter
     * @param quote the quote
     * @param escape the escape
     * @param linebreak the linebreak
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final Reader reader, final char delimiter, final char quote, final char escape, final char[] linebreak, final DataType<?>[] datatypes) throws IOException {
        this(reader, delimiter, quote, escape, linebreak, datatypes, null);
    }
    
    /**
     * Instantiate.
     *
     * @param reader the reader
     * @param delimiter the delimiter
     * @param quote the quote
     * @param escape the escape
     * @param linebreak the linebreak
     * @param options the options
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final Reader reader, final char delimiter, final char quote, final char escape, final char[] linebreak, final DataType<?>[] datatypes, CSVOptions options) throws IOException {
        this.reader = reader;
        this.datatypes = datatypes;
        if (datatypes != null) {
            cleansing = true;
        } else {
            cleansing = false;
        }
        settings = createSettings(delimiter, quote, escape, linebreak, options);
    }

    /**
     * Instantiate.
     *
     * @param filename the filename
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final String filename, final Charset charset) throws IOException {
        this(filename, charset, CSVSyntax.DEFAULT_DELIMITER);
    }

    /**
     * Instantiate.
     *
     * @param filename the filename
     * @param delimiter the delimiter
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final String filename, final Charset charset, final char delimiter) throws IOException {
        this(filename, charset, delimiter, CSVSyntax.DEFAULT_QUOTE);
    }

    /**
     * Instantiate.
     *
     * @param filename the filename
     * @param delimiter the delimiter
     * @param quote the quote
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final String filename, final Charset charset, final char delimiter, final char quote) throws IOException {
        this(filename, charset, delimiter, quote, CSVSyntax.DEFAULT_ESCAPE);
    }

    /**
     * Instantiate.
     *
     * @param filename the filename
     * @param delimiter the delimiter
     * @param quote the quote
     * @param escape the escape
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final String filename, final Charset charset, final char delimiter, final char quote, final char escape) throws IOException {
        this(filename, charset, delimiter, quote, escape, CSVSyntax.DEFAULT_LINEBREAK);
    }

    /**
     * Instantiate.
     *
     * @param filename the filename
     * @param delimiter the delimiter
     * @param quote the quote
     * @param escape the escape
     * @param linebreak the linebreak
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final String filename, final Charset charset, final char delimiter, final char quote, final char escape, final char[] linebreak) throws IOException {
        this(new File(filename), charset, delimiter, quote, escape, linebreak);
    }

    /**
     * Instantiate.
     *
     * @param filename the filename
     * @param config the config
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final String filename, final Charset charset, final CSVSyntax config) throws IOException {
        this(filename, charset, config, (DataType<?>[])null);
    }

    /**
     * Instantiate
     * @param filename
     * @param charset
     * @param config
     * @param options
     * @throws IOException 
     */
    public CSVDataInput(String filename, Charset charset, CSVSyntax config, CSVOptions options) throws IOException {
        this(new LazyFileReader(new File(filename), charset), config.getDelimiter(), config.getQuote(), config.getEscape(), config.getLinebreak(), null, options);
    }

    /**
     * Instantiate.
     * 
     * @param filename
     * @param config
     * @param datatypes
     * @throws IOException
     */
    public CSVDataInput(final String filename, final Charset charset, final CSVSyntax config, final DataType<?>[] datatypes) throws IOException {
        this(new LazyFileReader(new File(filename), charset), config.getDelimiter(), config.getQuote(), config.getEscape(), config.getLinebreak(), datatypes);
    }

    /**
     * Closes the reader.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void close() throws IOException {
        reader.close();
    }


    /**
     * Returns an iterator. <b>You must iterate trough all elements to prevent resource leaks!</b>
     * 
     * @return the iterator
     */
    public Iterator<String[]> iterator() {

        return new Iterator<String[]>() {

            // Next tuple
            boolean   initialized = false;
            CsvParser parser      = null;
            String[]  next        = null;

            @Override
            public boolean hasNext() {

                initParser();
                boolean result = next != null;
                if (!result && parser != null) {
                    parser.stopParsing();
                    parser = null;
                }
                return result;
            }

            @Override
            public String[] next() {

                // Init
                initParser();
                String[] result = next;
                next = parser.parseNext();
                
                // Replace each non matching value with the special NULL string
                if (cleansing) {

                    if (result.length != datatypes.length) {
                        throw new IllegalArgumentException("More columns available in CSV file than data types specified");
                    }

                    for (int i = 0; i < result.length; i++) {
                        if (!datatypes[i].isValid(result[i])) {
                            result[i] = DataType.NULL_VALUE;
                        }
                    }
                }
                return result;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not implemented");
            }

            /** Initializes the parser */

            private void initParser() {
                if (!initialized) {
                    parser = new CsvParser(settings);
                    parser.beginParsing(reader);
                    next = parser.parseNext();
                    initialized = true;
                }
            }
        };
    }

    /**
     * Creates the CsvParserSettings.
     *
     * @param delimiter the delimiter
     * @param quote the quote
     * @param escape the escape
     * @param linebreak the linebreak
     * @param options
     * @return the csv parser settings
     */
    private CsvParserSettings createSettings(final char delimiter, final char quote, final char escape, final char[] linebreak, CSVOptions options) {
        CsvFormat format = new CsvFormat();
        format.setDelimiter(delimiter);
        format.setQuote(quote);
        format.setQuoteEscape(escape);
        format.setLineSeparator(linebreak);
        format.setNormalizedNewline(CSVSyntax.getNormalizedLinebreak(linebreak));
        format.setComment('\0');

        CsvParserSettings settings = new CsvParserSettings();
        settings.setEmptyValue("");
        settings.setNullValue("");
        settings.setFormat(format);
        if (options != null) {
            options.apply(settings);
        }
        return settings;
    }
}
