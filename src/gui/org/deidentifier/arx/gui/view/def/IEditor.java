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

package org.deidentifier.arx.gui.view.def;

import org.eclipse.swt.widgets.Composite;

/**
 * Interface for an editor for a given data type.
 *
 * @author Fabian Prasser
 * @param <T>
 */
public interface IEditor<T> {

    /**
     * Does the editor accept the value.
     *
     * @param t
     * @return
     */
    public boolean accepts(T t);

    /**
     * Creates an according control.
     *
     * @param parent
     */
    public void createControl(Composite parent);

    /**
     * Returns the category.
     *
     * @return
     */
    public String getCategory();

    /**
     * Returns the label.
     *
     * @return
     */
    public String getLabel();

    /**
     * Returns the current value.
     *
     * @return
     */
    public T getValue();

    /**
     * Sets the value.
     *
     * @param t
     */
    public void setValue(T t);
}
