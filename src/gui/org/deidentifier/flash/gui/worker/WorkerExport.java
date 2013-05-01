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

package org.deidentifier.flash.gui.worker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.io.output.CountingOutputStream;
import org.deidentifier.flash.DataHandle;
import org.deidentifier.flash.gui.resources.Resources;
import org.deidentifier.flash.io.CSVDataOutput;
import org.eclipse.core.runtime.IProgressMonitor;

public class WorkerExport extends Worker<DataHandle> {

    private final String     path;
    private final char       separator;
    private volatile boolean stop = false;
    private final long       bytes;
    private final DataHandle handle;

    public WorkerExport(final String path,
                        final char separator,
                        final DataHandle handle,
                        final long bytes) {
        this.path = path;
        this.bytes = bytes;
        this.separator = separator;
        this.handle = handle;
    }

    @Override
    public void
            run(final IProgressMonitor arg0) throws InvocationTargetException,
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
                while ((cout.getByteCount() != bytes) && !stop) {
                    final int value = (int) (((double) cout.getByteCount() / (double) bytes) * 100d);
                    arg0.worked(Math.min(value, 99));
                    try {
                        Thread.sleep(10);
                    } catch (final InterruptedException e) {
                    }
                }
            }
        });
        t.setDaemon(true);
        t.start();

        // Load the data
        try {
            final CSVDataOutput csvout = new CSVDataOutput(cout, separator);
            csvout.write(handle.iterator());
            result = handle;
            stop = true;
            arg0.worked(100);
            arg0.done();
        } catch (final Exception e) {
            error = e;
            stop = true;
            arg0.done();
            return;
        }
    }
}
