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

import org.eclipse.jface.operation.IRunnableWithProgress;

/**
 * A base class for workers that perform asynchronous tasks in a progress dialog.
 *
 * @author Fabian Prasser
 * @param <T>
 */
public abstract class Worker<T> implements IRunnableWithProgress {

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
