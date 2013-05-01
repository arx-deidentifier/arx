/*
 * FLASH: Efficient, Stable and Optimal Data Anonymization
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

package org.deidentifier.flash.gui.view.impl.define;

import org.deidentifier.flash.gui.Controller;
import org.deidentifier.flash.gui.SWTUtil;
import org.deidentifier.flash.gui.resources.Resources;
import org.deidentifier.flash.gui.view.def.IAttachable;
import org.deidentifier.flash.gui.view.def.IView.ModelEvent.EventTarget;
import org.deidentifier.flash.gui.view.impl.common.DataView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

public class DefineView implements IAttachable {

    private static final String LEFT_TEXT  = Resources.getMessage("DefineView.0"); //$NON-NLS-1$
    private static final String RIGHT_TEXT = Resources.getMessage("DefineView.1"); //$NON-NLS-1$

    private final Group         compositeLeft;
    private final Group         compositeRight;
    private final Composite     center;

    public DefineView(final Composite parent, final Controller controller) {

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

        // Create views
        new DataView(compositeLeft, controller, EventTarget.INPUT, null);
        new DataDefinitionView(compositeRight, controller);
        new CriterionDefinitionView(compositeRight, controller);
    }

    @Override
    public Control getControl() {
        return center;
    }
}
