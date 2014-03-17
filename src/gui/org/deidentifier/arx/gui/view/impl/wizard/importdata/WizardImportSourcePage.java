package org.deidentifier.arx.gui.view.impl.wizard.importdata;

import org.deidentifier.arx.gui.view.impl.wizard.importdata.WizardImportData.source;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;


public class WizardImportSourcePage extends WizardPage {

    private Button btnCsv;
    private Button btnDatabase;
    private Button btnXls;

    private WizardImport wizardImport;


    public WizardImportSourcePage(WizardImport wizardImport)
    {

        super("WizardImportSourcePage");
        setTitle("Source");
        setDescription("Select the source you want to import data from");

        this.wizardImport = wizardImport;

    }

    public void createControl(Composite parent)
    {

        Composite container = new Composite(parent, SWT.NULL);

        setControl(container);
        container.setLayout(new GridLayout(1, false));

        btnCsv = new Button(container, SWT.RADIO);
        btnCsv.setText("CSV");
        btnCsv.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent arg0) {

                wizardImport.getData().setSource(source.CSV);

                setPageComplete(true);

            }

        });

        btnDatabase = new Button(container, SWT.RADIO);
        btnDatabase.setText("Database (JDBC)");
        btnDatabase.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent arg0) {

                wizardImport.getData().setSource(source.JDBC);

                setPageComplete(true);

            }

        });

        btnXls = new Button(container, SWT.RADIO);
        btnXls.setText("XLS (Excel)");
        btnXls.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent arg0) {

                wizardImport.getData().setSource(source.XLS);

                setPageComplete(true);

            }

        });

        setPageComplete(false);

    }

}
