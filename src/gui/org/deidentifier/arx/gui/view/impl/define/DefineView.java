/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
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
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IAttachable;
import org.deidentifier.arx.gui.view.def.IView.ModelEvent.EventTarget;
import org.deidentifier.arx.gui.view.impl.common.DataView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

public class DefineView implements IAttachable {

    private static final String LEFT_TEXT  = Resources.getMessage("DefineView.0"); //$NON-NLS-1$
    private static final String RIGHT_TEXT = Resources.getMessage("DefineView.1"); //$NON-NLS-1$
    
    private final Composite center;
    
    public DefineView(final Composite parent, final Controller controller) {

		// Define
		Group compositeLeft;
		Group compositeRight;
		Composite compositeTopRight;
		Composite compositeBottomRight;

        // Create center composite
        center = new Composite(parent, SWT.NONE);
        center.setLayoutData(SWTUtil.createFillGridData());
        final GridLayout centerLayout = new GridLayout();
        centerLayout.numColumns = 2;
        centerLayout.makeColumnsEqualWidth = true;
        center.setLayout(centerLayout);

        // Create left composite
        compositeLeft = new Group(center, SWT.NONE);
        compositeLeft.setText(LEFT_TEXT);
        compositeLeft.setLayoutData(SWTUtil.createFillGridData());
        final GridLayout leftLayout = new GridLayout();
        leftLayout.numColumns = 1;
        compositeLeft.setLayout(leftLayout);

        // Create right composite
        compositeRight = new Group(center, SWT.NONE);
        compositeRight.setText(RIGHT_TEXT);
        compositeRight.setLayoutData(SWTUtil.createFillGridData());
        final GridLayout rightLayout = new GridLayout();
        rightLayout.numColumns = 1;
        compositeRight.setLayout(rightLayout);
        
        // Create top-right composite
        GridData topRightLD = SWTUtil.createFillGridData();
        topRightLD.grabExcessVerticalSpace = true;
        compositeTopRight = new Composite(compositeRight, SWT.NONE);
        compositeTopRight.setLayoutData(topRightLD);
        final GridLayout topRightLayout = new GridLayout();
        topRightLayout.numColumns = 1;
        topRightLayout.marginHeight = 0;
        compositeTopRight.setLayout(topRightLayout);

        // Create bottom-right composite
        GridData bottomRightLD = SWTUtil.createFillGridData();
        bottomRightLD.grabExcessVerticalSpace = false;
        compositeBottomRight = new Composite(compositeRight, SWT.NONE);
        compositeBottomRight.setLayoutData(bottomRightLD);
        final GridLayout bottomRightLayout = new GridLayout();
        bottomRightLayout.numColumns = 1;
        bottomRightLayout.marginWidth = 0;
        bottomRightLayout.marginHeight = 0;
        compositeBottomRight.setLayout(bottomRightLayout);

        // Create views
        new DataView(compositeLeft, controller, EventTarget.INPUT, null);
        new SubsetDefinitionView(compositeLeft, controller);
        new DataDefinitionView(compositeTopRight, controller);
        new CriterionDefinitionView(compositeBottomRight, controller);
    }

    @Override
    public Control getControl() {
        return center;
    }
}
