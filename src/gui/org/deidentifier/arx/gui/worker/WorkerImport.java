/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
 * Copyright (C) 2014 Karol Babioch <karol@babioch.de>
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

package org.deidentifier.arx.gui.worker;

import java.lang.reflect.InvocationTargetException;

import org.deidentifier.arx.Data;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.io.datasource.Configuration;
import org.deidentifier.arx.io.importdata.ImportAdapter;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This worker loads external data
 * @author Fabian Prasser
 */
public class WorkerImport extends Worker<Data> {

    /** The path */
    private final Configuration config;
    /** The stop flag */
    private volatile boolean              stop = false;

    /**
     * Creates a new instance
     * @param path
     * @param separator
     */
    public WorkerImport(final Configuration config) {
        this.config = config;
    }

    @Override
    public void run(final IProgressMonitor arg0) throws InvocationTargetException,
                                                        InterruptedException {

        arg0.beginTask(Resources.getMessage("WorkerImport.0"), 100); //$NON-NLS-1$

        final ImportAdapter adapter;
        try {
            adapter = ImportAdapter.create(config);
        } catch (final Exception e) {
            error = e;
            arg0.done();
            return;
        }

        // Track progress
        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                int progress = 0;
                while (progress < 100 && !stop) {
                    progress = adapter.getProgress();
                    arg0.worked(Math.min(progress, 99));
                    try { Thread.sleep(10); } catch (final InterruptedException e) {/* Ignore*/ }
                }
            }
        });
        t.setDaemon(true);
        t.start();

        // Load the data
        try {
            result = Data.create(adapter);
            arg0.beginTask(Resources.getMessage("WorkerImport.1"), 1); //$NON-NLS-1$
            result.getHandle(); // Prepare the handle
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
