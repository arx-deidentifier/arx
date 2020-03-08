/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
 * Copyright 2014 Karol Babioch <karol@babioch.de>
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

import java.lang.reflect.InvocationTargetException;

import org.deidentifier.arx.Data;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.io.ImportAdapter;
import org.deidentifier.arx.io.ImportConfiguration;
import org.deidentifier.arx.io.ImportConfigurationJDBC;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This worker loads external data.
 *
 * @author Fabian Prasser
 */
public class WorkerImport extends Worker<Data> {

    /** The path. */
    private final ImportConfiguration config;

    /** The stop flag. */
    private volatile boolean          stop = false;

    /**
     * Creates a new instance.
     *
     * @param config
     */
    public WorkerImport(final ImportConfiguration config) {
        this.config = config;
    }

    @Override
    public void run(final IProgressMonitor arg0) throws InvocationTargetException,
                                                        InterruptedException {

        arg0.beginTask(Resources.getMessage("WorkerImport.0"), 100); //$NON-NLS-1$

        final ImportAdapter adapter;
        try {
            config.setOptimizedLoading(true);
            adapter = ImportAdapter.create(config);
        } catch (final Exception e) {
            close(config);
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
                    int work = Math.min(adapter.getProgress(), 99);
                    if (work != progress) {
                        arg0.worked(work - progress);
                        progress = work;
                    }
                    try { Thread.sleep(100); } 
                    catch (final InterruptedException e) {/* Ignore*/ }
                }
            }
        });
        t.setDaemon(true);
        t.start();

        // Load the data
        try {
            result = Data.create(adapter);
            result.getHandle(); // Prepare the handle
            stop = true;
            arg0.worked(1);
            close(config);
            arg0.done();
        } catch (final Exception e) {
            close(config);
            error = e;
            stop = true;
            arg0.done();
            return;
        }
    }

    /**
     * Closes any underlying JDBC connection
     * @param config
     */
    private void close(ImportConfiguration config) {
        if (config instanceof ImportConfigurationJDBC) {
            try {
                ((ImportConfigurationJDBC)config).close();
            } catch (Exception e) {
                // Die silently
            }
        }
    }
}
