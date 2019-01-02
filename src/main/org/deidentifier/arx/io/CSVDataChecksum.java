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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;

import com.univocity.parsers.csv.CsvFormat;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

/**
 * Provides methods for creating checksums CSV encoded data.
 * 
 * @author Fabian Prasser
 */
public class CSVDataChecksum {

    /** Settings. */
    private final CsvWriterSettings settings;

    /**
     * Instantiate.
     *
     */
    public CSVDataChecksum() {
        this(CSVSyntax.DEFAULT_DELIMITER);
    }

    /**
     * Instantiate.
     *
     * @param delimiter the delimiter
     */
    public CSVDataChecksum(final char delimiter) {
        this(delimiter, CSVSyntax.DEFAULT_QUOTE);
    }

    /**
     * Instantiate.
     *
     * @param delimiter the delimiter
     * @param quote the quote
     */
    public CSVDataChecksum(final char delimiter, final char quote) {
        this(delimiter, quote, CSVSyntax.DEFAULT_ESCAPE);
    }

    /**
     * Instantiate.
     *
     * @param delimiter the delimiter
     * @param quote the quote
     * @param escape the escape
     */
    public CSVDataChecksum(final char delimiter, final char quote, final char escape)  {
        this(delimiter, quote, escape, CSVSyntax.DEFAULT_LINEBREAK);
    }

    /**
     * Instantiate.
     *
     * @param delimiter the delimiter
     * @param quote the quote
     * @param escape the escape
     * @param linebreak the linebreak
     */
    public CSVDataChecksum(final char delimiter, final char quote, final char escape, final char[] linebreak) {
        settings = createSettings(delimiter, quote, escape, linebreak);
    }

    /**
     * Instantiate.
     *
     * @param config the config
     */
    public CSVDataChecksum(final CSVSyntax config) {
        this(config.getDelimiter(), config.getQuote(), config.getEscape(), config.getLinebreak());
    }
    
    /**
     * Returns a hex-encoded MD5 checksum for the given data
     * 
     * @param iterator
     * @return
     * @throws NoSuchAlgorithmException 
     */
    public String getSHA256Checksum(final Iterator<String[]> iterator) throws NoSuchAlgorithmException {

        // Initialize message digest
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        DigestOutputStream dis = new DigestOutputStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                // Simply drop the data
            }
        }, md);
        CsvWriter csvwriter = new CsvWriter(new OutputStreamWriter(dis), settings);

        // Write
        while (iterator.hasNext()) {
            csvwriter.writeRow((Object[]) iterator.next());
        }
        csvwriter.close();

        // Obtain digest
        byte[] digest = md.digest();

        // And convert to hex
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < digest.length; i++) {
            builder.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
        }
        return builder.toString();
    }
    
    /**
     * Creates the settings.
     *
     * @param delimiter the delimiter
     * @param quote the quote
     * @param escape the escape
     * @param linebreak the linebreak
     * @return the csv writer settings
     */
    private CsvWriterSettings createSettings(final char delimiter, final char quote, final char escape, final char[] linebreak) {
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
        return settings;
    }
}
