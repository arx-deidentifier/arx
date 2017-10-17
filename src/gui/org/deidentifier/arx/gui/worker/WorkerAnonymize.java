/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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

import org.apache.commons.math3.util.Pair;
import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXProcessStatistics;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.metric.MetricConfiguration;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This worker performs the anonymization process.
 *
 * @author Fabian Prasser
 */
public class WorkerAnonymize extends Worker<Pair<Pair<ARXResult, DataHandle>, ARXProcessStatistics>> {
    
    /** The model. */
    private final Model  model;

    /** Heuristic flag */
    private final int    maxTimePerIteration;

    /** Heuristic flag */
    private final double minRecordsPerIteration;

    /**
     * Creates a new instance.
     *
     * @param model
     * @param maxTimePerIteration 
     * @param minRecordsPerIteration
     */
    public WorkerAnonymize(final Model model, int maxTimePerIteration, double minRecordsPerIteration) {
        this.model = model;
        this.maxTimePerIteration = maxTimePerIteration;
        this.minRecordsPerIteration = minRecordsPerIteration;
    }

    @Override
    public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

        // Initialize anonymizer
        final ARXAnonymizer anonymizer = model.createAnonymizer();

        // Remember user-defined settings
        ARXConfiguration config = model.getInputConfig().getConfig();
        boolean heuristicSearchEnabled = config.isHeuristicSearchEnabled();
        int heuristicSearchTimeLimit = config.getHeuristicSearchTimeLimit();
        double suppressionLimit = config.getSuppressionLimit();
        
        // Perform all tasks
        try {
            
            // Release
            model.getInputConfig().getInput().getHandle().release();
            
            // Temporarily overwrite user-defined settings regarding the heuristic search
            if (maxTimePerIteration > 0) {
                config.setHeuristicSearchEnabled(true);
                config.setHeuristicSearchTimeLimit(maxTimePerIteration);
            }
            
            // Persistently overwrite user-defined settings to prepare local recoding
            if (minRecordsPerIteration != 0) {
                MetricConfiguration metricConfig = config.getQualityModel().getConfiguration();
                metricConfig.setGsFactor(0d);
                config.setQualityModel(config.getQualityModel().getDescription().createInstance(metricConfig));
                config.setSuppressionLimit(1d - minRecordsPerIteration);
            }
            
            // Prepare progress tracking
            monitor.beginTask(Resources.getMessage("WorkerAnonymize.0"), 100); //$NON-NLS-1$
            anonymizer.setListener(new ProgressListener(monitor));
            
            // Anonymize
        	ARXResult result = anonymizer.anonymize(model.getInputConfig().getInput(), config);

            // Apply optimal transformation, if any
            DataHandle output = null;
            if (result.isResultAvailable()) {
                output = result.getOutput(false);
            }
            ARXProcessStatistics statistics = result.getProcessStatistics();
            model.setAnonymizer(anonymizer);
            model.setTime(result.getTime());
            monitor.worked(10);
            
            // Local recoding
            if (output != null && minRecordsPerIteration != 0d) {
                monitor.beginTask(Resources.getMessage("WorkerAnonymize.4"), 100); //$NON-NLS-1$
                statistics = statistics.merge(result.optimizeIterativeFast(output, minRecordsPerIteration, new ProgressListener(monitor)));
            }
            
            // Store results
            this.result = new Pair<>(new Pair<>(result, output), statistics);
            
            // Now we are really done
            monitor.done();
            
        } catch (final Exception e) {
            
            // Handle errors
            error = e;
            monitor.done();
            return;
            
        } finally {
            
            // Reset to user-defined settings
            config.setHeuristicSearchEnabled(heuristicSearchEnabled);
            config.setHeuristicSearchTimeLimit(heuristicSearchTimeLimit);
            config.setSuppressionLimit(suppressionLimit);
        }
    }
}