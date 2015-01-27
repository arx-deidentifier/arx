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
 * @author Fabian Prasser 
 * @author Florian Kohlmayer
 */
public class CSVDataOutput {

    /** The output file. */
    private final OutputStream out;

    /** The separator. */
    private final char         separator;

    /** Size of the buffer. */
    private static final int   BUFFER_SIZE = 1024 * 1024;

    /** Are we writing to a stream?. */
    private boolean            stream      = false;

    /**
     * New instance.
     *
     * @param file
     * @param separator
     * @throws FileNotFoundException
     */
    public CSVDataOutput(final File file, final char separator) throws FileNotFoundException {
        this.out = new FileOutputStream(file);
        this.separator = separator;
    }

    /**
     * New instance.
     *
     * @param out
     * @param separator
     */
    public CSVDataOutput(final OutputStream out, final char separator) {
        this.out = out;
        this.separator = separator;
        this.stream = true;
    }

    /**
     * New instance.
     *
     * @param output
     * @param separator
     * @throws FileNotFoundException
     */
    public CSVDataOutput(final String output, final char separator) throws FileNotFoundException {
    	this.out = new FileOutputStream(new File(output));
        this.separator = separator;
    }

    /**
     * Write the results.
     *
     * @param iterator
     * @throws IOException
     */
    public void write(final Iterator<String[]> iterator) throws IOException {
        write(iterator, Integer.MAX_VALUE);
    }

    /**
     * Write the given number of columns from the results.
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

    /**
     * 
     *
     * @param hierarchy
     * @throws IOException
     */
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
