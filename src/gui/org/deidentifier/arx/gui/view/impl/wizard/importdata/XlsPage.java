package org.deidentifier.arx.gui.view.impl.wizard.importdata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.deidentifier.arx.DataType.ARXString;
import org.deidentifier.arx.io.importdata.Column;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
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


public class XlsPage extends WizardPage {

    private ImportDataWizard wizardImport;

    private Label lblLocation;
    private Combo comboLocation;
    private Button btnChoose;
    private Button btnContainsHeader;
    private Label lblSheet;
    private Combo comboSheets;
    private ComboViewer comboViewerSheets;

    private static final int PREVIEWLINES = 5;


    public XlsPage(ImportDataWizard wizardImport)
    {

        super("WizardImportXlsPage");

        setTitle("XLS");
        setDescription("Please provide the information requested below");

        this.wizardImport = wizardImport;

    }

    public void createControl(Composite parent)
    {

        Composite container = new Composite(parent, SWT.NULL);

        setControl(container);
        container.setLayout(new GridLayout(3, false));

        lblLocation = new Label(container, SWT.NONE);
        lblLocation.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblLocation.setText("Location");

        comboLocation = new Combo(container, SWT.READ_ONLY);
        comboLocation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        comboLocation.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent arg0) {

                wizardImport.getData().setFileLocation(comboLocation.getText());

                readSheets();

            }

        });

        btnChoose = new Button(container, SWT.NONE);
        btnChoose.setText("Browse...");
        btnChoose.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent arg0) {

                setPageComplete(false);
                setErrorMessage(null);

                final String path = wizardImport.getController().actionShowOpenFileDialog("*.xls");

                if (path == null) {

                    return;

                }

                if (comboLocation.indexOf(path) == -1) {

                    comboLocation.add(path, 0);

                }

                comboLocation.select(comboLocation.indexOf(path));
                wizardImport.getData().setFileLocation(comboLocation.getText());

                readSheets();

            }

        });

        lblSheet = new Label(container, SWT.NONE);
        lblSheet.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblSheet.setText("Sheet");

        comboViewerSheets = new ComboViewer(container, SWT.READ_ONLY);
        comboViewerSheets.setContentProvider(new ArrayContentProvider());

        comboSheets = comboViewerSheets.getCombo();
        comboSheets.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        comboSheets.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent arg0) {

                wizardImport.getData().setXlsSheetIndex(comboSheets.getSelectionIndex());
                readPreview();

                setPageComplete(true);

            }

        });

        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);

        btnContainsHeader = new Button(container, SWT.CHECK);
        btnContainsHeader.setText("First row contains column names");
        btnContainsHeader.setSelection(true);
        btnContainsHeader.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent arg0) {

                wizardImport.getData().setFirstRowContainsHeader(btnContainsHeader.getSelection());
                readPreview();

            }

        });

        new Label(container, SWT.NONE);

        setPageComplete(false);

    }

    private void readSheets() {

        setErrorMessage(null);

        ArrayList<String> sheets = new ArrayList<String>();

        try {

            FileInputStream file = new FileInputStream(new File(comboLocation.getText()));
            HSSFWorkbook workbook = new HSSFWorkbook(file);

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {

                sheets.add(workbook.getSheetName(i));

            }

            file.close();

        } catch (IOException e) {

            setErrorMessage("Error accessing file");

            return;

        }

        if (sheets.size() == 0) {

            setErrorMessage("File doesn't contain any sheets");

            return;

        }

        comboViewerSheets.setInput(sheets);

    }

    private void readPreview() {

        setErrorMessage(null);

        ArrayList<ArrayList<Cell>> result = new ArrayList<ArrayList<Cell>>();

        try {

            FileInputStream file = new FileInputStream(new File(comboLocation.getText()));
            HSSFWorkbook workbook = new HSSFWorkbook(file);

            HSSFSheet sheet = workbook.getSheetAt(comboSheets.getSelectionIndex());
            Iterator<Row> rowIterator = sheet.iterator();

            int count = 0;

            while (rowIterator.hasNext() && (count < PREVIEWLINES)) {

                ArrayList<Cell> cells = new ArrayList<Cell>();

                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();

                while(cellIterator.hasNext()) {

                    cells.add(cellIterator.next());

                }

                result.add(cells);

                count++;

            }

            file.close();

            if (result.size() == 0 || result.get(0).size() == 0) {

                setErrorMessage("Sheet doesn't contain any data");

                return;

            }

            ArrayList<WizardColumn> columns = new ArrayList<WizardColumn>();

            int index = 0;
            for (final Cell c : result.get(0)) {

                WizardColumn column = new WizardColumn(new Column(index, new ARXString()));

                if (btnContainsHeader.getSelection()) {

                    column.getColumn().setName(c.getStringCellValue());

                } else {

                    column.getColumn().setName("Column #" + index);

                }

                columns.add(column);

            }

            wizardImport.getData().setWizardColumns(columns);

        } catch (IOException e) {

            setErrorMessage("Error accessing file");

            return;

        }

    }

}
