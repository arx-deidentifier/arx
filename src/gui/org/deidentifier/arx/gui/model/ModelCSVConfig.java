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
package org.deidentifier.arx.gui.model;

import java.io.Serializable;

import org.deidentifier.arx.io.CSVConfig;

/**
 * The model for the CSV input and output.
 */
public class ModelCSVConfig implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long   serialVersionUID  = -8005129554010006592L;

    /** The delimiter. */
    private char                delimiter;

    /** The quote. */
    private char                quote;

    /** The escape. */
    private char                escape;

    /** The linebreak. */
    private char[]              linebreak;

    /** Default values. */
    private static final char   DEFAULT_DELIMITER = ';';

    /** Default values. */
    private static final char   DEFAULT_QUOTE     = '\"';

    /** Default values. */
    private static final char   DEFAULT_ESCAPE    = '\"';

    /** Default values. */
    private static final char[] DEFAULT_LINEBREAK = { '\n' };

    /**
     * Creates new config with default values.
     */
    public ModelCSVConfig() {
        delimiter = DEFAULT_DELIMITER;
        quote = DEFAULT_QUOTE;
        escape = DEFAULT_ESCAPE;
        linebreak = DEFAULT_LINEBREAK;
    }

    /**
     * Converts the GUI CSVConfig to the API CSVConfig.
     *
     * @return the CSV config
     */
    public CSVConfig convert() {
        CSVConfig config = new CSVConfig(getDelimiter(), getQuote(), getEscape(), getLinebreak());
        return config;
    }

    /**
     * Gets the delimiter.
     *
     * @return the delimiter
     */
    public char getDelimiter() {
        if (delimiter == '\0') {
            setDelimiter(DEFAULT_DELIMITER);
        }
        return delimiter;
    }

    /**
     * Gets the escape.
     *
     * @return the escape
     */
    public char getEscape() {
        if (escape == '\0') {
            setEscape(DEFAULT_ESCAPE);
        }
        return escape;
    }

    /**
     * Gets the linebreak.
     *
     * @return the linebreak
     */
    public char[] getLinebreak() {
        if (linebreak == null) {
            setLinebreak(DEFAULT_LINEBREAK);
        }
        return linebreak;
    }

    /**
     * Gets the quote.
     *
     * @return the quote
     */
    public char getQuote() {
        if (quote == '\0') {
            setQuote(DEFAULT_QUOTE);
        }
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
     * Sets the linebreak.
     *
     * @param linebreak the new linebreak
     */
    public void setLinebreak(char[] linebreak) {
        this.linebreak = linebreak;
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
