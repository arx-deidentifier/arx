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

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.resources.Resources;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This worker applies a transformation.
 *
 * @author Fabian Prasser
 */
public class WorkerTransform extends Worker<DataHandle> {

	/** The model. */
    private final Model model;

    /**
     * Creates a new instance.
     *
     * @param model
     */
    public WorkerTransform(final Model model) {
        this.model = model;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void
            run(final IProgressMonitor arg0) throws InvocationTargetException,
                                            InterruptedException {

        arg0.beginTask(Resources.getMessage("WorkerTransform.0"), 1); //$NON-NLS-1$

        try {
            arg0.worked(1);
            result = model.getResult().getOutput(model.getSelectedNode(), false);
        } catch (final Exception e) {
            error = e;
        }
        arg0.worked(100);
        arg0.done();
    }
}
