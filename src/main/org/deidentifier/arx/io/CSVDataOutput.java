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
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
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

    /** A writer. */
    private final Writer            writer;

    /** Settings. */
    private final CsvWriterSettings settings;

    /** Should the writer be closed. */
    private boolean           close;

    /**
     * Instantiate.
     *
     * @param file the file
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataOutput(final File file) throws IOException {
        this(file, CSVSyntax.DEFAULT_DELIMITER);
    }

    /**
     * Instantiate.
     *
     * @param file the file
     * @param delimiter the delimiter
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataOutput(final File file, final char delimiter) throws IOException {
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
    public CSVDataOutput(final File file, final char delimiter, final char quote) throws IOException {
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
    public CSVDataOutput(final File file, final char delimiter, final char quote, final char escape) throws IOException {
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
    public CSVDataOutput(final File file, final char delimiter, final char quote, final char escape, final char[] linebreak) throws IOException {
        this(new FileWriter(file), delimiter, quote, escape, linebreak);
    }

    /**
     * Instantiate.
     *
     * @param file the file
     * @param config the config
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataOutput(final File file, final CSVSyntax config) throws IOException {
        this(file, config.getDelimiter(), config.getQuote(), config.getEscape(), config.getLinebreak());
    }

    /**
     * Instantiate.
     *
     * @param stream the stream
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataOutput(final OutputStream stream) throws IOException {
        this(stream, CSVSyntax.DEFAULT_DELIMITER);
    }

    /**
     * Instantiate.
     *
     * @param stream the stream
     * @param delimiter the delimiter
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataOutput(final OutputStream stream, final char delimiter) throws IOException {
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
    public CSVDataOutput(final OutputStream stream, final char delimiter, final char quote) throws IOException {
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
    public CSVDataOutput(final OutputStream stream, final char delimiter, final char quote, final char escape) throws IOException {
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
    public CSVDataOutput(final OutputStream stream, final char delimiter, final char quote, final char escape, final char[] linebreak) throws IOException {
        this(new OutputStreamWriter(stream), delimiter, quote, escape, linebreak);
        close = false;
    }

    /**
     * Instantiate.
     *
     * @param stream the stream
     * @param delimiter the delimiter
     * @param quote the quote
     * @param escape the escape
     * @param linebreak the linebreak
     * @param charset to use
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataOutput(final OutputStream stream, final char delimiter, final char quote, final char escape, final char[] linebreak, Charset charset) throws IOException {
        this(new OutputStreamWriter(stream, charset), delimiter, quote, escape, linebreak);
        close = false;
    }
    
    /**
     * Instantiate.
     *
     * @param stream the stream
     * @param config the config
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataOutput(OutputStream stream, final CSVSyntax config) throws IOException {
        this(stream, config.getDelimiter(), config.getQuote(), config.getEscape(), config.getLinebreak());
    }

    /**
     * Instantiate.
     *
     * @param filename the filename
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataOutput(final String filename) throws IOException {
        this(filename, CSVSyntax.DEFAULT_DELIMITER);
    }

    /**
     * Instantiate.
     *
     * @param filename the filename
     * @param delimiter the delimiter
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataOutput(final String filename, final char delimiter) throws IOException {
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
    public CSVDataOutput(final String filename, final char delimiter, final char quote) throws IOException {
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
    public CSVDataOutput(final String filename, final char delimiter, final char quote, final char escape) throws IOException {
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
    public CSVDataOutput(final String filename, final char delimiter, final char quote, final char escape, final char[] linebreak) throws IOException {
        this(new File(filename), delimiter, quote, escape, linebreak);
    }

    /**
     * Instantiate.
     *
     * @param filename the filename
     * @param config the config
     * @throws IOException Signals that an I/O exception has occurred.
     */

    public CSVDataOutput(String filename, final CSVSyntax config) throws IOException {
        this(filename, config.getDelimiter(), config.getQuote(), config.getEscape(), config.getLinebreak());
    }

    /**
     * Instantiate.
     *
     * @param filename the filename
     * @param config the config
     * @param options the options
     * @throws IOException Signals that an I/O exception has occurred.
     */

    public CSVDataOutput(String filename, final CSVSyntax config, final CSVOptions options) throws IOException {
        this(new FileWriter(new File(filename)), config.getDelimiter(), config.getQuote(), config.getEscape(), config.getLinebreak(), options);
    }
    /**
     * Instantiates a new CSV data output.
     *
     * @param writer the writer
     * @param delimiter the delimiter
     * @param quote the quote
     * @param escape the escape
     * @param linebreak the linebreak
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataOutput(final Writer writer, final char delimiter, final char quote, final char escape, final char[] linebreak) throws IOException {
        this(writer, delimiter, quote, escape, linebreak, null);
    }
    
    /**
     * Instantiates a new CSV data output.
     *
     * @param writer the writer
     * @param delimiter the delimiter
     * @param quote the quote
     * @param escape the escape
     * @param linebreak the linebreak
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataOutput(final Writer writer, final char delimiter, final char quote, final char escape, final char[] linebreak, CSVOptions options) throws IOException {
        this.writer = writer;
        close = true;
        settings = createSettings(delimiter, quote, escape, linebreak, options);
    }

    /**
     * Write the results.
     *
     * @param iterator the iterator
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void write(final Iterator<String[]> iterator) throws IOException {

        CsvWriter csvwriter = new CsvWriter(writer, settings);
        while (iterator.hasNext()) {
            csvwriter.writeRow((Object[]) iterator.next());
        }
        if (close) {
            csvwriter.close();
        } else {
            csvwriter.flush();
        }
    }

    /**
     * Write.
     *
     * @param hierarchy the hierarchy
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void write(final String[][] hierarchy) throws IOException {

        CsvWriter csvwriter = new CsvWriter(writer, settings);
        for (int i = 0; i < hierarchy.length; i++) {
            csvwriter.writeRow((Object[]) hierarchy[i]);
        }
        if (close) {
            csvwriter.close();
        } else {
            csvwriter.flush();
        }
    }

    /**
     * Creates the settings.
     *
     * @param delimiter the delimiter
     * @param quote the quote
     * @param escape the escape
     * @param linebreak the linebreak
     * @param options the options
     * @return the csv writer settings
     */
    private CsvWriterSettings createSettings(final char delimiter, final char quote, final char escape, final char[] linebreak, final CSVOptions options) {
        CsvFormat format = new CsvFormat();
        format.setDelimiter(delimiter);
        format.setQuote(quote);
        format.setQuoteEscape(escape);
        format.setLineSeparator(linebreak);
        format.setNormalizedNewline(CSVSyntax.getNormalizedLinebreak(linebreak));

        CsvWriterSettings settings = new CsvWriterSettings();
        settings.setEmptyValue("");
        settings.setNullValue("");
        settings.setFormat(format);
        if (options != null) {
            options.apply(settings);
        }
        return settings;
    }
}
