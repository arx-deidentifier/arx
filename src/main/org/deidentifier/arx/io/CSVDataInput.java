/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
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
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;

import org.apache.poi.ss.formula.functions.T;
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

    /** A reader. */
    private final Reader            reader;

    /** Settings. */
    private final CsvParserSettings settings;

    /** Cleanisng enabled */
    private final boolean           cleansing;

    /** The data type for each column */
    private final DataType<T>[]     datatypes;

    /**
     * Instantiate.
     *
     * @param file the file
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final File file) throws IOException {
        this(file, CSVSyntax.DEFAULT_DELIMITER);
    }

    /**
     * Instantiate.
     *
     * @param file the file
     * @param delimiter the delimiter
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final File file, final char delimiter) throws IOException {
        this(file, delimiter, CSVSyntax.DEFAULT_QUOTE);
    }

    /**
     * Instantiate.
     *
     * @param file the file
     * @param delimiter the delimiter
     * @param quote the quote
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final File file, final char delimiter, final char quote) throws IOException {
        this(file, delimiter, quote, CSVSyntax.DEFAULT_ESCAPE);
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
    public CSVDataInput(final File file, final char delimiter, final char quote, final char escape) throws IOException {
        this(file, delimiter, quote, escape, CSVSyntax.DEFAULT_LINEBREAK);
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
    public CSVDataInput(final File file, final char delimiter, final char quote, final char escape, final char[] linebreak) throws IOException {
        this(new FileReader(file), delimiter, quote, escape, linebreak, null);
    }

    /**
     * Instantiate.
     *
     * @param file the file
     * @param config the config
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final File file, final CSVSyntax config) throws IOException {
        this(file, config, null);
    }

    /**
     * Instatiate.
     * 
     * @param file
     * @param config
     * @param datatype
     * @throws IOException
     */
    public CSVDataInput(final File file, final CSVSyntax config, final DataType<T>[] datatype) throws IOException {
        this(new FileReader(file), config.getDelimiter(), config.getQuote(), config.getEscape(), config.getLinebreak(), datatype);
    }

    /**
     * Instantiate.
     *
     * @param stream the stream
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final InputStream stream) throws IOException {
        this(stream, CSVSyntax.DEFAULT_DELIMITER);
    }

    /**
     * Instantiate.
     *
     * @param stream the stream
     * @param delimiter the delimiter
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final InputStream stream, final char delimiter) throws IOException {
        this(stream, delimiter, CSVSyntax.DEFAULT_QUOTE);
    }

    /**
     * Instantiate.
     *
     * @param stream the stream
     * @param delimiter the delimiter
     * @param quote the quote
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final InputStream stream, final char delimiter, final char quote) throws IOException {
        this(stream, delimiter, quote, CSVSyntax.DEFAULT_ESCAPE);
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
    public CSVDataInput(final InputStream stream, final char delimiter, final char quote, final char escape) throws IOException {
        this(stream, delimiter, quote, escape, CSVSyntax.DEFAULT_LINEBREAK);
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
    public CSVDataInput(final InputStream stream, final char delimiter, final char quote, final char escape, final char[] linebreak) throws IOException {
        this(new InputStreamReader(stream), delimiter, quote, escape, linebreak, null);
    }

    /**
     * Instantiate.
     *
     * @param stream the stream
     * @param config the config
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final InputStream stream, final CSVSyntax config) throws IOException {
        this(stream, config, null);
    }

    /**
     * Instantiate.
     * 
     * @param stream
     * @param config
     * @param datatypes
     * @throws IOException
     */
    public CSVDataInput(final InputStream stream, final CSVSyntax config, final DataType<T>[] datatypes) throws IOException {
        this(new InputStreamReader(stream), config.getDelimiter(), config.getQuote(), config.getEscape(), config.getLinebreak(), datatypes);
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
    public CSVDataInput(final Reader reader, final char delimiter, final char quote, final char escape, final char[] linebreak, final DataType<T>[] datatypes) throws IOException {
        this.reader = reader;
        this.datatypes = datatypes;
        if (datatypes != null) {
            cleansing = true;
        } else {
            cleansing = false;
        }
        settings = createSettings(delimiter, quote, escape, linebreak);
    }

    /**
     * Instantiate.
     *
     * @param filename the filename
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final String filename) throws IOException {
        this(filename, CSVSyntax.DEFAULT_DELIMITER);
    }

    /**
     * Instantiate.
     *
     * @param filename the filename
     * @param delimiter the delimiter
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final String filename, final char delimiter) throws IOException {
        this(filename, delimiter, CSVSyntax.DEFAULT_QUOTE);
    }

    /**
     * Instantiate.
     *
     * @param filename the filename
     * @param delimiter the delimiter
     * @param quote the quote
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final String filename, final char delimiter, final char quote) throws IOException {
        this(filename, delimiter, quote, CSVSyntax.DEFAULT_ESCAPE);
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
    public CSVDataInput(final String filename, final char delimiter, final char quote, final char escape) throws IOException {
        this(filename, delimiter, quote, escape, CSVSyntax.DEFAULT_LINEBREAK);
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
    public CSVDataInput(final String filename, final char delimiter, final char quote, final char escape, final char[] linebreak) throws IOException {
        this(new File(filename), delimiter, quote, escape, linebreak);
    }

    /**
     * Instantiate.
     *
     * @param filename the filename
     * @param config the config
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final String filename, final CSVSyntax config) throws IOException {
        this(filename, config, null);
    }

    /**
     * Instantiate.
     * 
     * @param filename
     * @param config
     * @param datatypes
     * @throws IOException
     */
    public CSVDataInput(final String filename, final CSVSyntax config, final DataType<T>[] datatypes) throws IOException {
        this(new FileReader(new File(filename)), config.getDelimiter(), config.getQuote(), config.getEscape(), config.getLinebreak(), datatypes);
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
     * Returns an iterator.
     *
     * @return the iterator
     */
    public Iterator<String[]> iterator() {

        final CsvParser parser = new CsvParser(settings);
        parser.beginParsing(reader);

        return new Iterator<String[]>() {

            // Next tuple
            String[] next = parser.parseNext();

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public String[] next() {
                String[] result = next;
                next = parser.parseNext();

                // Replace each non matching value with the special NULL string
                if (cleansing) {
                    if (result.length != datatypes.length) {
                        throw new IllegalArgumentException("More columns available in CSV file than data types specified!");
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
        };
    }

    /**
     * Creates the CsvParserSettings.
     *
     * @param delimiter the delimiter
     * @param quote the quote
     * @param escape the escape
     * @param linebreak the linebreak
     * @return the csv parser settings
     */
    private CsvParserSettings createSettings(final char delimiter, final char quote, final char escape, final char[] linebreak) {
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
        return settings;
    }
}
