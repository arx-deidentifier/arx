/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2014 Karol Babioch <karol@babioch.de>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.arx.gui.view.impl.wizard.importdata;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.io.datasource.column.JdbcColumn;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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

    /* Widgets */
    private Table table;
    private TableViewer tableViewer;


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

        tableViewer = new TableViewer(container, SWT.BORDER | SWT.FULL_SELECTION);
        tableViewer.setContentProvider(new ArrayContentProvider());
        tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent arg0)
            {

                int index = table.getSelectionIndex();
                String selectedTable = wizardImport.getData().getJdbcTables().get(index);

                readColumns(selectedTable);
                readPreview(selectedTable);

                wizardImport.getData().setSelectedJdbcTable(selectedTable);
                setPageComplete(true);

            }

        });

        table = tableViewer.getTable();
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        TableViewerColumn tableViewerColumnName = new TableViewerColumn(tableViewer, SWT.NONE);
        tableViewerColumnName.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element)
            {

                return (String)element;

            }

        });

        TableColumn tblclmnColumnName = tableViewerColumnName.getColumn();
        tblclmnColumnName.setWidth(100);
        tblclmnColumnName.setText("Name");

        setPageComplete(false);

    }

    private void readColumns(String selectedTable)
    {

        Connection connection = wizardImport.getData().getJdbcConnection();
        List<WizardColumn> columns = new ArrayList<WizardColumn>();

        int i = 0;
        try {

            ResultSet rs = connection.getMetaData().getColumns(null, null, selectedTable, null);

            while(rs.next()) {

                JdbcColumn column = new JdbcColumn(i++, rs.getString("COLUMN_NAME"), DataType.STRING);
                columns.add(new WizardColumn(column));

            }

        } catch (SQLException e) {

            setErrorMessage("Couldn't read columns");

        }

        wizardImport.getData().setWizardColumns(columns);

    }

    protected void readPreview(String selectedTable)
    {

        List<String[]> previewData = new ArrayList<String[]>();
        Connection connection = wizardImport.getData().getJdbcConnection();

        try {

            Statement statement = connection.createStatement();
            statement.setMaxRows(ImportData.previewDataMaxLines);
            statement.execute("SELECT * FROM " + selectedTable);
            ResultSet rs = statement.getResultSet();

            while(rs.next()) {

                String[] previewRow = new String[rs.getMetaData().getColumnCount()];

                for (int j = 0; j < previewRow.length; j++) {

                    previewRow[j] = rs.getString(j + 1);

                }

                previewData.add(previewRow);

            }

        } catch (SQLException e) {

            setErrorMessage("Couldn't read preview");

        }

        wizardImport.getData().setPreviewData(previewData);

    }

    @Override
    public void setVisible(boolean visible)
    {

        super.setVisible(visible);

        if (visible) {

            tableViewer.setInput(wizardImport.getData().getJdbcTables());

        } else {

            setPageComplete(false);

        }

    }

}
