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

    /**
     * Instantiate.
     *
     * @param file the file
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final File file) throws IOException {
        this(file, CSVUtil.DEFAULT_DELIMITER);
    }

    /**
     * Instantiate.
     *
     * @param file the file
     * @param delimiter the delimiter
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final File file, final char delimiter) throws IOException {
        this(file, delimiter, CSVUtil.DEFAULT_QUOTE);
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
        this(file, delimiter, quote, CSVUtil.DEFAULT_ESCAPE);
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
        this(file, delimiter, quote, escape, CSVUtil.DEFAULT_LINEBREAK);
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
        this(new FileReader(file), delimiter, quote, escape, linebreak);
    }

    /**
     * Instantiate.
     *
     * @param file the file
     * @param config the config
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final File file, final CSVConfig config) throws IOException {
        this(file, config.getDelimiter(), config.getQuote(), config.getEscape(), config.getLinebreak());
    }

    /**
     * Instantiate.
     *
     * @param stream the stream
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final InputStream stream) throws IOException {
        this(stream, CSVUtil.DEFAULT_DELIMITER);
    }

    /**
     * Instantiate.
     *
     * @param stream the stream
     * @param delimiter the delimiter
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final InputStream stream, final char delimiter) throws IOException {
        this(stream, delimiter, CSVUtil.DEFAULT_QUOTE);
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
        this(stream, delimiter, quote, CSVUtil.DEFAULT_ESCAPE);
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
        this(stream, delimiter, quote, escape, CSVUtil.DEFAULT_LINEBREAK);
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
        this(new InputStreamReader(stream), delimiter, quote, escape, linebreak);
    }

    /**
     * Instantiate.
     *
     * @param stream the stream
     * @param config the config
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final InputStream stream, final CSVConfig config) throws IOException {
        this(stream, config.getDelimiter(), config.getQuote(), config.getEscape(), config.getLinebreak());
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
    public CSVDataInput(final Reader reader, final char delimiter, final char quote, final char escape, final char[] linebreak) throws IOException {
        this.reader = reader;
        settings = createSettings(delimiter, quote, escape, linebreak);
    }

    /**
     * Instantiate.
     *
     * @param filename the filename
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final String filename) throws IOException {
        this(filename, CSVUtil.DEFAULT_DELIMITER);
    }

    /**
     * Instantiate.
     *
     * @param filename the filename
     * @param delimiter the delimiter
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataInput(final String filename, final char delimiter) throws IOException {
        this(filename, delimiter, CSVUtil.DEFAULT_QUOTE);
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
        this(filename, delimiter, quote, CSVUtil.DEFAULT_ESCAPE);
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
        this(filename, delimiter, quote, escape, CSVUtil.DEFAULT_LINEBREAK);
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
    public CSVDataInput(final String filename, final CSVConfig config) throws IOException {
        this(filename, config.getDelimiter(), config.getQuote(), config.getEscape(), config.getLinebreak());
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
        format.setNormalizedNewline(CSVUtil.getNormalizedLinebreakCharacter(linebreak));
        format.setComment('\0');

        CsvParserSettings settings = new CsvParserSettings();
        settings.setEmptyValue("");
        settings.setNullValue("");
        settings.setFormat(format);
        return settings;
    }
}
