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

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolder;
import org.deidentifier.arx.masking.MaskingConfiguration;
import org.deidentifier.arx.masking.MaskingType;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * This view displays all available attributes and allows to choose a masking operation for them
 *
 * @author Karol Babioch
 * @author Sandro Schaeffler
 * @author Peter Bock
 */
public class ViewAttributeConfiguration implements IView {

    /**
     * Container describing an attribute
     *
     * @author Karol Babioch
     */
    public class Attribute {

        /** Attribute name */
        private String      name;

        /** Attribute type */
        private DataType<?> type;

        /**
         * Creates an instance.
         * 
         * @param name
         * @param type
         */
        public Attribute(String name, DataType<?> type) {
            this.name = name;
            this.type = type;
        }

        /**
         * Tests equality on the attribute name.
         * 
         * @param attribute
         * @return
         */
        public boolean equals(String attribute) {
            if (name.equals(attribute)) {
                return true;
            } else {
                return false;
            }
        }

        /**
         * Returns the name.
         * @return
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the data type.
         * @return
         */
        public DataType<?> getType() {
            return type;
        }

    }

    /**
     * Content provider for attributes
     *
     * This content provider retrieves the available attributes from the model and iterates over them in order to
     * retrieve the required information.
     *
     * @author Karol Babioch
     */
    private class AttributeContentProvider implements IStructuredContentProvider {

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.IContentProvider#dispose()
         */
        @Override
        public void dispose() {
            // Nothing to do
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
         */
        @Override
        public Object[] getElements(Object inputData) {

            Model model = (Model) inputData;
            List<Attribute> list = new ArrayList<Attribute>();

            try {

                DataHandle data = model.getInputConfig().getInput().getHandle();

                // For each column
                for (int i = 0; i < data.getNumColumns(); i++) {

                    String attributeName = data.getAttributeName(i);
                    AttributeType attributeType = model.getInputDefinition().getAttributeType(attributeName);

                    // Skip if attribute is not identifying
                    if (attributeType != AttributeType.IDENTIFYING_ATTRIBUTE) {
                        continue;
                    }

                    DataType<?> dataType = model.getInputDefinition().getDataType(attributeName);
                    list.add(new Attribute(attributeName, dataType));
                }

            } catch (NullPointerException e) {
                // Silently catch NullPointerExceptions here (when model is not yet defined, etc.)
            }

            return list.toArray();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
         */
        @Override
        public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
            // Nothing to do
        }

    }

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
    public ViewAttributeConfiguration(final Composite parent, final Controller controller) {

        this.controller = controller;

        // Build view
        build(parent);

        // These events are triggered when data is imported or attribute configuration changes
        this.controller.addListener(ModelPart.INPUT, this); // TODO: Is this actually needed? Can data be imported with an attribute being set as identifying?
        this.controller.addListener(ModelPart.DATA_TYPE, this);
        this.controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        this.controller.addListener(ModelPart.SELECTED_ATTRIBUTE, this);
        this.controller.addListener(ModelPart.MASKING_CONFIGURATION, this);

    }

    /**
     * Build.
     * 
     * @param parent
     */
    private void build(Composite parent) {

        // Title bar
        ComponentTitledFolder folder = new ComponentTitledFolder(parent, controller, null, null);
        folder.setLayoutData(SWTUtil.createFillGridData());

        // First tab
        Composite composite = folder.createItem(Resources.getMessage("MaskingView.1"), null); //$NON-NLS-1$
        composite.setLayout(SWTUtil.createGridLayout(1));
        folder.setSelection(0);

        // Create table
        tableViewer = SWTUtil.createTableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION);
        tableViewer.setContentProvider(new AttributeContentProvider());

        Table table = tableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setLayoutData(SWTUtil.createFillGridData());
        table.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {

                Attribute attribute = (Attribute) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();
                controller.getModel().setSelectedAttribute(attribute.getName());

                // Send notification
                controller.update(new ModelEvent(this, ModelPart.SELECTED_ATTRIBUTE, attribute.getName()));

            }

        });

        // Column containing attribute names
        TableViewerColumn tableViewerColumnName = new TableViewerColumn(tableViewer, SWT.NONE);
        tableViewerColumnName.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {
                return ((Attribute) element).getName();
            }

        });

        TableColumn columnName = tableViewerColumnName.getColumn();
        columnName.setToolTipText(Resources.getMessage("MaskingView.6")); //$NON-NLS-1$
        columnName.setText(Resources.getMessage("MaskingView.7")); //$NON-NLS-1$
        columnName.setWidth(100);

        // Column containing attribute data type
        TableViewerColumn tableViewerColumnDataType = new TableViewerColumn(tableViewer, SWT.NONE);
        tableViewerColumnDataType.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {
                return ((Attribute) element).getType().getDescription().getLabel();
            }

        });

        TableColumn columnDataType = tableViewerColumnDataType.getColumn();
        columnDataType.setToolTipText(Resources.getMessage("MaskingView.8")); //$NON-NLS-1$
        columnDataType.setText(Resources.getMessage("MaskingView.9")); //$NON-NLS-1$
        columnDataType.setWidth(100);

        // Column containing masking operation
        TableViewerColumn tableViewerColumnMasking = new TableViewerColumn(tableViewer, SWT.NONE);
        tableViewerColumnMasking.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {

                Attribute attribute = ((Attribute) element);
                MaskingType maskingType = MaskingConfiguration.getMaskingType(attribute.getName());
                if (maskingType == null) {
                    return Resources.getMessage("MaskingView.10"); //$NON-NLS-1$
                }
                return maskingType.getLabel();
            }

        });

        TableColumn columnMasking = tableViewerColumnMasking.getColumn();
        columnMasking.setToolTipText(Resources.getMessage("MaskingView.11")); //$NON-NLS-1$
        columnMasking.setText(Resources.getMessage("MaskingView.12")); //$NON-NLS-1$
        columnMasking.setWidth(150);

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

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.arx.gui.view.def.IView#reset()
     */
    @Override
    public void reset() {
        tableViewer.getTable().clearAll();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.arx.gui.view.def.IView#update(org.deidentifier.arx.gui.model.ModelEvent)
     */
    @Override
    public void update(ModelEvent event) {

        // Disable redrawing, so changes won't be noticed by the user and appear to be atomic
        Model model = controller.getModel();
        tableViewer.getTable().setRedraw(false);

        // Remove all data
        tableViewer.getTable().removeAll();

        // Apply new data
        tableViewer.setInput(model);

        // Highlights the currently active (highlighted) attribute
        if (event.part == ModelPart.SELECTED_ATTRIBUTE) {
            Object[] currentlyIdentifying = ((AttributeContentProvider) tableViewer.getContentProvider()).getElements(model);
            String selectedAttribute = model.getSelectedAttribute();
            for (int i = 0; i < currentlyIdentifying.length; i++) {
                if (((Attribute) currentlyIdentifying[i]).equals(selectedAttribute)) {
                    tableViewer.setSelection(new StructuredSelection(tableViewer.getElementAt(i)), true);
                    break;
                }

            }
        }

        // Reenable redrawing
        tableViewer.getTable().setRedraw(true);

    }

}
