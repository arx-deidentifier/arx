/*
 * ARX: Powerful Data Anonymization
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

package org.deidentifier.arx.gui.view.def;

import org.eclipse.swt.widgets.Composite;

/**
 * Interface for an editor for a given data type
 * @author Fabian Prasser
 *
 * @param <T>
 */
public interface IEditor<T> {

    /**
     * Does the editor accept the value
     * @param t
     * @return
     */
    public boolean accepts(T t);

    /**
     * Creates an according control
     * @param parent
     */
    public void createControl(Composite parent);

    /**
     * Returns the category
     * @return
     */
    public String getCategory();

    /**
     * Returns the label
     * @return
     */
    public String getLabel();

    /**
     * Returns the current value
     * @return
     */
    public T getValue();

    /**
     * Sets the value
     * @param t
     */
    public void setValue(T t);
}
