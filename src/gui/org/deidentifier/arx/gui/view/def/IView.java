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

import org.deidentifier.arx.gui.model.ModelEvent;

/**
 * An interface for views
 * @author Fabian Prasser
 */
public interface IView {

    /**
     * Disposes the view
     */
    public void dispose();

    /**
     * Resets the view
     */
    public void reset();

    /**
     * Updates the view
     * @param event
     */
    public void update(ModelEvent event);
}
