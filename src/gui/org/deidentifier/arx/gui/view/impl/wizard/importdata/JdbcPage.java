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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.gui.Controller;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


public class JdbcPage extends WizardPage {

    private ImportDataWizard wizardImport;

    /* Widgets */
    private Label lblType;
    private Combo comboType;
    private Composite compositeSwap;
    private Text txtServer;
    private StackLayout layout;
    private Composite compositeRemote;
    private Composite compositeLocal;
    private Text txtPort;
    private Text txtUsername;
    private Text txtPassword;
    private Text txtDatabase;
    private Label lblLocation;
    private Combo comboLocation;
    private Button btnChoose;
    private Composite container;


    public JdbcPage(ImportDataWizard wizardImport)
    {

        super("WizardImportJdbcPage");
        setTitle("JDBC");
        setDescription("Please provide the information requested below");

        this.wizardImport = wizardImport;

    }

    public void createControl(Composite parent)
    {

        container = new Composite(parent, SWT.NULL);

        setControl(container);
        container.setLayout(new GridLayout(2, false));

        /* Type label + combobox */
        lblType = new Label(container, SWT.NONE);
        lblType.setText("Type");

        comboType = new Combo(container, SWT.READ_ONLY);
        comboType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        comboType.setItems(new String[] {"MySQL", "PostgreSQL", "SQLite"});
        comboType.addSelectionListener(new SelectionAdapter() {

            /**
             * Swaps the composites, clears it and triggers a relayout
             */
            @Override
            public void widgetSelected(SelectionEvent e)
            {

                setPageComplete(false);

                if (comboType.getText().equals("SQLite")) {

                    comboLocation.removeAll();
                    layout.topControl = compositeLocal;

                } else {

                    layout.topControl = compositeRemote;

                }

                compositeSwap.layout();

            }

        });

        /* Placeholder */
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);

        /* Swap composite */
        compositeSwap = new Composite(container, SWT.NONE);
        layout = new StackLayout();
        compositeSwap.setLayout(layout);
        compositeSwap.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

        /* Remote composite */
        createCompositeRemote();

        /* Local composite */
        createCompositeLocal();

        setPageComplete(false);

    }

    private void createCompositeRemote()
    {

        compositeRemote = new Composite(compositeSwap, SWT.NONE);
        compositeRemote.setLayout(new GridLayout(2, false));

        Label lblServer = new Label(compositeRemote, SWT.NONE);
        lblServer.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblServer.setText("Server");

        txtServer = new Text(compositeRemote, SWT.BORDER);
        txtServer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblPort = new Label(compositeRemote, SWT.NONE);
        lblPort.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblPort.setText("Port");

        txtPort = new Text(compositeRemote, SWT.BORDER);
        txtPort.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblUsername = new Label(compositeRemote, SWT.NONE);
        lblUsername.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblUsername.setText("Username");

        txtUsername = new Text(compositeRemote, SWT.BORDER);
        txtUsername.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblPassword = new Label(compositeRemote, SWT.NONE);
        lblPassword.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblPassword.setText("Password");

        txtPassword = new Text(compositeRemote, SWT.BORDER | SWT.PASSWORD);
        txtPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblDatabase = new Label(compositeRemote, SWT.NONE);
        lblDatabase.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblDatabase.setText("Database");

        txtDatabase = new Text(compositeRemote, SWT.BORDER);
        txtDatabase.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        txtDatabase.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e)
            {

                readTables();

            }

        });

    }

    private void createCompositeLocal()
    {

        compositeLocal = new Composite(compositeSwap, SWT.NONE);
        compositeLocal.setLayout(new GridLayout(3, false));

        /* Location label */
        lblLocation = new Label(compositeLocal, SWT.NONE);
        lblLocation.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblLocation.setText("Location");

        /* Combo box for selection of file */
        comboLocation = new Combo(compositeLocal, SWT.READ_ONLY);
        comboLocation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        comboLocation.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e)
            {

                readTables();

            }

        });

        /* Button to open file selection dialog */
        btnChoose = new Button(compositeLocal, SWT.NONE);
        btnChoose.setText("Browse...");
        btnChoose.addSelectionListener(new SelectionAdapter() {

            /**
             * Opens a file selection dialog for db files
             *
             * If a valid db file was selected, it is added to
             * {@link #comboLocation} when it wasn't already there. It is then
             * preselected within {@link #comboLocation}.
             *
             * @see {@link Controller#actionShowOpenFileDialog(String)}
             */
            @Override
            public void widgetSelected(SelectionEvent arg0) {

                /* Open file dialog */
                final String path = wizardImport.getController().actionShowOpenFileDialog(getShell(), "*.db");

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
    }

    protected void readTables()
    {

        Connection connection = null;

        try {

            if (comboType.getText().equals("SQLite")) {

                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:" + comboLocation.getText());

            } else if (comboType.getText().equals("MySQL")) {

                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://" + txtServer.getText() + ":" + txtPort.getText() + "/" + txtDatabase.getText(), txtUsername.getText(), txtPassword.getText());

            } else if (comboType.getText().equals("PostgreSQL")) {

                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection("jdbc:postgresql://" + txtServer.getText() + ":" + txtPort.getText() + "/" + txtDatabase.getText(), txtUsername.getText(), txtPassword.getText());

            }

        } catch (ClassNotFoundException e) {

            setErrorMessage("No JDBC driver for selected connection type");

        } catch (SQLException e) {

            setErrorMessage("Database connection error");

        }

        try {

            ResultSet rs = connection.getMetaData().getTables(null, null, "%", null);
            List<String> tables = new ArrayList<String>();

            while(rs.next()) {

                tables.add(rs.getString("TABLE_NAME"));

            }

            wizardImport.getData().setJdbcConnection(connection);
            wizardImport.getData().setJdbcTables(tables);

            setPageComplete(true);

        } catch (SQLException e)  {

            setErrorMessage("Couldn't read tables from database");

        }

    }

}
