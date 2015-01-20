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

import java.io.IOException;
import java.util.Iterator;

/**
 * Abstract class for reading CSV encoded information.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public abstract class CSVAbstractInput {

    /** The iterator. */
    protected Iterator<String[]> iterator;

    /** The separator. */
    protected final char         separator;

    /**
     * Constructor.
     *
     * @param separator
     */
    public CSVAbstractInput(final char separator) {
        this.separator = separator;
    }

    /**
     * Builds the iterator.
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
     * Closes the input.
     *
     * @throws IOException
     */
    public abstract void close() throws IOException;

    /**
     * Returns an iterator.
     *
     * @return
     * @throws IOException
     */
    public Iterator<String[]> iterator() throws IOException {
        if (iterator == null) {
            iterator = buildIterator();
        }
        return iterator;
    }

    /**
     * Reads a row.
     *
     * @return
     * @throws IOException
     */
    protected abstract String[] readRow() throws IOException;
}
