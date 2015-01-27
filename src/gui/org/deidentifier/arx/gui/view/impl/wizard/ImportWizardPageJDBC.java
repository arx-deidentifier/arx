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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


/**
 * JDBC page
 *
 * This page offers means to specify connection details for a database. For
 * now MySQL, PostgreSQL and SQLite is supported. In case of remote database
 * types (i.e. MySQL and PostgreSQL) the user is asked for the server and a
 * username and password. In case of SQLite the user can select any *.db file.
 *
 * After ther user specified the details a connection is established and
 * passed on to {@link ImportWizardModel}.
 *
 * This includes:
 *
 * <ul>
 *  <li>{@link ImportWizardModel#setJdbcConnection(Connection)<li>
 *  <li>{@link ImportWizardModel#setJdbcTables(List)<li>
 * </ul>
 * 
 * @author Karol Babioch
 * @author Fabian Prasser
 */
public class ImportWizardPageJDBC extends WizardPage {

    /** Reference to the wizard containing this page. */
    private ImportWizard wizardImport;

    /* SWT Widgets */
    /**  TODO */
    private Label lblType;
    
    /**  TODO */
    private Combo comboType;
    
    /**  TODO */
    private Composite compositeSwap;
    
    /**  TODO */
    private Text txtServer;
    
    /**  TODO */
    private StackLayout layout;
    
    /**  TODO */
    private Composite compositeRemote;
    
    /**  TODO */
    private Composite compositeLocal;
    
    /**  TODO */
    private Text txtPort;
    
    /**  TODO */
    private Text txtUsername;
    
    /**  TODO */
    private Text txtPassword;
    
    /**  TODO */
    private Text txtDatabase;
    
    /**  TODO */
    private Label lblLocation;
    
    /**  TODO */
    private Combo comboLocation;
    
    /**  TODO */
    private Button btnChoose;
    
    /**  TODO */
    private Composite container;

    /* String constants for different database types */
    /**  TODO */
    private static final String MYSQL = "MySQL";
    
    /**  TODO */
    private static final String POSTGRESQL = "PostgreSQL";
    
    /**  TODO */
    private static final String SQLITE = "SQLite";


    /**
     * Creates a new instance of this page and sets its title and description.
     *
     * @param wizardImport Reference to wizard containing this page
     */
    public ImportWizardPageJDBC(ImportWizard wizardImport) {

        super("WizardImportJdbcPage");
        setTitle("JDBC");
        setDescription("Please provide the information requested below");
        this.wizardImport = wizardImport;
    }

    /**
     * Creates the design of this page
     * 
     * This adds all the controls to the page along with their listeners.
     *
     * @param parent
     * @note {@link #compositeSwap} contains the actual text fields. Depending
     *       upon the status of {@link #comboType}, it will either display {@link #compositeRemote} or {@link #compositeLocal}.
     */
    public void createControl(Composite parent) {

        container = new Composite(parent, SWT.NULL);

        setControl(container);
        container.setLayout(new GridLayout(2, false));

        /* Type label + combobox */
        lblType = new Label(container, SWT.NONE);
        lblType.setText("Type");

        /* Combo for choosing database type */
        comboType = new Combo(container, SWT.READ_ONLY);
        comboType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        comboType.setItems(new String[] {MYSQL, POSTGRESQL, SQLITE});
        comboType.addSelectionListener(new SelectionAdapter() {

            /**
             * Swaps the composites, resets it and triggers a relayout
             */
            @Override
            public void widgetSelected(SelectionEvent e) {

                setMessage(null);
                setErrorMessage(null);
                setPageComplete(false);

                /* Display compositeLocal in case of SQLite */
                if (comboType.getText().equals(SQLITE)) {
                    comboLocation.removeAll();
                    layout.topControl = compositeLocal;

                /* Display compositeRemote otherwise */
                } else {

                    layout.topControl = compositeRemote;

                    /* Set default ports in case text field is empty */
                    if (txtPort.getText().isEmpty()) {

                        if (comboType.getText().equals(MYSQL)) {
                            txtPort.setText("3306");
                        } else if (comboType.getText().equals(POSTGRESQL)) {
                            txtPort.setText("5432");
                        }
                    }
                }

                /* Trigger relayout */
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

        /* Mark page as incomplete by default */
        setPageComplete(false);
    }

    /**
     * Creates the content of {@link #compositeLocal}
     *
     * This adds a file chooser and an appropriate combo to select files.
     * Selecting a file from the combo will trigger a read of the tables. If
     * everything is fine, the tables from the database will be read.
     *
     * @see {@link #readTables()}
     */
    private void createCompositeLocal() {

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

            /* Read tables from file */
            @Override
            public void widgetSelected(SelectionEvent e) {
                setPageComplete(false);
                setErrorMessage(null);

                connect();
                readTables();
            }
        });

        /* Button to open file selection dialog */
        btnChoose = new Button(compositeLocal, SWT.NONE);
        btnChoose.setText("Browse...");
        btnChoose.addSelectionListener(new SelectionAdapter() {

            /**
             * Opens a file selection dialog for "*.db" files
             *
             * If a valid file was selected, it is added to
             * {@link #comboLocation} when it wasn't already there. It is then
             * preselected within {@link #comboLocation}.
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

    /**
     * Creates the content of {@link #compositeRemote}
     *
     * This adds all of the labels and text fields necessary to connect to a
     * remote database server. If everything is fine, the tables from the
     * database will be read.
     *
     * @see {@link #readTables()}
     */
    private void createCompositeRemote() {

        compositeRemote = new Composite(compositeSwap, SWT.NONE);
        compositeRemote.setLayout(new GridLayout(2, false));

        /**
         * Tries to connect to database on traverse and focusLost events
         *
         * @see {@link #tryToConnect()}
         */
        class ConnectionListener extends FocusAdapter implements TraverseListener {

            /**
             * Handles focusLost events
             *
             * @see {@link #tryToConnect()}
             */
            @Override
            public void focusLost(FocusEvent e) {
                tryToConnect();
            }

            /**
             * Handles traverse events (enter, tab, etc.)
             *
             * @see {@link #tryToConnect()}
             */
            @Override
            public void keyTraversed(TraverseEvent e) {
                tryToConnect();
            }
        }

        ConnectionListener connectionListener = new ConnectionListener();

        Label lblServer = new Label(compositeRemote, SWT.NONE);
        lblServer.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblServer.setText("Server");

        txtServer = new Text(compositeRemote, SWT.BORDER);
        txtServer.setText("localhost");
        txtServer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        txtServer.addFocusListener(connectionListener);
        txtServer.addTraverseListener(connectionListener);

        Label lblPort = new Label(compositeRemote, SWT.NONE);
        lblPort.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblPort.setText("Port");

        txtPort = new Text(compositeRemote, SWT.BORDER);
        txtPort.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        txtPort.addFocusListener(connectionListener);
        txtPort.addTraverseListener(connectionListener);

        Label lblUsername = new Label(compositeRemote, SWT.NONE);
        lblUsername.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblUsername.setText("Username");

        txtUsername = new Text(compositeRemote, SWT.BORDER);
        txtUsername.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        txtUsername.addFocusListener(connectionListener);
        txtUsername.addTraverseListener(connectionListener);

        Label lblPassword = new Label(compositeRemote, SWT.NONE);
        lblPassword.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblPassword.setText("Password");

        txtPassword = new Text(compositeRemote, SWT.BORDER | SWT.PASSWORD);
        txtPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        txtPassword.addFocusListener(connectionListener);
        txtPassword.addTraverseListener(connectionListener);

        Label lblDatabase = new Label(compositeRemote, SWT.NONE);
        lblDatabase.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblDatabase.setText("Database");

        txtDatabase = new Text(compositeRemote, SWT.BORDER);
        txtDatabase.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        txtDatabase.addFocusListener(connectionListener);
        txtDatabase.addTraverseListener(connectionListener);
    }

    /**
     * Tries to establish a remote JDBC connection
     *
     * Unless all mandatory fields (everything besides the password) are
     * not empty this will try to connect to the database. It sets the message
     * and errors of this page accordingly and will also try to read in the
     * tables once a successfull connection has been established.
     *
     * @see {@link #readTables()}
     */
    private void tryToConnect() {

        setErrorMessage(null);
        setMessage(null);

        String server = txtServer.getText();
        String port = txtPort.getText();
        String username = txtUsername.getText();
        String database = txtDatabase.getText();

        if (server.isEmpty() || port.isEmpty() || username.isEmpty() || database.isEmpty()) {
            return;
        }

        setMessage("Trying to connect to database", INFORMATION);
        if (connect()) {
            setMessage("Successfully connected to database", INFORMATION);
            readTables();
        }
    }

    /**
     * Connects to the database
     *
     * This tries to establish an JDBC connection. In case of an error
     * appropriate error messages are set. Otherwise the connection is passed
     * on to {@link ImportWizardModel}. The return value indicates whether a
     * connection has been established.
     *
     * @return True if successfully connected, false otherwise
     *
     * @see {@link ImportWizardModel#setJdbcConnection(Connection)}
     */
    protected boolean connect() {

        try {

            Connection connection = null;

            if (comboType.getText().equals(SQLITE)) {

                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:" + comboLocation.getText());

            } else if (comboType.getText().equals(MYSQL)) {

                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://" + txtServer.getText() + ":" + txtPort.getText() + "/" + txtDatabase.getText(), txtUsername.getText(), txtPassword.getText());

            } else if (comboType.getText().equals(POSTGRESQL)) {

                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection("jdbc:postgresql://" + txtServer.getText() + ":" + txtPort.getText() + "/" + txtDatabase.getText(), txtUsername.getText(), txtPassword.getText());

            }
            try {
                if (!wizardImport.getData().getJdbcConnection().isClosed()) {
                    wizardImport.getData().getJdbcConnection().close();
                }
            } catch (Exception e){
                /* Die silently*/
            }
            wizardImport.getData().setJdbcConnection(connection);
            return true;

        } catch (ClassNotFoundException e) {
            setErrorMessage("No JDBC driver for selected connection type");
            return false;
        } catch (SQLException e) {
            /* Database connection error */
            setErrorMessage(e.getLocalizedMessage());
            return false;
        }
    }

    /**
     * Reads in the tables
     *
     * If successful, the page is marked as complete and a list of tables is
     * assigned to {@link ImportWizardModel}. Otherwise an appropriate error messages
     * is set.
     *
     * @see {@link ImportWizardModel#setJdbcTables(List)}
     */
    protected void readTables() {

        try {
            Connection connection = wizardImport.getData().getJdbcConnection();
            String[] tableTypes = {"TABLE", "VIEW"};
            ResultSet rs = connection.getMetaData().getTables(null, null, "%", tableTypes);
            List<String> tables = new ArrayList<String>();

            while(rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }

            wizardImport.getData().setJdbcTables(tables);
            setPageComplete(true);

        } catch (SQLException e)  {
            setErrorMessage("Couldn't read tables from database");
        }
    }
}
