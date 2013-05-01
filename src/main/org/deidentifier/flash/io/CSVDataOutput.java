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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;

/**
 * Provides methods for writing CSV encoded data.
 * 
 * @author Prasser, Kohlmayer
 */
public class CSVDataOutput {

    /** The output file. */
    private final OutputStream out;

    /** The separator. */
    private final char         separator;

    /** Size of the buffer */
    private static final int   BUFFER_SIZE = 1024 * 1024;

    /** Are we writing to a stream? */
    private boolean            stream      = false;

    /**
     * New instance
     * 
     * @param output
     * @param separator
     * @throws FileNotFoundException
     */
    public CSVDataOutput(final File file, final char separator) throws FileNotFoundException {
        out = new FileOutputStream(file);
        this.separator = separator;
    }

    /**
     * New instance
     * 
     * @param out
     * @param separator
     */
    public CSVDataOutput(final OutputStream out, final char separator) {
        this.out = out;
        this.separator = separator;
        stream = true;
    }

    /**
     * New instance
     * 
     * @param output
     * @param separator
     * @throws FileNotFoundException
     */
    public CSVDataOutput(final String output, final char separator) throws FileNotFoundException {
        out = new FileOutputStream(new File(output));
        this.separator = separator;
    }

    /**
     * Write the results
     * 
     * @param iterator
     * @throws IOException
     */
    public void write(final Iterator<String[]> iterator) throws IOException {
        write(iterator, Integer.MAX_VALUE);
    }

    /**
     * Write the given number of columns from the results
     * 
     * @param iterator
     * @param numColumns
     * @throws IOException
     */
    public void
            write(final Iterator<String[]> iterator, final int numColumns) throws IOException {
        BufferedWriter os = null;
        try {
            os = new BufferedWriter(new OutputStreamWriter(out), BUFFER_SIZE);
            while (iterator.hasNext()) {
                final String[] tuple = iterator.next();
                for (int i = 0; (i < tuple.length) && (i < numColumns); i++) {
                    os.write(tuple[i]);
                    if (i != (tuple.length - 1)) {
                        os.write(separator);
                    } else {
                        os.newLine();
                    }
                }
            }
            os.flush();
        } finally {
            if (!stream && (os != null)) {
                os.close();
            }
        }
    }

    public void write(final String[][] hierarchy) throws IOException {
        BufferedWriter os = null;
        try {
            os = new BufferedWriter(new OutputStreamWriter(out), BUFFER_SIZE);
            for (int h = 0; h < hierarchy.length; h++) {
                final String[] tuple = hierarchy[h];
                for (int i = 0; i < tuple.length; i++) {
                    os.write(tuple[i]);
                    if (i != (tuple.length - 1)) {
                        os.write(separator);
                    } else {
                        os.newLine();
                    }
                }
            }
            os.flush();
        } finally {
            if (!stream && (os != null)) {
                os.close();
            }
        }

    }
}
