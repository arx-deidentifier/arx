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

package org.deidentifier.arx.gui.view.impl.common;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.def.IView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * This class implements an a menu for the editor for generalization hierarchies.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 * @author Ledian Xhani
 * @author Ljubomir Dshevlekov
 * 
 */
public class ComponentHierarchyMenu implements IView {

    /** Model */
    private Model              model;

    /** Menu. */
    private Menu               menu;

    /** Hierarchy. */
    private ComponentHierarchy hierarchy;

    /** Controller. */
    private Controller         controller;

    /** Item. */
    private MenuItem           itemInsertRow;

    /** Item. */
    private MenuItem           itemDeleteRow;

    /** Item. */
    private MenuItem           itemInsertColumn;

    /** Item. */
    private MenuItem           itemDeleteColumn;

    /** Item. */
    private MenuItem           itemMoveRowUp;

    /** Item. */
    private MenuItem           itemMoveRowDown;

    /** Item. */
    private MenuItem           itemEditItem;

    /** Item. */
    private MenuItem           itemRenameItem;

    /** Item. */
    private MenuItem           itemClear;

    /** Item. */
    private MenuItem           itemInitialize;

    /** Item. */
    private MenuItem           itemTopBottomCoding;

    /** Item. */
    private MenuItem           itemAttributeSuppression;
    
    /**
     * Creates a new instance
     * 
     * @param hierarchy
     * @param controller
     */
    public ComponentHierarchyMenu(ComponentHierarchy hierarchy,
                                  Controller controller) {
        
        // Init
        this.hierarchy = hierarchy;
        this.controller = controller;
        this.controller.addListener(ModelPart.MODEL, this);
        this.createMenu();

        // Listen
        this.hierarchy.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(final MouseEvent e) {
                if (e.button == 3) {
                    onMouseDown(ComponentHierarchyMenu.this.hierarchy.getControl().toDisplay(e.x, e.y));
                }
            }
        });
    }
    

    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    @Override
    public void reset() {
        // Empty by design
    }

    @Override
    public void update(ModelEvent event) {
        if (event.part == ModelPart.MODEL) {
            this.model = (Model) event.data;
        }
    }
    
    /**
     * Checks and asks users whether functional hierarchies should be removed
     * @return
     */
    private boolean check() {
        if (model != null && model.getInputConfig() != null && model.getSelectedAttribute() != null) {
            if (model.getInputConfig().getHierarchyBuilder(model.getSelectedAttribute()) != null) {
                return controller.actionShowQuestionDialog(Resources.getMessage("HierarchyView.20"),  //$NON-NLS-1$
                                                                   Resources.getMessage("HierarchyView.21")); //$NON-NLS-1$
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    /**
     * Creates all components required for making the table editable.
     */
    private void createMenu() {
        
        // Creates the editors menu
        this.menu = new Menu(hierarchy.getControl());
        
        // Insert row action
        itemInsertRow = new MenuItem(menu, SWT.NONE);
        itemInsertRow.setText(Resources.getMessage("HierarchyView.7")); //$NON-NLS-1$
        itemInsertRow.addSelectionListener(new SelectionAdapter() {
            @Override public void widgetSelected(final SelectionEvent e) {
                if (check()) {
                    hierarchy.actionInsertRow();
                }
            }
        });
        
        // Delete row action
        itemDeleteRow = new MenuItem(menu, SWT.NONE);
        itemDeleteRow.setText(Resources.getMessage("HierarchyView.8")); //$NON-NLS-1$
        itemDeleteRow.addSelectionListener(new SelectionAdapter() {
            @Override public void widgetSelected(final SelectionEvent e) {
                if (check()) {
                    hierarchy.actionDeleteRow();
                }
            }
        });
        
        // Separator
        new MenuItem(menu, SWT.SEPARATOR);
        
        // Insert column action
        itemInsertColumn = new MenuItem(menu, SWT.NONE);
        itemInsertColumn.setText(Resources.getMessage("HierarchyView.9")); //$NON-NLS-1$
        itemInsertColumn.addSelectionListener(new SelectionAdapter() {
            @Override public void widgetSelected(final SelectionEvent e) {
                if (check()) {
                    hierarchy.actionInsertColumn();
                }
            }
        });
        
        // Delete column action
        itemDeleteColumn = new MenuItem(menu, SWT.NONE);
        itemDeleteColumn.setText(Resources.getMessage("HierarchyView.10")); //$NON-NLS-1$
        itemDeleteColumn.addSelectionListener(new SelectionAdapter() {
            @Override public void widgetSelected(final SelectionEvent e) {
                if (check()) {
                    hierarchy.actionDeleteColumn();
                }
            }
        });
        
        // Separator
        new MenuItem(menu, SWT.SEPARATOR);
        
        // Move up
        itemMoveRowUp = new MenuItem(menu, SWT.NONE);
        itemMoveRowUp.setText(Resources.getMessage("HierarchyView.11")); //$NON-NLS-1$
        itemMoveRowUp.addSelectionListener(new SelectionAdapter() {
            @Override public void widgetSelected(final SelectionEvent e) {
                if (check()) {
                    hierarchy.actionMoveRowUp();
                }
            }
        });
        
        // Move down
        itemMoveRowDown = new MenuItem(menu, SWT.NONE);
        itemMoveRowDown.setText(Resources.getMessage("HierarchyView.12")); //$NON-NLS-1$
        itemMoveRowDown.addSelectionListener(new SelectionAdapter() {
            @Override public void widgetSelected(final SelectionEvent e) {
                if (check()) {
                    hierarchy.actionMoveRowDown();
                }
            }
        });
        
        // Separator
        new MenuItem(menu, SWT.SEPARATOR);
        
        // Edit item action
        itemEditItem = new MenuItem(menu, SWT.NONE);
        itemEditItem.setText(Resources.getMessage("HierarchyView.18")); //$NON-NLS-1$
        itemEditItem.addSelectionListener(new SelectionAdapter() {
            @Override public void widgetSelected(final SelectionEvent e) {
                if (check()) {
                    String value = getUserValue();
                    if (value != null) {
                        hierarchy.actionEditItem(value);
                    }
                }
            }
        });
        
        // Rename item action
        itemRenameItem = new MenuItem(menu, SWT.NONE);
        itemRenameItem.setText(Resources.getMessage("HierarchyView.15")); //$NON-NLS-1$
        itemRenameItem.addSelectionListener(new SelectionAdapter() {
            @Override public void widgetSelected(final SelectionEvent e) {
                if (check()) {
                    String value = getUserValue();
                    if (value != null) {
                        hierarchy.actionRenameItem(value);
                    }
                }
            }
        });
        
        // Separator
        new MenuItem(menu, SWT.SEPARATOR);
        
        // Action clear
        itemClear = new MenuItem(menu, SWT.NONE);
        itemClear.setText(Resources.getMessage("HierarchyView.16")); //$NON-NLS-1$
        itemClear.addSelectionListener(new SelectionAdapter() {
            @Override public void widgetSelected(final SelectionEvent e) {
                if (check()) {
                    hierarchy.actionClear();
                }
            }
        });
        
        // Action intialize
        itemInitialize = new MenuItem(menu, SWT.NONE);
        itemInitialize.setText(Resources.getMessage("HierarchyView.19")); //$NON-NLS-1$
        itemInitialize.addSelectionListener(new SelectionAdapter() {
            @Override public void widgetSelected(final SelectionEvent e) {
                if (check()) {
                    if (hierarchy.isRowSelected() || hierarchy.isColumnSelected() ||
                        hierarchy.isCellSelected() || model == null || model.getInputConfig() == null ||
                        model.getInputConfig().getInput() == null ||
                        model.getSelectedAttribute() == null) { return; }
    
                    controller.actionMenuEditInitializeHierarchy();
                }
            }
        });
        
        // Action top/bottom coding
        itemTopBottomCoding = new MenuItem(menu, SWT.NONE);
        itemTopBottomCoding.setText(Resources.getMessage("HierarchyView.22")); //$NON-NLS-1$
        itemTopBottomCoding.addSelectionListener(new SelectionAdapter() {
            @Override public void widgetSelected(final SelectionEvent e) {
                if (check()) {
                    if (hierarchy.isRowSelected() || hierarchy.isColumnSelected() ||
                        hierarchy.isCellSelected() || model == null || model.getInputConfig() == null ||
                        model.getInputConfig().getInput() == null ||
                        model.getSelectedAttribute() == null) { return; }
    
                    controller.actionMenuEditCreateTopBottomCodingHierarchy();
                }
            }
        });

        // Action attribute suppression
        itemAttributeSuppression = new MenuItem(menu, SWT.NONE);
        itemAttributeSuppression.setText(Resources.getMessage("HierarchyView.23")); //$NON-NLS-1$
        itemAttributeSuppression.addSelectionListener(new SelectionAdapter() {
            @Override public void widgetSelected(final SelectionEvent e) {
                if (check()) {
                    if (hierarchy.isRowSelected() || hierarchy.isColumnSelected() ||
                        hierarchy.isCellSelected() || model == null || model.getInputConfig() == null ||
                        model.getInputConfig().getInput() == null ||
                        model.getSelectedAttribute() == null) { return; }
    
                    controller.actionMenuEditCreateAttributeSuppressionHierarchy();
                }
            }
        });
    }

    /**
     * Queries the user for a new value
     * @return
     */
    private String getUserValue() {
        
        String current = hierarchy.getSelectedValue();
        return controller.actionShowInputDialog(controller.getResources().getShell(),
                                                Resources.getMessage("HierarchyView.13"), //$NON-NLS-1$
                                                Resources.getMessage("HierarchyView.14"), current); //$NON-NLS-1$
    }

    /**
     * Mouse down action.
     *
     * @param point
     */
    private void onMouseDown(Point point) {
        
        // Init
        boolean cell = hierarchy.isCellSelected();
        boolean row = hierarchy.isRowSelected();
        boolean column = hierarchy.isColumnSelected();
        
        // Update menu items
        itemInsertRow.setEnabled(row || cell);
        itemDeleteRow.setEnabled(row || cell);
        itemMoveRowUp.setEnabled(row || cell);
        itemMoveRowDown.setEnabled(row || cell);
        // ---------
        itemInsertColumn.setEnabled(column || cell);
        itemDeleteColumn.setEnabled(column || cell);
        // ---------
        itemEditItem.setEnabled(cell);
        itemRenameItem.setEnabled(cell);
        // ---------
        itemClear.setEnabled(cell || row || column);
        itemInitialize.setEnabled(hierarchy.isEmpty());
        itemTopBottomCoding.setEnabled(hierarchy.isEmpty());
        itemAttributeSuppression.setEnabled(hierarchy.isEmpty());
        
        // Show
        this.menu.setLocation(point);
        this.menu.setVisible(true);
    }
}
