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
import org.deidentifier.arx.masking.Variable;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

/**
 * This view displays all available attributes and allows them to be configured for masking
 *
 * @author Karol Babioch
 */
public class ViewVariableConfiguration implements IView {

    private Controller controller;

    private Composite parentComposite;

    private TableViewer tableViewer;


    public ViewVariableConfiguration(final Composite parent, final Controller controller) {

        this.controller = controller;
        this.parentComposite = build(parent);

        this.controller.addListener(ModelPart.MASKING_VARIABLE, this);

    }

    private Composite build(Composite parent) {

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

                    // Add variable to model
                    controller.getModel().getMaskingModel().addVariable(new Variable(value));

                    // Send update message
                    controller.update(new ModelEvent(this, ModelPart.MASKING_VARIABLE, null));

                }

            }

        });
        bar.add("Remove variable", controller.getResources().getManagedImage("remove.png"), new Runnable() {

            @Override
            public void run() {

                controller.actionShowInfoDialog(controller.getResources().getShell(), "TODO", "Not yet implemented!");

            }

        });

        // Create title bar
        ComponentTitledFolder folder = new ComponentTitledFolder(parent, controller, bar, null);
        folder.setLayoutData(SWTUtil.createFillGridData());
        Composite composite = folder.createItem("Variables", null);
        composite.setLayout(SWTUtil.createGridLayout(1));
        folder.setSelection(0);

        tableViewer = SWTUtil.createTableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION);
        tableViewer.setContentProvider(new ArrayContentProvider());
        tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent arg0) {
                Variable var = (Variable) ((IStructuredSelection)tableViewer.getSelection()).getFirstElement();
                controller.update(new ModelEvent(this, ModelPart.MASKING_VARIABLE, var));
            }

        });

        Table table = tableViewer.getTable();
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        table.setLayoutData(SWTUtil.createFillGridData());

        TableViewerColumn col1 = new TableViewerColumn(tableViewer, SWT.NONE);
        col1.getColumn().setWidth(100);
        col1.getColumn().setText("Name");
        col1.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((Variable)element).getName();
            }
        });

        TableViewerColumn col2 = new TableViewerColumn(tableViewer, SWT.NONE);
        col2.getColumn().setWidth(100);
        col2.getColumn().setText("Distribution");
        col2.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return "";
            }
        });

        // Create horizontal separator
        Label separator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
        separator.setLayoutData(SWTUtil.createFillHorizontallyGridData());


        return composite;

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
        tableViewer.setInput(controller.getModel().getMaskingModel().getVariables());

        // Reenable redrawing
        tableViewer.getTable().setRedraw(true);

    }

}
