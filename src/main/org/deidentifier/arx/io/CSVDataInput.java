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
    
    /** Default values*/
    private static final char DEFAULT_DELIMITER = ';';
    /** Default values*/
    private static final char DEFAULT_QUOTE = '\"';
    /** Default values*/
    private static final char DEFAULT_ESCAPE = '\"';

    /** A reader. */
    private final Reader reader;
    
    /** Settings*/
    private final CsvParserSettings settings;

    /**
     * Instantiate.
     *
     * @param file
     * @throws IOException
     */
    public CSVDataInput(final File file) throws IOException {
        this(file, DEFAULT_DELIMITER, DEFAULT_QUOTE, DEFAULT_ESCAPE);
    }

    /**
     * Instantiate.
     *
     * @param file
     * @param delimiter
     * @throws IOException
     */
    public CSVDataInput(final File file, final char delimiter) throws IOException {
        this(file, delimiter, DEFAULT_QUOTE, DEFAULT_ESCAPE);
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
        this(file, delimiter, quote, DEFAULT_ESCAPE);
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

        CsvFormat format = new CsvFormat();
        format.setDelimiter(delimiter);
        format.setQuote(quote);
        format.setQuoteEscape(escape);
        
        settings = new CsvParserSettings();
        settings.setEmptyValue("");
        settings.setNullValue("");
        settings.setFormat(format);
        
        reader = new FileReader(file);
    }
    
    /**
     * Instantiate.
     *
     * @param stream
     * @throws IOException
     */
    public CSVDataInput(final InputStream stream) throws IOException {
        this(stream, DEFAULT_DELIMITER, DEFAULT_QUOTE, DEFAULT_ESCAPE);
    }

    /**
     * Instantiate.
     *
     * @param stream
     * @param delimiter
     * @throws IOException
     */
    public CSVDataInput(final InputStream stream, final char delimiter) throws IOException {
        this(stream, delimiter, DEFAULT_QUOTE, DEFAULT_ESCAPE);
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
        this(stream, delimiter, quote, DEFAULT_ESCAPE);
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

        CsvFormat format = new CsvFormat();
        format.setDelimiter(delimiter);
        format.setQuote(quote);
        format.setQuoteEscape(escape);
        
        settings = new CsvParserSettings();
        settings.setEmptyValue("");
        settings.setNullValue("");
        settings.setFormat(format);
        
        reader = new InputStreamReader(stream);
    }


    /**
     * Instantiate.
     *
     * @param filename
     * @throws IOException
     */
    public CSVDataInput(final String filename) throws IOException {
        this(filename, DEFAULT_DELIMITER, DEFAULT_QUOTE, DEFAULT_ESCAPE);
    }

    /**
     * Instantiate.
     *
     * @param filename
     * @param delimiter
     * @throws IOException
     */
    public CSVDataInput(final String filename, final char delimiter) throws IOException {
        this(new File(filename), delimiter, DEFAULT_QUOTE, DEFAULT_ESCAPE);
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
        this(new File(filename), delimiter, quote, DEFAULT_ESCAPE);
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
        this(new File(filename), delimiter, quote, escape);
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
        
        return new Iterator<String[]>(){
            
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
}