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

import java.lang.reflect.InvocationTargetException;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXListener;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.resources.Resources;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This worker performs the anonymization process.
 *
 * @author Fabian Prasser
 */
public class WorkerAnonymize extends Worker<ARXResult> {

	/** The model. */
    private final Model      model;

    /**
     * Creates a new instance.
     *
     * @param model
     */
    public WorkerAnonymize(final Model model) {
        this.model = model;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void run(final IProgressMonitor arg0) throws InvocationTargetException,
                                                        InterruptedException {

        // Track progress
        arg0.beginTask(Resources.getMessage("WorkerAnonymize.0"), 110); //$NON-NLS-1$

        // Initialize anonymizer
        final ARXAnonymizer anonymizer = model.createAnonymizer();

        // Update the progress bar
        anonymizer.setListener(new ARXListener() {
            int count = 0;
            int previous = 0;
            public void nodeTagged(final int searchSpaceSize) {
                if (arg0.isCanceled()) { 
                    throw new RuntimeException(Resources.getMessage("WorkerAnonymize.1")); //$NON-NLS-1$ 
                } 
                if (count==0) {
                    arg0.worked(10);
                }
                
                count++;
                int progress = (int) ((double)count / (double) searchSpaceSize * 100d);
                if (progress != previous) {
                    previous = progress;
                    arg0.worked(1);
                }
            }
        });

        // Perform all tasks
        try {
            
            // Anonymize
            model.getInputConfig().getInput().getHandle().release();
        	result = anonymizer.anonymize(model.getInputConfig().getInput(), model.getInputConfig().getConfig());

            // Apply optimal transformation, if any
            arg0.beginTask(Resources.getMessage("WorkerAnonymize.3"), 10); //$NON-NLS-1$
            if (result.isResultAvailable()) {
                result.getOutput(false);
            }
            model.setAnonymizer(anonymizer);
            model.setTime(result.getTime());
            arg0.worked(10);
            arg0.done();
        } catch (final Exception e) {
            error = e;
            arg0.done();
            return;
        }
    }
}
