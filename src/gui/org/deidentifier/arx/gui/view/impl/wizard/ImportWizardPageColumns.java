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
import java.util.LinkedHashMap;
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
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
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
     * Implements a context menu for editing the data type column within the column page
     */
    public class DatatypeContextMenu {

        /** Types*/
        private Map<ImportWizardModelColumn, Map<String, DataType<?>>> matching = new HashMap<ImportWizardModelColumn, Map<String, DataType<?>>>();

        /** Types*/
        private Map<ImportWizardModelColumn, Map<String, DataType<?>>> nonmatching = new HashMap<ImportWizardModelColumn, Map<String, DataType<?>>>();

        /** Viewer*/
        private final TableViewer viewer;
        
        /**
         * Creates a new editor for the given {@link TableViewer}.
         * 
         * @param viewer The TableViewer this editor is implemented for
         * @param columns The columns
         */
        public DatatypeContextMenu(final TableViewer viewer, List<ImportWizardModelColumn> columns) {

            this.update(columns);
            this.viewer = viewer;
            viewer.getTable().addMenuDetectListener(new MenuDetectListener() {
                @Override
                public void menuDetected(MenuDetectEvent e) {
                    // Check selection
                   int index = table.getSelectionIndex();
                   if (index == -1) {
                     return; 
                   }
                   
                   // Create and show context menu
                   getMenu(viewer, (ImportWizardModelColumn) table.getItem(index).getData()).setVisible(true);      
                }
            });
        }
        
        /**
         * Creates a specific menu
         * @param viewer
         * @param column
         * @return
         */
        private Menu getMenu(final TableViewer viewer, final ImportWizardModelColumn wizardColumn) {

            // Prepare menu
            Menu menu = new Menu(viewer.getTable());
            viewer.getTable().setMenu(menu);

            MenuItem item = new MenuItem(menu, SWT.NONE);
            item.setText(Resources.getMessage("ImportWizardPageColumns.31")); //$NON-NLS-1$
            item.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent arg0) {
                    boolean selected = !wizardColumn.isEnabled();
                    wizardColumn.setEnabled(selected);
                    viewer.update(wizardColumn, null);
                }
            });

            item = new MenuItem(menu, SWT.SEPARATOR);
            
            item = new MenuItem(menu, SWT.NONE);
            item.setText(Resources.getMessage("ImportWizardPageColumns.32")); //$NON-NLS-1$
            item.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent arg0) {
                    String name = wizardImport.getController()
                                              .actionShowInputDialog(wizardImport.getShell(),
                                                                     Resources.getMessage("ImportWizardPageColumns.38"), //$NON-NLS-1$
                                                                     Resources.getMessage("ImportWizardPageColumns.39"), //$NON-NLS-1$
                                                                     wizardColumn.getColumn().getAliasName());
                    if (name != null) {
                        wizardColumn.getColumn().setAliasName(name);
                        viewer.update(wizardColumn, null);
                    }
                }
            });

            item = new MenuItem(menu, SWT.SEPARATOR);
            
            Menu datatype = new Menu(menu);
            item = new MenuItem(menu, SWT.CASCADE);
            item.setText(Resources.getMessage("ImportWizardPageColumns.33")); //$NON-NLS-1$
            item.setMenu(datatype);
            
            Menu matching = new Menu(datatype);
            item = new MenuItem(datatype, SWT.CASCADE);
            item.setText(Resources.getMessage("ImportWizardPageColumns.34")); //$NON-NLS-1$
            item.setMenu(matching);
            
            Menu nonmatching = new Menu(datatype);
            item = new MenuItem(datatype, SWT.CASCADE);
            item.setText(Resources.getMessage("ImportWizardPageColumns.35")); //$NON-NLS-1$
            item.setMenu(nonmatching);
            
            // For each entry
            for (final Entry<String, DataType<?>> entry : this.matching.get(wizardColumn).entrySet()) {

                // Create menu item
                item = new MenuItem(matching, SWT.NONE);
                item.setText(entry.getKey());
                item.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent arg0) {
                        ImportColumn column = wizardColumn.getColumn();
                        column.setDataType(entry.getValue());
                        viewer.update(wizardColumn, null);
                        return;
                    }
                });
            }

            // For each entry
            for (final Entry<String, DataType<?>> entry : this.nonmatching.get(wizardColumn).entrySet()) {

                // Create menu item
                item = new MenuItem(nonmatching, SWT.NONE);
                item.setText(entry.getKey());
                item.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent arg0) {
                        ImportColumn column = wizardColumn.getColumn();
                        column.setDataType(entry.getValue());
                        viewer.update(wizardColumn, null);
                        return;
                    }
                });
            }
            
            item = new MenuItem(menu, SWT.SEPARATOR);
            
            item = new MenuItem(menu, SWT.NONE);
            item.setText(Resources.getMessage("ImportWizardPageColumns.36")); //$NON-NLS-1$
            item.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent arg0) {
                    selectAll(true);
                }
            });

            item = new MenuItem(menu, SWT.NONE);
            item.setText(Resources.getMessage("ImportWizardPageColumns.37")); //$NON-NLS-1$
            item.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent arg0) {
                    selectAll(false);
                }
            });

            // Done
            return menu;
        }
        
        /**
         * Selects all or none of the columns
         * @param select
         */
        private void selectAll(boolean select) {
            for (int i = 0; i < table.getItems().length; i++) {
                wizardImport.getData().getWizardColumns().get(i).setEnabled(select);
                viewer.update(wizardImport.getData().getWizardColumns().get(i), null);
            }
            check();
        }
        
        /**
         * Update the context menu
         * @param columns
         */
        public void update(List<ImportWizardModelColumn> columns) {
            
            // Return if no columns
            if (columns == null) {
                return;
            }

            // Clear
            this.matching.clear();
            this.nonmatching.clear();
            
            // Match types
            Map<ImportWizardModelColumn, List<Pair<DataType<?>, Double>>> matchingtypes = wizardImport.getData().getMatchingDataTypes(columns);
            
            // Process
            for (ImportWizardModelColumn column : columns) {
                
                // Prepare
                this.matching.put(column, new LinkedHashMap<String, DataType<?>>());
                this.nonmatching.put(column, new LinkedHashMap<String, DataType<?>>());
                List<String> labels = new ArrayList<String>();
                List<DataType<?>> types = new ArrayList<DataType<?>>();
                
                // Enable first match
                column.getColumn().setDataType(matchingtypes.get(column).iterator().next().getFirst());
                viewer.update(column, null);
                
                // Store rest
                for (Pair<DataType<?>, Double> match : matchingtypes.get(column)) {
                    
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
                    
                    if (match.getSecond() > 0.5d) {
                        this.matching.get(column).put(label, type);
                    } else {
                        this.nonmatching.get(column).put(label, type);   
                    }
                }
            }
        }
    }

    /** Reference to the wizard containing this page. */
    private ImportWizard           wizardImport;
    
    /** View */
    private Table                  table;

    /** View */
    private TableViewer            tableViewer;
    
    /** View*/
    private DatatypeContextMenu    tableDatatypeContextMenu; 

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
    private Button                 btnUp;

    /** View */
    private Button                 btnDown;

    /** View */
    private Button                 btnCleansing;

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
        tableViewer = SWTUtil.createTableViewer(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL);
        tableViewer.setContentProvider(new ArrayContentProvider());

        /* Actual table for {@link #checkboxTableViewer} */
        table = tableViewer.getTable();
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
        tableViewerColumnEnabled = new TableViewerColumn(tableViewer, SWT.NONE);
        tableViewerColumnEnabled.setLabelProvider(new ColumnLabelProvider() {
            /** Cells within this column should always be empty */
            @Override
            public String getText(Object element) {
                ImportWizardModelColumn column = (ImportWizardModelColumn) element;
                return column.isEnabled() ? Resources.getMessage("ImportWizardPageColumns.29") : Resources.getMessage("ImportWizardPageColumns.30"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        });

        /* Actual column for {@link tableViewerColumnEnabled} */
        tblclmnEnabled = tableViewerColumnEnabled.getColumn();
        tblclmnEnabled.setToolTipText(Resources.getMessage("ImportWizardPageColumns.11")); //$NON-NLS-1$
        tblclmnEnabled.setText(Resources.getMessage("ImportWizardPageColumns.12")); //$NON-NLS-1$
        tblclmnEnabled.setWidth(40);

        /* Column containing the names */
        tableViewerColumnName = new TableViewerColumn(tableViewer, SWT.NONE);
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
        tableViewerColumnDatatype = new TableViewerColumn(tableViewer, SWT.NONE);
        tableDatatypeContextMenu = new DatatypeContextMenu(tableViewer, wizardImport.getData().getWizardColumns());
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
        tableViewerColumnFormat = new TableViewerColumn(tableViewer, SWT.NONE);
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
                    tableViewer.setInput(columns);
                    tableViewer.getTable().select(current - 1);
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
                    tableViewer.setInput(columns);
                    tableViewer.getTable().select(current + 1);
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
        
        // Hint
        setMessage(Resources.getMessage("ImportWizardPageColumns.28")); //$NON-NLS-1$

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
            tableDatatypeContextMenu.update(wizardImport.getData().getWizardColumns());
            tableViewer.setInput(wizardImport.getData().getWizardColumns());
            check();
        }
    }

    /**
     * Checks whether the current selection of columns is suited for import
     */
    private void check(){

        // Check selection
        boolean selected = false;
        for (ImportWizardModelColumn column : wizardImport.getData().getWizardColumns()) {
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