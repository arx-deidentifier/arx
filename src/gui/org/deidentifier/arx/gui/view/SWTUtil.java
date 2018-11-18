/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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
import java.util.Map;

import org.apache.commons.math3.analysis.function.Log;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.resources.Resources;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
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
     * Changes a control's font
     * @param control
     * @param style
     */
    public static void changeFont(Control control, int style) {
        FontDescriptor boldDescriptor = FontDescriptor.createFrom(control.getFont()).setStyle(style);
        final Font boldFont = boldDescriptor.createFont(control.getDisplay());
        control.setFont(boldFont);
        control.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent arg0) {
                if (boldFont != null && !boldFont.isDisposed()) {
                    boldFont.dispose();
                }
            }
            
        });
    }

    /**
     * Adds a bar chart to a column
     * @param table
     * @param column
     */
    public static void createColumnWithBarCharts(final Table table, final TableColumn column) {
        
        int index = -1;
        for (int i=0; i< table.getColumnCount(); i++) {
            if (table.getColumn(i)==column) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            return;
        }
        final Display display = table.getDisplay();
        final int columnIndex = index;
        table.addListener(SWT.PaintItem, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (event.index == columnIndex) {
                    GC gc = event.gc;
                    TableItem item = (TableItem) event.item;
                    Object object = item.getData(String.valueOf(columnIndex));
                    if (object == null || !(object instanceof Double)) {
                        return;
                    }

                    // Store
                    Color foreground = gc.getForeground();
                    Color background = gc.getBackground();
                    
                    // Draw NaN
                    Double percent = (Double)object;
                    if (percent.isNaN() || percent.isInfinite()) {
                        String text = percent.isNaN() ? "NaN" : "Infinite";
                        gc.setForeground(display.getSystemColor(SWT.COLOR_LIST_FOREGROUND));
                        Point size = event.gc.textExtent(text);
                        int offset = Math.max(0, (event.height - size.y) / 2);
                        gc.drawText(text, event.x + 2, event.y + offset, true);
                        
                    // Draw value
                    } else {
                        
                        // Initialize
                        String text = SWTUtil.getPrettyString((Double)object * 100d) + "%";
                        percent = percent >= 0d ? percent : 0d;
                        percent = percent <= 1d ? percent : 1d;
                        
                        // Draw bar
                        gc.setBackground(display.getSystemColor(SWT.COLOR_GRAY));
                        gc.setForeground(GUIHelper.getColor(240, 240, 240));
                        int width = (int) Math.round((column.getWidth() - 1) * percent);
                        width = width >= 1 ? width : 1;
                        gc.fillGradientRectangle(event.x, event.y, width, event.height, true);
                        
                        // Draw border
                        Rectangle rect2 = new Rectangle(event.x, event.y, width - 1, event.height - 1);
                        gc.setForeground(GUIHelper.getColor(150, 150, 150));
                        gc.drawRectangle(rect2);
                        
                        // Draw text
                        gc.setForeground(display.getSystemColor(SWT.COLOR_LIST_FOREGROUND));
                        Point size = event.gc.textExtent(text);
                        int offset = Math.max(0, (event.height - size.y) / 2);
                        gc.drawText(text, event.x + 2, event.y + offset, true);
                    }

                    // Reset
                    gc.setForeground(background);
                    gc.setBackground(foreground);   
                }
            }
        });     
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
     * Creates grid data with a horizontal span.
     *
     * @return
     */
    public static GridData createFillVerticallyGridData(int span) {
        GridData data = createFillVerticallyGridData();
        data.horizontalSpan = span;
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
                        } else if (item.getData(String.valueOf(i)) != null && 
                                   item.getData(String.valueOf(i)) instanceof Double) {
                            builder.append(getPrettyString(((Double) item.getData(String.valueOf(i))).doubleValue() * 100d) + "%"); //$NON-NLS-1$
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
     * Creates a grid layout with equal-width columns
     * @param columns
     * @return
     */
    public static GridLayout createGridLayoutWithEqualWidth(int columns) {
        GridLayout layout = createGridLayout(columns);
        layout.makeColumnsEqualWidth = true;
        return layout;
    }

    /**
     * Creates a help button in the given folder.
     *
     * @param controller
     * @param folder
     * @param id
     */
    public static void createHelpButton(final Controller controller, final CTabFolder folder, final String id) {
        createHelpButton(controller, folder, id, null);
    }

    /**
     * Creates a help button in the given folder.
     *
     * @param controller
     * @param folder
     * @param id
     * @param helpids
     */
    public static void createHelpButton(final Controller controller,
                                        final CTabFolder folder,
                                        final String id,
                                        final Map<Composite, String> helpids) {
        ToolBar toolbar = new ToolBar(folder, SWT.FLAT);
        folder.setTopRight( toolbar, SWT.RIGHT );
        ToolItem item = new ToolItem( toolbar, SWT.PUSH );
        item.setImage(controller.getResources().getManagedImage("help.png"));  //$NON-NLS-1$
        item.setToolTipText(Resources.getMessage("General.0")); //$NON-NLS-1$
        createDisabledImage(item);
        int height = toolbar.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
        folder.setTabHeight(Math.max(height, folder.getTabHeight()));
        item.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                if (helpids != null && folder.getSelectionIndex() >= 0 &&
                    helpids.get(folder.getItem(folder.getSelectionIndex()).getControl()) != null) {
                    controller.actionShowHelpDialog(helpids.get(folder.getItem(folder.getSelectionIndex()).getControl()));
                } else {
                    controller.actionShowHelpDialog(id);
                }
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
     * Fixes the application menu on OSX.
     * @param controller
     */
    public static void fixOSXMenu(final Controller controller) {
        
        // Check if we are on mac
        if (!isMac()) {
            return;
        }
        
        // Just disable all items in the system menu
        // TODO: Something like this could help:
        // https://stackoverflow.com/questions/32409679/capture-about-preferences-and-quit-menu-items
        // However, I had trouble unregistering the existing events for the items
        Menu systemMenu = Display.getCurrent().getSystemMenu();
        for (MenuItem systemItem : systemMenu.getItems()) {
        	systemItem.setEnabled(false);
        }
    }

    /**
     * Tries to fix a bug when resizing sash forms in OSX
     * @param sash
     */
    public static void fixOSXSashBug(final SashForm sash) {
        
        // Only if on OSX
        if (isMac()) {
            
            // Listen for resize event in first composite
            for (Control c : sash.getChildren()) {
                if (c instanceof Composite) {
                    
                    // In case of resize, redraw the sash form
                    c.addControlListener(new ControlAdapter(){
                        @Override
                        public void controlResized(ControlEvent arg0) {
                            sash.redraw();
                        }
                    });
                    return;
                }
            }
        }
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
     * Converts a boolean into a pretty string
     * @param value
     * @return
     */
    public static String getPrettyString(boolean value) {
        if (value) {
            return Resources.getMessage("PropertiesView.159");
        } else {
            return Resources.getMessage("PropertiesView.170");
        }
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
     * Fallback for objects of unknown type
     * @param value
     * @return
     */
    public static String getPrettyString(Object value) {
        if (value instanceof Boolean) {
            return SWTUtil.getPrettyString(((Boolean)value).booleanValue());
        } else if (value instanceof Double) {
            return SWTUtil.getPrettyString(((Double)value).doubleValue());
        } if (value instanceof Integer) {
            return SWTUtil.getPrettyString(((Integer)value).intValue());
        } if (value instanceof Long) {
            return SWTUtil.getPrettyString(((Long)value).longValue());
        }
        return String.valueOf(value);
    }
   
    /**
     * Are we running on an OSX system
     * @return
     */
    public static boolean isMac() {
        return System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0; //$NON-NLS-1$ //$NON-NLS-2$
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
}