package org.deidentifier.arx.gui.view.impl.wizard.importdata;

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.Entry;
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


public class ColumnPage extends WizardPage {

    private ImportDataWizard wizardImport;

    private Table table;
    private CheckboxTableViewer checkboxTableViewer;
    private TableColumn tblclmnName;
    private TableViewerColumn tableViewerColumnName;
    private TableColumn tblclmnDatatype;
    private TableViewerColumn tableViewerColumnDatatype;
    private TableColumn tblclmnEnabled;
    private TableViewerColumn tableViewerColumnEnabled;

    private Boolean selectAll = true;


    public ColumnPage(ImportDataWizard wizardImport)
    {

        super("WizardImportCsvPage");

        this.wizardImport = wizardImport;

        setTitle("Columns");
        setDescription("Please check and/or modify the settings below");

    }

    public void createControl(Composite parent)
    {

        Composite container = new Composite(parent, SWT.NULL);

        setControl(container);
        container.setLayout(new GridLayout(1, false));

        checkboxTableViewer = CheckboxTableViewer.newCheckList(container, SWT.BORDER | SWT.FULL_SELECTION);
        checkboxTableViewer.setContentProvider(new ArrayContentProvider());
        checkboxTableViewer.addCheckStateListener(new ICheckStateListener() {

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

        table = checkboxTableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        tableViewerColumnEnabled = new TableViewerColumn(checkboxTableViewer, SWT.NONE);
        tableViewerColumnEnabled.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element)
            {

                return null;

            }

        });

        tblclmnEnabled = tableViewerColumnEnabled.getColumn();
        tblclmnEnabled.setToolTipText("Select all");
        tblclmnEnabled.setWidth(30);
        tblclmnEnabled.addSelectionListener(new ColumnEnabledSelectionListener());

        tableViewerColumnName = new TableViewerColumn(checkboxTableViewer, SWT.NONE);
        tableViewerColumnName.setEditingSupport(new NameEditingSupport(checkboxTableViewer));
        tableViewerColumnName.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element)
            {

                ImportDataColumn column = (ImportDataColumn)element;

                return column.getName();

            }

        });

        tblclmnName = tableViewerColumnName.getColumn();
        tblclmnName.setToolTipText("Name of the column");
        tblclmnName.setWidth(300);
        tblclmnName.setText("Name");

        tableViewerColumnDatatype = new TableViewerColumn(checkboxTableViewer, SWT.NONE);
        tableViewerColumnDatatype.setEditingSupport(new DatatypeEditingSupport(checkboxTableViewer));
        tableViewerColumnDatatype.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element)
            {

                DataType<?> column = ((ImportDataColumn) element).getDatatype();

                for (Entry<?> entry : DataType.LIST) {

                    if (entry.newInstance().getClass() == column.getClass()) {

                        return entry.getLabel();

                    }

                }

                return null;

            }

        });

        tblclmnDatatype = tableViewerColumnDatatype.getColumn();
        tblclmnDatatype.setToolTipText("Datatype of the column");
        tblclmnDatatype.setWidth(100);
        tblclmnDatatype.setText("Datatype");

        setPageComplete(false);

    }

    @Override
    public void setVisible(boolean visible)
    {

        super.setVisible(visible);

        if (visible) {

            checkboxTableViewer.setInput(wizardImport.getData().getColumns());

        }

    }

    private final class ColumnEnabledSelectionListener extends SelectionAdapter {

        @Override
        public void widgetSelected(SelectionEvent arg0) {

            for(int i = 0; i < table.getItems().length; i++) {

                setChecked(i, selectAll);

            }

            selectAll = !selectAll;

            if (selectAll) {

                tblclmnEnabled.setToolTipText("Select all");

            } else {

                tblclmnEnabled.setToolTipText("Deselect all");

            }

        }

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
     *
     * TODO Implement better editor for datatypes of arx framework
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

            for (Entry<?> entry : DataType.LIST) {

                labels.add(entry.getLabel());

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

            for (Entry<?> entry : DataType.LIST) {

                if (entry.newInstance().getClass() == datatype.getClass()) {

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

            for (Entry<?> entry : DataType.LIST) {

                if (entry.getLabel().equals(label)) {

                    ((ImportDataColumn)element).setDatatype(entry.newInstance());
                    getViewer().update(element, null);

                    return;

                }

            }

        }

    }

}
