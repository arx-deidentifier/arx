/*
 * ARX: Powerful Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.arx.gui.worker.io;

import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Wraps a writer
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class FileBuilder {
	
	/** Stream*/
    private final OutputStreamWriter w;

    /**
     * Create a new instance
     * @param w
     */
    public FileBuilder(final OutputStreamWriter w) {
        this.w = w;
    }

    /**
     * Append a string
     * @param s
     * @return
     * @throws IOException
     */
    public FileBuilder append(final String s) throws IOException {
        w.write(s);
        return this;
    }

    /**
     * Flush
     * @throws IOException
     */
    public void flush() throws IOException {
        w.flush();
    }
}
