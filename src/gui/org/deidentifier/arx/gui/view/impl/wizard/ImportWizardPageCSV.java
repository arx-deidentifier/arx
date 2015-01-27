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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.io.CSVDataInput;
import org.deidentifier.arx.io.ImportAdapter;
import org.deidentifier.arx.io.ImportColumn;
import org.deidentifier.arx.io.ImportColumnCSV;
import org.deidentifier.arx.io.ImportConfigurationCSV;
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

import com.carrotsearch.hppc.CharIntOpenHashMap;
import com.carrotsearch.hppc.IntIntOpenHashMap;


/**
 * CSV page
 *
 * This page offers means to import data from a CSV file. It contains
 * mechanisms to select such a file, and offers the user the ability to define
 * the separator and whether or not the first row contains a header describing
 * each column. A live preview makes sure the user will immediately see whether
 * or not his choices make any sense.
 *
 * All of the data gathered on this page is stored within {@link ImportWizardModel}.
 *
 * This includes:
 *
 * <ul>
 *  <li>{@link ImportWizardModel#setWizardColumns(List)}</li>
 *  <li>{@link ImportWizardModel#setFirstRowContainsHeader(boolean)</li>
 *  <li>{@link ImportWizardModel#setFileLocation(String)}</li>
 *  <li>{@link ImportWizardModel#setCsvSeparator(char)}</li>
 * </ul>
 * 
 * @author Karol Babioch
 * @author Fabian Prasser
 */
public class ImportWizardPageCSV extends WizardPage {

    /**
     * Label provider for CSV columns
     *
     * A new instance of this object will be initiated for each column of
     * {@link tableViewerPreview}. This class holds the index of the
     * appropriate column {@link #index}, making sure they will return the
     * correct value for each column.
     */
    class CSVColumnLabelProvider extends ColumnLabelProvider {

        /** Index of the column this instance is representing. */
        private int index;


        /**
         * Creates new instance of this class for the given index.
         *
         * @param index Index the instance should be created for
         */
        public CSVColumnLabelProvider(int index) {
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
    private Combo comboSeparator;
    
    /**  TODO */
    private Label lblSeparator;
    
    /**  TODO */
    private Table tablePreview;

    /**  TODO */
    private TableViewer tableViewerPreview;

    /**
     * Currently selected separator (index).
     *
     * @see {@link #separators}
     */
    private int selection;

    /**
     * Supported separators.
     *
     * @see {@link #labels}
     * @note This are the separators itself. The appropriate combobox will
     *       display the {@link #labels} instead.
     */
    private final char[] separators = {';', ',', '|', '\t'};

    /**
     * Labels for separators defined in {@link #separators}.
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


    /**  TODO */
    private final ArrayList<String[]> previewData = new ArrayList<String[]>();

    /**
     * Creates a new instance of this page and sets its title and description.
     *
     * @param wizardImport Reference to wizard containing this page
     */
    public ImportWizardPageCSV(ImportWizard wizardImport)
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
             * Resets {@link customSeparator} and evaluates page
             */
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                /* Make widgets visible */
                lblSeparator.setVisible(true);
                comboSeparator.setVisible(true);
                btnContainsHeader.setVisible(true);
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
             */
            @Override
            public void widgetSelected(SelectionEvent arg0) {

                /* Open file dialog */
                final String path = wizardImport.getController().actionShowOpenFileDialog(getShell(), 
                                                                                          "*.csv");
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

        /* Separator label */
        lblSeparator = new Label(container, SWT.NONE);
        lblSeparator.setVisible(false);
        lblSeparator.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblSeparator.setText("Separator");

        /* Separator combobox */
        comboSeparator = new Combo(container, SWT.READ_ONLY);
        comboSeparator.setVisible(false);

        /* Add labels */
        for (final String s : labels) {
            comboSeparator.add(s);
        }

        comboSeparator.select(selection);
        comboSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        comboSeparator.addSelectionListener(new SelectionAdapter() {

            /**
             * Set selection index and customSeparator and (re-)evaluates page
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
     * This goes through up to {@link ImportWizardModel#previewDataMaxLines} lines
     * and tries to detect the used separator by counting how often each of
     * the available {@link #separators} is used.
     *
     * @throws IOException In case file couldn't be accessed successfully
     */
    private void detectSeparator() throws IOException {

        final BufferedReader r = new BufferedReader(new FileReader(new File(comboLocation.getText())));
        final IntIntOpenHashMap map = new IntIntOpenHashMap();
        final CharIntOpenHashMap separators = new CharIntOpenHashMap();
        for (int i=0; i<this.separators.length; i++) {
            separators.put(this.separators[i], i);
        }
        int count = 0;

        /* Iterate over data */
        String line = r.readLine();
        while ((count < ImportWizardModel.previewDataMaxLines) && (line != null)) {

            /* Iterate over line character by character */
            final char[] a = line.toCharArray();
            for (final char c : a) {
                if (separators.containsKey(c)) {
                    map.putOrAdd(separators.get(c), 0, 1);
                }
            }
            line = r.readLine();
            count++;
        }
        r.close();

        if (map.isEmpty()) {
            selection = 0;
            return;
        }

        /* Check which separator was used the most */
        int max = Integer.MIN_VALUE;
        final int [] keys = map.keys;
        final int [] values = map.values;
        final boolean [] allocated = map.allocated;
        for (int i = 0; i < allocated.length; i++) {
            if (allocated[i] && values[i] > max) {
                max = values[i];
                selection = keys[i];
            }
        }
    }

    /**
     * Evaluates the page
     *
     * This checks whether the current settings on the page make any sense.
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
            if (!customSeparator) {
                detectSeparator();
                comboSeparator.select(selection);
            }
            readPreview();

        } catch (IOException | IllegalArgumentException e) {
            setErrorMessage(e.getMessage());
            return;
        } catch (RuntimeException e) {
            if (e.getCause()!=null) {
                setErrorMessage(e.getCause().getMessage());
            } else {
                setErrorMessage(e.getMessage());
            }
            return;
        }

        /* Put data into container */
        ImportWizardModel data = wizardImport.getData();

        data.setWizardColumns(wizardColumns);
        data.setPreviewData(previewData);
        data.setFirstRowContainsHeader(btnContainsHeader.getSelection());
        data.setFileLocation(comboLocation.getText());
        data.setCsvSeparator(separators[selection]);

        /* Mark page as completed */
        setPageComplete(true);

    }

    /**
     * Reads in preview data
     * 
     * This goes through up to {@link ImportWizardModel#previewDataMaxLines} lines
     * within the appropriate file and reads them in. It uses {@link ImportAdapter} in combination with {@link ImportConfigurationCSV} to actually read in the data.
     *
     * @throws IOException
     */
    private void readPreview() throws IOException {

        /* Reset preview data */
        previewData.clear();

        /* Parameters from the user interface */
        final String location = comboLocation.getText();
        final char separator = separators[selection];
        final boolean containsHeader = btnContainsHeader.getSelection();

        /* Variables needed for processing */
        final CSVDataInput in = new CSVDataInput(location, separator);
        final Iterator<String[]> it = in.iterator();
        final String[] firstLine;
        wizardColumns = new ArrayList<ImportWizardModelColumn>();
        ImportConfigurationCSV config = new ImportConfigurationCSV(location, separator, containsHeader);

        /* Check whether there is at least one line in file and retrieve it */
        if (it.hasNext()) {
            firstLine = it.next();
        } else {
            throw new IOException("No data in file");
        }

        /* Iterate over columns and add it to {@link #allColumns} */
        for (int i = 0; i < firstLine.length; i++) {

            ImportColumn column = new ImportColumnCSV(i, DataType.STRING);
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

        in.close();

        /* Remove first entry as it always contains name of columns */
        previewData.remove(0);

        /* Check whether there is actual any data */
        if (previewData.size() == 0) {
            throw new IOException("No preview data in file");
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
        for (ImportWizardModelColumn column : wizardColumns) {

            TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewerPreview, SWT.NONE);
            tableViewerColumn.setLabelProvider(new CSVColumnLabelProvider(((ImportColumnCSV) column.getColumn()).getIndex()));

            TableColumn tableColumn = tableViewerColumn.getColumn();
            tableColumn.setWidth(100);

            if (btnContainsHeader.getSelection()) {
                tableColumn.setText(column.getColumn().getAliasName());
                tableColumn.setToolTipText("Column #" + ((ImportColumnCSV) column.getColumn()).getIndex());
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
}
