/*
 * FLASH: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.flash.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Reads a CSV encoded generalization hierarchy
 * 
 * @author Prasser, Kohlmayer
 */
public class CSVHierarchyInput {

    /** The data */
    private String[][] data;

    /**
     * Create from file
     * 
     * @param file
     * @param separator
     * @throws IOException
     */
    public CSVHierarchyInput(final File file, final char separator) throws IOException {
        load(new CSVDataInput(file, separator));
    }

    /**
     * Create from stream
     * 
     * @param stream
     * @param separator
     * @throws IOException
     */
    public CSVHierarchyInput(final InputStream stream, final char separator) throws IOException {
        load(new CSVDataInput(stream, separator));
    }

    /**
     * Create from path
     * 
     * @param file
     * @param separator
     * @throws IOException
     */
    public CSVHierarchyInput(final String file, final char separator) throws IOException {
        load(new CSVDataInput(file, separator));
    }

    /**
     * Returns the hierarchy
     * 
     * @return
     */
    public String[][] getHierarchy() {
        return data;
    }

    /**
     * Loads the data
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
