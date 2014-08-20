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
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * This class provides some utility methods for working with SWT
 * @author Fabian Prasser
 *
 */
public class SWTUtil {

    /**
     * Centers the shell on the given monitor
     * @param shell
     * @param monitor
     */
    public static void center(Shell shell, Monitor monitor) {
        Rectangle shellRect = shell.getBounds();
        Rectangle displayRect = monitor.getClientArea();
        int x = (displayRect.width - shellRect.width) / 2;
        int y = (displayRect.height - shellRect.height) / 2;
        shell.setLocation(displayRect.x + x, displayRect.y + y);
    }

    /**
     * Centers the given shell
     * @param shell
     * @param parent
     */
    public static void center(final Shell shell, final Shell parent) {

        final Rectangle bounds = parent.getBounds();
        final Point p = shell.getSize();
        final int left = (bounds.width - p.x) / 2;
        final int top = (bounds.height - p.y) / 2;
        shell.setBounds(left + bounds.x, top + bounds.y, p.x, p.y);
    }

    /**
     * Creates grid data
     * @return
     */
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

    /**
     * Creates grid data
     * @return
     */
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

    /**
     * Creates grid data
     * @return
     */
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

    /**
     * Creates grid data
     * @return
     */
    public static GridData createGridData() {
        final GridData data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.verticalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = false;
        data.grabExcessVerticalSpace = false;
        return data;
    }

    /**
     * Creates a grid layout
     * @param columns
     * @return
     */
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

    /**
     * Creates a grid layout
     * @param columns
     * @param compact
     * @return
     */
    public static GridLayout createGridLayout(int columns, boolean compact) {
        if (compact) return createGridLayout(columns);
        final GridLayout layout = new GridLayout();
        layout.numColumns = columns;
        return layout;
    }

    /**
     * Creates a help button in the given folder
     * @param controller
     * @param tabFolder
     * @param id
     */
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

    /**
     * Creates grid data
     * @return
     */
    public static GridData createNoFillGridData() {
        final GridData d = new GridData();
        d.horizontalAlignment = SWT.LEFT;
        d.verticalAlignment = SWT.TOP;
        d.grabExcessHorizontalSpace = false;
        d.grabExcessVerticalSpace = false;
        return d;
    }

    /**
     * Creates grid data
     * @param i
     * @return
     */
    public static Object createSpanColumnsAndFillGridData(final int i) {
        final GridData d = new GridData();
        d.grabExcessHorizontalSpace = true;
        d.grabExcessVerticalSpace = true;
        d.horizontalSpan = i;
        return d;
    }

    /**
     * Creates grid data
     * @param i
     * @return
     */
    public static Object createSpanColumnsGridData(final int i) {
        final GridData d = new GridData();
        d.grabExcessHorizontalSpace = false;
        d.grabExcessVerticalSpace = false;
        d.horizontalSpan = i;
        return d;
    }


    /**
     * Disables the composite and its children
     * @param elem
     */
    public static void disable(final Composite elem) {
        setEnabled(elem, false);
    }

    /**
     * Disables the control
     * @param elem
     */
    public static void disable(final Control elem) {
        elem.setEnabled(false);
    }

    /**
     * Enables the composite and its children
     * @param elem
     */
    public static void enable(final Composite elem) {
        setEnabled(elem, true);
    }

    /**
     * Enables the control
     * @param elem
     */
    public static void enable(final Control elem) {
        elem.setEnabled(true);
    }

    /**
     * En-/disables the composite and its children
     * @param elem
     * @param val
     */
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
}
