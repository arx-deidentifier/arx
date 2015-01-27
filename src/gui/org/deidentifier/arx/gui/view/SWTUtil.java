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
 * This class provides some utility methods for working with SWT.
 *
 * @author Fabian Prasser
 */
public class SWTUtil {

    /** Static settings. */
    public static final int SLIDER_MAX = 1000;

    /**
     * Centers the shell on the given monitor.
     *
     * @param shell
     * @param monitor
     */
    public static void center(Shell shell, Monitor monitor) {
        Rectangle shellRect = shell.getBounds();
        Rectangle displayRect = monitor.getBounds();
        int x = (displayRect.width - shellRect.width) / 2;
        int y = (displayRect.height - shellRect.height) / 2;
        shell.setLocation(displayRect.x + x, displayRect.y + y);
    }

    /**
     * Centers the given shell.
     *
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
     * Creates grid data.
     *
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
     * Creates grid data.
     *
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
     * Creates grid data.
     *
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
     * Creates grid data.
     *
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
     * Creates a grid layout.
     *
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
     * Creates a grid layout.
     *
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
     * Creates a help button in the given folder.
     *
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
     * Creates grid data.
     *
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
     * Creates grid data.
     *
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
     * Creates grid data.
     *
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
     * Disables the composite and its children.
     *
     * @param elem
     */
    public static void disable(final Composite elem) {
        setEnabled(elem, false);
    }

    /**
     * Disables the control.
     *
     * @param elem
     */
    public static void disable(final Control elem) {
        elem.setEnabled(false);
    }

    /**
     * Converts the double value to a slider selection.
     *
     * @param min
     * @param max
     * @param value
     * @return
     */
    public static int doubleToSlider(final double min,
                                     final double max,
                                     final double value) {
        int val = (int)Math.round((value - min) / (max - min) * SLIDER_MAX);
        if (val < 0) {
            val = 0;
        }
        if (val > SLIDER_MAX) {
            val = SLIDER_MAX;
        }
        return val;
    }

    /**
     * Enables the composite and its children.
     *
     * @param elem
     */
    public static void enable(final Composite elem) {
        setEnabled(elem, true);
    }

    /**
     * Enables the control.
     *
     * @param elem
     */
    public static void enable(final Control elem) {
        elem.setEnabled(true);
    }

    /**
     * Converts the integer value to a slider selection.
     *
     * @param min
     * @param max
     * @param value
     * @return
     */
    public static int intToSlider(final int min, final int max, final int value) {
        return doubleToSlider(min, max, value);
    }

    /**
     * Converts the slider value to a double.
     *
     * @param min
     * @param max
     * @param value
     * @return
     */
    public static double sliderToDouble(final double min,
                                        final double max,
                                        final int value) {
        double val = ((double) value / (double) SLIDER_MAX) * (max - min) + min;
        if (val < min) {
            val = min;
        }
        if (val > max) {
            val = max;
        }
        return val;
    }

    /**
     * Converts the slider value to an integer.
     *
     * @param min
     * @param max
     * @param value
     * @return
     */
    public static int sliderToInt(final int min, final int max, final int value) {
        return (int)Math.round(sliderToDouble(min, max, value));
    }

    /**
     * En-/disables the composite and its children.
     *
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
