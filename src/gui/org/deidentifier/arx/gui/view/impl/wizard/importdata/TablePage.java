package org.deidentifier.arx.gui.view.impl.wizard.importdata;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;


public class TablePage extends WizardPage {

    private ImportDataWizard wizardImport;

    private Table table;
    private TableViewer checkboxTableViewer;
    private TableColumn tblclmnName;
    private TableViewerColumn tableViewerColumnName;
    private TableColumn tblclmnNumberOfRows;
    private TableViewerColumn tableViewerNumberOfRows;


    public TablePage(ImportDataWizard wizardImport)
    {

        super("WizardImportTablePage");

        this.wizardImport = wizardImport;

        setTitle("Tables");
        setDescription("Please select the table you want to import from");

    }

    public void createControl(Composite parent)
    {

        Composite container = new Composite(parent, SWT.NULL);

        setControl(container);
        container.setLayout(new GridLayout(1, false));

        checkboxTableViewer = CheckboxTableViewer.newCheckList(container, SWT.BORDER | SWT.FULL_SELECTION);
        checkboxTableViewer.setContentProvider(new ArrayContentProvider());
        ((CheckboxTableViewer)checkboxTableViewer).addCheckStateListener(new ICheckStateListener() {

            @Override
            public void checkStateChanged(CheckStateChangedEvent event)
            {

                setPageComplete(false);

                ((ImportDataColumn)event.getElement()).setEnabled(event.getChecked());

                for (ImportDataColumn column : wizardImport.getData().getWizardColumns()) {

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

        tableViewerColumnName = new TableViewerColumn(checkboxTableViewer, SWT.NONE);
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

        tableViewerNumberOfRows = new TableViewerColumn(checkboxTableViewer, SWT.NONE);
        tableViewerNumberOfRows.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element)
            {

                return null;

            }

        });

        tblclmnNumberOfRows = tableViewerNumberOfRows.getColumn();
        tblclmnNumberOfRows.setToolTipText("Number of rows");
        tblclmnNumberOfRows.setWidth(100);
        tblclmnNumberOfRows.setText("# rows");

        setPageComplete(false);

    }

}
