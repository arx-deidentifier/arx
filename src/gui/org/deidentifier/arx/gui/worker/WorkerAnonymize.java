/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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

package org.deidentifier.arx.gui.worker;

import java.lang.reflect.InvocationTargetException;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXListener;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.resources.Resources;
import org.eclipse.core.runtime.IProgressMonitor;

public class WorkerAnonymize extends Worker<ARXResult> {

    private final Model      model;

    public WorkerAnonymize(final Model model) {
        this.model = model;
    }

    @Override
    public void
            run(final IProgressMonitor arg0) throws InvocationTargetException,
                                            InterruptedException {

        // Track progress
        arg0.beginTask(Resources.getMessage("WorkerAnonymize.0"), 110); //$NON-NLS-1$

        // Initialize anonymizer
        final ARXAnonymizer anonymizer = model.createAnonymizer();

        // Add listener
        anonymizer.setListener(new ARXListener() {
            int count = 0;

            @Override
            public void nodeTagged(final int numNodes) {
                final int val = (int) (((double) (++count) / (double) numNodes) * 100d);
                arg0.worked(10 + Math.min(val, 99));
                if (arg0.isCanceled()) { throw new RuntimeException(Resources.getMessage("WorkerAnonymize.1")); } //$NON-NLS-1$
            }
        });

        // Perform all tasks
        try {

            // Anonymize
        	result = anonymizer.anonymize(model.getInputConfig().getInput(), model.getInputConfig().getConfig());
            arg0.beginTask(Resources.getMessage("WorkerAnonymize.2"), 2); //$NON-NLS-1$

            // Determine minimum and maximum information loss
            result.getHandle(result.getLattice().getBottom());
            arg0.worked(1);
            result.getHandle(result.getLattice().getTop());
            arg0.beginTask(Resources.getMessage("WorkerAnonymize.3"), 1); //$NON-NLS-1$
            if (result.isResultAvailable()) {
                result.getHandle();
            }
            model.setAnonymizer(anonymizer);
            model.setTime(result.getTime());
            arg0.worked(2);
            arg0.done();
        } catch (final Exception e) {
            error = e;
            arg0.done();
            return;
        }
    }
}
