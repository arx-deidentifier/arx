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

package org.deidentifier.arx.gui.view;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.resources.Resources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class SWTUtil {

    public static void center(final Shell shell, final Shell parent) {

        final Rectangle bounds = parent.getBounds();
        final Point p = shell.getSize();
        final int left = (bounds.width - p.x) / 2;
        final int top = (bounds.height - p.y) / 2;
        shell.setBounds(left + bounds.x, top + bounds.y, p.x, p.y);
    }

    public static GridData createFillGridData() {
        final GridData data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.verticalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        data.horizontalIndent=0;
        data.verticalIndent=0;
        return data;
    }

    public static GridData createFillHorizontallyGridData() {
        final GridData data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.verticalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = false;
        data.horizontalIndent=0;
        data.verticalIndent=0;
        return data;
    }

    public static GridData createFillVerticallyGridData() {
        final GridData data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.verticalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = false;
        data.grabExcessVerticalSpace = true;
        data.horizontalIndent=0;
        data.verticalIndent=0;
        return data;
    }

    public static GridData createGridData() {
        final GridData data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.verticalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = false;
        data.grabExcessVerticalSpace = false;
        return data;
    }

    public static GridData createNoFillGridData() {
        final GridData d = new GridData();
        d.horizontalAlignment = SWT.LEFT;
        d.verticalAlignment = SWT.TOP;
        d.grabExcessHorizontalSpace = false;
        d.grabExcessVerticalSpace = false;
        return d;
    }

    public static Object createSpanColumnsAndFillGridData(final int i) {
        final GridData d = new GridData();
        d.grabExcessHorizontalSpace = true;
        d.grabExcessVerticalSpace = true;
        d.horizontalSpan = i;
        return d;
    }

    public static Object createSpanColumnsGridData(final int i) {
        final GridData d = new GridData();
        d.grabExcessHorizontalSpace = false;
        d.grabExcessVerticalSpace = false;
        d.horizontalSpan = i;
        return d;
    }

    public static void disable(final Composite elem) {
        setEnabled(elem, false);
    }

    public static void enable(final Composite elem) {
        setEnabled(elem, true);
    }

    private static void setEnabled(final Composite elem, final boolean val) {
        elem.setEnabled(val);
        for (final Control c : elem.getChildren()) {
            if (c instanceof Composite) {
                setEnabled((Composite) c, val);
            } else {
                c.setEnabled(val);
            }
        }
    }

    public static void createHelpButton(final Controller controller, final CTabFolder tabFolder, final String id) {
        ToolBar toolbar = new ToolBar(tabFolder, SWT.FLAT);
        tabFolder.setTopRight( toolbar, SWT.RIGHT );
        ToolItem item = new ToolItem( toolbar, SWT.PUSH );
        item.setImage(controller.getResources().getImage("help.png"));  //$NON-NLS-1$
        item.setToolTipText(Resources.getMessage("General.0")); //$NON-NLS-1$
        int height = toolbar.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
        tabFolder.setTabHeight(Math.max(height, tabFolder.getTabHeight()));
        item.addSelectionListener(new SelectionAdapter(){

            @Override
            public void widgetSelected(SelectionEvent arg0) {
                controller.actionShowHelpDialog(id);
            }
        });
    }

    public static GridLayout createGridLayout(int columns) {
        final GridLayout layout = new GridLayout();
        layout.numColumns = columns;
        layout.marginBottom = 0;
        layout.marginHeight = 0;
        layout.marginLeft = 0;
        layout.marginRight = 0;
        layout.marginTop = 0;
        layout.marginWidth = 0;
        return layout;
    }

    public static GridLayout createGridLayout(int columns, boolean compact) {
        if (compact) return createGridLayout(columns);
        final GridLayout layout = new GridLayout();
        layout.numColumns = columns;
        return layout;
    }
}
