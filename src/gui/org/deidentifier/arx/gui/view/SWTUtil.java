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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.apache.commons.math3.analysis.function.Log;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.resources.Resources;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import de.linearbits.swt.table.DynamicTable;

/**
 * This class provides some utility methods for working with SWT.
 *
 * @author Fabian Prasser
 */
public class SWTUtil {

    /** Constant */
    public static final int     SLIDER_MAX = 1000;

    /** Constant */
    private static final double LN2        = new Log().value(2);

    /** Constant */
    private static final double LN3        = new Log().value(3);

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
     * Registers an image for a tool item. Generates a version of the image
     * that renders well on windows toolbars, when disabled.
     * 
     * @param item
     * @param image
     */
    public static void createDisabledImage(ToolItem item) {
        final Image image = new Image(item.getDisplay(), item.getImage(), SWT.IMAGE_GRAY);
        item.setDisabledImage(image);
        item.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent arg0) {
                if (image != null && !image.isDisposed()) {
                    image.dispose();
                }
            }
        });
    }

    /**
     * Creates grid data.
     *
     * @return
     */
    public static GridData createFillGridData() {
        return createFillGridData(1);
    }

    /**
     * Creates grid data.
     *
     * @return
     */
    public static GridData createFillGridData(int span) {
        final GridData data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.verticalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        data.horizontalIndent=0;
        data.verticalIndent=0;
        data.horizontalSpan = span;
        return data;
    }

    /**
     * Creates grid data.
     *
     * @return
     */
    public static GridData createFillHorizontallyGridData() {
        return createFillHorizontallyGridData(true);
    }

    /**
     * Creates grid data.
     *
     * @return
     */
    public static GridData createFillHorizontallyGridData(boolean fill) {
        return createFillHorizontallyGridData(fill, 1);
    }
    /**
     * Creates grid data.
     *
     * @return
     */
    public static GridData createFillHorizontallyGridData(boolean fill, int span) {
        final GridData data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.verticalAlignment = fill ? SWT.FILL : SWT.CENTER;
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = false;
        data.horizontalSpan = span;
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
     * Creates a generic tooltip for the table
     * @param table
     */
    public static void createGenericTooltip(final Table table) {
        table.addMouseMoveListener(new MouseMoveListener() {
            private TableItem current = null;

            @Override
            public void mouseMove(MouseEvent arg0) {
                TableItem item = table.getItem(new Point(arg0.x, arg0.y));
                if (item != null && item != current) {
                    current = item;
                    StringBuilder builder = new StringBuilder();
                    builder.append("("); //$NON-NLS-1$
                    int columns = item.getParent().getColumnCount();
                    for (int i = 0; i < columns; i++) {
                        String value = item.getText(i);
                        if (value != null && !value.equals("")) { //$NON-NLS-1$
                            builder.append(value);
                            if (i < columns - 1) {
                                builder.append(", "); //$NON-NLS-1$
                            }
                        }
                    }
                    builder.append(")"); //$NON-NLS-1$
                    table.setToolTipText(builder.toString());
                }
            }
        });
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
        item.setImage(controller.getResources().getManagedImage("help.png"));  //$NON-NLS-1$
        item.setToolTipText(Resources.getMessage("General.0")); //$NON-NLS-1$
        createDisabledImage(item);
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
    public static Object createSpanColumnsGridData(final int i) {
        final GridData d = new GridData();
        d.grabExcessHorizontalSpace = false;
        d.grabExcessVerticalSpace = false;
        d.horizontalSpan = i;
        return d;
    }

    /**
     * Returns a table. Implements hacks for fixing OSX bugs.
     * @param parent
     * @param style
     * @return
     */
    public static Table createTable(Composite parent, int style) {
        Table table = new Table(parent, style);
        fixOSXTableBug(table);
        return table;
    }

    /**
     * Returns a dynamic table. Implements hacks for fixing OSX bugs.
     * @param parent
     * @param style
     * @return
     */
    public static DynamicTable createTableDynamic(Composite parent, int style) {
        DynamicTable table = new DynamicTable(parent, style);
        fixOSXTableBug(table);
        return table;
    }

    /**
     * Returns a table viewer. Implements hacks for fixing OSX bugs.
     * @param parent
     * @param style
     * @return
     */
    public static TableViewer createTableViewer(Composite container, int style) {
        TableViewer viewer = new TableViewer(container, style);
        fixOSXTableBug(viewer.getTable());
        return viewer;
    }

    /**
     * Returns a checkbox table viewer. Implements hacks for fixing OSX bugs.
     * @param parent
     * @param style
     * @return
     */
    public static CheckboxTableViewer createTableViewerCheckbox(Composite container, int style) {
        CheckboxTableViewer viewer = CheckboxTableViewer.newCheckList(container, style);
        fixOSXTableBug(viewer.getTable());
        return viewer;
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
     * Returns a pretty string representing the given double
     * @param value
     * @return
     */
    public static String getPrettyString(double value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        if (value == LN2) {
            return "ln(2)";
        } else if (value == LN3) {
            return "ln(3)";
        } else if (value == 0) {
            return "0";
        } else if (Math.abs(value) < 0.00001) {
            return new DecimalFormat("#.#####E0", symbols).format(value).replace('E', 'e');
        } else if (Math.abs(value) < 1) {
            return new DecimalFormat("#.#####", symbols).format(value);
        } else if (Math.abs(value) < 100000) {
            return new DecimalFormat("######.#####", symbols).format(value);
        } else {
            return String.valueOf(value).replace('E', 'e');
        }
    }

    /**
     * Returns a pretty string representing the given value
     * @param value
     * @return
     */
    public static String getPrettyString(int value) {
        return String.valueOf(value);
    }

    /**
     * Returns a pretty string representing the given value
     * @param value
     * @return
     */
    public static String getPrettyString(long value) {
        return String.valueOf(value);
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
     * Are we running on an OSX system
     * @return
     */
    public static boolean isMac() {
        return System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0; //$NON-NLS-1$ //$NON-NLS-2$
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
     * Fixes bugs on OSX when scrolling in tables
     * @param table
     */
    private static void fixOSXTableBug(final Table table) {
        if (isMac()) {
            SelectionListener bugFixer = new SelectionListener(){
                
                @Override
                public void widgetDefaultSelected(SelectionEvent arg0) {
                    widgetSelected(arg0);
                }

                @Override
                public void widgetSelected(SelectionEvent arg0) {
                    table.redraw();
                }
            };
            table.getVerticalBar().addSelectionListener(bugFixer);
            table.getHorizontalBar().addSelectionListener(bugFixer);
        }
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