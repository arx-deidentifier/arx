/*
 * ARX: Powerful Data Anonymization
 * Copyright 2014 Karol Babioch <karol@babioch.de>
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

package org.deidentifier.arx.gui.view.impl.wizard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.DataTypeDescription;
import org.deidentifier.arx.DataType.DataTypeWithFormat;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.io.ImportColumn;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * Column overview page
 * 
 * This pages gives the user an overview of the detected columns and allows him
 * to change things around. First of all columns can be enabled or disabled on
 * an individual basis. Secondly the order of the columns can be changed around.
 * Furthermore a data type along with a format string can be defined for each
 * column.
 * 
 * A single column is represented by {@link ImportWizardModelColumn}, the list
 * of all detected columns can be accessed by
 * {@link ImportWizardModel#getWizardColumns()}.
 * 
 * @author Karol Babioch
 * @author Fabian Prasser
 */
public class ImportWizardPageColumns extends WizardPage {
    
    /**
     * Implements editing support for datatype column within the column page
     * 
     * This allows to change the datatype of columns. The modifications are
     * performed with a combo box {@link ComboBoxCellEditor}.
     */
    public class DatatypeEditingSupport extends EditingSupport {

        /** Reference to actual editor. */
        private AutoDropComboBoxViewerCellEditor editor;

        /**
         * Allowed values for the user to choose from
         * 
         * This array contains all of the choices the user can make. The array
         * gets populated during runtime.
         */
        private String[]           choices;

        /**
         * Creates a new editor for the given {@link TableViewer}.
         * 
         * @param viewer
         *            The TableViewer this editor is implemented for
         */
        public DatatypeEditingSupport(TableViewer viewer) {

            super(viewer);

            List<String> labels = new ArrayList<String>();
            for (DataTypeDescription<?> description : DataType.list()) {
                /* Remove OrderedString from list of choices for now */
                if (description.newInstance().getClass() == DataType.ORDERED_STRING.getClass()) {
                    continue;
                }
                labels.add(description.getLabel());
            }

            choices = labels.toArray(new String[labels.size()]);
            editor = new AutoDropComboBoxViewerCellEditor(viewer.getTable());
            editor.setContentProvider(new ArrayContentProvider());
            editor.setInput(choices);
        }

        /**
         * Indicates that enabled cells within this column can be edited.
         *
         * @param column
         * @return
         */
        @Override
        protected boolean canEdit(Object column) {
            return ((ImportWizardModelColumn) column).isEnabled();
        }

        /**
         * Returns a reference to {@link #editor}.
         *
         * @param arg0
         * @return
         */
        @Override
        protected CellEditor getCellEditor(Object arg0) {
            return editor;
        }

        /**
         * Returns current index of {@link #choices} for given column datatype.
         *
         * @param element
         * @return
         */
        @Override
        protected Object getValue(Object element) {

            DataType<?> datatype = ((ImportWizardModelColumn) element).getColumn()
                                                                      .getDataType();
            return datatype.getDescription().getLabel();
        }
        
        /**
         * Applies datatype choice made by the user
         * 
         * If a datatype, which requires a format string, was selected an input
         * dialog will be shown {@link actionShowFormatInputDialog}. Otherwise
         * the choice is directly applied. THe input dialog itself will make
         * sure that the format string is valid for the datatype. This method on
         * the other hand will try to apply the format string to the available
         * preview data {@link ImportWizardModel#getPreviewData()} making sure
         * that it matches. In case of an error the choice is discarded.
         *
         * @param element
         * @param value
         */
        @Override
        protected void setValue(Object element, Object value) {
            
            final String HEADER = "Format string";
            final String BODY = "Please provide a format string describing each item of this column";

            String label = (String)value;
            ImportWizardModelColumn wizardColumn = (ImportWizardModelColumn) element;
            ImportColumn column = wizardColumn.getColumn();
            List<String> previewData = wizardImport.getData().getPreviewData(wizardColumn);

            for (DataTypeDescription<?> description : DataType.list()) {
                if (description.getLabel().equals(label)) {
                    
                    DataType<?> datatype = null;
                    if (description.hasFormat()) {

                        final Controller controller = wizardImport.getController();
                        String format = null;
                        if (column.getDataType().getClass() == description.newInstance()
                                                                          .getClass()) {

                            format = controller.actionShowFormatInputDialog(getShell(),
                                                                            HEADER,
                                                                            BODY,
                                                                            ((DataTypeWithFormat) column.getDataType()).getFormat(),
                                                                            wizardImport.getModel().getLocale(),
                                                                            description,
                                                                            previewData);

                        } else {
                            format = controller.actionShowFormatInputDialog(getShell(),
                                                                            HEADER,
                                                                            BODY,
                                                                            wizardImport.getModel().getLocale(),
                                                                            description,
                                                                            previewData);
                        }

                        if (format != null) {
                            datatype = description.newInstance(format, wizardImport.getModel().getLocale());
                        } else {
                            /* Invalid string or aborted by user */
                            return;
                        }
                    } else {
                        /* Datatype has no format */
                        datatype = description.newInstance();
                    }
                    
                    for (String data : previewData) {
                        if (!datatype.isValid(data)) {
                            datatype = DataType.STRING;
                        }
                    }

                    /* Apply datatype */
                    column.setDataType(datatype);
                    getViewer().update(element, null);
                    return;
                }
            }
        }
    }

    /**
     * Implements the editing support for name column
     * 
     * This allows to change the name of columns. The modifications are
     * performed within a simple text field {@link TextCellEditor}.
     */
    public class NameEditingSupport extends EditingSupport {

        /** Reference to actual editor. */
        private TextCellEditor editor;

        /**
         * Creates a new editor for the given {@link TableViewer}.
         * 
         * @param viewer
         *            The TableViewer this editor is implemented for
         */
        public NameEditingSupport(TableViewer viewer) {

            super(viewer);
            editor = new TextCellEditor(viewer.getTable());
        }

        /**
         * Indicates that enabled cells within this column can be edited.
         *
         * @param column
         * @return
         */
        @Override
        protected boolean canEdit(Object column) {
            return ((ImportWizardModelColumn) column).isEnabled();
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.EditingSupport#getCellEditor(java.lang.Object)
         */
        @Override
        protected CellEditor getCellEditor(Object arg0) {
            return editor;
        }

        /**
         * Retrieves name of column ({@link ImportColumn#getAliasName()}).
         *
         * @param arg0
         * @return
         */
        @Override
        protected Object getValue(Object arg0) {
            return ((ImportWizardModelColumn) arg0).getColumn().getAliasName();
        }

        /**
         * Sets name for given column ({@link ImportColumn#setAliasName(String)}).
         *
         * @param element
         * @param value
         */
        @Override
        protected void setValue(Object element, Object value) {

            ((ImportWizardModelColumn) element).getColumn()
                                               .setAliasName((String) value);
            getViewer().update(element, null);
        }
    }

    /**
     * Works around JFace bugs. 
     * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=230398
     *
     */
    private static class AutoDropComboBoxViewerCellEditor extends ComboBoxViewerCellEditor {
        
        /**
         * 
         *
         * @param parent
         */
        protected AutoDropComboBoxViewerCellEditor(Composite parent) {
            super(parent, SWT.READ_ONLY);
            setActivationStyle(DROP_DOWN_ON_MOUSE_ACTIVATION);
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ComboBoxViewerCellEditor#createControl(org.eclipse.swt.widgets.Composite)
         */
        @Override
        protected Control createControl(Composite parent) {
            final Control control = super.createControl(parent);
            getViewer().getCCombo().addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    focusLost();
                }
            });
            return control;
        }
    }
    /**
     * Listener for click events of the "enabled" column
     * 
     * By clicking on the column all items can be selected and/or deselected at
     * once. The result of the action depends upon {@link #selectAll}.
     */
    private final class ColumnEnabledSelectionListener extends SelectionAdapter {

        /**
         * (Un)checks all of the items at once
         * 
         * This iterates through all of the items and invokes {@link #setChecked(int, Boolean)} for all of them. Furthermore the
         * tooltip is changed appropriately.
         *
         * @param arg0
         */
        @Override
        public void widgetSelected(SelectionEvent arg0) {

            for (int i = 0; i < table.getItems().length; i++) {
                setChecked(i, selectAll);
            }

            selectAll = !selectAll;

            if (selectAll) {
                tblclmnEnabled.setToolTipText("Select all");
                tblclmnEnabled.setText("SA");
            } else {
                tblclmnEnabled.setToolTipText("Deselect all");
                tblclmnEnabled.setText("DA");
            }
        }

        /**
         * Applies a boolean value to the given item.
         *
         * @param i Item that <code>check</code> should be applied to
         * @param check Value that should be applied to item <code>i</code>
         */
        private void setChecked(int i, Boolean check) {

            table.getItem(i).setChecked(check);
            wizardImport.getData().getWizardColumns().get(i).setEnabled(check);

            setPageComplete(false);

            for (ImportWizardModelColumn column : wizardImport.getData()
                                                              .getWizardColumns()) {

                if (column.isEnabled()) {
                    setPageComplete(true);
                    return;
                }
            }
        }
    }
    
    /** Reference to the wizard containing this page. */
    private ImportWizard        wizardImport;
    /* Widgets */
    /**  TODO */
    private Table               table;
    
    /**  TODO */
    private CheckboxTableViewer checkboxTableViewer;
    
    /**  TODO */
    private TableColumn         tblclmnName;
    
    /**  TODO */
    private TableViewerColumn   tableViewerColumnName;
    
    /**  TODO */
    private TableColumn         tblclmnDatatype;
    
    /**  TODO */
    private TableViewerColumn   tableViewerColumnDatatype;
    
    /**  TODO */
    private TableColumn         tblclmnEnabled;
    
    /**  TODO */
    private TableViewerColumn   tableViewerColumnEnabled;
    
    /**  TODO */
    private TableColumn         tblclmnFormat;

    /**  TODO */
    private TableViewerColumn   tableViewerColumnFormat;

    /**  TODO */
    private Button              btnUp;

    /**  TODO */
    private Button              btnDown;

    /** Indicator for the next action of {@link ColumnEnabledSelectionListener}. */
    private boolean             selectAll = false;

    /**
     * Creates a new instance of this page and sets its title and description.
     *
     * @param wizardImport Reference to wizard containing this page
     */
    public ImportWizardPageColumns(ImportWizard wizardImport) {

        super("WizardImportCsvPage");

        this.wizardImport = wizardImport;
        
        setTitle("Columns");
        setDescription("Please check and/or modify the detected columns");

    }

    /**
     * Creates the design of this page along with the appropriate listeners.
     *
     * @param parent
     */
    public void createControl(Composite parent) {

        Composite container = new Composite(parent, SWT.NULL);

        setControl(container);
        container.setLayout(new GridLayout(2, false));

        /* TableViewer for the columns with a checkbox in each row */
        checkboxTableViewer = CheckboxTableViewer.newCheckList(container,
                                                               SWT.BORDER |
                                                                       SWT.FULL_SELECTION);
        checkboxTableViewer.setContentProvider(new ArrayContentProvider());
        checkboxTableViewer.setCheckStateProvider(new ICheckStateProvider() {

            /** @return {@link ImportWizardModelColumn#isEnabled()} */
            @Override
            public boolean isChecked(Object column) {
                return ((ImportWizardModelColumn) column).isEnabled();
            }

            /** No column should be grayed out */
            @Override
            public boolean isGrayed(Object column) {
                return false;
            }
        });
        checkboxTableViewer.addCheckStateListener(new ICheckStateListener() {

            /**
             * Sets the enabled status for the given item
             * 
             * Using {@link ImportWizardModelColumn#setEnabled(boolean)} this
             * method will set the enabled flag for the given column.
             * Furthermore it makes sure the page is marked as complete once at
             * least one item is selected.
             */
            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {

                setPageComplete(false);
                ((ImportWizardModelColumn) event.getElement()).setEnabled(event.getChecked());
                for (ImportWizardModelColumn column : wizardImport.getData()
                                                                  .getWizardColumns()) {

                    if (column.isEnabled()) {
                        setPageComplete(true);
                        return;
                    }
                }
            }
        });

        /* Actual table for {@link #checkboxTableViewer} */
        table = checkboxTableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
        table.addSelectionListener(new SelectionAdapter() {

            /**
             * Makes the buttons for column reordering (un)clickable
             * 
             * This checks the current selection and will enable and/or disable
             * the {@link #btnUp} and {@link #btnDown} if either the first or
             * last item is currently selected.
             */
            @Override
            public void widgetSelected(SelectionEvent e) {

                /* Check for first item */
                if (table.getSelectionIndex() == 0) {
                    btnUp.setEnabled(false);
                } else {
                    btnUp.setEnabled(true);
                }

                /* Check for last item */
                if (table.getSelectionIndex() == table.getItemCount() - 1) {
                    btnDown.setEnabled(false);
                } else {
                    btnDown.setEnabled(true);
                }
            }
        });

        /* Empty column to make checkboxes appear in an own cell */
        tableViewerColumnEnabled = new TableViewerColumn(checkboxTableViewer, SWT.NONE);
        tableViewerColumnEnabled.setLabelProvider(new ColumnLabelProvider() {
            /** Cells within this column should always be empty */
            @Override
            public String getText(Object element) {
                return null;
            }
        });

        /* Actual column for {@link tableViewerColumnEnabled} */
        tblclmnEnabled = tableViewerColumnEnabled.getColumn();
        tblclmnEnabled.setToolTipText("Deselect all");
        tblclmnEnabled.setText("DA");
        tblclmnEnabled.setWidth(40);
        tblclmnEnabled.addSelectionListener(new ColumnEnabledSelectionListener());

        /* Column containing the names */
        tableViewerColumnName = new TableViewerColumn(checkboxTableViewer, SWT.NONE);
        tableViewerColumnName.setEditingSupport(new NameEditingSupport(checkboxTableViewer));
        tableViewerColumnName.setLabelProvider(new ColumnLabelProvider() {

            /**
             * Gets name of cells from {@link ImportColumn#getAliasName()}
             * 
             * This also makes sure that all column names are unique using
             * {@link #uniqueColumnNames()}. In case there are duplicates it
             * sets an error message.
             */
            @Override
            public String getText(Object element) {

                if (!uniqueColumnNames()) {
                    setErrorMessage("Column names need to be unique");
                    setPageComplete(false);
                } else {
                    setErrorMessage(null);
                    setPageComplete(true);
                }

                ImportWizardModelColumn column = (ImportWizardModelColumn) element;
                return column.getColumn().getAliasName();
            }
        });

        /* Actual column for {@link tableViewerColumnName} */
        tblclmnName = tableViewerColumnName.getColumn();
        tblclmnName.setToolTipText("Name of the column");
        tblclmnName.setWidth(300);
        tblclmnName.setText("Name");

        /* Column containing the datatypes */
        tableViewerColumnDatatype = new TableViewerColumn(checkboxTableViewer,
                                                          SWT.NONE);
        tableViewerColumnDatatype.setEditingSupport(new DatatypeEditingSupport(checkboxTableViewer));
        tableViewerColumnDatatype.setLabelProvider(new ColumnLabelProvider() {

            /**
             * Gets string representation for given datatype of column
             * 
             * Internally it makes use of {@link ImportColumn#getDataType()}.
             */
            @Override
            public String getText(Object element) {

                DataType<?> datatype = ((ImportWizardModelColumn) element).getColumn()
                                                                          .getDataType();

                for (DataTypeDescription<?> description : DataType.list()) {
                    if (description.newInstance().getClass() == datatype.getClass()) {
                        return description.getLabel();
                    }
                }
                return null;
            }
        });

        /* Actual column for {@link tableViewerColumnDatatype} */
        tblclmnDatatype = tableViewerColumnDatatype.getColumn();
        tblclmnDatatype.setToolTipText("Datatype of the column");
        tblclmnDatatype.setWidth(120);
        tblclmnDatatype.setText("Datatype");

        /* Column containing the format of the format */
        tableViewerColumnFormat = new TableViewerColumn(checkboxTableViewer,
                                                        SWT.NONE);
        tableViewerColumnFormat.setLabelProvider(new ColumnLabelProvider() {

            /**
             * Returns format string of datatype for column
             * 
             * This retrieves the used format string of the chosen datatype for
             * each column.
             * 
             * @note In case of simple datatypes without a format specifier an
             *       empty string is returned.
             * 
             * @param element
             *            Column in question
             */
            @Override
            public String getText(Object element) {

                DataType<?> column = ((ImportWizardModelColumn) element).getColumn()
                                                                        .getDataType();
                if (column instanceof DataTypeWithFormat) {
                    return ((DataTypeWithFormat) column).getFormat();
                }
                return "";
            }
        });

        /* Actual column for {@link tableViewerColumnFormat} */
        tblclmnFormat = tableViewerColumnFormat.getColumn();
        tblclmnFormat.setWidth(120);
        tblclmnFormat.setToolTipText("Format of the associated datatype");
        tblclmnFormat.setWidth(100);
        tblclmnFormat.setText("Format");

        /* Buttons to move column up */
        btnUp = new Button(container, SWT.NONE);
        btnUp.setText("Move up");
        btnUp.setImage(wizardImport.getController()
                                   .getResources()
                                   .getImage("arrow_up.png"));
        btnUp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        btnUp.setEnabled(false);
        btnUp.addSelectionListener(new SelectionAdapter() {

            /**
             * Swaps the current element with the one above
             * 
             * This makes also sure that the button is disabled once the top is
             * reached by notifying the appropriate selection listener.
             */
            @Override
            public void widgetSelected(SelectionEvent e) {

                int current = table.getSelectionIndex();
                if (current > 0) {
                    List<ImportWizardModelColumn> columns = wizardImport.getData()
                                                                        .getWizardColumns();
                    Collections.swap(columns, current, current - 1);
                    checkboxTableViewer.setInput(columns);
                    table.notifyListeners(SWT.Selection, null);
                }
            }
        });

        /* Buttons to move column down */
        btnDown = new Button(container, SWT.NONE);
        btnDown.setText("Move down");
        btnDown.setImage(wizardImport.getController()
                                     .getResources()
                                     .getImage("arrow_down.png"));
        btnDown.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        btnDown.setEnabled(false);
        btnDown.addSelectionListener(new SelectionAdapter() {

            /**
             * Swaps the current element with the one below
             * 
             * This makes also sure that the button is disabled once the bottom
             * is reached by notifying the appropriate selection listener.
             */
            @Override
            public void widgetSelected(SelectionEvent e) {

                int current = table.getSelectionIndex();
                if (current < table.getItemCount() - 1) {

                    List<ImportWizardModelColumn> columns = wizardImport.getData()
                                                                        .getWizardColumns();
                    Collections.swap(columns, current, current + 1);
                    checkboxTableViewer.setInput(columns);
                    table.notifyListeners(SWT.Selection, null);
                }
            }
        });

        /* Wait for at least one column to be enabled */
        setPageComplete(false);
    }

    /**
     * Adds input to table viewer once page gets visible.
     *
     * @param visible
     */
    @Override
    public void setVisible(boolean visible) {

        super.setVisible(visible);
        if (visible) {
            checkboxTableViewer.setInput(wizardImport.getData()
                                                     .getWizardColumns());
            setPageComplete((wizardImport.getData().getWizardColumns().size() > 0));
        }
    }

    /**
     * Checks whether column names are unique.
     *
     * @return True if column names are unique, false otherwise
     */
    protected boolean uniqueColumnNames() {

        for (ImportWizardModelColumn c1 : wizardImport.getData()
                                                      .getWizardColumns()) {

            for (ImportWizardModelColumn c2 : wizardImport.getData()
                                                          .getWizardColumns()) {

                if (c1 != c2 &&
                    c1.getColumn()
                      .getAliasName()
                      .equals(c2.getColumn().getAliasName())) {

                    return false;
                }
            }
        }
        return true;
    }
}
