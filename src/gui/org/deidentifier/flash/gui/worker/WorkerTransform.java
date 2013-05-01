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

import java.lang.reflect.InvocationTargetException;

import org.deidentifier.flash.DataHandle;
import org.deidentifier.flash.gui.Model;
import org.deidentifier.flash.gui.resources.Resources;
import org.eclipse.core.runtime.IProgressMonitor;

public class WorkerTransform extends Worker<DataHandle> {

    private final Model model;

    public WorkerTransform(final Model model) {
        this.model = model;
    }

    @Override
    public void
            run(final IProgressMonitor arg0) throws InvocationTargetException,
                                            InterruptedException {

        arg0.beginTask(Resources.getMessage("WorkerTransform.0"), 100); //$NON-NLS-1$

        try {
            arg0.worked(1);
            result = model.getResult().getHandle(model.getSelectedNode());
        } catch (final Exception e) {
            error = e;
        }
        arg0.worked(100);
        arg0.done();
    }
}
