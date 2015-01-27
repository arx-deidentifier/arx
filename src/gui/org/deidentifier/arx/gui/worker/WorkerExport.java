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

package org.deidentifier.arx.gui.worker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.io.output.CountingOutputStream;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.io.CSVDataOutput;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This worker exports data to disk.
 *
 * @author Fabian Prasser
 */
public class WorkerExport extends Worker<DataHandle> {

	/** The stop flag. */
	private volatile boolean stop = false;
	
	/** The path. */
	private final String path;
	
	/** The separator. */
	private final char separator;
	
	/** The byte count. */
	private final long bytes;
	
	/** The data. */
	private final DataHandle handle;

	/**
     * Creates a new instance.
     *
     * @param path
     * @param separator
     * @param handle
     * @param config
     * @param bytes
     */
    public WorkerExport(final String path,
                        final char separator,
                        final DataHandle handle,
                        final ARXConfiguration config,
                        final long bytes) {
    	
        this.path = path;
        this.bytes = bytes;
        this.separator = separator;
        this.handle = handle;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void run(final IProgressMonitor arg0) throws InvocationTargetException,
                                            			InterruptedException {

        arg0.beginTask(Resources.getMessage("WorkerExport.0"), 100); //$NON-NLS-1$

        // Create output stream
        final File file = new File(path);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
        } catch (final FileNotFoundException e) {
            error = e;
            arg0.done();
            return;
        }

        // Track progress
        final CountingOutputStream cout = new CountingOutputStream(out);
        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                int previous = 0;
                while ((cout.getByteCount() != bytes) && !stop) {
                    int progress = (int) ((double) cout.getByteCount() / (double) bytes * 100d);
                    if (progress != previous) {
                        arg0.worked(progress - previous);
                        previous = progress;
                    }
                    try { Thread.sleep(100); } 
                    catch (final InterruptedException e) {/* Ignore*/}
                }
            }
        });
        t.setDaemon(true);
        t.start();

        // Export the data
        try {
            final CSVDataOutput csvout = new CSVDataOutput(cout, separator);
            csvout.write(handle.getView().iterator());
            cout.close();
            result = handle;
            stop = true;
            arg0.done();
        } catch (final Exception e) {
            error = e;
            stop = true;
            arg0.done();
            return;
        }
    }
}
