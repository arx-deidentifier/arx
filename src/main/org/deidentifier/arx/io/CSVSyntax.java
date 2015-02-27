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

import java.io.Serializable;

/**
 * Syntax for a CSV file.
 * 
 * @author Florian Kohlmayer
 * @author Fabian Prasser
 */
public class CSVSyntax implements Serializable{

    /** SVUID */
    private static final long serialVersionUID = -3978502790060734961L;

    /** The delimiter. */
    private char              delimiter;

    /** The quote. */
    private char              quote;

    /** The escape. */
    private char              escape;

    /** The linebreak. */
    private char[]            linebreak;

    /**
     * Instantiates a new syntax for a CSV file.
     */
    public CSVSyntax() {
        this(CSVUtil.DEFAULT_DELIMITER);
    }
    /**
     * Instantiates a new syntax for a CSV file.
     *
     * @param delimiter the delimiter
     */
    public CSVSyntax(final char delimiter) {
        this(delimiter, CSVUtil.DEFAULT_QUOTE);
    }

    /**
     * Instantiates a new syntax for a CSV file.
     *
     * @param delimiter the delimiter
     * @param quote the quote
     */
    public CSVSyntax(final char delimiter, final char quote) {
        this(delimiter, quote, CSVUtil.DEFAULT_ESCAPE);
    }

    /**
     * Instantiates a new syntax for a CSV file.
     *
     * @param delimiter the delimiter
     * @param quote the quote
     * @param escape the escape
     */
    public CSVSyntax(final char delimiter, final char quote, final char escape) {
        this(delimiter, quote, escape, CSVUtil.DEFAULT_LINEBREAK);
    }

    /**
     * Instantiates a new syntax for a CSV file.
     *
     * @param delimiter the delimiter
     * @param quote the quote
     * @param escape the escape
     * @param linebreak the line break
     */
    public CSVSyntax(final char delimiter, final char quote, final char escape, final char[] linebreak) {
        this.delimiter = delimiter;
        this.quote = quote;
        this.escape = escape;
        this.linebreak = linebreak;
    }

    /**
     * Instantiates a new syntax for a CSV file.
     *
     * @param delimiter the delimiter
     * @param quote the quote
     * @param escape the escape
     * @param linebreak the line break
     */
    public CSVSyntax(final char delimiter, final char quote, final char escape, final String linebreak) {
        this.delimiter = delimiter;
        this.quote = quote;
        this.escape = escape;
        if (linebreak.length() > 2) {
            throw new IllegalArgumentException("Line break too large");
        }
        this.linebreak = linebreak.toCharArray();
    }
    /**
     * Gets the delimiter.
     *
     * @return the delimiter
     */
    public char getDelimiter() {
        return delimiter;
    }

    /**
     * Gets the escape.
     *
     * @return the escape
     */
    public char getEscape() {
        return escape;
    }

    /**
     * Gets the line break.
     *
     * @return the line break
     */
    public char[] getLinebreak() {
        return linebreak;
    }

    /**
     * Gets the quote.
     *
     * @return the quote
     */
    public char getQuote() {
        return quote;
    }

    /**
     * Sets the delimiter.
     *
     * @param delimiter the new delimiter
     */
    public void setDelimiter(char delimiter) {
        this.delimiter = delimiter;
    }

    /**
     * Sets the escape.
     *
     * @param escape the new escape
     */
    public void setEscape(char escape) {
        this.escape = escape;
    }

    /**
     * Sets the line break.
     *
     * @param linebreak the new line break
     */
    public void setLinebreak(char[] linebreak) {
        this.linebreak = linebreak;
    }

    /**
     * Sets the line break.
     *
     * @param linebreak the new line break
     */
    public void setLinebreak(String linebreak) {
        if (linebreak.length() > 2) {
            throw new IllegalArgumentException("Line break too large");
        }
        this.linebreak = linebreak.toCharArray();
    }

    /**
     * Sets the quote.
     *
     * @param quote the new quote
     */
    public void setQuote(char quote) {
        this.quote = quote;
    }
}
