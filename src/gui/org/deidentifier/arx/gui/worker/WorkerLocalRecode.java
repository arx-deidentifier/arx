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

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.resources.Resources;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This worker that optimizes a transformation.
 *
 * @author Fabian Prasser
 */
public class WorkerLocalRecode extends Worker<DataHandle> {

	/** The model. */
    private final Model model;

    /**
     * Creates a new instance.
     *
     * @param model
     */
    public WorkerLocalRecode(final Model model) {
        this.model = model;
    }

    @Override
    public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        
        if (model == null || model.getResult() == null || model.getOutput() == null) {
            return;
        }
        
        try {
            monitor.beginTask(Resources.getMessage("WorkerLocalRecode.0"), 100); //$NON-NLS-1$
            
            switch (model.getLocalRecodingModel().getMode()) {
            case FIXPOINT:
                model.getResult().optimizeIterative(model.getOutput(), 
                                                    model.getLocalRecodingModel().getGsFactor(), 
                                                    Integer.MAX_VALUE,
                                                    0d,
                                                    new ProgressListener(monitor));
                break;
            case FIXPOINT_ADAPTIVE:
                model.getResult().optimizeIterative(model.getOutput(), 
                                                    model.getLocalRecodingModel().getGsFactor(), 
                                                    Integer.MAX_VALUE,
                                                    model.getLocalRecodingModel().getAdaptionFactor(),
                                                    new ProgressListener(monitor));
                break;
            case MULTI_PASS:
                model.getResult().optimizeIterative(model.getOutput(), 
                                                    model.getLocalRecodingModel().getGsFactor(), 
                                                    model.getLocalRecodingModel().getNumIterations(),
                                                    0d,
                                                    new ProgressListener(monitor));
                break;
            case ITERATIVE:
                model.getResult().optimizeIterativeFast(model.getOutput(),
                                                        1d / (double)model.getLocalRecodingModel().getNumIterations(),
                                                        model.getLocalRecodingModel().getGsFactor(), 
                                                        new ProgressListener(monitor));
                break;
            case SINGLE_PASS:
                model.getResult().optimize(model.getOutput(), model.getLocalRecodingModel().getGsFactor(), new ProgressListener(monitor));
                break;
            }
        } catch (final Exception e) {
            error = e;
        }
        monitor.done();
    }
}
