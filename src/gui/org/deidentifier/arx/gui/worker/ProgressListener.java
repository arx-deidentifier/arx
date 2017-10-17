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

import org.deidentifier.arx.ARXListener;
import org.deidentifier.arx.gui.resources.Resources;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Simple progress listener for ARX jobs
 * 
 * @author Fabian Prasser
 */
final class ProgressListener implements ARXListener{

    /** Monitor */
    private final IProgressMonitor monitor;

    /** State */
    private int                    previous = 0;

    /** Time analysis */
    private ProgressAnalysis       analysis = new ProgressAnalysis();

    /**
     * Creates a new instance
     * @param monitor
     */
    ProgressListener(IProgressMonitor monitor) {
        this.monitor = monitor;
        this.analysis.start();
    }
    
    /**
     * Progress
     * 
     * @param progress
     */
    public void progress(final double progress) {
        if (monitor.isCanceled()) { 
            throw new RuntimeException(Resources.getMessage("WorkerAnonymize.1")); //$NON-NLS-1$ 
        }
        int current = (int) (Math.round(progress * 100d));
        if (current != previous) {
            monitor.worked(current - previous);
            analysis.update(current);
            previous = current;
            monitor.subTask(Resources.getMessage("Worker.1") + " " + analysis.getTimeRemaining()); //$NON-NLS-1$ 
        }
    }
}