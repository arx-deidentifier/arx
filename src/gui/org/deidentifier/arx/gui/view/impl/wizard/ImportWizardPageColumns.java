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

package org.deidentifier.arx.gui.view.impl.wizard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.util.Pair;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.DataTypeWithFormat;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.io.ImportColumn;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
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
     * Implements editing support for data type column within the column page
     * 
     * This allows to change the data type of columns. The modifications are
     * performed with a combo box {@link ComboBoxCellEditor}.
     */
    public class DatatypeEditingSupport extends EditingSupport {

        /** Reference to actual viewer. */
        private TableViewer viewer;

        /** Editors*/
        private Map<ImportWizardModelColumn, AutoDropComboBoxViewerCellEditor> editors = new
                HashMap<ImportWizardModelColumn, AutoDropComboBoxViewerCellEditor>();
        
        /** Types*/
        private Map<ImportWizardModelColumn, Map<String, DataType<?>>> types = 
                new HashMap<ImportWizardModelColumn, Map<String, DataType<?>>>();

        /**
         * Creates a new editor for the given {@link TableViewer}.
         * 
         * @param viewer The TableViewer this editor is implemented for
         * @param columns The columns
         */
        public DatatypeEditingSupport(TableViewer viewer) {
            super(viewer);
            this.viewer = viewer;
        }
        
        /**
         * Updates this editing support
         * @param columns
         */
        public void update(List<ImportWizardModelColumn> columns) {
            
            for (ImportWizardModelColumn column : columns) {
                this.types.put(column, new HashMap<String, DataType<?>>());
                
                List<Pair<DataType<?>, Double>> matchingtypes = wizardImport.getData().getMatchingDataTypes(column);
                List<String> labels = new ArrayList<String>();
                List<DataType<?>> types = new ArrayList<DataType<?>>();
                for (Pair<DataType<?>, Double> match : matchingtypes) {
                    
                    StringBuilder builder = new StringBuilder();
                    builder.append(match.getFirst().getDescription().getLabel());
                    if (match.getFirst() instanceof DataTypeWithFormat && ((DataTypeWithFormat)match.getFirst()).getFormat() != null) {
                        builder.append(" ("); //$NON-NLS-1$
                        builder.append(((DataTypeWithFormat)match.getFirst()).getFormat());
                        builder.append(")"); //$NON-NLS-1$
                    }
                    builder.append(" "); //$NON-NLS-1$
                    builder.append((int)(match.getSecond() * 100d));
                    builder.append("%"); //$NON-NLS-1$
                    
                    String label = builder.toString();
                    DataType<?> type = match.getFirst();
                    labels.add(label);
                    types.add(type);
                    this.types.get(column).put(label, type);
                }
                
                AutoDropComboBoxViewerCellEditor editor = new AutoDropComboBoxViewerCellEditor(viewer.getTable());
                editor.setContentProvider(new ArrayContentProvider());
                editor.setInput(labels.toArray(new String[labels.size()]));
                editors.put(column, editor);
            }
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
            return editors.get(arg0);
        }

        /**
         * Returns current index of {@link #choices} for given column datatype.
         *
         * @param element
         * @return
         */
        @Override
        protected Object getValue(Object element) {
            ImportWizardModelColumn column = (ImportWizardModelColumn) element;
            for (Entry<String, DataType<?>> entry : types.get(column).entrySet()) {
                if (entry.getValue() == column.getColumn().getDataType()) {
                    return entry.getKey();
                }
            }
            return null;
        }
        
        /**
         * Applies data type choice made by the user
         * 
         * If a data type, which requires a format string, was selected an input
         * dialog will be shown {@link actionShowFormatInputDialog}. Otherwise
         * the choice is directly applied. THe input dialog itself will make
         * sure that the format string is valid for the data type. This method on
         * the other hand will try to apply the format string to the available
         * preview data {@link ImportWizardModel#getPreviewData()} making sure
         * that it matches. In case of an error the choice is discarded.
         *
         * @param element
         * @param value
         */
        @Override
        protected void setValue(Object element, Object value) {

            // Extract
            String label = (String)value;
            ImportWizardModelColumn wizardColumn = (ImportWizardModelColumn) element;
            ImportColumn column = wizardColumn.getColumn();
            DataType<?> type = types.get(wizardColumn).get(label);
            
            /* Apply datatype */
            column.setDataType(type);
            getViewer().update(element, null);
            return;                
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

            ((ImportWizardModelColumn) element).getColumn().setAliasName((String) value);
            getViewer().update(element, null);
            check();
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
                tblclmnEnabled.setToolTipText(Resources.getMessage("ImportWizardPageColumns.4")); //$NON-NLS-1$
                tblclmnEnabled.setText(Resources.getMessage("ImportWizardPageColumns.5")); //$NON-NLS-1$
            } else {
                tblclmnEnabled.setToolTipText(Resources.getMessage("ImportWizardPageColumns.6")); //$NON-NLS-1$
                tblclmnEnabled.setText(Resources.getMessage("ImportWizardPageColumns.7")); //$NON-NLS-1$
            }
            check();
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
    private ImportWizard           wizardImport;
    
    /** View */
    private Table                  table;

    /** View */
    private CheckboxTableViewer    checkboxTableViewer;

    /** View */
    private TableColumn            tblclmnName;

    /** View */
    private TableViewerColumn      tableViewerColumnName;

    /** View */
    private TableColumn            tblclmnDatatype;

    /** View */
    private TableViewerColumn      tableViewerColumnDatatype;

    /** View */
    private TableColumn            tblclmnEnabled;

    /** View */
    private TableViewerColumn      tableViewerColumnEnabled;

    /** View */
    private TableColumn            tblclmnFormat;

    /** View */
    private TableViewerColumn      tableViewerColumnFormat;

    /** View */
    private DatatypeEditingSupport tableViewerColumnDatatypeEditingSupport;

    /** View */
    private Button                 btnUp;

    /** View */
    private Button                 btnDown;

    /** View */
    private Button                 btnCleansing;

    /** Indicator for the next action of {@link ColumnEnabledSelectionListener}. */
    private boolean                selectAll = false;

    /**
     * Creates a new instance of this page and sets its title and description.
     *
     * @param wizardImport Reference to wizard containing this page
     */
    public ImportWizardPageColumns(ImportWizard wizardImport) {

        super("WizardImportCsvPage"); //$NON-NLS-1$

        this.wizardImport = wizardImport;
        
        setTitle(Resources.getMessage("ImportWizardPageColumns.9")); //$NON-NLS-1$
        setDescription(Resources.getMessage("ImportWizardPageColumns.10")); //$NON-NLS-1$

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
        checkboxTableViewer = SWTUtil.createTableViewerCheckbox(container, SWT.BORDER | SWT.FULL_SELECTION);
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
                ((ImportWizardModelColumn) event.getElement()).setEnabled(event.getChecked());
                check();
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
        tblclmnEnabled.setToolTipText(Resources.getMessage("ImportWizardPageColumns.11")); //$NON-NLS-1$
        tblclmnEnabled.setText(Resources.getMessage("ImportWizardPageColumns.12")); //$NON-NLS-1$
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
                ImportWizardModelColumn column = (ImportWizardModelColumn) element;
                return column.getColumn().getAliasName();
            }
        });

        /* Actual column for {@link tableViewerColumnName} */
        tblclmnName = tableViewerColumnName.getColumn();
        tblclmnName.setToolTipText(Resources.getMessage("ImportWizardPageColumns.13")); //$NON-NLS-1$
        tblclmnName.setWidth(300);
        tblclmnName.setText(Resources.getMessage("ImportWizardPageColumns.14")); //$NON-NLS-1$

        /* Column containing the datatypes */
        tableViewerColumnDatatype = new TableViewerColumn(checkboxTableViewer, SWT.NONE);
        tableViewerColumnDatatypeEditingSupport = new DatatypeEditingSupport(checkboxTableViewer);
        tableViewerColumnDatatype.setEditingSupport(tableViewerColumnDatatypeEditingSupport);
        tableViewerColumnDatatype.setLabelProvider(new ColumnLabelProvider() {

            /**
             * Gets string representation for given datatype of column
             * 
             * Internally it makes use of {@link ImportColumn#getDataType()}.
             */
            @Override
            public String getText(Object element) {
                ImportWizardModelColumn column = (ImportWizardModelColumn) element;
                DataType<?> datatype = column.getColumn().getDataType();
                return datatype.getDescription().getLabel();
            }
        });

        /* Actual column for {@link tableViewerColumnDatatype} */
        tblclmnDatatype = tableViewerColumnDatatype.getColumn();
        tblclmnDatatype.setToolTipText(Resources.getMessage("ImportWizardPageColumns.15")); //$NON-NLS-1$
        tblclmnDatatype.setWidth(120);
        tblclmnDatatype.setText(Resources.getMessage("ImportWizardPageColumns.16")); //$NON-NLS-1$

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
                return ""; //$NON-NLS-1$
            }
        });

        /* Actual column for {@link tableViewerColumnFormat} */
        tblclmnFormat = tableViewerColumnFormat.getColumn();
        tblclmnFormat.setWidth(120);
        tblclmnFormat.setToolTipText(Resources.getMessage("ImportWizardPageColumns.18")); //$NON-NLS-1$
        tblclmnFormat.setWidth(100);
        tblclmnFormat.setText(Resources.getMessage("ImportWizardPageColumns.19")); //$NON-NLS-1$

        /* Buttons to move column up */
        btnUp = new Button(container, SWT.NONE);
        btnUp.setText(Resources.getMessage("ImportWizardPageColumns.20")); //$NON-NLS-1$
        btnUp.setImage(wizardImport.getController()
                                   .getResources()
                                   .getManagedImage("arrow_up.png")); //$NON-NLS-1$
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
        btnDown.setText(Resources.getMessage("ImportWizardPageColumns.22")); //$NON-NLS-1$
        btnDown.setImage(wizardImport.getController()
                                     .getResources()
                                     .getManagedImage("arrow_down.png")); //$NON-NLS-1$
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
        
        btnCleansing = new Button(container, SWT.CHECK);
        btnCleansing.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).span(2, 1).create());
        btnCleansing.setText(Resources.getMessage("ImportWizardPageColumns.24")); //$NON-NLS-1$
        btnCleansing.setToolTipText(Resources.getMessage("ImportWizardPageColumns.25")); //$NON-NLS-1$
        btnCleansing.setEnabled(true);
        btnCleansing.setSelection(wizardImport.getData().isPerformCleansing());
        btnCleansing.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
                wizardImport.getData().setPerformCleansing(btnCleansing.getSelection());
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
            
            for (ImportWizardModelColumn column : wizardImport.getData().getWizardColumns()) {
                column.getColumn().setDataType(wizardImport.getData().getMatchingDataTypes(column).iterator().next().getFirst());
            }
            
            tableViewerColumnDatatypeEditingSupport.update(wizardImport.getData()
                                                                       .getWizardColumns());
            
            checkboxTableViewer.setInput(wizardImport.getData()
                                                     .getWizardColumns());
            check();
        }
    }

    /**
     * Checks whether the current selection of columns is suited for import
     */
    private void check(){

        // Check selection
        boolean selected = false;
        for (ImportWizardModelColumn column : wizardImport.getData()
                                                          .getWizardColumns()) {
            selected |= column.isEnabled();
        }
        
        if (!selected) {
            setErrorMessage(Resources.getMessage("ImportWizardPageColumns.26")); //$NON-NLS-1$
            setPageComplete(false);
            return;
        }

        // Check names
        for (ImportWizardModelColumn c1 : wizardImport.getData().getWizardColumns()) {
            if (c1.isEnabled()) {
                String name1 = c1.getColumn().getAliasName();
                for (ImportWizardModelColumn c2 : wizardImport.getData().getWizardColumns()) {
                    if (c2.isEnabled() && c1 != c2 && name1.equals(c2.getColumn().getAliasName())) {
                        setErrorMessage(Resources.getMessage("ImportWizardPageColumns.27") + name1); //$NON-NLS-1$
                        setPageComplete(false);
                        return;
                    }
                }
            }
        }
        
        // Everything is fine
        setErrorMessage(null);
        setPageComplete(true);
    }
}