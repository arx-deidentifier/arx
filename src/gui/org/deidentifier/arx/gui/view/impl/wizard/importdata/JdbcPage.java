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

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


public class JdbcPage extends WizardPage {

    @SuppressWarnings("unused")
    private ImportDataWizard wizardImport;

    private Label lblType;
    private Combo comboType;
    private Label lblServer;
    private Text txtServer;
    private Label lblUsername;
    private Text txtUsername;
    private Label lblPassword;
    private Text txtPassword;
    private Label lblDatabase;
    private Text txtDatabase;
    private Label lblPort;
    private Text txtPort;


    public JdbcPage(ImportDataWizard wizardImport)
    {

        super("WizardImportJdbcPage");
        setTitle("JDBC");
        setDescription("Please provide the information requested below");

        this.wizardImport = wizardImport;

    }

    public void createControl(Composite parent)
    {

        Composite container = new Composite(parent, SWT.NULL);

        setControl(container);
        container.setLayout(new GridLayout(2, false));

        lblType = new Label(container, SWT.NONE);
        lblType.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblType.setText("Type");

        comboType = new Combo(container, SWT.READ_ONLY);
        comboType.setItems(new String[] {"MySQL"});
        comboType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        comboType.select(0);
        comboType.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent arg0)
            {

                evaluatePage();

            }

        });

        lblServer = new Label(container, SWT.NONE);
        lblServer.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblServer.setText("Server");

        txtServer = new Text(container, SWT.BORDER);
        txtServer.setText("localhost");
        txtServer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        txtServer.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent arg0) {

                evaluatePage();

            }

        });

        lblPort = new Label(container, SWT.NONE);
        lblPort.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblPort.setText("Port");

        txtPort = new Text(container, SWT.BORDER);
        txtPort.setText("3306");
        txtPort.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        txtPort.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent arg0) {

                evaluatePage();

            }

        });

        lblUsername = new Label(container, SWT.NONE);
        lblUsername.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblUsername.setText("Username");

        txtUsername = new Text(container, SWT.BORDER);
        txtUsername.setText("jdbc");
        txtUsername.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        txtUsername.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent arg0) {

                evaluatePage();

            }

        });

        lblPassword = new Label(container, SWT.NONE);
        lblPassword.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblPassword.setText("Password");

        txtPassword = new Text(container, SWT.BORDER | SWT.PASSWORD);
        txtPassword.setText("jdbc");
        txtPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        txtPassword.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent arg0) {

                evaluatePage();

            }

        });

        lblDatabase = new Label(container, SWT.NONE);
        lblDatabase.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblDatabase.setText("Database");

        txtDatabase = new Text(container, SWT.BORDER);
        txtDatabase.setText("jdbc");
        txtDatabase.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        txtDatabase.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent arg0) {

                evaluatePage();

            }

        });

        setPageComplete(false);

    }

    private void evaluatePage()
    {

        setPageComplete(false);
        setErrorMessage(null);

        if (txtServer.getText().length() == 0) {

            setErrorMessage("Please provide a server");

            return;

        }

        if (txtUsername.getText().length() == 0) {

            setErrorMessage("Please provide a username");

            return;

        }

        if (txtDatabase.getText().length() == 0) {

            setErrorMessage("Please provide a database");

            return;

        }

        if (jdbcConnect()) {

            setPageComplete(true);

        } else {

            setErrorMessage("Unable to connect to database");

        }

    }

    private boolean jdbcConnect()
    {

        return true;

    }

}
