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

import org.eclipse.swt.SWT;

/**
 * Configuration for table view
 * @author Fabian Prasser
 *
 */
public class ComponentTableConfiguration {

    /** Alignment*/
    public ComponentTableAlignmentConfiguration alignment = new ComponentTableAlignmentConfiguration(SWT.CENTER);
    /** Header*/
    public ComponentTableHeaderConfiguration header = new ComponentTableHeaderConfigurationDefault(100);
    /** Selection*/
    public ComponentTableSelectionConfiguration selection = new ComponentTableSelectionConfiguration(false, false, false);
}
