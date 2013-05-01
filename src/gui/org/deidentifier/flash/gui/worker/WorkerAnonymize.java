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

import org.deidentifier.flash.FLASHAdapter;
import org.deidentifier.flash.FLASHAnonymizer;
import org.deidentifier.flash.FLASHResult;
import org.deidentifier.flash.gui.Controller;
import org.deidentifier.flash.gui.Model;
import org.deidentifier.flash.gui.resources.Resources;
import org.eclipse.core.runtime.IProgressMonitor;

public class WorkerAnonymize extends Worker<FLASHResult> {

    private final Model      model;
    private final Controller controller;

    public WorkerAnonymize(final Controller controller, final Model model) {
        this.model = model;
        this.controller = controller;
    }

    @Override
    public void
            run(final IProgressMonitor arg0) throws InvocationTargetException,
                                            InterruptedException {

        // Track progress
        arg0.beginTask(Resources.getMessage("WorkerAnonymize.0"), 110); //$NON-NLS-1$

        // Initialize anonymizer
        final FLASHAnonymizer anonymizer = model.createAnonymizer();

        anonymizer.setListener(new FLASHAdapter() {
            int count = 0;

            @Override
            public void nodeTagged(final int numNodes) {
                final int val = (int) (((double) (++count) / (double) numNodes) * 100d);
                arg0.worked(10 + Math.min(val, 99));
                if (arg0.isCanceled()) { throw new RuntimeException(Resources.getMessage("WorkerAnonymize.1")); } //$NON-NLS-1$
            }
        });

        // Anonymize
        try {
            switch (model.getInputConfig().getCriterion()) {
            case K_ANONYMITY:
                result = anonymizer.kAnonymize(model.getInputConfig()
                                                    .getInput(),
                                               model.getInputConfig().getK(),
                                               model.getInputConfig()
                                                    .getRelativeMaxOutliers());
                break;
            case L_DIVERSITY:
                switch (model.getInputConfig().getLDiversityCriterion()) {
                case ENTROPY:
                    result = anonymizer.lDiversify(model.getInputConfig()
                                                        .getInput(),
                                                   model.getInputConfig()
                                                        .getL(),
                                                   true,
                                                   model.getInputConfig()
                                                        .getRelativeMaxOutliers());
                    break;
                case DISTINCT:
                    result = anonymizer.lDiversify(model.getInputConfig()
                                                        .getInput(),
                                                   model.getInputConfig()
                                                        .getL(),
                                                   false,
                                                   model.getInputConfig()
                                                        .getRelativeMaxOutliers());
                    break;
                case RECURSIVE:
                    result = anonymizer.lDiversify(model.getInputConfig()
                                                        .getInput(),
                                                   model.getInputConfig()
                                                        .getC(),
                                                   model.getInputConfig()
                                                        .getL(),
                                                   model.getInputConfig()
                                                        .getRelativeMaxOutliers());
                    break;
                }
                break;
            case T_CLOSENESS:
                switch (model.getInputConfig().getTClosenessCriterion()) {
                case EMD_EQUAL:
                    result = anonymizer.tClosify(model.getInputConfig()
                                                      .getInput(),
                                                 model.getInputConfig().getK(),
                                                 model.getInputConfig().getT(),
                                                 model.getInputConfig()
                                                      .getRelativeMaxOutliers());
                    break;
                case EMD_HIERARCHICAL:
                    result = anonymizer.tClosify(model.getInputConfig()
                                                      .getInput(),
                                                 model.getInputConfig().getK(),
                                                 model.getInputConfig().getT(),
                                                 model.getInputConfig()
                                                      .getRelativeMaxOutliers(),
                                                 model.getInputConfig()
                                                      .getSensitiveHierarchy());
                    break;
                }
                break;
            }

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
