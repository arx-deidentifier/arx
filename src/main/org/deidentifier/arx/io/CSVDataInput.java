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

    /** Settings */
    private final CsvParserSettings settings;

    /**
     * Instantiate.
     *
     * @param file
     * @throws IOException
     */
    public CSVDataInput(final File file) throws IOException {
        this(file, CSVUtil.DEFAULT_DELIMITER);
    }

    /**
     * Instantiate.
     *
     * @param file
     * @param delimiter
     * @throws IOException
     */
    public CSVDataInput(final File file, final char delimiter) throws IOException {
        this(file, delimiter, CSVUtil.DEFAULT_QUOTE);
    }

    /**
     * Instantiate.
     *
     * @param file
     * @param delimiter
     * @param quote
     * @throws IOException
     */
    public CSVDataInput(final File file, final char delimiter, final char quote) throws IOException {
        this(file, delimiter, quote, CSVUtil.DEFAULT_ESCAPE);
    }

    /**
     * Instantiate.
     *
     * @param file
     * @param delimiter
     * @param quote
     * @param escape
     * @throws IOException
     */
    public CSVDataInput(final File file, final char delimiter, final char quote, final char escape) throws IOException {
        this(file, delimiter, quote, escape, CSVUtil.DEFAULT_LINEBREAK);
    }

    /**
     * Instantiate.
     * 
     * @param file
     * @param delimiter
     * @param quote
     * @param escape
     * @param linebreak
     * @throws IOException
     */
    public CSVDataInput(final File file, final char delimiter, final char quote, final char escape, final char[] linebreak) throws IOException {
        reader = new FileReader(file);
        settings = createSettings(delimiter, quote, escape, linebreak);
    }

    /**
     * Instantiate.
     *
     * @param stream
     * @throws IOException
     */
    public CSVDataInput(final InputStream stream) throws IOException {
        this(stream, CSVUtil.DEFAULT_DELIMITER);
    }

    /**
     * Instantiate.
     *
     * @param stream
     * @param delimiter
     * @throws IOException
     */
    public CSVDataInput(final InputStream stream, final char delimiter) throws IOException {
        this(stream, delimiter, CSVUtil.DEFAULT_QUOTE);
    }

    /**
     * Instantiate.
     *
     * @param stream
     * @param delimiter
     * @param quote
     * @throws IOException
     */
    public CSVDataInput(final InputStream stream, final char delimiter, final char quote) throws IOException {
        this(stream, delimiter, quote, CSVUtil.DEFAULT_ESCAPE);
    }

    /**
     * Instantiate.
     *
     * @param stream
     * @param delimiter
     * @param quote
     * @param escape
     * @throws IOException
     */
    public CSVDataInput(final InputStream stream, final char delimiter, final char quote, final char escape) throws IOException {
        this(stream, delimiter, quote, escape, CSVUtil.DEFAULT_LINEBREAK);
    }

    /**
     * Instantiate.
     *
     * @param stream
     * @param delimiter
     * @param quote
     * @param escape
     * @throws IOException
     */
    public CSVDataInput(final InputStream stream, final char delimiter, final char quote, final char escape, final char[] linebreak) throws IOException {
        reader = new InputStreamReader(stream);
        settings = createSettings(delimiter, quote, escape, linebreak);
    }

    /**
     * Instantiate.
     *
     * @param filename
     * @throws IOException
     */
    public CSVDataInput(final String filename) throws IOException {
        this(filename, CSVUtil.DEFAULT_DELIMITER);
    }

    /**
     * Instantiate.
     *
     * @param filename
     * @param delimiter
     * @throws IOException
     */
    public CSVDataInput(final String filename, final char delimiter) throws IOException {
        this(filename, delimiter, CSVUtil.DEFAULT_QUOTE);
    }

    /**
     * Instantiate.
     *
     * @param filename
     * @param delimiter
     * @param quote
     * @throws IOException
     */
    public CSVDataInput(final String filename, final char delimiter, final char quote) throws IOException {
        this(filename, delimiter, quote, CSVUtil.DEFAULT_ESCAPE);
    }

    /**
     * Instantiate.
     *
     * @param filename
     * @param delimiter
     * @param quote
     * @param escape
     * @throws IOException
     */
    public CSVDataInput(final String filename, final char delimiter, final char quote, final char escape) throws IOException {
        this(filename, delimiter, quote, escape, CSVUtil.DEFAULT_LINEBREAK);
    }

    /**
     * Instantiate.
     * 
     * @param filename
     * @param delimiter
     * @param quote
     * @param escape
     * @param linebreak
     * @throws IOException
     */
    public CSVDataInput(final String filename, final char delimiter, final char quote, final char escape, final char[] linebreak) throws IOException {
        this(new File(filename), delimiter, quote, escape, linebreak);
    }

    /**
     * Closes the reader
     * @throws IOException
     */
    public void close() throws IOException {
        reader.close();
    }

    /**
     * Returns an iterator
     * @return
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

    private CsvParserSettings createSettings(final char delimiter, final char quote, final char escape, final char[] linebreak) {
        CsvFormat format = new CsvFormat();
        format.setDelimiter(delimiter);
        format.setQuote(quote);
        format.setQuoteEscape(escape);
        format.setLineSeparator(linebreak);

        if (linebreak[0] == '\n') {
            format.setNormalizedNewline('\n');
        } else if ((linebreak[0] == '\r') && (linebreak.length < 2)) {
            format.setNormalizedNewline('\r');
        } else {
            format.setNormalizedNewline('\n');
        }

        CsvParserSettings settings = new CsvParserSettings();
        settings.setEmptyValue("");
        settings.setNullValue("");
        settings.setFormat(format);
        return settings;
    }
}
