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

package org.deidentifier.arx.gui.view.impl.masking;

import java.util.Map;
import java.util.Map.Entry;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolder;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolderButtonBar;
import org.deidentifier.arx.gui.view.impl.menu.DialogVariableConfiguration;
import org.deidentifier.arx.masking.AttributeParameters;
import org.deidentifier.arx.masking.MaskingConfiguration;
import org.deidentifier.arx.masking.variable.DistributionParameter;
import org.deidentifier.arx.masking.variable.RandomVariable;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
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

    /** Button */
    private ToolItem    buttonAdd;

    /** Button */
    private ToolItem    buttonEdit;

    /** Button */
    private ToolItem    buttonRemove;

    /** Controller */
    private Controller  controller;

    /** Table viewer */
    private TableViewer tableViewer;

    /**
     * Creates an instance.
     * 
     * @param parent
     * @param controller
     */
    public ViewVariableConfiguration(final Composite parent, final Controller controller) {

        this.controller = controller;
        build(parent);

        controller.addListener(ModelPart.RANDOM_VARIABLE, this);

    }

    /**
     * Build component.
     * 
     * @param parent
     */
    private void build(Composite parent) {

        // Create button bar
        ComponentTitledFolderButtonBar bar = new ComponentTitledFolderButtonBar(null); // TODO Assign help id
        bar.add(Resources.getMessage("VariableConfigurationView.0"), controller.getResources().getManagedImage("add.png"), new Runnable() { //$NON-NLS-1$ FS

            @Override
            public void run() {
                // Open dialog to configure new variable
                DialogVariableConfiguration config = new DialogVariableConfiguration(controller);
                config.setBlockOnOpen(true);
                if (config.open() == Window.OK)
                    tableViewer.getTable().select(tableViewer.getTable().getItemCount() - 1);

            }

        });
        bar.add(Resources.getMessage("VariableConfigurationView.1"), controller.getResources().getManagedImage("remove.png"), new Runnable() { //$NON-NLS-1$

            @Override
            public void run() {

                // Get currently selected variable
                RandomVariable variable = (RandomVariable) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();

                // Delete distribution mapped to selected attribute
                int deletedIndex = -1;
                Map<String, AttributeParameters> entries = MaskingConfiguration.getMapping();
                for (Entry<String, AttributeParameters> entry : entries.entrySet()) {
                    int value = entry.getValue().getDistributionIndex();
                    if (tableViewer.getTable().getSelectionIndex() == value - 1) {
                        MaskingConfiguration.removeMasking(entry.getKey());
                        deletedIndex = value - 1;
                        break; // TODO: this only removes the first masking using the deleted distribution, remove break if multiple maskings can use the same distribution
                    }
                }
                // Update the indices after an attribute has been removed
                // (another for loop because the map is ordered alphabetically, not by Distributionindex)
                for (Entry<String, AttributeParameters> entry : entries.entrySet()) {
                    int value = entry.getValue().getDistributionIndex();
                    // Compared to Value-1 due to "Identity" being index 0
                    if (deletedIndex >= 0 && deletedIndex < entries.size() - 1 && value - 1 > deletedIndex) {
                        entry.getValue().setDistribution(value - 1);
                    }
                }
                // selects the first item in the table, after deletion
                if (tableViewer.getTable().getItemCount() > 1) {
                    if (tableViewer.getTable().getSelectionIndex() > 0)
                        tableViewer.getTable().setSelection(0);
                    else
                        tableViewer.getTable().setSelection(1);
                }

                // Update button status
                updateButtons();

                // Remove from controller
                controller.getModel().getMaskingModel().removeRandomVariable((variable));

                // update VariableConfiguration View List
                controller.update(new ModelEvent(this, ModelPart.RANDOM_VARIABLE, (RandomVariable) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement()));

            }

        });
        bar.add(Resources.getMessage("VariableConfigurationView.2"), controller.getResources().getManagedImage("edit.png"), new Runnable() { //$NON-NLS-1$

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
        Composite composite = folder.createItem(Resources.getMessage("MaskingView.3"), null); //$NON-NLS-1$
        composite.setLayout(SWTUtil.createGridLayout(1));
        folder.setSelection(0);

        // Get references to buttons
        buttonAdd = folder.getButtonItem(Resources.getMessage("VariableConfigurationView.0")); //$NON-NLS-1$
        buttonRemove = folder.getButtonItem(Resources.getMessage("VariableConfigurationView.1")); //$NON-NLS-1$
        buttonEdit = folder.getButtonItem(Resources.getMessage("VariableConfigurationView.2")); //$NON-NLS-1$

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

                // Update button status
                updateButtons();

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
        columnName.setToolTipText(Resources.getMessage("VariableConfigurationView.3")); //$NON-NLS-1$
        columnName.setText(Resources.getMessage("VariableConfigurationView.4")); //$NON-NLS-1$
        columnName.setWidth(150);

        // Column containing distribution type
        TableViewerColumn tableViewerColumnDistribution = new TableViewerColumn(tableViewer, SWT.NONE);
        tableViewerColumnDistribution.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {
                return ((RandomVariable) element).getDistributionType().getDescription().getLabel();
            }

        });

        TableColumn columnDistribution = tableViewerColumnDistribution.getColumn();
        columnDistribution.setToolTipText(Resources.getMessage("VariableConfigurationView.5")); //$NON-NLS-1$
        columnDistribution.setText(Resources.getMessage("VariableConfigurationView.6")); //$NON-NLS-1$
        columnDistribution.setWidth(300);

        // Column displaying number
        TableViewerColumn tableViewerColumnNumber = new TableViewerColumn(tableViewer, SWT.NONE);
        tableViewerColumnNumber.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {
                return getParameterValue(((RandomVariable) element).getParameter("number"));
            }

        });

        TableColumn columnNumber = tableViewerColumnNumber.getColumn();
        columnNumber.setToolTipText(Resources.getMessage("VariableConfigurationView.7")); //$NON-NLS-1$
        columnNumber.setText(Resources.getMessage("VariableConfigurationView.8")); //$NON-NLS-1$
        columnNumber.setWidth(50);

        // Column displaying probability
        TableViewerColumn tableViewerColumnProbability = new TableViewerColumn(tableViewer, SWT.NONE);
        tableViewerColumnProbability.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {
                return getParameterValue(((RandomVariable) element).getParameter("probability"));
            }

        });

        TableColumn columnProbability = tableViewerColumnProbability.getColumn();
        columnProbability.setToolTipText(Resources.getMessage("VariableConfigurationView.9")); //$NON-NLS-1$
        columnProbability.setText(Resources.getMessage("VariableConfigurationView.10")); //$NON-NLS-1$
        columnProbability.setWidth(50);

        // Set default status for buttons
        updateButtons();

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.arx.gui.view.def.IView#dispose()
     */
    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    /**
     * Returns value in case it exists, or empty string otherwise
     * @param parameter
     * @return
     */
    private String getParameterValue(DistributionParameter<?> parameter) {

        if (parameter != null) {
            return String.valueOf(parameter.getValue());
        }

        return "";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.arx.gui.view.def.IView#reset()
     */
    @Override
    public void reset() {
        tableViewer.getTable().clearAll();
    }

    // TODO Maybe make this more effective by only updating affected row?
    @Override
    public void update(ModelEvent event) {

        if (event.part == ModelPart.RANDOM_VARIABLE) {

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

            // Set default status for buttons
            updateButtons();

        }

    }

    /**
     * Update buttons.
     */
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
