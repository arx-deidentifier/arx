/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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
import org.deidentifier.arx.ARXConfiguration;
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
    private final Model model;

    /** Heuristic flag */
    private final int   timeLimit;

    /**
     * Creates a new instance.
     *
     * @param model
     * @param heuristicSearch 
     */
    public WorkerAnonymize(final Model model, int timeLimit) {
        this.model = model;
        this.timeLimit = timeLimit;
    }

    @Override
    public void run(final IProgressMonitor arg0) throws InvocationTargetException,
                                                        InterruptedException {

        // Initialize anonymizer
        final ARXAnonymizer anonymizer = model.createAnonymizer();

        // Update the progress bar
        anonymizer.setListener(new ARXListener() {
            int previous = 0;
            public void progress(final double progress) {
                if (arg0.isCanceled()) { 
                    throw new RuntimeException(Resources.getMessage("WorkerAnonymize.1")); //$NON-NLS-1$ 
                } 
                int current = (int)(Math.round(progress * 100d));
                if (current != previous) {
                    arg0.worked(current - previous);
                    previous = current;
                }
            }
        });

        // Remember user-defined settings
        ARXConfiguration config = model.getInputConfig().getConfig();
        boolean heuristicSearchEnabled = config.isHeuristicSearchEnabled();
        int heuristicSearchTimeLimit = config.getHeuristicSearchTimeLimit();
        
        // Perform all tasks
        try {
            
            // Release
            model.getInputConfig().getInput().getHandle().release();
            
            // Temporarily overwrite user-defined settings
            if (timeLimit > 0) {
                config.setHeuristicSearchEnabled(true);
                config.setHeuristicSearchTimeLimit(timeLimit);
            }
            
            // Anonymize
            arg0.beginTask(Resources.getMessage("WorkerAnonymize.0"), 110); //$NON-NLS-1$
        	result = anonymizer.anonymize(model.getInputConfig().getInput(), config);

            // Apply optimal transformation, if any
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
        } finally {
            // Reset to user-defined settings
            config.setHeuristicSearchEnabled(heuristicSearchEnabled);
            config.setHeuristicSearchTimeLimit(heuristicSearchTimeLimit);
        }
    }
}
