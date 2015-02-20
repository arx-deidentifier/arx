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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Reads a CSV encoded generalization hierarchy.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class CSVHierarchyInput {

    /** The data. */
    private String[][] data;


    /**
     * Instantiate.
     *
     * @param file
     * @throws IOException
     */
    public CSVHierarchyInput(final File file) throws IOException {
        load(new CSVDataInput(file));
    }

    /**
     * Instantiate.
     *
     * @param file
     * @param delimiter
     * @throws IOException
     */
    public CSVHierarchyInput(final File file, final char delimiter) throws IOException {
        load(new CSVDataInput(file, delimiter));
    }

    /**
     * Instantiate.
     *
     * @param file
     * @param delimiter
     * @param quote
     * @throws IOException
     */
    public CSVHierarchyInput(final File file, final char delimiter, final char quote) throws IOException {
        load(new CSVDataInput(file, delimiter, quote));
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
    public CSVHierarchyInput(final File file, final char delimiter, final char quote, final char escape) throws IOException {
        load(new CSVDataInput(file, delimiter, quote, escape));
    }
    
    /**
     * Instantiate.
     *
     * @param stream
     * @throws IOException
     */
    public CSVHierarchyInput(final InputStream stream) throws IOException {
        load(new CSVDataInput(stream));
    }

    /**
     * Instantiate.
     *
     * @param stream
     * @param delimiter
     * @throws IOException
     */
    public CSVHierarchyInput(final InputStream stream, final char delimiter) throws IOException {
        load(new CSVDataInput(stream, delimiter));
    }

    /**
     * Instantiate.
     *
     * @param stream
     * @param delimiter
     * @param quote
     * @throws IOException
     */
    public CSVHierarchyInput(final InputStream stream, final char delimiter, final char quote) throws IOException {
        load(new CSVDataInput(stream, delimiter, quote));
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
    public CSVHierarchyInput(final InputStream stream, final char delimiter, final char quote, final char escape) throws IOException {
        load(new CSVDataInput(stream, delimiter, quote, escape));
    }


    /**
     * Instantiate.
     *
     * @param filename
     * @throws IOException
     */
    public CSVHierarchyInput(final String filename) throws IOException {
        load(new CSVDataInput(filename));
    }

    /**
     * Instantiate.
     *
     * @param filename
     * @param delimiter
     * @throws IOException
     */
    public CSVHierarchyInput(final String filename, final char delimiter) throws IOException {
        load(new CSVDataInput(filename, delimiter));
    }

    /**
     * Instantiate.
     *
     * @param filename
     * @param delimiter
     * @param quote
     * @throws IOException
     */
    public CSVHierarchyInput(final String filename, final char delimiter, final char quote) throws IOException {
        load(new CSVDataInput(filename, delimiter, quote));
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
    public CSVHierarchyInput(final String filename, final char delimiter, final char quote, final char escape) throws IOException {
        load(new CSVDataInput(filename, delimiter, quote, escape));
    }

    /**
     * Returns the hierarchy.
     *
     * @return
     */
    public String[][] getHierarchy() {
        return data;
    }

    /**
     * Loads the data.
     *
     * @param input
     * @throws IOException
     */
    private void load(final CSVDataInput input) throws IOException {

        final Iterator<String[]> iter = input.iterator();
        final List<String[]> elems = new ArrayList<String[]>();
        while (iter.hasNext()) {
            final String[] line = iter.next();
            elems.add(line);
        }

        data = elems.toArray(new String[elems.size()][]);
    }
}
