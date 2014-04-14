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


/**
 * Table overview page
 *
 * This pages gives the user an overview of the detected tables and allows him
 * to select the desired one by clicking on it. The tables itself are retrieved
 * from {@link ImportData#getJdbcTables()()}. The selected one will be assigned
 * via {@link ImportData#setSelectedJdbcTable(String)} along with the detected
 * columns for this table using {@link ImportData#setWizardColumns(List)} and
 * its preview data {@link ImportData#setPreviewData(List)}.
 */
public class TablePage extends WizardPage {

    /**
     * Reference to the wizard containing this page
     */
    private ImportDataWizard wizardImport;

    /* SWT Widgets */
    private Table table;
    private TableViewer tableViewer;


    /**
     * Creates a new instance of this page and sets its title and description
     *
     * @param wizardImport Reference to wizard containing this page
     */
    public TablePage(ImportDataWizard wizardImport)
    {

        super("WizardImportTablePage");
        this.wizardImport = wizardImport;
        setTitle("Tables");
        setDescription("Please select the table you want to import from");

    }

    /**
     * Creates the design of this page along with the appropriate listeners
     */
    public void createControl(Composite parent)
    {

        Composite container = new Composite(parent, SWT.NULL);

        setControl(container);
        container.setLayout(new GridLayout(1, false));

        /* TableViewer for the detected tables */
        tableViewer = new TableViewer(container, SWT.BORDER | SWT.FULL_SELECTION);
        tableViewer.setContentProvider(new ArrayContentProvider());
        tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            /**
             * Reads in the columns and preview data for selected table
             */
            @Override
            public void selectionChanged(SelectionChangedEvent arg0)
            {

                /* Save selected table */
                int index = table.getSelectionIndex();
                String selectedTable = wizardImport.getData().getJdbcTables().get(index);
                wizardImport.getData().setSelectedJdbcTable(selectedTable);

                readColumns();
                readPreview();

                setPageComplete(true);

            }

        });

        /* Table for {@link #tableViewer} */
        table = tableViewer.getTable();
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        /* Column for table names */
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

    /**
     * Reads in the columns of currently selected table
     *
     * If this can be performed successful, the columns will be made available
     * for the next page by {@link ImportData#setWizardColumns(List)}.
     * Otherwise an appropriate error message is set.
     */
    private void readColumns()
    {

        String selectedTable = wizardImport.getData().getSelectedJdbcTable();

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

    /**
     * Reads in the preview data for currently selected table
     *
     * If this can be performed successful, the preview data will be made
     * available for the following pages by
     * {@link ImportData#setPreviewData(List)}. Otherwise an appropriate error
     * message is set.
     */
    protected void readPreview()
    {

        String selectedTable = wizardImport.getData().getSelectedJdbcTable();

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

    /**
     * Applies previously detected tables to {@link #tableViewer}
     */
    @Override
    public void setVisible(boolean visible)
    {

        super.setVisible(visible);

        if (visible) {

            tableViewer.setInput(wizardImport.getData().getJdbcTables());

            /* Mark page as complete when table has been selected before */
            if (wizardImport.getData().getSelectedJdbcTable() != null) {

                setPageComplete(true);

            }

        } else {

            setPageComplete(false);

        }

    }

}
