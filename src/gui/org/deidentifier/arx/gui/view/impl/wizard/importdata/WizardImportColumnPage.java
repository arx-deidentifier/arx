package org.deidentifier.arx.gui.view.impl.wizard.importdata;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;


public class WizardImportColumnPage extends WizardPage {

    private WizardImport wizardImport;

    private Table table;
    private CheckboxTableViewer checkboxTableViewer;
    private TableColumn tblclmnName;
    private TableViewerColumn tableViewerColumnName;
    private TableColumn tblclmnDatatype;
    private TableViewerColumn tableViewerColumnDatatype;
    private TableColumn tblclmnEnabled;
    private TableViewerColumn tableViewerColumnEnabled;

    private Boolean selectAll = true;


    public WizardImportColumnPage(WizardImport wizardImport)
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

                ((WizardImportDataColumn)event.getElement()).setEnabled(event.getChecked());

                for (WizardImportDataColumn column : wizardImport.getData().getColumns()) {

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
        tableViewerColumnName.setEditingSupport(new WizardImportColumnPageNameEditingSupport(checkboxTableViewer));
        tableViewerColumnName.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element)
            {

                WizardImportDataColumn column = (WizardImportDataColumn)element;

                return column.getName();

            }

        });

        tblclmnName = tableViewerColumnName.getColumn();
        tblclmnName.setToolTipText("Name of the column");
        tblclmnName.setWidth(300);
        tblclmnName.setText("Name");

        tableViewerColumnDatatype = new TableViewerColumn(checkboxTableViewer, SWT.NONE);
        tableViewerColumnDatatype.setEditingSupport(new WizardImportColumnPageDatatypeEditingSupport(checkboxTableViewer));
        tableViewerColumnDatatype.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element)
            {

                WizardImportDataColumn column = (WizardImportDataColumn)element;

                return column.getDatatype();

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

            for (WizardImportDataColumn column : wizardImport.getData().getColumns()) {

                if (column.isEnabled()) {

                    setPageComplete(true);

                    return;

                }

            }

        }

    }

}
