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

import org.deidentifier.arx.gui.view.impl.wizard.ImportWizardModel.SourceType;
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
 * {@link ImportWizardModel#setSourceType(SourceType)} and the page is marked as
 * completed.
 *
 * These source types are supported:
 *
 * <ul>
 *  <li>{@link ImportWizardPageCSV} CSV</li>
 *  <li>{@link ImportWizardPageJDBC} Database (JDBC)</li>
 *  <li>{@link ImportWizardPageExcel} Excel (XLS, XLSX)</li>
 * </ul>
 * 
 * @author Karol Babioch
 * @author Fabian Prasser
 */
public class ImportWizardPageSource extends WizardPage {

    /* Widgets */
    /**  TODO */
    private Button btnCsv;
    
    /**  TODO */
    private Button btnExcel;
    
    /**  TODO */
    private Button btnJdbc;

    /** Reference to the wizard containing this page. */
    private ImportWizard wizardImport;


    /**
     * Creates a new instance of this page and sets its title and description.
     *
     * @param wizardImport Reference to wizard containing this page
     */
    public ImportWizardPageSource(ImportWizard wizardImport) {
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
     *
     * @param parent
     */
    public void createControl(Composite parent) {

        Composite container = new Composite(parent, SWT.NULL);

        setControl(container);
        container.setLayout(new GridLayout(1, false));

        /* Add button for CSV */
        btnCsv = new Button(container, SWT.RADIO);
        btnCsv.setText("CSV");
        btnCsv.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent arg0) {
                wizardImport.getData().setSourceType(SourceType.CSV);
                setPageComplete(true);
            }
        });

        /* Add button for Excel */
        btnExcel = new Button(container, SWT.RADIO);
        btnExcel.setText("Excel (XLS, XLSX)");
        btnExcel.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent arg0) {
                wizardImport.getData().setSourceType(SourceType.EXCEL);
                setPageComplete(true);
            }
        });

        /* Add button for JDBC */
        btnJdbc = new Button(container, SWT.RADIO);
        btnJdbc.setText("Database (JDBC)");
        btnJdbc.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent arg0) {
                wizardImport.getData().setSourceType(SourceType.JDBC);
                setPageComplete(true);
            }
        });

        /* Preselect CSV source*/
        btnCsv.setSelection(true);
        wizardImport.getData().setSourceType(SourceType.CSV);
        setPageComplete(true);
    }
}
