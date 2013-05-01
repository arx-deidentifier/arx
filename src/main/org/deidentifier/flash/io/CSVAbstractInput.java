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

import java.io.IOException;
import java.util.Iterator;

/**
 * Abstract class for reading CSV encoded information
 * 
 * @author Prasser, Kohlmayer
 */
public abstract class CSVAbstractInput {

    /** The iterator */
    protected Iterator<String[]> iterator;

    /** The separator. */
    protected final char         separator;

    /**
     * Constructor
     * 
     * @param file
     * @param separator
     */
    public CSVAbstractInput(final char separator) {
        this.separator = separator;
    }

    /**
     * Builds the iterator
     * 
     * @return
     * @throws IOException
     */
    protected Iterator<String[]> buildIterator() throws IOException {
        return new Iterator<String[]>() {
            String[] nextRow = readRow();

            @Override
            public boolean hasNext() {
                return nextRow != null;
            }

            @Override
            public String[] next() {
                final String[] currentRow = nextRow;
                try {
                    nextRow = readRow();
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
                return currentRow;
            }

            @Override
            public void remove() {
                // Empty by design
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Closes the input
     * 
     * @throws IOException
     */
    public abstract void close() throws IOException;

    /**
     * Returns an iterator
     * 
     * @return
     */
    public Iterator<String[]> iterator() throws IOException {
        if (iterator == null) {
            iterator = buildIterator();
        }
        return iterator;
    }

    /** Reads a row */
    protected abstract String[] readRow() throws IOException;
}
