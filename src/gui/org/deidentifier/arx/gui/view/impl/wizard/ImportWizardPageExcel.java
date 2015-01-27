/*
 * ARX: Powerful Data Anonymization
 * Copyright 2014 Karol Babioch <karol@babioch.de>
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.io.ImportAdapter;
import org.deidentifier.arx.io.ImportColumn;
import org.deidentifier.arx.io.ImportColumnExcel;
import org.deidentifier.arx.io.ImportConfigurationExcel;
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
 * All of the data gathered on this page is stored within {@link ImportWizardModel}.
 *
 * This includes:
 *
 * <ul>
 *  <li>{@link ImportWizardModel#setWizardColumns(List)}</li>
 *  <li>{@link ImportWizardModel#setFirstRowContainsHeader(boolean)</li>
 *  <li>{@link ImportWizardModel#setFileLocation(String)}</li>
 *  <li>{@link ImportWizardModel#setExcelSheetIndex(int)}</li>
 * </ul>
 * 
 * @author Karol Babioch
 * @author Fabian Prasser
 */
public class ImportWizardPageExcel extends WizardPage {

    /**
     * Label provider for Excel columns
     *
     * A new instance of this object will be initiated for each column of
     * {@link tableViewerPreview}. This class holds the index of the
     * appropriate column {@link #index}, making sure they will return the
     * correct value for each column.
     */
    class ExcelColumnLabelProvider extends ColumnLabelProvider {

        /** Index of the column this instance is representing. */
        private int index;


        /**
         * Creates new instance of this class for the given index.
         *
         * @param index Index the instance should be created for
         */
        public ExcelColumnLabelProvider(int index) {
            this.index = index;
        }

        /**
         * Returns the string value for the given column.
         *
         * @param element
         * @return
         */
        @Override
        public String getText(Object element) {
            return ((String[]) element)[index];
        }
    }

    /** Reference to the wizard containing this page. */
    private ImportWizard wizardImport;

    /** Columns detected by this page and passed on to {@link ImportWizardModel}. */
    private ArrayList<ImportWizardModelColumn> wizardColumns;
    /* Widgets */
    /**  TODO */
    private Label lblLocation;
    
    /**  TODO */
    private Combo comboLocation;
    
    /**  TODO */
    private Button btnChoose;
    
    /**  TODO */
    private Button btnContainsHeader;
    
    /**  TODO */
    private Combo comboSheet;
    
    /**  TODO */
    private Label lblSheet;
    
    /**  TODO */
    private Table tablePreview;

    /**  TODO */
    private TableViewer tableViewerPreview;

    /** Preview data. */
    ArrayList<String[]> previewData = new ArrayList<String[]>();


    /** Workbook Either HSSFWorkbook or XSSFWorkbook, depending upon file type. */
    private Workbook workbook;
    
    /** Input stream. */
    private InputStream stream;

    /**
     * Creates a new instance of this page and sets its title and description.
     *
     * @param wizardImport Reference to wizard containing this page
     */
    public ImportWizardPageExcel(ImportWizard wizardImport)
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
     * @param parent
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
                final String path = wizardImport.getController().actionShowOpenFileDialog(getShell(), 
                                                                                          "*.xls;*.xlsx");
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
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean value){
        super.setVisible(value);
        try { 
            if (stream != null) stream.close();
        } catch (Exception e){
            /* Die silently*/
        }
    }

    /**
     * Evaluates the page
     *
     * This checks whether the current settings on the page make any sense
     * and applies them appropriately. It basically checks tries to read in
     * the preview data {@link #readPreview()}.
     *
     * If everything is fine, the settings are being put into the appropriate
     * data container {@link ImportWizardModel} and the  current page is marked as
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
        ImportWizardModel data = wizardImport.getData();

        data.setWizardColumns(wizardColumns);
        data.setPreviewData(previewData);
        data.setFirstRowContainsHeader(btnContainsHeader.getSelection());
        data.setFileLocation(comboLocation.getText());
        data.setExcelSheetIndex(comboSheet.getSelectionIndex());

        /* Mark page as completed */
        setPageComplete(true);

    }

    /**
     * Reads in preview data
     * 
     * This goes through up to {@link ImportWizardModel#previewDataMaxLines} lines
     * within the appropriate file and reads them in. It uses {@link ImportAdapter} in combination with {@link ImportConfigurationExcel} to actually read in the data.
     *
     * @throws IOException
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
        ImportConfigurationExcel config = new ImportConfigurationExcel(location, sheetIndex, containsHeader);
        wizardColumns = new ArrayList<ImportWizardModelColumn>();

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

            ImportColumn column = new ImportColumnExcel(i, DataType.STRING);
            ImportWizardModelColumn wizardColumn = new ImportWizardModelColumn(column);

            wizardColumns.add(wizardColumn);
            config.addColumn(column);
        }

        /* Create adapter to import data with given configuration */
        ImportAdapter importAdapter = ImportAdapter.create(config);

        /* Get up to {ImportData#previewDataMaxLines} lines for previewing */
        int count = 0;
        while (importAdapter.hasNext() && (count <= ImportWizardModel.previewDataMaxLines)) {
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
        for (ImportWizardModelColumn column : wizardColumns) {

            TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewerPreview, SWT.NONE);
            tableViewerColumn.setLabelProvider(new ExcelColumnLabelProvider(((ImportColumnExcel) column.getColumn()).getIndex()));

            TableColumn tableColumn = tableViewerColumn.getColumn();
            tableColumn.setWidth(100);

            if (btnContainsHeader.getSelection()) {
                tableColumn.setText(column.getColumn().getAliasName());
                tableColumn.setToolTipText("Column #" + ((ImportColumnExcel) column.getColumn()).getIndex());
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
     * Reads in the available sheets from file
     * 
     * This reads in the available sheets from the file chosen at {@link #comboLocation} and adds them as items to {@link #comboSheet}.
     *
     * @throws IOException
     */
    private void readSheets() throws IOException {

        /* Remove previous items */
        comboSheet.removeAll();

        /* Get workbook */
        try {
            try { 
                if (stream != null) stream.close();
            } catch (Exception e){
                /* Die silently*/
            }
            
            stream = new FileInputStream(comboLocation.getText());
            workbook = WorkbookFactory.create(stream);
        } catch (InvalidFormatException e) {
            throw new IOException("File format invalid");
        }

        /* Add all sheets to combo */
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            comboSheet.add(workbook.getSheetName(i));
        }
    }
}
