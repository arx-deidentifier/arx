/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
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

import org.eclipse.jface.operation.IRunnableWithProgress;

/**
 * A base class for workers that perform asynchronous tasks in a progress dialog
 * @author Fabian Prasser
 *
 * @param <T>
 */
public abstract class Worker<T> implements IRunnableWithProgress {

	/** Error, if any*/
    protected Exception error  = null;
    /** Result, if any*/
    protected T         result = null;

    /**
     * Returns the error
     * @return
     */
    public Exception getError() {
        return error;
    }

    /**
     * Returns the result
     * @return
     */
    public T getResult() {
        return result;
    }

    /**
     * Sets the error
     * @param e
     */
    public void setError(final Exception e) {
        this.error = e;
    }
}
