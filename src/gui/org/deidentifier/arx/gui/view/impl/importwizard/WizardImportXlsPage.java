package org.deidentifier.arx.gui.view.impl.importwizard;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
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


public class WizardImportXlsPage extends WizardPage {

    private WizardImport wizardImport;

    private ArrayList<String> sheets;
    private ArrayList<WizardImportDataColumn> columns;

    private Label lblLocation;
    private Combo comboLocation;
    private Button btnChoose;
    private Button btnContainsHeader;
    private Combo comboSheet;
    private Label lblSheet;
    private Table tablePreview;
    private TableViewer tableViewerPreview;

    private static final int PREVIEWLINES = 5;


    public WizardImportXlsPage(WizardImport wizardImport)
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

                readSheets();

            }

        });

        lblSheet = new Label(container, SWT.NONE);
        lblSheet.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblSheet.setText("Sheet");

        comboSheet = new Combo(container, SWT.READ_ONLY);
        comboSheet.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        comboSheet.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent arg0) {

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

                

            }

        });

        new Label(container, SWT.NONE);

        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);

        tableViewerPreview = new TableViewer(container, SWT.BORDER | SWT.FULL_SELECTION);
        tableViewerPreview.setContentProvider(new ArrayContentProvider());

        tablePreview = tableViewerPreview.getTable();
        GridData gd_tablePreview = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
        gd_tablePreview.heightHint = 150;
        tablePreview.setLayoutData(gd_tablePreview);
        tablePreview.setLinesVisible(true);
        tablePreview.setVisible(false);

        setPageComplete(false);

    }

    private void readSheets() {

        setErrorMessage(null);

    }

}
