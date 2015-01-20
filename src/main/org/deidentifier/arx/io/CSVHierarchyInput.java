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
     * Create from file.
     *
     * @param file
     * @param separator
     * @throws IOException
     */
    public CSVHierarchyInput(final File file, final char separator) throws IOException {
        load(new CSVDataInput(file, separator));
    }

    /**
     * Create from stream.
     *
     * @param stream
     * @param separator
     * @throws IOException
     */
    public CSVHierarchyInput(final InputStream stream, final char separator) throws IOException {
        load(new CSVDataInput(stream, separator));
    }

    /**
     * Create from path.
     *
     * @param file
     * @param separator
     * @throws IOException
     */
    public CSVHierarchyInput(final String file, final char separator) throws IOException {
        load(new CSVDataInput(file, separator));
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
    private void load(final CSVAbstractInput input) throws IOException {

        final Iterator<String[]> iter = input.iterator();
        final List<String[]> elems = new ArrayList<String[]>();
        while (iter.hasNext()) {
            final String[] line = iter.next();
            elems.add(line);
        }

        data = elems.toArray(new String[elems.size()][]);
    }
}
