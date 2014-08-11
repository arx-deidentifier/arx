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

package org.deidentifier.arx.gui.view.impl.common;

/**
 * Configuration for table view
 * @author Fabian Prasser
 *
 */
public class ComponentTableSelectionConfiguration {
    
    /** Able to select columns */
    public boolean column;
    /** Able to select rows */
    public boolean row;
    /** Able to select cells */
    public boolean cell;
    
    /**
     * Creates a new instance
     */
    public ComponentTableSelectionConfiguration(boolean row,
                                                boolean column,
                                                boolean cell) {
        this.column = column;
        this.row = row;
        this.cell = cell;
    }
}
