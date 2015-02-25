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
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;

import com.univocity.parsers.csv.CsvFormat;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

/**
 * Provides methods for writing CSV encoded data.
 * 
 * @author Fabian Prasser 
 * @author Florian Kohlmayer
 */
public class CSVDataOutput {

    /** Default values*/
    private static final char DEFAULT_DELIMITER = ';';
    /** Default values*/
    private static final char DEFAULT_QUOTE = '\"';
    /** Default values*/
    private static final char DEFAULT_ESCAPE = '\"';
    /** Default values*/
    private static final char[] DEFAULT_LINEBREAK = {'\n'};

    /** A writer. */
    private final Writer writer;
    
    /** Settings*/
    private final CsvWriterSettings settings;
    
    /** Should the writer be closed*/
    private final boolean close;

    /**
     * Instantiate.
     *
     * @param file
     * @throws IOException
     */
    public CSVDataOutput(final File file) throws IOException {
        this(file, DEFAULT_DELIMITER, DEFAULT_QUOTE, DEFAULT_ESCAPE, DEFAULT_LINEBREAK);
    }

    /**
     * Instantiate.
     *
     * @param file
     * @param delimiter
     * @throws IOException
     */
    public CSVDataOutput(final File file, final char delimiter) throws IOException {
        this(file, delimiter, DEFAULT_QUOTE, DEFAULT_ESCAPE, DEFAULT_LINEBREAK);
    }

    /**
     * Instantiate.
     *
     * @param file
     * @param delimiter
     * @param quote
     * @throws IOException
     */
    public CSVDataOutput(final File file, final char delimiter, final char quote) throws IOException {
        this(file, delimiter, quote, DEFAULT_ESCAPE, DEFAULT_LINEBREAK);
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
    public CSVDataOutput(final File file, final char delimiter, final char quote, final char escape) throws IOException {
        this(file, delimiter, quote, escape, DEFAULT_LINEBREAK);
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
    public CSVDataOutput(final File file, final char delimiter, final char quote, final char escape, final char[] linebreak) throws IOException {

        CsvFormat format = new CsvFormat();
        format.setDelimiter(delimiter);
        format.setQuote(quote);
        format.setQuoteEscape(escape);
        format.setLineSeparator(linebreak);
        
        if (linebreak[0] == '\n') {
            format.setNormalizedNewline('\n');
        } else  if (linebreak[0] == '\r' && linebreak.length < 2) {
            format.setNormalizedNewline('\r');
        } else {
            format.setNormalizedNewline('\n');
        }
        
        settings = new CsvWriterSettings();
        settings.setEmptyValue("");
        settings.setNullValue("");
        settings.setFormat(format);
        
        writer = new FileWriter(file);
        close = true;
    }
    
    /**
     * Instantiate.
     *
     * @param stream
     * @throws IOException
     */
    public CSVDataOutput(final OutputStream stream) throws IOException {
        this(stream, DEFAULT_DELIMITER, DEFAULT_QUOTE, DEFAULT_ESCAPE, DEFAULT_LINEBREAK);
    }

    /**
     * Instantiate.
     *
     * @param stream
     * @param delimiter
     * @throws IOException
     */
    public CSVDataOutput(final OutputStream stream, final char delimiter) throws IOException {
        this(stream, delimiter, DEFAULT_QUOTE, DEFAULT_ESCAPE, DEFAULT_LINEBREAK);
    }

    /**
     * Instantiate.
     *
     * @param stream
     * @param delimiter
     * @param quote
     * @throws IOException
     */
    public CSVDataOutput(final OutputStream stream, final char delimiter, final char quote) throws IOException {
        this(stream, delimiter, quote, DEFAULT_ESCAPE, DEFAULT_LINEBREAK);
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
    public CSVDataOutput(final OutputStream stream, final char delimiter, final char quote, final char escape) throws IOException {
        this(stream, delimiter, quote, escape, DEFAULT_LINEBREAK);
    }
    
    /**
     * Instantiate.
     * 
     * @param stream
     * @param delimiter
     * @param quote
     * @param escape
     * @param linebreak
     * @throws IOException
     */
    public CSVDataOutput(final OutputStream stream, final char delimiter, final char quote, final char escape, final char[] linebreak) throws IOException {

        CsvFormat format = new CsvFormat();
        format.setDelimiter(delimiter);
        format.setQuote(quote);
        format.setQuoteEscape(escape);
        format.setLineSeparator(linebreak);
        
        if (linebreak[0] == '\n') {
            format.setNormalizedNewline('\n');
        } else  if (linebreak[0] == '\r' && linebreak.length < 2) {
            format.setNormalizedNewline('\r');
        } else {
            format.setNormalizedNewline('\n');
        }
        
        settings = new CsvWriterSettings();
        settings.setEmptyValue("");
        settings.setNullValue("");
        settings.setFormat(format);
        
        writer = new OutputStreamWriter(stream);
        close = false;
    }


    /**
     * Instantiate.
     *
     * @param filename
     * @throws IOException
     */
    public CSVDataOutput(final String filename) throws IOException {
        this(new File(filename), DEFAULT_DELIMITER, DEFAULT_QUOTE, DEFAULT_ESCAPE, DEFAULT_LINEBREAK);
    }

    /**
     * Instantiate.
     *
     * @param filename
     * @param delimiter
     * @throws IOException
     */
    public CSVDataOutput(final String filename, final char delimiter) throws IOException {
        this(new File(filename), delimiter, DEFAULT_QUOTE, DEFAULT_ESCAPE, DEFAULT_LINEBREAK);
    }

    /**
     * Instantiate.
     *
     * @param filename
     * @param delimiter
     * @param quote
     * @throws IOException
     */
    public CSVDataOutput(final String filename, final char delimiter, final char quote) throws IOException {
        this(new File(filename), delimiter, quote, DEFAULT_ESCAPE, DEFAULT_LINEBREAK);
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
    public CSVDataOutput(final String filename, final char delimiter, final char quote, final char escape) throws IOException {
        this(new File(filename), delimiter, quote, escape);
    }

    /**
     * Write the results.
     * 
     * @param iterator
     * @throws IOException
     */
    public void write(final Iterator<String[]> iterator) throws IOException {

        CsvWriter csvwriter = new CsvWriter(writer, settings);
        while (iterator.hasNext()) {
            csvwriter.writeRow((Object[]) iterator.next());
        }
        if (close) csvwriter.close();
        else csvwriter.flush();
    }

    /**
     * 
     * 
     * @param hierarchy
     * @throws IOException
     */
    public void write(final String[][] hierarchy) throws IOException {

        CsvWriter csvwriter = new CsvWriter(writer, settings);
        for (int i=0; i<hierarchy.length; i++) {
            csvwriter.writeRow((Object[]) hierarchy[i]);
        }
        if (close) csvwriter.close();
        else csvwriter.flush();
    }
}
