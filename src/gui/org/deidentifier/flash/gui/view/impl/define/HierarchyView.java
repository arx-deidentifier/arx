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

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.flash.AttributeType;
import org.deidentifier.flash.AttributeType.Hierarchy;
import org.deidentifier.flash.DataDefinition;
import org.deidentifier.flash.gui.Controller;
import org.deidentifier.flash.gui.Model;
import org.deidentifier.flash.gui.SWTUtil;
import org.deidentifier.flash.gui.resources.Resources;
import org.deidentifier.flash.gui.view.def.IHierarchyEditorView;
import org.deidentifier.flash.gui.view.def.IView;
import org.deidentifier.flash.gui.view.def.IView.ModelEvent.EventTarget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * This class implements an editor for generalization hierarchies. It is partly
 * based upon code implemented by Ledian Xhani and Ljubomir Dshevlekov.
 * 
 * @author Prasser, Kohlmayer, Xhani, Dshevlekov
 * 
 */
public class HierarchyView implements IHierarchyEditorView, IView {

    private static final String ITEM_ALL  = Resources.getMessage("HierarchyView.0"); //$NON-NLS-1$

    /** Editors cell color */
    private static final Color  COLOR     = Display.getCurrent()
                                                   .getSystemColor(SWT.COLOR_GRAY);

    /** Editors table */
    private Table               table;

    /** Controller */
    private Controller          controller;

    /** Bottom parent container */
    private Composite           base;

    /** The column attribute */
    private final String        attribute;

    /** The selected row for the insert/remove row event */
    private TableItem           targetRow;

    /** The selected column for the insert/remove column event */
    private TableColumn         targetColumn;

    /** The model */
    private Model               model;

    /** The combo for min generalization */
    private Combo               min;

    /** The combo for max generalization */
    private Combo               max;

    /** The current hierarchy */
    private String[][]          hierarchy = new String[][] { new String[] {} };

    /** Is the view editable? */
    private boolean             editable  = true;

    /**
     * Constructor for not editable views
     * 
     * @param parent
     * @param attribute
     */
    public HierarchyView(final Composite parent, final String attribute) {

        this.attribute = attribute;
        editable = false;
        create(parent);

    }

    /**
     * Constructor for editable views
     * 
     * @param parent
     * @param attribute
     * @param controller
     */
    public HierarchyView(final Composite parent,
                         final String attribute,
                         final Controller controller) {

        // Register
        controller.addListener(EventTarget.HIERARCHY, this);
        controller.addListener(EventTarget.MODEL, this);
        controller.addListener(EventTarget.ATTRIBUTE_TYPE, this);

        this.controller = controller;
        this.attribute = attribute;

        // build
        editable = true;
        create(parent);
    }

    private void create(final Composite parent) {

        base = new Composite(parent, SWT.NONE);
        final GridData bottomLayoutData = SWTUtil.createFillGridData();
        bottomLayoutData.grabExcessVerticalSpace = true;
        final GridLayout bottomLayout = new GridLayout();
        bottomLayout.numColumns = 1;
        base.setLayout(bottomLayout);
        base.setLayoutData(bottomLayoutData);

        final Label l = new Label(base, SWT.NONE);
        l.setText(Resources.getMessage("HierarchyView.2") + attribute + Resources.getMessage("HierarchyView.3")); //$NON-NLS-1$ //$NON-NLS-2$

        table = new Table(base, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        final TableColumn newColumn = new TableColumn(table, SWT.NONE, 0);
        newColumn.setWidth(60);
        table.redraw();
        updateColumnTitles();

        if (editable) {

            final Composite bottom = new Composite(base, SWT.NONE);
            bottom.setLayoutData(SWTUtil.createFillHorizontallyGridData());
            final GridLayout layout = new GridLayout();
            layout.numColumns = 4;
            bottom.setLayout(layout);

            final Label l1 = new Label(bottom, SWT.NONE);
            l1.setText(Resources.getMessage("HierarchyView.4")); //$NON-NLS-1$
            min = new Combo(bottom, SWT.READ_ONLY);
            min.setLayoutData(SWTUtil.createFillHorizontallyGridData());
            min.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(final SelectionEvent arg0) {
                    if (min.getSelectionIndex() >= 0) {
                        if (min.getSelectionIndex() > (max.getSelectionIndex() + 1)) {
                            min.select(max.getSelectionIndex() + 1);
                        } else {
                            if (model != null) {
                                String val = min.getItem(min.getSelectionIndex());
                                if (val.equals(ITEM_ALL)) {
                                    val = "1"; //$NON-NLS-1$
                                }
                                model.getInputConfig()
                                     .getInput()
                                     .getDefinition()
                                     .setMinimumGeneralization(attribute,
                                                               Integer.valueOf(val) - 1);
                                controller.update(new ModelEvent(this,
                                                                 EventTarget.ATTRIBUTE_TYPE,
                                                                 attribute));
                            }
                        }
                    }
                }
            });

            final Label l2 = new Label(bottom, SWT.NONE);
            l2.setText(Resources.getMessage("HierarchyView.6")); //$NON-NLS-1$
            max = new Combo(bottom, SWT.READ_ONLY);
            max.setLayoutData(SWTUtil.createFillHorizontallyGridData());
            max.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(final SelectionEvent arg0) {
                    if (max.getSelectionIndex() >= 0) {
                        if (max.getSelectionIndex() < (min.getSelectionIndex() - 1)) {
                            max.select(min.getSelectionIndex() - 1);
                        } else {
                            if (model != null) {
                                String val = max.getItem(max.getSelectionIndex());
                                if (val.equals(ITEM_ALL)) {
                                    val = String.valueOf(max.getItem(max.getSelectionIndex() - 1));
                                }
                                model.getInputConfig()
                                     .getInput()
                                     .getDefinition()
                                     .setMaximumGeneralization(attribute,
                                                               Integer.valueOf(val) - 1);
                                controller.update(new ModelEvent(this,
                                                                 EventTarget.ATTRIBUTE_TYPE,
                                                                 attribute));
                            }
                        }
                    }
                }
            });
        }

        init();
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    private void init() {
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        table.setLayoutData(SWTUtil.createFillGridData());

        // Saved the reference to the selected row and/or column
        if (editable) {
            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseDown(final MouseEvent e) {
                    if (e.button != 3) { return; }
                    targetRow = null;
                    targetColumn = null;
                    final Point point = new Point(e.x, e.y);
                    targetRow = table.getItem(point);
                    if (targetRow == null) { return; }
                    for (int i = 0; i < table.getColumnCount(); i++) {
                        if (targetRow.getBounds(i).contains(point)) {
                            targetColumn = table.getColumn(i);
                            break;
                        }
                    }
                }
            });
        }

        // Creates the editors menu
        final Menu menu = new Menu(table);
        if (editable) {
            table.setMenu(menu);
        }

        final MenuItem insertRow = new MenuItem(menu, SWT.NONE);

        insertRow.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                int index = table.getItemCount();
                if (targetRow != null) {
                    index = table.indexOf(targetRow) + 1;
                }
                final TableItem newItem = new TableItem(table, SWT.NONE, index);
                newItem.setBackground(COLOR);
                targetRow = null;

                // TODO: No need to rebuild the whole array
                updateHierarchy();
            }
        });

        insertRow.setText(Resources.getMessage("HierarchyView.7")); //$NON-NLS-1$

        final MenuItem deleteRow = new MenuItem(menu, SWT.NONE);
        deleteRow.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                if (null == targetRow) { return; }
                final int index = table.indexOf(targetRow);
                final TableItem toRemove = table.getItem(index);
                toRemove.dispose();
                targetRow = null;

                // TODO: No need to rebuild the whole array
                updateHierarchy();
            }
        });

        deleteRow.setText(Resources.getMessage("HierarchyView.8")); //$NON-NLS-1$

        new MenuItem(menu, SWT.SEPARATOR);

        final MenuItem insertColumn = new MenuItem(menu, SWT.NONE);
        insertColumn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                int index = table.getColumnCount();
                if (targetColumn != null) {
                    index = table.indexOf(targetColumn) + 1;
                }
                final TableColumn newColumn = new TableColumn(table,
                                                              SWT.NONE,
                                                              index);
                newColumn.setWidth(60);
                table.redraw();
                updateColumnTitles();
                targetColumn = null;

                updateHierarchy();
            }
        });

        insertColumn.setText(Resources.getMessage("HierarchyView.9")); //$NON-NLS-1$

        final MenuItem deleteColumn = new MenuItem(menu, SWT.NONE);

        deleteColumn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                if (null == targetColumn) { return; }
                final int index = table.indexOf(targetColumn);
                final TableColumn toRemove = table.getColumn(index);
                toRemove.dispose();
                table.redraw();
                updateColumnTitles();
                targetColumn = null;

                updateHierarchy();
            }
        });

        deleteColumn.setText(Resources.getMessage("HierarchyView.10")); //$NON-NLS-1$

        new MenuItem(menu, SWT.SEPARATOR);

        final MenuItem up = new MenuItem(menu, SWT.NONE);
        up.setText(Resources.getMessage("HierarchyView.11")); //$NON-NLS-1$
        up.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                if (null == targetRow) { return; }

                final int index = table.indexOf(targetRow);
                if (index <= 0) { return; }

                table.setRedraw(false);
                final TableItem o1 = table.getItems()[index - 1];
                final TableItem o2 = table.getItems()[index];

                final TableItem n1 = new TableItem(table, SWT.NONE, index);
                n1.setBackground(COLOR);
                for (int i = 0; i < table.getColumnCount(); i++) {
                    n1.setText(i, o1.getText(i));
                }

                final TableItem n2 = new TableItem(table, SWT.NONE, index);
                n2.setBackground(COLOR);
                for (int i = 0; i < table.getColumnCount(); i++) {
                    n2.setText(i, o2.getText(i));
                }

                o1.dispose();
                o2.dispose();

                table.setRedraw(true);
                targetRow = null;

                // TODO: No need to rebuild the whole array
                updateHierarchy();
            }
        });

        final MenuItem down = new MenuItem(menu, SWT.NONE);
        down.setText(Resources.getMessage("HierarchyView.12")); //$NON-NLS-1$
        down.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                if (null == targetRow) { return; }

                final int index = table.indexOf(targetRow);
                if (index >= (table.getItemCount() - 1)) { return; }

                table.setRedraw(false);
                final TableItem o1 = table.getItems()[index];
                final TableItem o2 = table.getItems()[index + 1];

                final TableItem n1 = new TableItem(table, SWT.NONE, index);
                n1.setBackground(COLOR);
                for (int i = 0; i < table.getColumnCount(); i++) {
                    n1.setText(i, o1.getText(i));
                }

                final TableItem n2 = new TableItem(table, SWT.NONE, index);
                n2.setBackground(COLOR);
                for (int i = 0; i < table.getColumnCount(); i++) {
                    n2.setText(i, o2.getText(i));
                }

                o1.dispose();
                o2.dispose();

                table.setRedraw(true);
                targetRow = null;
                targetRow = null;

                // TODO: No need to rebuild the whole array
                updateHierarchy();
            }
        });

        new MenuItem(menu, SWT.SEPARATOR);

        final MenuItem renameColumn = new MenuItem(menu, SWT.NONE);

        renameColumn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                if (null == targetColumn) { return; }
                if (null == targetRow) { return; }
                final int index = table.indexOf(targetColumn);

                final String oldValue = targetRow.getText(index);
                final String newValue = controller.actionShowInputDialog(Resources.getMessage("HierarchyView.13"), Resources.getMessage("HierarchyView.14"), oldValue); //$NON-NLS-1$ //$NON-NLS-2$

                if (newValue != null) {
                    int row = 0;
                    for (final TableItem i : table.getItems()) {
                        if (i.getText(index).equals(oldValue)) {
                            i.setText(index, newValue);
                        }
                        if (hierarchy[row][index].equals(oldValue)) {
                            hierarchy[row][index] = newValue;
                        }
                        row++;
                    }
                    table.redraw();
                    updateColumnTitles();
                }
            }
        });

        renameColumn.setText(Resources.getMessage("HierarchyView.15")); //$NON-NLS-1$

        new MenuItem(menu, SWT.SEPARATOR);

        final MenuItem clear = new MenuItem(menu, SWT.NONE);

        clear.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                if (null == targetColumn) { return; }
                setHierarchy(Hierarchy.create());
                base.redraw();
                targetColumn = null;
                targetRow = null;
            }
        });

        clear.setText(Resources.getMessage("HierarchyView.16")); //$NON-NLS-1$

        // Settings labels data
        final GridData combo_data = new GridData();
        combo_data.heightHint = 16;
        combo_data.widthHint = 60;

        if (editable) {
            // The table editor
            final TableEditor editor = new TableEditor(table);
            editor.horizontalAlignment = SWT.LEFT;
            editor.grabHorizontal = true;

            // Make table editable
            table.addListener(SWT.MouseDoubleClick, new Listener() {
                @Override
                public void handleEvent(final Event event) {
                    final Rectangle clientArea = table.getClientArea();
                    final Point pt = new Point(event.x, event.y);
                    int index = table.getTopIndex();
                    while (index < table.getItemCount()) {
                        boolean visible = false;
                        final TableItem item = table.getItem(index);
                        for (int i = 0; i < table.getColumnCount(); i++) {
                            final Rectangle rect = item.getBounds(i);
                            if (rect.contains(pt)) {
                                final int column = i;
                                final Text text = new Text(table, SWT.NONE);
                                final int row = index;
                                final Listener textListener = new Listener() {
                                    @Override
                                    public void handleEvent(final Event e) {
                                        switch (e.type) {
                                        case SWT.FocusOut: {
                                            item.setText(column, text.getText());
                                            hierarchy[row][column] = text.getText();
                                            text.dispose();
                                            break;
                                        }
                                        case SWT.Traverse: {
                                            switch (e.detail) {
                                            case SWT.TRAVERSE_RETURN:
                                                item.setText(column,
                                                             text.getText());
                                                hierarchy[row][column] = text.getText();
                                            case SWT.TRAVERSE_ESCAPE: {
                                                text.dispose();
                                                e.doit = false;
                                            }
                                            }
                                            break;
                                        }
                                        }
                                    }
                                };
                                text.addListener(SWT.FocusOut, textListener);
                                text.addListener(SWT.Traverse, textListener);
                                editor.setEditor(text, item, i);
                                text.setText(item.getText(i));
                                text.selectAll();
                                text.setFocus();
                                return;
                            }
                            if (!visible && rect.intersects(clientArea)) {
                                visible = true;
                            }
                        }
                        if (!visible) { return; }
                        index++;
                    }
                }
            });
        }
    }

    private void pushHierarchy() {

        updateMinAndMax();

        if (model == null) { return; }

        final DataDefinition definition = model.getInputConfig()
                                               .getInput()
                                               .getDefinition();

        final Hierarchy h = Hierarchy.create(hierarchy);
        // If current attribute is quasi-identifying
        if (definition.getAttributeType(attribute) instanceof Hierarchy) {
            model.getInputConfig()
                 .getInput()
                 .getDefinition()
                 .setAttributeType(attribute, h);
            controller.update(new ModelEvent(this,
                                             EventTarget.ATTRIBUTE_TYPE,
                                             attribute));
        }

        // If current attribute is sensitive
        if (definition.getAttributeType(attribute) == AttributeType.SENSITIVE_ATTRIBUTE) {
            model.getInputConfig().setSensitiveHierarchy(h);
            controller.update(new ModelEvent(this,
                                             EventTarget.ATTRIBUTE_TYPE,
                                             attribute));
        }
    }

    @Override
    public void reset() {
        setHierarchy(Hierarchy.create());
        base.redraw();
    }

    @Override
    public void setHierarchy(final AttributeType.Hierarchy type) {

        hierarchy = type.getHierarchy();
        table.setRedraw(false);

        for (final TableColumn t : table.getColumns()) {
            t.dispose();
        }
        for (final TableItem i : table.getItems()) {
            i.dispose();
        }

        if ((type == null) || (type.getHierarchy() == null) ||
            (type.getHierarchy().length == 0)) {
            table.setRedraw(true);
            table.redraw();
            return;
        }

        final TableColumn[] column = new TableColumn[type.getHierarchy()[0].length];
        for (int i = 0; i < column.length; i++) {
            column[i] = new TableColumn(table, SWT.NONE);
            column[i].setText(Resources.getMessage("HierarchyView.1") + (i + 1)); //$NON-NLS-1$
            column[i].pack();
        }

        for (int i = 0; i < type.getHierarchy().length; i++) {
            final TableItem item = new TableItem(table, SWT.NONE);
            item.setBackground(Display.getCurrent()
                                      .getSystemColor(SWT.COLOR_GRAY));
            for (int j = 0; j < type.getHierarchy()[i].length; j++) {
                item.setText(j, type.getHierarchy()[i][j]);
            }
        }

        for (final TableColumn t : table.getColumns()) {
            t.pack();
        }

        table.setRedraw(true);
        table.redraw();

        pushHierarchy();
    }

    @Override
    public void setLayoutData(final Object d) {
        base.setLayoutData(d);
    }

    @Override
    public void update(final ModelEvent event) {

        if (event.target == EventTarget.HIERARCHY) {
            if (attribute.equals(model.getSelectedAttribute())) {
                setHierarchy((Hierarchy) event.data);
                base.setEnabled(true);
                base.redraw();
            }
        } else if (event.target == EventTarget.MODEL) {
            model = (Model) event.data;
        } else if (event.target == EventTarget.INPUT) {
            final DataDefinition d = model.getInputConfig()
                                          .getInput()
                                          .getDefinition();
            final AttributeType type = d.getAttributeType(attribute);
            if (type instanceof Hierarchy) {
                setHierarchy((Hierarchy) type);
                base.setEnabled(true);
                base.redraw();
            } else if ((type == AttributeType.SENSITIVE_ATTRIBUTE) &&
                       (model.getInputConfig().getSensitiveHierarchy() != null)) {
                setHierarchy(model.getInputConfig().getSensitiveHierarchy());
                base.setEnabled(true);
                base.redraw();
            } else {
                reset();
            }
        } else if (event.target == EventTarget.ATTRIBUTE_TYPE) {
            if (event.data.equals(this.attribute)) {
                pushHierarchy();
            }
        }
    }

    /** Updates the titles of the columns after an event */
    private void updateColumnTitles() {
        int idx = 0;
        for (final TableColumn col : table.getColumns()) {
            idx++;
            col.setText(Resources.getMessage("HierarchyView.17") + idx); //$NON-NLS-1$
        }
    }

    private void updateHierarchy() {
        final int rows = table.getItemCount();
        final int cols = table.getColumnCount();
        final String[][] s = new String[rows][cols];
        int idx = 0;
        for (final TableItem item : table.getItems()) {
            for (int i = 0; i < cols; i++) {
                s[idx][i] = item.getText(i);
            }
            idx++;
        }
        hierarchy = s;
        pushHierarchy();
    }

    /**
     * Updates the min and max combos
     */
    private void updateMinAndMax() {
        if (min == null) { return; }

        final List<String> minItems = new ArrayList<String>();
        final List<String> maxItems = new ArrayList<String>();
        minItems.add(ITEM_ALL);
        for (int i = 1; i <= table.getColumnCount(); i++) {
            minItems.add(String.valueOf(i));
            maxItems.add(String.valueOf(i));
        }
        maxItems.add(ITEM_ALL);

        int minIndex = min.getSelectionIndex();
        if (minIndex >= 0) {
            final String minSelection = min.getItem(minIndex);
            minIndex = minItems.indexOf(minSelection);
        }
        minIndex = minIndex >= 0 ? minIndex : 0;

        int maxIndex = max.getSelectionIndex();
        if (maxIndex >= 0) {
            final String maxSelection = max.getItem(maxIndex);
            maxIndex = maxItems.indexOf(maxSelection);
        }
        maxIndex = maxIndex >= 0 ? maxIndex : maxItems.size() - 1;

        if (minIndex > (maxIndex + 1)) {
            minIndex = maxIndex + 1;
        }

        min.setItems(minItems.toArray(new String[] {}));
        max.setItems(maxItems.toArray(new String[] {}));

        min.select(minIndex);
        max.select(maxIndex);
    }
}
