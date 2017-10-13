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

import org.deidentifier.arx.gui.resources.Resources;
import org.eclipse.jface.operation.IRunnableWithProgress;

/**
 * A base class for workers that perform asynchronous tasks in a progress dialog.
 *
 * @author Fabian Prasser
 * @param <T>
 */
public abstract class Worker<T> implements IRunnableWithProgress {

	/**
     * Returns the time left as a string
     * @param millis
     * @return
     */
    public static String getTimeLeft(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        StringBuffer result = new StringBuffer();
        
        if (seconds > 59) {
            seconds = 0;
        }
        if (minutes > 59) {
            minutes = 0;
        }
        if (hours > 24) {
            hours = 0;
        }
        if (days == 0 && hours == 0 && minutes == 0 && seconds == 0) {
            seconds = 1;
        }
        
        if (days != 0) {
            result.append(days).append(" ").append(Resources.getMessage("Worker.2")); //$NON-NLS-1$
        }
        if (hours != 0) {
            if (result.length() != 0) {
                result.append(", ");
            }
            result.append(hours).append(" ").append(Resources.getMessage("Worker.3")); //$NON-NLS-1$ 
        }
        if (minutes != 0) {
            if (result.length() != 0) {
                result.append(", ");
            }
            result.append(minutes).append(" ").append(Resources.getMessage("Worker.4")); //$NON-NLS-1$ 
        }
        if (seconds != 0) {
            if (result.length() != 0) {
                result.append(", ");
            }
            result.append(seconds).append(" ").append(Resources.getMessage("Worker.5")); //$NON-NLS-1$ 
        }
        return result.toString(); 
    }
    
    /** Error, if any. */
    protected Exception error  = null;

    /** Result, if any. */
    protected T         result = null;

    /**
     * Returns the error.
     *
     * @return
     */
    public Exception getError() {
        return error;
    }

    /**
     * Returns the result.
     *
     * @return
     */
    public T getResult() {
        return result;
    }
    
    /**
     * Sets the error.
     *
     * @param e
     */
    public void setError(final Exception e) {
        this.error = e;
    }
}
