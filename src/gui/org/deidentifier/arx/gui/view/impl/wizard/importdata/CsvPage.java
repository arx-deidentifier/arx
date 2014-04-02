package org.deidentifier.arx.gui.view.impl.wizard.importdata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.io.CSVDataInput;
import org.deidentifier.arx.io.importdata.CSVConfiguration;
import org.deidentifier.arx.io.importdata.Column;
import org.deidentifier.arx.io.importdata.DataSourceImportAdapter;
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
 * CSV page
 *
 * This page offers means to import data from a CSV file. It contains
 * mechanisms to select such a file, and offers the user the ability to define
 * the separator and whether or not the first row contains a header describing
 * each column. A live preview makes sure the user will immediately see whether
 * or not his choices make any sense.
 *
 * All of the data gathered on this page is stored within {@link ImportData}.
 *
 * This includes:
 *
 * <ul>
 *  <li>{@link ImportData#setWizardColumns(List)}</li>
 *  <li>{@link ImportData#setFirstRowContainsHeader(boolean)</li>
 *  <li>{@link ImportData#setFileLocation(String)}</li>
 *  <li>{@link ImportData#setCsvSeparator(char)}</li>
 * </ul>
 */
public class CsvPage extends WizardPage {

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
    private Combo comboSeparator;
    private Label lblSeparator;
    private Table tablePreview;
    private TableViewer tableViewerPreview;

    /**
     * Currently selected separator (index)
     *
     * @see {@link #separators}
     */
    private int selection;

    /**
     * Supported separators
     *
     * @note This are the separators itself. The appropriate combobox will
     * display the {@link #labels} instead.
     *
     * @see {@link #labels}
     */
    private final char[] separators = {';', ',', '|', '\t'};

    /**
     * Labels for separators defined in {@link #separators}
     *
     * @see {@link #separators}
     */
    private final String[] labels = {";", ",", "|", "Tab"};

    /**
     * Indicates whether separator was detected automatically or by the user
     *
     * The separator will usually be detected automatically
     * {@link #detectSeparator()}. In case the user selected another
     * separator by hand, this flag will be set to true, making sure the rest
     * of the logic knows about it.
     */
    private boolean customSeparator;

    final ArrayList<String[]> previewData = new ArrayList<String[]>();


    /**
     * Creates a new instance of this page and sets its title and description
     *
     * @param wizardImport Reference to wizard containing this page
     */
    public CsvPage(ImportDataWizard wizardImport)
    {

        super("WizardImportCsvPage");

        setTitle("CSV");
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
             * Resets {@link customSeparator} and evaluates page
             */
            @Override
            public void widgetSelected(SelectionEvent arg0) {

                customSeparator = false;
                evaluatePage();

            }

        });

        /* Button to open file selection dialog */
        btnChoose = new Button(container, SWT.NONE);
        btnChoose.setText("Browse...");
        btnChoose.addSelectionListener(new SelectionAdapter() {

            /**
             * Opens a file selection dialog for CSV files
             *
             * If a valid CSV file was selected, it is added to
             * {@link #comboLocation} when it wasn't already there. It is then
             * preselected within {@link #comboLocation} and the page is
             * evaluated {@see #evaluatePage}.
             *
             * @see {@link Controller#actionShowOpenFileDialog(String)}
             */
            @Override
            public void widgetSelected(SelectionEvent arg0) {

                setPageComplete(false);
                setErrorMessage(null);

                final String path = wizardImport.getController().actionShowOpenFileDialog("*.csv");

                if (path == null) {

                    return;

                }

                if (comboLocation.indexOf(path) == -1) {

                    comboLocation.add(path, 0);

                }

                comboLocation.select(comboLocation.indexOf(path));
                customSeparator = false;
                evaluatePage();

            }

        });

        /* Separator label */
        lblSeparator = new Label(container, SWT.NONE);
        lblSeparator.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblSeparator.setText("Separator");

        /* Separator combobox */
        /* TODO: Fix bug(s) when separator is selected multiple times */
        comboSeparator = new Combo(container, SWT.READ_ONLY);

        /* Add labels */
        for (final String s : labels) {

            comboSeparator.add(s);

        }

        comboSeparator.select(selection);
        comboSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        comboSeparator.addSelectionListener(new SelectionAdapter() {

            /**
             * Set the selection index and customSeparator and evalutes page
             */
            @Override
            public void widgetSelected(final SelectionEvent arg0) {

                selection = comboSeparator.getSelectionIndex();
                customSeparator = true;
                evaluatePage();

            }

        });

        /* Place holders */
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);

        /* Contains header button */
        btnContainsHeader = new Button(container, SWT.CHECK);
        btnContainsHeader.setText("First row contains column names");
        btnContainsHeader.setSelection(true);
        btnContainsHeader.addSelectionListener(new SelectionAdapter() {

            /**
             * Evaluates page with each change
             */
            @Override
            public void widgetSelected(SelectionEvent arg0) {

                evaluatePage();

            }

        });

        /* Place holder */
        new Label(container, SWT.NONE);

        /* Place holders */
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
     * Tries to detect the separator used within this file
     *
     * This goes through up to {@link ImportData#previewDataMaxLines} lines
     * and tries to detect the used separator by counting how often each of
     * the available {@link #separators} is used.
     *
     * @throws IOException In case file couldn't be accessed successfully
     */
    private void detectSeparator() throws IOException {

        final BufferedReader r = new BufferedReader(new FileReader(new File(comboLocation.getText())));
        int count = 0;
        final Map<Integer, Integer> map = new HashMap<Integer, Integer>();

        String line = r.readLine();

        /* Iterate over data */
        while ((count < ImportData.previewDataMaxLines) && (line != null)) {

            final char[] a = line.toCharArray();

            /* Iterate over line character by character */
            for (final char c : a) {

                /* Iterate over separators and put matches into hash map */
                for (int i = 0; i < separators.length; i++) {

                    if (c == separators[i]) {

                        if (!map.containsKey(i)) {

                            map.put(i, 0);

                        }

                        map.put(i, map.get(i) + 1);

                    }

                }

            }

            line = r.readLine();

            count++;

        }

        r.close();

        if (map.isEmpty()) {

            return;

        }

        int max = Integer.MIN_VALUE;

        /* Check which separator was used the most */
        for (final int key : map.keySet()) {

            if (map.get(key) > max) {

                max = map.get(key);
                selection = key;

            }

        }

    }

    /**
     * Reads in preview data
     *
     * This goes through up to {@link ImportData#previewDataMaxLines} lines
     * within the appropriate file and reads them in. It uses
     * {@link DataSourceImportAdapter} in combination with
     * {@link CSVConfiguration} to actually read in the data.
     *
     * TODO: Don't throw Exception, only IOException if necessary
     */
    private void readPreview() throws Exception {

        /* Parameters from the user interface */
        final String location = comboLocation.getText();
        final char separator = separators[selection];
        final boolean containsHeader = btnContainsHeader.getSelection();

        /* Variables needed for processing */
        final CSVDataInput in = new CSVDataInput(location, separator);
        final Iterator<String[]> it = in.iterator();
        final String[] firstLine;

        /* Check whether there is at least one line in file and retrieve it */
        if (it.hasNext()) {

            firstLine = it.next();

        } else {

            throw new IOException("No data in file");

        }

        /* Initialize {@link #allColumns} */
        wizardColumns = new ArrayList<WizardColumn>();
        List<Column> columns = new ArrayList<Column>();

        /* Iterate over columns and add it to {@link #allColumns} */
        for (int i = 0; i < firstLine.length; i++) {

            Column column = new Column(i, DataType.STRING);
            WizardColumn wizardColumn = new WizardColumn(column);

            wizardColumns.add(wizardColumn);
            columns.add(column);

        }

        /* Create configuration for CSV file and columns to it */
        CSVConfiguration config = new CSVConfiguration(location, separator, containsHeader);

        for (Column c : columns) {

            config.addColumn(c);

        }

        /* Create adapter to import data with given configuration */
        DataSourceImportAdapter importAdapter = DataSourceImportAdapter.create(config);

        /* Get up to {ImportData#previewDataMaxLines} lines for previewing */
        int count = 0;
        while (importAdapter.hasNext() && (count <= ImportData.previewDataMaxLines)) {

            previewData.add(importAdapter.next());
            count++;

        }

        in.close();

        /* Remove first entry as it always contains name of columns */
        previewData.remove(0);

        /* Check whether there is actual any data */
        if (previewData.size() == 0) {

            throw new Exception("No preview data in file");

        }

        /*
         * Show preview in appropriate table
         */

        /* Disable redrawing once redesign is finished */
        tablePreview.setRedraw(false);

        /* Remove all of the old columns */
        while (tablePreview.getColumnCount() > 0) {

            tablePreview.getColumns()[0].dispose();

        }

        /* Add new columns */
        for (WizardColumn column : wizardColumns) {

            TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewerPreview, SWT.NONE);
            tableViewerColumn.setLabelProvider(new CSVColumnLabelProvider(column.getColumn().getIndex()));

            TableColumn tableColumn = tableViewerColumn.getColumn();
            tableColumn.setWidth(100);

            if (btnContainsHeader.getSelection()) {

                tableColumn.setText(column.getColumn().getName());
                tableColumn.setToolTipText("Column #" + column.getColumn().getIndex());

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
     * This checks whether the current settings on the page make any sense.
     * If everything is fine, the settings are being put into the appropriate
     * data container {@link ImportData} and the  current page is marked as
     * complete by invoking {@link #setPageComplete(boolean)}. Otherwise an
     * error message is set, which will make sure the user is informed about
     * the reason for the error.
     */
    private void evaluatePage() {

        setPageComplete(false);
        setErrorMessage(null);

        if (comboLocation.getText().equals("")) {

            return;

        }

        try {

            if (!customSeparator) {

                detectSeparator();
                comboSeparator.select(selection);

            }

            readPreview();

        } catch (Exception e) {

            setErrorMessage("Error while trying to access the file");

            return;

        }

        /* Put data into container */
        wizardImport.getData().setWizardColumns(wizardColumns);
        wizardImport.getData().setPreviewData(previewData);
        wizardImport.getData().setFirstRowContainsHeader(btnContainsHeader.getSelection());
        wizardImport.getData().setFileLocation(comboLocation.getText());
        wizardImport.getData().setCsvSeparator(separators[selection]);

        setPageComplete(true);

    }

    /**
     * Label provider for CSV columns
     *
     * A new instance of this object will be initiated for each column of
     * {@link tableViewerPreview}. This class holds the index of the
     * appropriate column {@link #index}, making sure they will return the
     * correct value for each column.
     */
    class CSVColumnLabelProvider extends ColumnLabelProvider {

        /**
         * Index of the column this instance is representing
         */
        private int index;


        /**
         * Creates new instance of this class for the given index
         *
         * @param index Index the instance should be created for
         */
        public CSVColumnLabelProvider(int index) {

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
