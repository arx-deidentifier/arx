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
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Reads a CSV encoded generalization hierarchy.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class CSVHierarchyInput { // NO_UCD

    /** The data. */
    private String[][] data;

    /**
     * Instantiate.
     *
     * @param file the file
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVHierarchyInput(final File file, final Charset charset) throws IOException {
        load(new CSVDataInput(file, charset));
    }

    /**
     * Instantiate.
     *
     * @param file the file
     * @param delimiter the delimiter
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVHierarchyInput(final File file, final Charset charset, final char delimiter) throws IOException {
        load(new CSVDataInput(file, charset, delimiter));
    }

    /**
     * Instantiate.
     *
     * @param file the file
     * @param delimiter the delimiter
     * @param quote the quote
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVHierarchyInput(final File file, final Charset charset, final char delimiter, final char quote) throws IOException {
        load(new CSVDataInput(file, charset, delimiter, quote));
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
    public CSVHierarchyInput(final File file, final Charset charset, final char delimiter, final char quote, final char escape) throws IOException {
        load(new CSVDataInput(file, charset, delimiter, quote, escape));
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
    public CSVHierarchyInput(final File file, final Charset charset, final char delimiter, final char quote, final char escape, final char[] linebreak) throws IOException {
        load(new CSVDataInput(file, charset, delimiter, quote, escape, linebreak));
    }

    /**
     * Instantiate.
     *
     * @param file the file
     * @param config the config
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVHierarchyInput(final File file, final Charset charset, final CSVSyntax config) throws IOException {
        load(new CSVDataInput(file, charset, config));
    }

    /**
     * Instantiate.
     *
     * @param stream the stream
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVHierarchyInput(final InputStream stream, final Charset charset) throws IOException {
        load(new CSVDataInput(stream, charset));
    }

    /**
     * Instantiate.
     *
     * @param stream the stream
     * @param delimiter the delimiter
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVHierarchyInput(final InputStream stream, final Charset charset, final char delimiter) throws IOException {
        load(new CSVDataInput(stream, charset, delimiter));
    }

    /**
     * Instantiate.
     *
     * @param stream the stream
     * @param delimiter the delimiter
     * @param quote the quote
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVHierarchyInput(final InputStream stream, final Charset charset, final char delimiter, final char quote) throws IOException {
        load(new CSVDataInput(stream, charset, delimiter, quote));
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
    public CSVHierarchyInput(final InputStream stream, final Charset charset, final char delimiter, final char quote, final char escape) throws IOException {
        load(new CSVDataInput(stream, charset, delimiter, quote, escape));
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
    public CSVHierarchyInput(final InputStream stream, final Charset charset, final char delimiter, final char quote, final char escape, final char[] linebreak) throws IOException {
        load(new CSVDataInput(stream, charset, delimiter, quote, escape, linebreak));
    }

    /**
     * Instantiate.
     *
     * @param stream the stream
     * @param config the config
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVHierarchyInput(final InputStream stream, final Charset charset, final CSVSyntax config) throws IOException {
        load(new CSVDataInput(stream, charset, config));
    }

    /**
     * Instantiate.
     *
     * @param filename the filename
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVHierarchyInput(final String filename, final Charset charset) throws IOException {
        load(new CSVDataInput(filename, charset));
    }

    /**
     * Instantiate.
     *
     * @param filename the filename
     * @param delimiter the delimiter
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVHierarchyInput(final String filename, final Charset charset, final char delimiter) throws IOException {
        load(new CSVDataInput(filename, charset, delimiter));
    }

    /**
     * Instantiate.
     *
     * @param filename the filename
     * @param delimiter the delimiter
     * @param quote the quote
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVHierarchyInput(final String filename, final Charset charset, final char delimiter, final char quote) throws IOException {
        load(new CSVDataInput(filename, charset, delimiter, quote));
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
    public CSVHierarchyInput(final String filename, final Charset charset, final char delimiter, final char quote, final char escape) throws IOException {
        load(new CSVDataInput(filename, charset, delimiter, quote, escape));
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
    public CSVHierarchyInput(final String filename, final Charset charset, final char delimiter, final char quote, final char escape, final char[] linebreak) throws IOException {
        load(new CSVDataInput(filename, charset, delimiter, quote, escape, linebreak));
    }

    /**
     * Instantiate.
     *
     * @param filename the filename
     * @param config the config
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVHierarchyInput(final String filename, final Charset charset, final CSVSyntax config) throws IOException {
        load(new CSVDataInput(filename, charset, config));
    }

    /**
     * Returns the hierarchy.
     *
     * @return the hierarchy
     */
    public String[][] getHierarchy() {
        return data;
    }

    /**
     * Loads the data.
     *
     * @param input the input
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void load(final CSVDataInput input) throws IOException {

        final Iterator<String[]> iter = input.iterator(false);
        final List<String[]> elems = new ArrayList<String[]>();
        while (iter.hasNext()) {
            final String[] line = iter.next();
            elems.add(line);
        }

        data = elems.toArray(new String[elems.size()][]);
    }
}
