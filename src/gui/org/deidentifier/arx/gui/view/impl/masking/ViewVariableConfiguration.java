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
import org.deidentifier.arx.masking.RandomVariable;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * This view displays all available attributes and allows them to be configured for masking
 *
 * @author Karol Babioch
 */
public class ViewVariableConfiguration implements IView {

    private Controller controller;

    private TableViewer tableViewer;


    private class NameEditingSupport extends EditingSupport {

        private TextCellEditor editor;

        public NameEditingSupport(TableViewer viewer) {

            super(viewer);
            editor = new TextCellEditor(viewer.getTable());

        }

        @Override
        protected boolean canEdit(Object arg0) {

            return true;

        }

        @Override
        protected CellEditor getCellEditor(Object element) {

            return editor;

        }

        @Override
        protected Object getValue(Object element) {

            return ((RandomVariable)element).getName();

        }

        @Override
        protected void setValue(Object element, Object value) {

            RandomVariable variable = ((RandomVariable)element);
            variable.setName((String)value);

            // Send notification about update
            controller.update(new ModelEvent(this, ModelPart.MASKING_CONFIGURATION_FOR_VARIABLE_CHANGED, variable));

        }

    }

    private class DistributionEditingSupport extends EditingSupport {

        private ComboBoxCellEditor editor;

        private String[] choices = new String[] { "Binomial", "Geometric", };

        public DistributionEditingSupport(TableViewer viewer) {

            super(viewer);

            editor = new ComboBoxCellEditor(viewer.getTable(), choices, SWT.READ_ONLY);

        }

        @Override
        protected boolean canEdit(Object arg0) {

            return true;

        }

        @Override
        protected CellEditor getCellEditor(Object element) {

            return editor;

        }

        @Override
        protected Object getValue(Object element) {

            return 0;

        }

        @Override
        protected void setValue(Object element, Object value) {

            RandomVariable variable = (RandomVariable)element;
            variable.setDistribution(choices[(int)value]);
            controller.update(new ModelEvent(this, ModelPart.MASKING_CONFIGURATION_FOR_VARIABLE_CHANGED, variable));

        }

    }


    public ViewVariableConfiguration(final Composite parent, final Controller controller) {

        this.controller = controller;

        build(parent);

        this.controller.addListener(ModelPart.MASKING_CONFIGURATION_FOR_VARIABLE_CHANGED, this);

    }

    private void build(Composite parent) {

        // Create button bar
        ComponentTitledFolderButtonBar bar = new ComponentTitledFolderButtonBar(null); // TODO Assign help id
        bar.add("Add variable", controller.getResources().getManagedImage("add.png"), new Runnable() {

            @Override
            public void run() {

                // Ask user for variable name
                Shell shell = controller.getResources().getShell();
                String value = controller.actionShowInputDialog(shell, "Add a variable", "Please enter a variable name", "");

                // Check if valid name was entered
                if (value != null && value.length() > 0) {

                    // Create new variable
                    RandomVariable variable = new RandomVariable(value, "Binomial");

                    // Add variable to model
                    controller.getModel().getMaskingModel().addRandomVariable(variable);

                    // Send notification about update
                    controller.update(new ModelEvent(this, ModelPart.MASKING_CONFIGURATION_FOR_VARIABLE_CHANGED, variable));

                }

            }

        });
        bar.add("Remove variable", controller.getResources().getManagedImage("remove.png"), new Runnable() {

            @Override
            public void run() {

                // Check if one variable was selected
                if (((IStructuredSelection)tableViewer.getSelection()).size() != 1) {

                    return;

                }

                // Get currently selected variable
                RandomVariable variable = (RandomVariable) ((IStructuredSelection)tableViewer.getSelection()).getFirstElement();

                // Remove from controller
                controller.getModel().getMaskingModel().removeRandomVariable((variable));

                // Send notification about update
                controller.update(new ModelEvent(this, ModelPart.MASKING_CONFIGURATION_FOR_VARIABLE_CHANGED, variable));

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

        Table table = tableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setLayoutData(SWTUtil.createFillGridData());
        table.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {

                RandomVariable variable = (RandomVariable) ((IStructuredSelection)tableViewer.getSelection()).getFirstElement();

                // Send notification
                controller.update(new ModelEvent(this, ModelPart.MASKING_VARIABLE_SELECTED, variable));

            }

        });


        // Column containing variable name
        TableViewerColumn tableViewerColumnName = new TableViewerColumn(tableViewer, SWT.NONE);
        tableViewerColumnName.setEditingSupport(new NameEditingSupport(tableViewer));
        tableViewerColumnName.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {

                return ((RandomVariable)element).getName();

            }

        });

        TableColumn columnName = tableViewerColumnName.getColumn();
        columnName.setToolTipText("Name of the variable");
        columnName.setText("Variable");
        columnName.setWidth(150);


        // Column containing distribution type
        TableViewerColumn tableViewerColumnDistribution = new TableViewerColumn(tableViewer, SWT.NONE);
        tableViewerColumnDistribution.setEditingSupport(new DistributionEditingSupport(tableViewer));
        tableViewerColumnDistribution.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {

                return ((RandomVariable)element).getDistribution();

            }

        });

        TableColumn columnDistribution = tableViewerColumnDistribution.getColumn();
        columnDistribution.setToolTipText("Distribution of the variable");
        columnDistribution.setText("Distribution");
        columnDistribution.setWidth(150);

    }

    @Override
    public void dispose() {

        controller.removeListener(this);

    }

    @Override
    public void reset() {

        tableViewer.getTable().clearAll();

    }

    @Override
    public void update(ModelEvent event) {

        // Disable redrawing, so changes won't be noticed by the user and appear to be atomic
        tableViewer.getTable().setRedraw(false);

        // Remove all data
        tableViewer.getTable().removeAll();

        // Apply new data
        tableViewer.setInput(controller.getModel().getMaskingModel().getRandomVariables());

        // Reenable redrawing
        tableViewer.getTable().setRedraw(true);

    }

}
