package org.deidentifier.arx.gui.view.impl.wizard.importdata;

import org.deidentifier.arx.gui.view.impl.wizard.importdata.ImportData.SourceType;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;


/**
 * Source selection page
 *
 * This page provides means to select the source the user wants to import data
 * from. Once the user makes a choice, it is stored stored using
 * {@link ImportData#setSourceType(SourceType)} and the page is marked as
 * completed.
 *
 * These source types are supported:
 *
 * <ul>
 *  <li>{@link CsvPage} CSV</li>
 *  <li>{@link JdbcPage} Database (JDBC)</li>
 *  <li>{@link XlsPage} Excel (XLS)</li>
 * </ul>
 */
public class SourcePage extends WizardPage {

    /*
     * Widgets
     */
    private Button btnCsv;
    private Button btnJdbc;
    private Button btnXls;

    /**
     * Reference to the wizard containing this page
     */
    private ImportDataWizard wizardImport;


    /**
     * Creates a new instance of this page and sets its title and description
     *
     * @param wizardImport Reference to wizard containing this page
     */
    public SourcePage(ImportDataWizard wizardImport)
    {

        super("WizardImportSourcePage");
        setTitle("Source");
        setDescription("Select the source you want to import data from");

        this.wizardImport = wizardImport;

    }

    /**
     * Creates the design of this page
     *
     * This adds all the controls to the page along with their listeners. It
     * basically waits for any radio button to be pressed, which will mark the
     * page as completed and lets the user proceed to the next page.
     */
    public void createControl(Composite parent)
    {

        Composite container = new Composite(parent, SWT.NULL);

        setControl(container);
        container.setLayout(new GridLayout(1, false));

        /*
         * Add button for CSV
         */
        btnCsv = new Button(container, SWT.RADIO);
        btnCsv.setText("CSV");
        btnCsv.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent arg0) {

                wizardImport.getData().setSourceType(SourceType.CSV);
                setPageComplete(true);

            }

        });

        /*
         * Add button for JDBC
         */
        btnJdbc = new Button(container, SWT.RADIO);
        btnJdbc.setEnabled(false);
        btnJdbc.setText("Database (JDBC)");
        btnJdbc.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent arg0) {

                wizardImport.getData().setSourceType(SourceType.JDBC);
                setPageComplete(true);

            }

        });

        /*
         * Add button for Excel
         */
        btnXls = new Button(container, SWT.RADIO);
        btnXls.setEnabled(false);
        btnXls.setText("XLS (Excel)");
        btnXls.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent arg0) {

                wizardImport.getData().setSourceType(SourceType.XLS);
                setPageComplete(true);

            }

        });

        /*
         * Mark page as incomplete until a button is pressed
         */
        setPageComplete(false);

    }

}
