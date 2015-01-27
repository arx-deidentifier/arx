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

package org.deidentifier.arx.gui.worker.io;

import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Wraps a writer.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class FileBuilder {
	
	/** Stream. */
    private final OutputStreamWriter w;

    /**
     * Create a new instance.
     *
     * @param w
     */
    public FileBuilder(final OutputStreamWriter w) {
        this.w = w;
    }

    /**
     * Append a string.
     *
     * @param s
     * @return
     * @throws IOException
     */
    public FileBuilder append(final String s) throws IOException {
        w.write(s);
        return this;
    }

    /**
     * Flush.
     *
     * @throws IOException
     */
    public void flush() throws IOException {
        w.flush();
    }
}
