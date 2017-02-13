/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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

package org.deidentifier.arx.gui.view.impl.masking;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolder;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolderButtonBar;
import org.deidentifier.arx.gui.view.impl.menu.DialogVariableConfiguration;
import org.deidentifier.arx.masking.RandomVariable;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolItem;

/**
 * This view displays all random variables and allows them to be configured (distribution, parameters, etc.)
 *
 * @author Karol Babioch
 */
public class ViewVariableConfiguration implements IView {

    private Controller controller;

    private TableViewer tableViewer;

    private ToolItem buttonAdd;
    private ToolItem buttonRemove;
    private ToolItem buttonEdit;


    public ViewVariableConfiguration(final Composite parent, final Controller controller) {

        this.controller = controller;
        build(parent);

        controller.addListener(ModelPart.MASKING_VARIABLE_CHANGED, this);

    }

    private void build(Composite parent) {

        // Create button bar
        ComponentTitledFolderButtonBar bar = new ComponentTitledFolderButtonBar(null); // TODO Assign help id
        bar.add("Add variable", controller.getResources().getManagedImage("add.png"), new Runnable() {

            @Override
            public void run() {

                // Open dialog to configure new variable
                new DialogVariableConfiguration(controller).open();

            }

        });
        bar.add("Remove variable", controller.getResources().getManagedImage("remove.png"), new Runnable() {

            @Override
            public void run() {

                // Get currently selected variable
                RandomVariable variable = (RandomVariable) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();

                // Remove from controller
                controller.getModel().getMaskingModel().removeRandomVariable((variable));

                // Send notification about update
                controller.update(new ModelEvent(this, ModelPart.MASKING_VARIABLE_CHANGED, variable));

            }

        });
        bar.add("Edit variable", controller.getResources().getManagedImage("edit.png"), new Runnable() {

            @Override
            public void run() {

                // Get currently selected variable
                RandomVariable variable = (RandomVariable) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();

                // Open dialog to edit existing variable
                new DialogVariableConfiguration(controller, variable).open();

            }

        });

        // Create title bar
        ComponentTitledFolder folder = new ComponentTitledFolder(parent, controller, bar, null);
        folder.setLayoutData(SWTUtil.createFillGridData());
        Composite composite = folder.createItem("Variable configuration", null);
        composite.setLayout(SWTUtil.createGridLayout(1));
        folder.setSelection(0);

        // Create table
        tableViewer = SWTUtil.createTableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION);
        tableViewer.setContentProvider(new ArrayContentProvider());
        tableViewer.addDoubleClickListener(new IDoubleClickListener() {

            @Override
            public void doubleClick(DoubleClickEvent event) {

                // Get currently selected variable
                RandomVariable variable = (RandomVariable) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();

                // Edit config dialog for variable
                new DialogVariableConfiguration(controller, variable).open();

            }
        });

        Table table = tableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setLayoutData(SWTUtil.createFillGridData());
        table.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {

                RandomVariable variable = (RandomVariable) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();

                // Update button status
                updateButtons();

                // Send notification
                controller.update(new ModelEvent(this, ModelPart.MASKING_VARIABLE_SELECTED, variable));

            }

        });

        // Column containing variable name
        TableViewerColumn tableViewerColumnName = new TableViewerColumn(tableViewer, SWT.NONE);
        tableViewerColumnName.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {

                return ((RandomVariable) element).getName();

            }

        });

        TableColumn columnName = tableViewerColumnName.getColumn();
        columnName.setToolTipText("Name of the variable");
        columnName.setText("Variable");
        columnName.setWidth(150);

        // Column containing distribution type
        TableViewerColumn tableViewerColumnDistribution = new TableViewerColumn(tableViewer, SWT.NONE);
        tableViewerColumnDistribution.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {

                return ((RandomVariable) element).getDistribution();

            }

        });

        TableColumn columnDistribution = tableViewerColumnDistribution.getColumn();
        columnDistribution.setToolTipText("Distribution of the variable");
        columnDistribution.setText("Distribution");
        columnDistribution.setWidth(150);

        // Set default status for buttons
        buttonAdd = folder.getButtonItem("Add variable");
        buttonRemove = folder.getButtonItem("Remove variable");
        buttonEdit = folder.getButtonItem("Edit variable");
        updateButtons();

    }

    @Override
    public void dispose() {

        controller.removeListener(this);

    }

    @Override
    public void reset() {

        tableViewer.getTable().clearAll();

    }

    // TODO Maybe make this more effective by only updating affected row?
    @Override
    public void update(ModelEvent event) {

        // Disable redrawing, so changes won't be noticed by the user and appear to be atomic
        tableViewer.getTable().setRedraw(false);

        // Save selection
        ISelection selection = tableViewer.getSelection();

        // Remove all data
        tableViewer.getTable().removeAll();

        // Apply new data
        tableViewer.setInput(controller.getModel().getMaskingModel().getRandomVariables());

        // Restore selection
        tableViewer.setSelection(selection, true);

        // Reenable redrawing
        tableViewer.getTable().setRedraw(true);

        // Check whether buttons need to be enabled or disabled
        updateButtons();

    }

    private void updateButtons() {

        // Refresh data
        tableViewer.refresh();

        // Always enabled
        buttonAdd.setEnabled(true);

        // Only enable when something is selected
        boolean enableButtons = !tableViewer.getSelection().isEmpty();

        buttonRemove.setEnabled(enableButtons);
        buttonEdit.setEnabled(enableButtons);

    }

}
