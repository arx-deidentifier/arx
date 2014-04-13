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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.io.datasource.ExcelFileConfiguration;
import org.deidentifier.arx.io.datasource.column.Column;
import org.deidentifier.arx.io.datasource.column.ExcelColumn;
import org.deidentifier.arx.io.importdata.ImportAdapter;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;


/**
 * Excel page
 *
 * This page offers means to import data from an Excel file. It contains
 * mechanisms to select such a file, and offers the user the ability to choose
 * the sheet to import from and whether or not the first row contains a header
 * describing each column. A live preview makes sure the user will immediately
 * see whether or not his choices make any sense.
 *
 * All of the data gathered on this page is stored within {@link ImportData}.
 *
 * This includes:
 *
 * <ul>
 *  <li>{@link ImportData#setWizardColumns(List)}</li>
 *  <li>{@link ImportData#setFirstRowContainsHeader(boolean)</li>
 *  <li>{@link ImportData#setFileLocation(String)}</li>
 *  <li>{@link ImportData#setExcelSheetIndex(int)}</li>
 * </ul>
 */
public class ExcelPage extends WizardPage {

    /**
     * Reference to the wizard containing this page
     */
    private ImportDataWizard wizardImport;

    /**
     * Columns detected by this page and passed on to {@link ImportData}
     */
    private ArrayList<WizardColumn> wizardColumns;

    /* Widgets */
    private Label lblLocation;
    private Combo comboLocation;
    private Button btnChoose;
    private Button btnContainsHeader;
    private Combo comboSheet;
    private Label lblSheet;
    private Table tablePreview;
    private TableViewer tableViewerPreview;

    /**
     * Preview data
     */
    ArrayList<String[]> previewData = new ArrayList<String[]>();

    /**
     * Workbook
     *
     * Either HSSFWorkbook or XSSFWorkbook, depending upon file type
     */
    private Workbook workbook;


    /**
     * Creates a new instance of this page and sets its title and description
     *
     * @param wizardImport Reference to wizard containing this page
     */
    public ExcelPage(ImportDataWizard wizardImport)
    {

        super("WizardImportExcelPage");

        setTitle("Excel");
        setDescription("Please provide the information requested below");
        this.wizardImport = wizardImport;

    }

    /**
     * Creates the design of this page
     *
     * This adds all the controls to the page along with their listeners.
     *
     * @note {@link #tablePreview} is not visible until a file is loaded.
     */
    public void createControl(Composite parent)
    {

        Composite container = new Composite(parent, SWT.NULL);
        setControl(container);
        container.setLayout(new GridLayout(3, false));

        /* Location label */
        lblLocation = new Label(container, SWT.NONE);
        lblLocation.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblLocation.setText("Location");

        /* Combo box for selection of file */
        comboLocation = new Combo(container, SWT.READ_ONLY);
        comboLocation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        comboLocation.addSelectionListener(new SelectionAdapter() {

            /**
             * Reads the sheets and selects active one
             */
            @Override
            public void widgetSelected(SelectionEvent arg0) {

                /* Try to read in sheets */
                try {

                    readSheets();

                } catch (IOException e) {

                    setErrorMessage("Couldn't read sheets from file");

                }

                /* Make widgets visible */
                comboSheet.setVisible(true);
                lblSheet.setVisible(true);
                btnContainsHeader.setVisible(true);

                /* Select active sheet and notify comboSheet about change */
                comboSheet.select(workbook.getActiveSheetIndex());
                comboSheet.notifyListeners(SWT.Selection, null);

            }

        });

        /* Button to open file selection dialog */
        btnChoose = new Button(container, SWT.NONE);
        btnChoose.setText("Browse...");
        btnChoose.addSelectionListener(new SelectionAdapter() {

            /**
             * Opens a file selection dialog for Excel files
             *
             * Both XLS and XLSX files can be selected. If a valid file was
             * selected, it is added to {@link #comboLocation} when it wasn't
             * already there. In either case it gets preselected.
             *
             * @see {@link Controller#actionShowOpenFileDialog(String)}
             */
            @Override
            public void widgetSelected(SelectionEvent arg0) {

                /* Open file dialog */
                final String path = wizardImport.getController().actionShowOpenFileDialog("*.xls;*.xlsx");

                if (path == null) {

                    return;

                }

                /* Check whether path was already added */
                if (comboLocation.indexOf(path) == -1) {

                    comboLocation.add(path, 0);

                }

                /* Select path and notify comboLocation about change */
                comboLocation.select(comboLocation.indexOf(path));
                comboLocation.notifyListeners(SWT.Selection, null);

            }

        });

        /* Sheet label */
        lblSheet = new Label(container, SWT.NONE);
        lblSheet.setVisible(false);
        lblSheet.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblSheet.setText("Sheet");

        /* Sheet combobox */
        comboSheet = new Combo(container, SWT.READ_ONLY);
        comboSheet.setVisible(false);
        comboSheet.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        comboSheet.addSelectionListener(new SelectionAdapter() {

            /**
             * (Re-)Evaluate page
             */
            @Override
            public void widgetSelected(final SelectionEvent arg0) {

                evaluatePage();

            }

        });

        /* Place holders */
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);

        /* Contains header button */
        btnContainsHeader = new Button(container, SWT.CHECK);
        btnContainsHeader.setVisible(false);
        btnContainsHeader.setText("First row contains column names");
        btnContainsHeader.setSelection(true);
        btnContainsHeader.addSelectionListener(new SelectionAdapter() {

            /**
             * (Re-)Evaluate page
             */
            @Override
            public void widgetSelected(SelectionEvent arg0) {

                evaluatePage();

            }

        });

        /* Place holders */
        new Label(container, SWT.NONE);

        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);

        /* Preview table viewer */
        tableViewerPreview = new TableViewer(container, SWT.BORDER | SWT.FULL_SELECTION);
        tableViewerPreview.setContentProvider(new ArrayContentProvider());

        /* Actual table for {@link #tableViewerPreview} */
        tablePreview = tableViewerPreview.getTable();
        GridData gd_tablePreview = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
        gd_tablePreview.heightHint = 150;
        tablePreview.setLayoutData(gd_tablePreview);
        tablePreview.setLinesVisible(true);
        tablePreview.setVisible(false);

        /* Set page to incomplete by default */
        setPageComplete(false);

    }

    /**
     * Reads in the available sheets from file
     *
     * This reads in the available sheets from the file chosen at
     * {@link #comboLocation} and adds them as items to {@link #comboSheet}.
     */
    private void readSheets() throws IOException {

        /* Remove previous items */
        comboSheet.removeAll();

        /* Get workbook */
        try {

            workbook = WorkbookFactory.create(new FileInputStream(comboLocation.getText()));

        } catch (InvalidFormatException e) {

            throw new IOException("File format invalid");

        }

        /* Add all sheets to combo */
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {

            comboSheet.add(workbook.getSheetName(i));

        }

    }
    
    /**
     * Reads in preview data
     *
     * This goes through up to {@link ImportData#previewDataMaxLines} lines
     * within the appropriate file and reads them in. It uses
     * {@link ImportAdapter} in combination with
     * {@link ExcelFileConfiguration} to actually read in the data.
     */
    private void readPreview() throws IOException {

        /* Reset preview data */
        previewData.clear();

        /* Parameters from the user interface */
        final String location = comboLocation.getText();
        final int sheetIndex = comboSheet.getSelectionIndex();
        final boolean containsHeader = btnContainsHeader.getSelection();

        /* Variables needed for processing */
        Sheet sheet = workbook.getSheetAt(sheetIndex);
        Iterator<Row> rowIterator = sheet.iterator();
        ExcelFileConfiguration config = new ExcelFileConfiguration(location, sheetIndex, containsHeader);
        wizardColumns = new ArrayList<WizardColumn>();

        /* Check whether there is at least one row in sheet and retrieve it */
        if (!rowIterator.hasNext()) {

            throw new IOException("Sheet contains no actual data");

        }

        /* Get first row */
        Row firstRow = rowIterator.next();

        /* Check whether there is at least one column in row */
        if (firstRow.getPhysicalNumberOfCells() < 1) {

            throw new IOException("First row contains no data");

        }

        /* Iterate over columns and add them */
        for (int i = 0; i < firstRow.getPhysicalNumberOfCells(); i++) {

            Column column = new ExcelColumn(i, DataType.STRING);
            WizardColumn wizardColumn = new WizardColumn(column);

            wizardColumns.add(wizardColumn);
            config.addColumn(column);

        }

        /* Create adapter to import data with given configuration */
        ImportAdapter importAdapter = ImportAdapter.create(config);

        /* Get up to {ImportData#previewDataMaxLines} lines for previewing */
        int count = 0;
        while (importAdapter.hasNext() && (count <= ImportData.previewDataMaxLines)) {

            previewData.add(importAdapter.next());
            count++;

        }

        /* Remove first entry as it always contains name of columns */
        previewData.remove(0);

        /* Check whether there is actual any data */
        if (previewData.size() == 0) {

            throw new IOException("No actual data in file");

        }

        /* Disable redrawing once redesign is finished */
        tablePreview.setRedraw(false);

        /* Remove all of the old columns */
        while (tablePreview.getColumnCount() > 0) {

            tablePreview.getColumns()[0].dispose();

        }

        /* Add new columns */
        for (WizardColumn column : wizardColumns) {

            TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewerPreview, SWT.NONE);
            tableViewerColumn.setLabelProvider(new ExcelColumnLabelProvider(((ExcelColumn) column.getColumn()).getIndex()));

            TableColumn tableColumn = tableViewerColumn.getColumn();
            tableColumn.setWidth(100);

            if (btnContainsHeader.getSelection()) {

                tableColumn.setText(column.getColumn().getAliasName());
                tableColumn.setToolTipText("Column #" + ((ExcelColumn) column.getColumn()).getIndex());

            }

            ColumnViewerToolTipSupport.enableFor(tableViewerPreview, ToolTip.NO_RECREATE);

        }

        /* Setup preview table */
        tableViewerPreview.setInput(previewData);
        tablePreview.setHeaderVisible(btnContainsHeader.getSelection());
        tablePreview.setVisible(true);
        tablePreview.layout();
        tablePreview.setRedraw(true);

    }

    /**
     * Evaluates the page
     *
     * This checks whether the current settings on the page make any sense
     * and applies them appropriately. It basically checks tries to read in
     * the preview data {@link #readPreview()}.
     *
     * If everything is fine, the settings are being put into the appropriate
     * data container {@link ImportData} and the  current page is marked as
     * complete by invoking {@link #setPageComplete(boolean)}. Otherwise an
     * error message is set, which will make sure the user is informed about
     * the reason for the error.
     */
    private void evaluatePage() {

        setPageComplete(false);
        setErrorMessage(null);
        tablePreview.setVisible(false);

        if (comboLocation.getText().equals("")) {

            return;

        }

        try {

            readPreview();

        } catch (IOException | IllegalArgumentException e) {

            setErrorMessage(e.getMessage());

            return;

        }

        /* Put data into container */
        ImportData data = wizardImport.getData();

        data.setWizardColumns(wizardColumns);
        data.setPreviewData(previewData);
        data.setFirstRowContainsHeader(btnContainsHeader.getSelection());
        data.setFileLocation(comboLocation.getText());
        data.setExcelSheetIndex(comboSheet.getSelectionIndex());

        /* Mark page as completed */
        setPageComplete(true);

    }

    /**
     * Label provider for Excel columns
     *
     * A new instance of this object will be initiated for each column of
     * {@link tableViewerPreview}. This class holds the index of the
     * appropriate column {@link #index}, making sure they will return the
     * correct value for each column.
     */
    class ExcelColumnLabelProvider extends ColumnLabelProvider {

        /**
         * Index of the column this instance is representing
         */
        private int index;


        /**
         * Creates new instance of this class for the given index
         *
         * @param index Index the instance should be created for
         */
        public ExcelColumnLabelProvider(int index) {

            this.index = index;

        }

        /**
         * Returns the string value for the given column
         */
        @Override
        public String getText(Object element) {

            return ((String[]) element)[index];

        }

        /**
         * Returns tooltip for each element of given column
         *
         * The tooltip contains the current row as well as the column index
         * itself.
         */
        @Override
        public String getToolTipText(Object element) {

            int row = previewData.indexOf(element);

            return "Row: " + (row + 1) + ", Column: " + (index + 1);

        }

    }

}
