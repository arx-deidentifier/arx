package org.deidentifier.arx.gui.view.impl.wizard.importdata;

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.DataTypeDescription;
import org.deidentifier.arx.gui.Controller;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;


/**
 * Column overview page
 *
 * This pages gives the user an overview of the columns, allows him to change
 * the name and datatype of each column and whether or not it should actually
 * be imported.
 *
 * The columns need to be stored within {{@link ImportDataWizard#data} and
 * every change is written back to this object.
 */
public class ColumnPage extends WizardPage {

    /**
     * Reference to the wizard containing this page
     */
    private ImportDataWizard wizardImport;

    /*
     * Widgets
     */
    private Table table;
    private CheckboxTableViewer checkboxTableViewer;
    private TableColumn tblclmnName;
    private TableViewerColumn tableViewerColumnName;
    private TableColumn tblclmnDatatype;
    private TableViewerColumn tableViewerColumnDatatype;
    private TableColumn tblclmnEnabled;
    private TableViewerColumn tableViewerColumnEnabled;

    /**
     * Indicator for the next action of {@link ColumnEnabledSelectionListener}
     */
    private Boolean selectAll = true;


    /**
     * Creates a new instance of this page and sets its title and description
     *
     * @param wizardImport Reference to wizard containing this page
     */
    public ColumnPage(ImportDataWizard wizardImport)
    {

        super("WizardImportCsvPage");

        this.wizardImport = wizardImport;

        setTitle("Columns");
        setDescription("Please check and/or modify the settings below");

    }

    /**
     * Creates the design of this page along with the appropriate listeners
     */
    public void createControl(Composite parent)
    {

        Composite container = new Composite(parent, SWT.NULL);

        setControl(container);
        container.setLayout(new GridLayout(1, false));

        /*
         * TableViewer for the columns with a checkbox in each row
         */
        checkboxTableViewer = CheckboxTableViewer.newCheckList(container, SWT.BORDER | SWT.FULL_SELECTION);
        checkboxTableViewer.setContentProvider(new ArrayContentProvider());
        checkboxTableViewer.addCheckStateListener(new ICheckStateListener() {

            /**
             * Sets the status of the given item
             *
             * Furthermore marks the page as completed once at least one item
             * is selected.
             */
            @Override
            public void checkStateChanged(CheckStateChangedEvent event)
            {

                setPageComplete(false);

                ((ImportDataColumn)event.getElement()).setEnabled(event.getChecked());

                for (ImportDataColumn column : wizardImport.getData().getColumns()) {

                    if (column.isEnabled()) {

                        setPageComplete(true);

                        return;

                    }

                }

            }

        });

        /*
         * Actual table for {@link #checkboxTableViewer}
         */
        table = checkboxTableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        /*
         * Pseudo column to make checkboxes appear in a cell
         */
        tableViewerColumnEnabled = new TableViewerColumn(checkboxTableViewer, SWT.NONE);
        tableViewerColumnEnabled.setLabelProvider(new ColumnLabelProvider() {

            /**
             * Always empty as the cells should only contain the checkbox
             */
            @Override
            public String getText(Object element)
            {

                return null;

            }

        });

        /*
         * Actual column for {@link tableViewerColumnEnabled}
         */
        tblclmnEnabled = tableViewerColumnEnabled.getColumn();
        tblclmnEnabled.setToolTipText("Select all");
        tblclmnEnabled.setText("SA");
        tblclmnEnabled.setWidth(40);
        tblclmnEnabled.addSelectionListener(new ColumnEnabledSelectionListener());

        /*
         * Column containing the names
         */
        tableViewerColumnName = new TableViewerColumn(checkboxTableViewer, SWT.NONE);
        tableViewerColumnName.setEditingSupport(new NameEditingSupport(checkboxTableViewer));
        tableViewerColumnName.setLabelProvider(new ColumnLabelProvider() {

            /**
             * Gets name of cells from  {@link ImportDataColumn#getName()}
             */
            @Override
            public String getText(Object element)
            {

                ImportDataColumn column = (ImportDataColumn)element;

                return column.getName();

            }

        });

        /*
         * Actual column for {@link tableViewerColumnName}
         */
        tblclmnName = tableViewerColumnName.getColumn();
        tblclmnName.setToolTipText("Name of the column");
        tblclmnName.setWidth(300);
        tblclmnName.setText("Name");

        /*
         * Column containing the datatypes
         */
        tableViewerColumnDatatype = new TableViewerColumn(checkboxTableViewer, SWT.NONE);
        tableViewerColumnDatatype.setEditingSupport(new DatatypeEditingSupport(checkboxTableViewer));
        tableViewerColumnDatatype.setLabelProvider(new ColumnLabelProvider() {

            /**
             * Gets datatype of cells from {@link ImportDataColumn#getDatatype()}
             */
            @Override
            public String getText(Object element)
            {

                DataType<?> column = ((ImportDataColumn) element).getDatatype();

                for (DataTypeDescription<?> DataTypeDescription : DataType.LIST) {

                    if (DataTypeDescription.newInstance().getClass() == column.getClass()) {

                        return DataTypeDescription.getLabel();

                    }

                }

                return null;

            }

        });

        /*
         * Actual column for {@link tableViewerColumnDatatype}
         */
        tblclmnDatatype = tableViewerColumnDatatype.getColumn();
        tblclmnDatatype.setToolTipText("Datatype of the column");
        tblclmnDatatype.setWidth(100);
        tblclmnDatatype.setText("Datatype");

        /*
         * Wait for at least one column to be enabled
         */
        setPageComplete(false);

    }

    /**
     * Adds input to table once page gets visible
     */
    @Override
    public void setVisible(boolean visible)
    {

        super.setVisible(visible);

        if (visible) {

            checkboxTableViewer.setInput(wizardImport.getData().getColumns());

        }

    }

    /**
     * Listens for click events of the "enabled" column
     *
     * By clicking on the column all items can be selected and/or deselected
     * at once. The action depends upon {@link ColumnPage#selectAll}.
     */
    private final class ColumnEnabledSelectionListener extends SelectionAdapter {

        /**
         * Iterates through all of the items and invokes
         * {@link #setChecked(int, Boolean)} to all of them. Furthermore the
         * tooltip is changed appropriately.
         */
        @Override
        public void widgetSelected(SelectionEvent arg0) {

            for(int i = 0; i < table.getItems().length; i++) {

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
         * Applies a boolean value to the given item
         *
         * @param i Item that <code>check</code> should be applied to
         * @param check Value that should be applied to item <code>i</code>
         */
        private void setChecked(int i, Boolean check) {

            table.getItem(i).setChecked(check);
            wizardImport.getData().getColumns().get(i).setEnabled(check);

            setPageComplete(false);

            for (ImportDataColumn column : wizardImport.getData().getColumns()) {

                if (column.isEnabled()) {

                    setPageComplete(true);

                    return;

                }

            }

        }

    }


    /**
     * Implements the editing support for name column within the column page
     *
     * This allows to change the name of columns with the column page
     * {@link ColumnPage}. The modifications are performed within a simple text
     * field {@link TextCellEditor}. The name itself is stored with the
     * appropriate {@link ImportDataColumn} object.
     */
    public class NameEditingSupport extends EditingSupport {

        /**
         * Actual editor
         */
        private TextCellEditor editor;


        /**
         * Creates a new editor for the given {@link TableViewer}.
         *
         * @param viewer The TableViewer this editor is implemented for
         */
        public NameEditingSupport(TableViewer viewer)
        {

            super(viewer);

            editor = new TextCellEditor(viewer.getTable());

        }

        /**
         * Indicate that all cells within this column can be edited
         */
        @Override
        protected boolean canEdit(Object arg0)
        {

            return true;

        }

        /**
         * Returns a reference to {@link #editor}.
         */
        @Override
        protected CellEditor getCellEditor(Object arg0)
        {

            return editor;

        }

        /**
         * Gets name of column ({@link ImportDataColumn#getName()})
         */
        @Override
        protected Object getValue(Object arg0)
        {

            return ((ImportDataColumn)arg0).getName();

        }

        /**
         * Sets name of column ({@link ImportDataColumn#setName(String)})
         */
        @Override
        protected void setValue(Object element, Object value)
        {

            ((ImportDataColumn)element).setName((String)value);
            getViewer().update(element, null);

        }

    }


    /**
     * Implements editing support for datatype column within the column page
     *
     * This allows to change the datatype of columns with the column page
     * {@link ColumnPage}. The modifications are performed with a combo box
     * {@link ComboBoxCellEditor}. The datatype itself is stored with the
     * appropriate {@link ImportDataColumn} object.
     */
    public class DatatypeEditingSupport extends EditingSupport {

        /**
         * Actual editor
         */
        private ComboBoxCellEditor editor;

        /**
         * Allowed values for the user to choose from
         */
        private String[] choices;


        /**
         * Creates a new editor for the given {@link TableViewer}.
         *
         * @param viewer The TableViewer this editor is implemented for
         */
        public DatatypeEditingSupport(TableViewer viewer)
        {

            super(viewer);

            List<String> labels = new ArrayList<String>();

            for (DataTypeDescription<?> DataTypeDescription : DataType.LIST) {

                labels.add(DataTypeDescription.getLabel());

            }

            choices = labels.toArray(new String[labels.size()]);

            editor = new ComboBoxCellEditor(viewer.getTable(), choices, SWT.READ_ONLY);

        }

        /**
         * Indicate that all cells within this column can be edited
         */
        @Override
        protected boolean canEdit(Object arg0)
        {

            return true;

        }

        /**
         * Returns a reference to {@link #editor}.
         */
        @Override
        protected CellEditor getCellEditor(Object arg0)
        {

            return editor;

        }

        /**
         * Returns index of datatype for given column
         */
        @Override
        protected Object getValue(Object element)
        {

            DataType<?> datatype = ((ImportDataColumn)element).getDatatype();

            int i = 0;

            for (DataTypeDescription<?> DataTypeDescription : DataType.LIST) {

                if (DataTypeDescription.newInstance().getClass() == datatype.getClass()) {

                    return i;

                }

                i++;

            }

            return null;


        }

        /**
         * Sets new datatype for given column
         *
         * Internally this function makes use of
         * {@link ImportDataColumn#setDatatype(Class)}. The values itself are
         * taken from {@link #choices}.
         */
        @Override
        protected void setValue(Object element, Object value)
        {

            String label = choices[(int) value];

            for (DataTypeDescription<?> DataTypeDescription : DataType.LIST) {

                if (DataTypeDescription.getLabel().equals(label)) {

                    DataType<?> datatype = null;

                    if (DataTypeDescription.hasFormat()) {

                        final Controller controller = wizardImport.getController();

                        final String format = controller.actionShowFormatInputDialog(
                            "Format string",
                            "Please provide a format string describing each item of this column",
                            DataTypeDescription, wizardImport.getData().getPreviewDataForColumn(((ImportDataColumn)element))
                        );

                        if (format != null) {

                            datatype = DataTypeDescription.newInstance(format);

                        } else {

                            /*
                             *  Invalid string or aborted by user
                             */
                            return;

                        }

                    } else {

                        /*
                         * Datatype has no format
                         */
                        datatype = DataTypeDescription.newInstance();

                    }

                    /*
                     * Apply datatype
                     */
                    ((ImportDataColumn)element).setDatatype(datatype);
                    getViewer().update(element, null);

                    return;

                }

            }

        }

    }

}
