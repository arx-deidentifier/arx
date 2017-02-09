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

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolder;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolderButtonBar;
import org.deidentifier.arx.masking.Masking;
import org.deidentifier.arx.masking.MaskingConfiguration;
import org.deidentifier.arx.masking.MaskingType;
import org.deidentifier.arx.masking.MaskingType.MaskingTypeDescription;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
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
 * This view displays all available attributes and allows them to be configured for masking
 *
 * @author Karol Babioch
 */
public class ViewAttributeConfiguration implements IView {

    /**
     * Container describing an attribute
     *
     * @author Karol Babioch
     */
    private class Attribute {

        private String name;
        private DataType<?> type;


        public Attribute(String name, DataType<?> type) {

            this.name = name;
            this.type = type;

        }

        public String getName() {

            return name;

        }

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

        @Override
        public void dispose() {

        }

        @Override
        public void inputChanged(Viewer arg0, Object arg1, Object arg2) {

        }

        @Override
        public Object[] getElements(Object inputData) {

            Model model = (Model)inputData;
            List<Attribute> list = new ArrayList<Attribute>();

            try {

                DataHandle data = model.getInputConfig().getInput().getHandle();

                for (int i = 0; i < data.getNumColumns(); i++) {
    
                    String name = data.getAttributeName(i);
                    DataType<?> type = model.getInputDefinition().getDataType(name);
                    list.add(new Attribute(name, type));
    
                }

            } catch (NullPointerException e) {

                // Silently catch NullPointerExceptions here (when model is not yet defined, etc.)
                // TODO: Maybe too generic, but handling all different cases might be more complicated
                System.out.println("NullPointer");

            }

            return list.toArray();

        }

    }

    /**
     * Editor class for changing the masking type
     *
     * @TODO Change it in the same way as ImportWizardPageColumn to fix platform specific issues?
     *
     * @author Karol Babioch
     */
    private class MaskingEditingSupport extends EditingSupport {

        private String[] items;

        public MaskingEditingSupport(TableViewer viewer) {

            super(viewer);

        }

        @Override
        protected boolean canEdit(Object element) {

            for (MaskingTypeDescription description : MaskingType.list()) {

                for (DataType<?> datatype : description.getSupportedDataTypes()) {

                    if (datatype.getClass() == ((Attribute)element).getType().getClass()) {

                        return true;

                    }

                }

            }

            return false;

        }

        @Override
        protected CellEditor getCellEditor(Object element) {

            List<String> items = new ArrayList<>();

            items.add("None");

            for (MaskingTypeDescription description : MaskingType.list()) {

                for (DataType<?> datatype : description.getSupportedDataTypes()) {

                    if (datatype.getClass() == ((Attribute)element).getType().getClass()) {

                        items.add(description.getLabel());

                    }

                }

            }

            this.items = items.toArray(new String[items.size()]);

            return new ComboBoxCellEditor(((TableViewer) this.getViewer()).getTable(), this.items, SWT.READ_ONLY);

        }

        @Override
        protected Object getValue(Object element) {

            MaskingConfiguration maskingConfiguration = controller.getModel().getMaskingModel().getMaskingConfiguration();
            String attributeName = ((Attribute)element).getName();
            Masking masking = maskingConfiguration.getMasking(attributeName);

            // Check whether attribute is masked
            if (masking != null) {

                // Iterate over available items
                for (int i = 0; i < items.length; i++) {

                    if (items[i].equals(masking.getMaskingType().getDescription().getLabel())) {

                        return i;

                    }

                }

            }

            // Preselect first element (None) by default
            return 0;

        }

        @Override
        protected void setValue(Object element, Object value) {

            MaskingConfiguration maskingConfiguration = controller.getModel().getMaskingModel().getMaskingConfiguration();
            String attributeName = ((Attribute)element).getName();
            int item = (int)value;

            if (item == 0) {

                maskingConfiguration.removeMasking(attributeName);

            } else {

                for (MaskingTypeDescription description : MaskingType.list()) {

                    if (description.getLabel().equals(this.items[item])) {

                        maskingConfiguration.addMasking(attributeName, new Masking(description.newInstance()));

                    }

                }

            }

            // Send update event
            controller.update(new ModelEvent(this, ModelPart.MASKING_FOR_ATTRIBUTE_CHANGED, null));

        }

    }

    private Controller controller;

    private Composite parentComposition;

    private TableViewer tableViewer;


    public ViewAttributeConfiguration(final Composite parent, final Controller controller) {

        this.controller = controller;
        this.parentComposition = build(parent);

        // These events are triggered when data is imported, i.e. attributes are "created" // TODO Are these all or correct?
        this.controller.addListener(ModelPart.INPUT, this);
        this.controller.addListener(ModelPart.DATA_TYPE, this);

        // Get notified whenever the masking for an attribute is changed
        this.controller.addListener(ModelPart.MASKING_FOR_ATTRIBUTE_CHANGED, this);

    }


    private Composite build(Composite parent) {

        // Create button bar
        ComponentTitledFolderButtonBar bar = new ComponentTitledFolderButtonBar(null); // TODO Assign help id
        bar.add("Add attribute", controller.getResources().getManagedImage("add.png"), new Runnable() {

            @Override
            public void run() {

                controller.actionShowInfoDialog(controller.getResources().getShell(), "NOT YET IMPLEMENTED", "This could be used to add attributes for data generation (i.e. random data)");

            }

        });
        bar.add("Remove attribute", controller.getResources().getManagedImage("remove.png"), new Runnable() {

            @Override
            public void run() {

                controller.actionShowInfoDialog(controller.getResources().getShell(), "NOT YET IMPLEMENTED", "Remove attributes? But only previously added ones? Or all ones, i.e. also the ones imported?");

            }

        });

        // Title bar
        ComponentTitledFolder folder = new ComponentTitledFolder(parent, controller, bar, null);
        folder.setLayoutData(SWTUtil.createFillGridData());

        // First tab
        Composite composite = folder.createItem("Attribute configuration", null);
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

                controller.update(new ModelEvent(this, ModelPart.MASKING_ATTRIBUTE_SELECTED, String.valueOf(table.getSelectionIndex())));

            }

        });


        // Column containing attribute names
        TableViewerColumn tableViewerColumnName = new TableViewerColumn(tableViewer, SWT.NONE);
        tableViewerColumnName.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {

                return ((Attribute)element).getName();

            }

        });

        TableColumn columnName = tableViewerColumnName.getColumn();
        columnName.setToolTipText("Name of the attribute");
        columnName.setText("Attribute");
        columnName.setWidth(200);


        // Column containing attribute data type
        TableViewerColumn tableViewerColumnDataType = new TableViewerColumn(tableViewer, SWT.NONE);
        tableViewerColumnDataType.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {

                return ((Attribute)element).getType().getDescription().getLabel();

            }

        });

        TableColumn columnDataType = tableViewerColumnDataType.getColumn();
        columnDataType.setToolTipText("Data type of the attribute");
        columnDataType.setText("Data type");
        columnDataType.setWidth(100);


        // Column containing masking operation
        TableViewerColumn tableViewerColumnMasking = new TableViewerColumn(tableViewer, SWT.NONE);
        tableViewerColumnMasking.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {

                Attribute attribute = ((Attribute)element);
                MaskingConfiguration maskingConfiguration = controller.getModel().getMaskingModel().getMaskingConfiguration();
                Masking masking = maskingConfiguration.getMasking(attribute.getName());

                if (masking == null) {

                    return "None";

                }

                return masking.getMaskingType().getDescription().getLabel();

            }

        });
        tableViewerColumnMasking.setEditingSupport(new MaskingEditingSupport(tableViewer));

        TableColumn columnMasking = tableViewerColumnMasking.getColumn();
        columnMasking.setToolTipText("Masking operation for attribute");
        columnMasking.setText("Masking type");
        columnMasking.setWidth(100);


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
        tableViewer.setInput(controller.getModel());

        // Reenable redrawing
        tableViewer.getTable().setRedraw(true);

    }

}
