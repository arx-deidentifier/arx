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

package org.deidentifier.arx.gui.view.impl.define;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.ILayout;
import org.deidentifier.arx.gui.view.impl.common.ViewDataInput;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * This class implements the layout for the data definition perspective.
 *
 * @author Fabian Prasser
 */
public class LayoutDefinition implements ILayout {

    /**  TODO */
    private final Composite center;
    
    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     */
    public LayoutDefinition(final Composite parent, final Controller controller) {

		// Define
        Composite compositeLeft;
		Composite compositeRight;
		Composite compositeTopRight;
		Composite compositeBottomRight;

        // Create center composite
        center = new Composite(parent, SWT.NONE);
        center.setLayoutData(SWTUtil.createFillGridData());
        final GridLayout centerLayout = SWTUtil.createGridLayout(2);
        centerLayout.makeColumnsEqualWidth = true;
        center.setLayout(centerLayout);

        // Create left composite
        compositeLeft = new Composite(center, SWT.NONE);
        compositeLeft.setLayoutData(SWTUtil.createFillGridData());
        compositeLeft.setLayout(SWTUtil.createGridLayout(1));

        // Create right composite
        compositeRight = new Composite(center, SWT.NONE);
        compositeRight.setLayoutData(SWTUtil.createFillGridData());
        compositeRight.setLayout(SWTUtil.createGridLayout(1));
        
        // Create top-right composite
        compositeTopRight = new Composite(compositeRight, SWT.NONE);
        compositeTopRight.setLayoutData(SWTUtil.createFillGridData());
        compositeTopRight.setLayout(SWTUtil.createGridLayout(1));

        // Create bottom-right composite
        compositeBottomRight = new Composite(compositeRight, SWT.NONE);
        compositeBottomRight.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        compositeBottomRight.setLayout(SWTUtil.createGridLayout(1));

        // Create views
        new ViewDataInput(compositeLeft, controller); 
        new ViewSubsetDefinition(compositeLeft, controller);
        new ViewDataDefinition(compositeTopRight, controller);
        new ViewGeneralSettings(compositeBottomRight, controller);
    }
}
