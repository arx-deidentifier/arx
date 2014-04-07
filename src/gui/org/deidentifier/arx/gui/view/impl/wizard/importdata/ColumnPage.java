package org.deidentifier.arx.gui.view.impl.wizard.importdata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.DataTypeDescription;
import org.deidentifier.arx.DataType.DataTypeWithFormat;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.io.importdata.Column;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;


/**
 * Column overview page
 *
 * This pages gives the user an overview of the detected columns and allows him
 * to change things around. First of all columns can be enabled or disabled
 * on an individual basis. Secondly the order of the columns can be changed
 * around. Furthermore a data type along with a format string can be defined
 * for each column.
 *
 * A single column is represented by {@link WizardColumn}, the list of all
 * detected columns can be accessed by {@link ImportData#getWizardColumns()}.
 */
public class ColumnPage extends WizardPage {

    /**
     * Reference to the wizard containing this page
     */
    private ImportDataWizard wizardImport;

    /* Widgets */
    private Table table;
    private CheckboxTableViewer checkboxTableViewer;
    private TableColumn tblclmnName;
    private TableViewerColumn tableViewerColumnName;
    private TableColumn tblclmnDatatype;
    private TableViewerColumn tableViewerColumnDatatype;
    private TableColumn tblclmnEnabled;
    private TableViewerColumn tableViewerColumnEnabled;
    private TableColumn tblclmnFormat;
    private TableViewerColumn tableViewerColumnFormat;
    private Button btnUp;
    private Button btnDown;

    /**
     * Indicator for the next action of {@link ColumnEnabledSelectionListener}
     */
    private boolean selectAll = false;


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
        setDescription("Please check and/or modify the detected columns");

    }

    /**
     * Creates the design of this page along with the appropriate listeners
     */
    public void createControl(Composite parent)
    {

        Composite container = new Composite(parent, SWT.NULL);

        setControl(container);
        container.setLayout(new GridLayout(2, false));

        /* TableViewer for the columns with a checkbox in each row */
        checkboxTableViewer = CheckboxTableViewer.newCheckList(container, SWT.BORDER | SWT.FULL_SELECTION);
        checkboxTableViewer.setContentProvider(new ArrayContentProvider());
        checkboxTableViewer.setCheckStateProvider(new ICheckStateProvider() {

            /**
             * No column should be grayed out
             */
            @Override
            public boolean isGrayed(Object column)
            {

                return false;

            }

            /**
             * @return {@link WizardColumn#isEnabled()}
             */
            @Override
            public boolean isChecked(Object column)
            {

                return ((WizardColumn)column).isEnabled();

            }

        });
        checkboxTableViewer.addCheckStateListener(new ICheckStateListener() {

            /**
             * Sets the enabled status for the given item
             *
             * Using {@link WizardColumn#setEnabled(boolean)} this method will
             * set the enabled flag for the given column. Furthermore it makes
             * sure the page is marked as complete once at least one item is
             * selected.
             */
            @Override
            public void checkStateChanged(CheckStateChangedEvent event)
            {

                setPageComplete(false);

                ((WizardColumn)event.getElement()).setEnabled(event.getChecked());

                for (WizardColumn column : wizardImport.getData().getWizardColumns()) {

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
             * This checks the current selection and will enable and/or
             * disable the {@link #btnUp} and {@link #btnDown} if either
             * the first or last item is currently selected.
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

            /**
             * Cells within this column should always be empty
             */
            @Override
            public String getText(Object element)
            {

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
             * Gets name of cells from  {@link Column#getName()}
             */
            @Override
            public String getText(Object element)
            {

                WizardColumn column = (WizardColumn)element;

                return column.getColumn().getName();

            }

        });

        /* Actual column for {@link tableViewerColumnName} */
        tblclmnName = tableViewerColumnName.getColumn();
        tblclmnName.setToolTipText("Name of the column");
        tblclmnName.setWidth(300);
        tblclmnName.setText("Name");

        /* Column containing the datatypes */
        tableViewerColumnDatatype = new TableViewerColumn(checkboxTableViewer, SWT.NONE);
        tableViewerColumnDatatype.setEditingSupport(new DatatypeEditingSupport(checkboxTableViewer));
        tableViewerColumnDatatype.setLabelProvider(new ColumnLabelProvider() {

            /**
             * Gets string representation for given datatype of column
             *
             * Internally it makes use of {@link Column#getDataType()}.
             */
            @Override
            public String getText(Object element)
            {

                DataType<?> datatype = ((WizardColumn) element).getColumn().getDataType();

                for (DataTypeDescription<?> description : DataType.LIST) {

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
        tableViewerColumnFormat = new TableViewerColumn(checkboxTableViewer, SWT.NONE);
        tableViewerColumnFormat.setLabelProvider(new ColumnLabelProvider() {

            /**
             * Returns format string of datatype for column
             *
             * This retrieves the used format string of the chosen datatype for
             * each column.
             *
             * @note In case of simple datatypes without a format specifier an
             * empty string is returned.
             *
             * @param element Column in question
             */
            @Override
            public String getText(Object element)
            {

                DataType<?> column = ((WizardColumn) element).getColumn().getDataType();

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
        btnUp.setImage(wizardImport.getController().getResources().getImage("arrow_up.png"));
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

                    List<WizardColumn> columns = wizardImport.getData().getWizardColumns();
                    Collections.swap(columns, current, current - 1);
                    checkboxTableViewer.setInput(columns);
                    table.notifyListeners(SWT.Selection, null);

                }

            }

        });

        /* Buttons to move column down */
        btnDown = new Button(container, SWT.NONE);
        btnDown.setText("Move down");
        btnDown.setImage(wizardImport.getController().getResources().getImage("arrow_down.png"));
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

                    List<WizardColumn> columns = wizardImport.getData().getWizardColumns();
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
     * Adds input to table viewer once page gets visible
     */
    @Override
    public void setVisible(boolean visible)
    {

        super.setVisible(visible);

        if (visible) {

            checkboxTableViewer.setInput(wizardImport.getData().getWizardColumns());
            setPageComplete((wizardImport.getData().getWizardColumns().size() > 0));

        }

    }

    /**
     * Listener for click events of the "enabled" column
     *
     * By clicking on the column all items can be selected and/or deselected
     * at once. The result of the action depends upon {@link #selectAll}.
     */
    private final class ColumnEnabledSelectionListener extends SelectionAdapter {

        /**
         * (Un)checks all of the items at once
         *
         * This iterates through all of the items and invokes
         * {@link #setChecked(int, Boolean)} for all of them. Furthermore the
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
            wizardImport.getData().getWizardColumns().get(i).setEnabled(check);

            setPageComplete(false);

            for (WizardColumn column : wizardImport.getData().getWizardColumns()) {

                if (column.isEnabled()) {

                    setPageComplete(true);

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

        /**
         * Reference to actual editor
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
         * Indicates that enabled cells within this column can be edited
         */
        @Override
        protected boolean canEdit(Object column)
        {

            if (((WizardColumn) column).isEnabled()) {

                return true;

            }

            return false;

        }

        @Override
        protected CellEditor getCellEditor(Object arg0)
        {

            return editor;

        }

        /**
         * Retrieves name of column ({@link Column#getName()})
         */
        @Override
        protected Object getValue(Object arg0)
        {

            return ((WizardColumn)arg0).getColumn().getName();

        }

        /**
         * Sets name for given column ({@link Column#setName(String)})
         */
        @Override
        protected void setValue(Object element, Object value)
        {

            ((WizardColumn)element).getColumn().setName((String)value);
            getViewer().update(element, null);

        }

    }


    /**
     * Implements editing support for datatype column within the column page
     *
     * This allows to change the datatype of columns. The modifications are
     * performed with a combo box {@link ComboBoxCellEditor}.
     */
    public class DatatypeEditingSupport extends EditingSupport {

        /**
         * Reference to actual editor
         */
        private ComboBoxCellEditor editor;

        /**
         * Allowed values for the user to choose from
         *
         * This array contains all of the choices the user can make. The array
         * gets populated during runtime.
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

            for (DataTypeDescription<?> description : DataType.LIST) {

                /* Remove OrderedString from list of choices for now */
                if (description.newInstance().getClass() == DataType.ORDERED_STRING.getClass()) {

                    continue;

                }

                labels.add(description.getLabel());

            }

            choices = labels.toArray(new String[labels.size()]);
            editor = new ComboBoxCellEditor(viewer.getTable(), choices, SWT.READ_ONLY);

        }

        /**
         * Indicates that enabled cells within this column can be edited
         */
        @Override
        protected boolean canEdit(Object column)
        {

            if (((WizardColumn) column).isEnabled()) {

                return true;

            }

            return false;

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
         * Returns current index of {@link #choices} for given column datatype
         */
        @Override
        protected Object getValue(Object element)
        {

            DataType<?> datatype = ((WizardColumn)element).getColumn().getDataType();

            int i = 0;

            for (DataTypeDescription<?> description : DataType.LIST) {

                if (description.newInstance().getClass() == datatype.getClass()) {

                    return i;

                }

                i++;

            }

            return null;


        }

        /**
         * Applies datatype choice made by the user
         *
         * If a datatype, which requires a format string, was selected an input
         * dialog will be shown {@link actionShowFormatInputDialog}. Otherwise
         * the choice is directly applied. THe input dialog itself will make
         * sure that the format string is valid for the datatype. This method
         * on the other hand will try to apply the format string to the
         * available preview data {@link ImportData#getPreviewData()} making
         * sure that it matches. In case of an error the choice is discarded.
         */
        @Override
        protected void setValue(Object element, Object value)
        {

            String label = choices[(int) value];
            WizardColumn wizardColumn = (WizardColumn)element;
            Column column = wizardColumn.getColumn();
            List<String> previewData;

            try {

                previewData = wizardImport.getData().getPreviewData(wizardColumn);

            } catch (Exception e) {

                return;

            }

            for (DataTypeDescription<?> description : DataType.LIST) {

                if (description.getLabel().equals(label)) {

                    DataType<?> datatype = null;

                    if (description.hasFormat()) {

                        final Controller controller = wizardImport.getController();
                        String format = null;

                        if (column.getDataType().getClass() == description.newInstance().getClass()) {

                            format = controller.actionShowFormatInputDialog(
                                "Format string",
                                "Please provide a format string describing each item of this column",
                                ((DataTypeWithFormat)column.getDataType()).getFormat(),
                                description,
                                previewData
                            );

                        } else {

                            format = controller.actionShowFormatInputDialog(
                                "Format string",
                                "Please provide a format string describing each item of this column",
                                description,
                                previewData
                            );

                        }

                        if (format != null) {

                            datatype = description.newInstance(format);

                        } else {

                            /* Invalid string or aborted by user */
                            return;

                        }

                    } else {

                        /* Datatype has no format */
                        datatype = description.newInstance();

                    }

                    /* Apply datatype */
                    column.setDataType(datatype);
                    getViewer().update(element, null);

                    return;

                }

            }

        }

    }

}
