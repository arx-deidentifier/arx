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
        bar.add(Resources.getMessage("VariableConfigurationView.0"), controller.getResources().getManagedImage("add.png"), new Runnable() {

            @Override
            public void run() {

                // Open dialog to configure new variable
                new DialogVariableConfiguration(controller).open();

            }

        });
        bar.add(Resources.getMessage("VariableConfigurationView.1"), controller.getResources().getManagedImage("remove.png"), new Runnable() {

            @Override
            public void run() {

                // Get currently selected variable
                RandomVariable variable = (RandomVariable) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();
                
                // Delete distribution mapped to selected attribute
                int deletedIndex=-1;
                Map<String,AttributeParameters> entries = MaskingConfiguration.getMapping();
                for (Entry<String,AttributeParameters> entry : entries.entrySet())
                {
                	int value = entry.getValue().getDistributionIndex();
                	if (tableViewer.getTable().getSelectionIndex() == value) 
                	{
                		MaskingConfiguration.removeMasking(entry.getKey());
                		deletedIndex = value;
                		break;
                	}
                }
                //Update the indices after an attribute has been removed 
                //(another for loop because the map is ordered alphabetically, not by Distributionindex)
                for (Entry<String,AttributeParameters> entry : entries.entrySet())
                {
                	int value = entry.getValue().getDistributionIndex();
                	if (deletedIndex>=0 && deletedIndex<entries.size()-1 && value>deletedIndex)
                	{
                		entry.getValue().setDistribution(value-1);
                	}
                }
            	controller.update(new ModelEvent(this, ModelPart.MASKING_ATTRIBUTE_CHANGED, null));
                
                // Remove from controller
                controller.getModel().getMaskingModel().removeRandomVariable((variable));

                // Send notification about update
                controller.update(new ModelEvent(this, ModelPart.MASKING_VARIABLE_CHANGED, variable));

            }

        });
        bar.add(Resources.getMessage("VariableConfigurationView.2"), controller.getResources().getManagedImage("edit.png"), new Runnable() {

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
        Composite composite = folder.createItem(Resources.getMessage("MaskingView.3"), null);
        composite.setLayout(SWTUtil.createGridLayout(1));
        folder.setSelection(0);

        // Get references to buttons
        buttonAdd = folder.getButtonItem(Resources.getMessage("VariableConfigurationView.0"));
        buttonRemove = folder.getButtonItem(Resources.getMessage("VariableConfigurationView.1"));
        buttonEdit = folder.getButtonItem(Resources.getMessage("VariableConfigurationView.2"));

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
        columnName.setText("Name");
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
        columnDistribution.setToolTipText("Distribution of the variable");
        columnDistribution.setText("Distribution");
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
        columnNumber.setToolTipText("Number");
        columnNumber.setText("N");
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
        columnProbability.setToolTipText("Probability");
        columnProbability.setText("P");
        columnProbability.setWidth(50);

        // Column displaying mean
        TableViewerColumn tableViewerColumnMean = new TableViewerColumn(tableViewer, SWT.NONE);
        tableViewerColumnMean.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {

                return getParameterValue(((RandomVariable) element).getParameter("mean"));

            }

        });

//        TableColumn columnMean = tableViewerColumnMean.getColumn();
//        columnMean.setToolTipText("Mean");
//        columnMean.setText("μ");
//        columnMean.setWidth(50);
//
//        // Column displaying standard deviation
//        TableViewerColumn tableViewerStdDev = new TableViewerColumn(tableViewer, SWT.NONE);
//        tableViewerStdDev.setLabelProvider(new ColumnLabelProvider() {
//
//            @Override
//            public String getText(Object element) {
//
//                return getParameterValue(((RandomVariable) element).getParameter("stddev"));
//
//            }
//
//        });
//
//        TableColumn columnStdDev = tableViewerStdDev.getColumn();
//        columnStdDev.setToolTipText("Standard deviation");
//        columnStdDev.setText("σ");
//        columnStdDev.setWidth(50);
//
//        // Column displaying location parameter
//        TableViewerColumn tableViewerLocation = new TableViewerColumn(tableViewer, SWT.NONE);
//        tableViewerLocation.setLabelProvider(new ColumnLabelProvider() {
//
//            @Override
//            public String getText(Object element) {
//
//                return getParameterValue(((RandomVariable) element).getParameter("location"));
//
//            }
//
//        });
//
//        TableColumn columnLocation = tableViewerLocation.getColumn();
//        columnLocation.setToolTipText("Location");
//        columnLocation.setText("x");
//        columnLocation.setWidth(50);
//
//        // Column displaying scale parameter
//        TableViewerColumn tableViewerScale = new TableViewerColumn(tableViewer, SWT.NONE);
//        tableViewerScale.setLabelProvider(new ColumnLabelProvider() {
//
//            @Override
//            public String getText(Object element) {
//
//                return getParameterValue(((RandomVariable) element).getParameter("scale"));
//
//            }
//
//        });
//
//        TableColumn columnScale = tableViewerScale.getColumn();
//        columnScale.setToolTipText("Scale");
//        columnScale.setText("γ");
//        columnScale.setWidth(50);
//
//        // Column displaying degrees of freedom
//        TableViewerColumn tableViewerDegrees = new TableViewerColumn(tableViewer, SWT.NONE);
//        tableViewerDegrees.setLabelProvider(new ColumnLabelProvider() {
//
//            @Override
//            public String getText(Object element) {
//
//                return getParameterValue(((RandomVariable) element).getParameter("degrees"));
//
//            }
//
//        });
//
//        TableColumn columnDegrees = tableViewerDegrees.getColumn();
//        columnDegrees.setToolTipText("Degrees of freedom");
//        columnDegrees.setText("k");
//        columnDegrees.setWidth(50);
//
//        // Column displaying rate
//        TableViewerColumn tableViewerRate = new TableViewerColumn(tableViewer, SWT.NONE);
//        tableViewerRate.setLabelProvider(new ColumnLabelProvider() {
//
//            @Override
//            public String getText(Object element) {
//
//                return getParameterValue(((RandomVariable) element).getParameter("rate"));
//
//            }
//
//        });
//
//        TableColumn columnRate = tableViewerRate.getColumn();
//        columnRate.setToolTipText("Rate");
//        columnRate.setText("λ");
//        columnRate.setWidth(50);

        // Set default status for buttons
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

        // Set default status for buttons
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

    // Returns value in case it exists, or empty string otherwise
    private String getParameterValue(DistributionParameter<?> parameter) {

        if (parameter != null) {

            return String.valueOf(parameter.getValue());

        }

        return "";

    }

}
