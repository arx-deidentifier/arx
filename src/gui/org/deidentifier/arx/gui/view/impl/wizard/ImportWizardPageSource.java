/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2014 Karol Babioch <karol@babioch.de>
 * Copyright (C) 2014 Fabian Prasser
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
    private Button btnCsv;
    private Button btnExcel;
    private Button btnJdbc;

    /**
     * Reference to the wizard containing this page
     */
    private ImportWizard wizardImport;


    /**
     * Creates a new instance of this page and sets its title and description
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
