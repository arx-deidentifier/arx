/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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

import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.impl.common.DelayedChangeListener;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
 * now MS SQL, MySQL, PostgreSQL and SQLite is supported. In case of remote database
 * types (i.e. MS SQL, MySQL and PostgreSQL) the user is asked for the server and a
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
    private ImportWizard        wizardImport;
    
    /** Widget */
    private Label               lblType;
    
    /** Widget */
    private Combo               comboType;
    
    /** Widget */
    private Composite           compositeSwap;
    
    /** Widget */
    private Text                txtServer;
    
    /** Widget */
    private StackLayout         layout;
    
    /** Widget */
    private Composite           compositeRemote;
    
    /** Widget */
    private Composite           compositeLocal;
    
    /** Widget */
    private Text                txtPort;
    
    /** Widget */
    private Text                txtUsername;
    
    /** Widget */
    private Text                txtPassword;
    
    /** Widget */
    private Text                txtDatabase;
    
    /** Widget */
    private Label               lblLocation;
    
    /** Widget */
    private Combo               comboLocation;
    
    /** Widget */
    private Button              btnChoose;
    
    /** Widget */
    private Composite           container;
    
    /** Widget */
    private static final String ORACLE     = "Oracle";    //$NON-NLS-1$
                                                           
    /** Widget */
    private static final String MSSQL      = "MS SQL";    //$NON-NLS-1$
                                                           
    /** Widget */
    private static final String MYSQL      = "MySQL";     //$NON-NLS-1$
                                                           
    /** Widget */
    private static final String POSTGRESQL = "PostgreSQL"; //$NON-NLS-1$
                                                           
    /** Widget */
    private static final String SQLITE     = "SQLite";    //$NON-NLS-1$
                                                           
    /**
     * Creates a new instance of this page and sets its title and description.
     *
     * @param wizardImport Reference to wizard containing this page
     */
    public ImportWizardPageJDBC(ImportWizard wizardImport) {
        
        super("WizardImportJdbcPage"); //$NON-NLS-1$
        setTitle("JDBC"); //$NON-NLS-1$
        setDescription(Resources.getMessage("ImportWizardPageJDBC.6")); //$NON-NLS-1$
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
        lblType.setText(Resources.getMessage("ImportWizardPageJDBC.7")); //$NON-NLS-1$
        
        /* Combo for choosing database type */
        comboType = new Combo(container, SWT.READ_ONLY);
        comboType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        comboType.setItems(new String[] { ORACLE, MSSQL, POSTGRESQL, MYSQL, SQLITE });
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
                    
                    /* Set default ports*/
                    if (comboType.getText().equals(MSSQL)) {
                        txtPort.setText("1433"); //$NON-NLS-1$
                    } else if (comboType.getText().equals(MYSQL)) {
                        txtPort.setText("3306"); //$NON-NLS-1$
                    } else if (comboType.getText().equals(POSTGRESQL)) {
                        txtPort.setText("5432"); //$NON-NLS-1$
                    } else if (comboType.getText().equals(ORACLE)) {
                        txtPort.setText("1521"); //$NON-NLS-1$
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
        lblLocation.setText(Resources.getMessage("ImportWizardPageJDBC.11")); //$NON-NLS-1$
        
        /* Combo box for selection of file */
        comboLocation = new Combo(compositeLocal, SWT.READ_ONLY);
        comboLocation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        comboLocation.addSelectionListener(new SelectionAdapter() {
            
            /* Read tables from file */
            @Override
            public void widgetSelected(SelectionEvent e) {
                setErrorMessage(null);
                connect();
                boolean ok = readTables();
                setPageComplete(ok);
                if (ok) {
                    setMessage(Resources.getMessage("ImportWizardPageJDBC.21"), INFORMATION); //$NON-NLS-1$
                }
            }
        });
        
        /* Button to open file selection dialog */
        btnChoose = new Button(compositeLocal, SWT.NONE);
        btnChoose.setText(Resources.getMessage("ImportWizardPageJDBC.12")); //$NON-NLS-1$
        btnChoose.addSelectionListener(new SelectionAdapter() {
            
            /**
             * Opens a file selection dialog for "*.db" files
             *
             * If a valid file was selected, it is added to {@link #comboLocation} when it wasn't already there. It is then
             * preselected within {@link #comboLocation}.
             */
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                
                /* Open file dialog */
                final String path = wizardImport.getController().actionShowOpenFileDialog(getShell(), "*.db"); //$NON-NLS-1$
                
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
        
        // Tries to connect to database on changes
        DelayedChangeListener connectionTester = new DelayedChangeListener(1000) {
            @Override
            public void delayedEvent() {
                tryToConnect();
            }
        };
        
        Label lblServer = new Label(compositeRemote, SWT.NONE);
        lblServer.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblServer.setText(Resources.getMessage("ImportWizardPageJDBC.14")); //$NON-NLS-1$
        
        txtServer = new Text(compositeRemote, SWT.BORDER);
        txtServer.setText("localhost"); //$NON-NLS-1$
        txtServer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        txtServer.addModifyListener(connectionTester);
        
        Label lblPort = new Label(compositeRemote, SWT.NONE);
        lblPort.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblPort.setText(Resources.getMessage("ImportWizardPageJDBC.16")); //$NON-NLS-1$
        
        txtPort = new Text(compositeRemote, SWT.BORDER);
        txtPort.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        txtPort.addModifyListener(connectionTester);
        
        Label lblUsername = new Label(compositeRemote, SWT.NONE);
        lblUsername.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblUsername.setText(Resources.getMessage("ImportWizardPageJDBC.0")); //$NON-NLS-1$
        
        txtUsername = new Text(compositeRemote, SWT.BORDER);
        txtUsername.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        txtUsername.addModifyListener(connectionTester);
        
        Label lblPassword = new Label(compositeRemote, SWT.NONE);
        lblPassword.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblPassword.setText(Resources.getMessage("ImportWizardPageJDBC.1")); //$NON-NLS-1$
        
        txtPassword = new Text(compositeRemote, SWT.BORDER | SWT.PASSWORD);
        txtPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        txtPassword.addModifyListener(connectionTester);
        
        Label lblDatabase = new Label(compositeRemote, SWT.NONE);
        lblDatabase.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblDatabase.setText(Resources.getMessage("ImportWizardPageJDBC.19")); //$NON-NLS-1$
        
        txtDatabase = new Text(compositeRemote, SWT.BORDER);
        txtDatabase.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        txtDatabase.addModifyListener(connectionTester);
    }
    
    /**
     * Tries to establish a remote JDBC connection
     *
     * Unless all mandatory fields (everything besides the password) are
     * not empty this will try to connect to the database. It sets the message
     * and errors of this page accordingly and will also try to read in the
     * tables once a successful connection has been established.
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
            setMessage(Resources.getMessage("ImportWizardPageJDBC.6")); //$NON-NLS-1$
            setPageComplete(false);
            return;
        }
        
        setMessage(Resources.getMessage("ImportWizardPageJDBC.20"), INFORMATION); //$NON-NLS-1$
        if (connect()) {
            setMessage(Resources.getMessage("ImportWizardPageJDBC.21"), INFORMATION); //$NON-NLS-1$
            setPageComplete(readTables());
        } else {
            setPageComplete(false);
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
    private boolean connect() {
        
        try {
            
            Connection connection = null;
            
            if (comboType.getText().equals(SQLITE)) {
                
                Class.forName("org.sqlite.JDBC"); //$NON-NLS-1$
                connection = DriverManager.getConnection("jdbc:sqlite:" + comboLocation.getText()); //$NON-NLS-1$
                
            } else if (comboType.getText().equals(POSTGRESQL)) {
                
                Class.forName("org.postgresql.Driver"); //$NON-NLS-1$
                connection = DriverManager.getConnection("jdbc:postgresql://" + txtServer.getText() + ":" + txtPort.getText() + "/" + txtDatabase.getText(), txtUsername.getText(), txtPassword.getText()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                
            } else if (comboType.getText().equals(MSSQL)) {
                
                Class.forName("net.sourceforge.jtds.jdbc.Driver"); //$NON-NLS-1$
                connection = DriverManager.getConnection("jdbc:jtds:sqlserver://" + txtServer.getText() + ":" + txtPort.getText() + "/" + txtDatabase.getText(), txtUsername.getText(), txtPassword.getText()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                
            } else if (comboType.getText().equals(MYSQL)) {
                
                Class.forName("com.mysql.jdbc.Driver"); //$NON-NLS-1$
                connection = DriverManager.getConnection("jdbc:mysql://" + txtServer.getText() + ":" + txtPort.getText() + "/" + txtDatabase.getText(), txtUsername.getText(), txtPassword.getText()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            } else if (comboType.getText().equals(ORACLE)) {
                
                Class.forName("oracle.jdbc.driver.OracleDriver"); //$NON-NLS-1$
                connection = DriverManager.getConnection("jdbc:oracle:thin:@" + txtServer.getText() + ":" + txtPort.getText() + ":" + txtDatabase.getText(), txtUsername.getText(), txtPassword.getText()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            
            try {
                if (!wizardImport.getData().getJdbcConnection().isClosed()) {
                    wizardImport.getData().getJdbcConnection().close();
                }
            } catch (Exception e) {
                /* Die silently */
            }
            wizardImport.getData().setJdbcConnection(connection);
            return true;
            
        } catch (ClassNotFoundException e) {
            setErrorMessage(Resources.getMessage("ImportWizardPageJDBC.36")); //$NON-NLS-1$
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
     * assigned to {@link ImportWizardModel}. Otherwise an appropriate error message
     * is set.
     *
     * @see {@link ImportWizardModel#setJdbcTables(List)}
     */
    private boolean readTables() {
        ResultSet rs = null;
        try {
            Connection connection = wizardImport.getData().getJdbcConnection();
            String[] tableTypes = { "TABLE", "VIEW" }; //$NON-NLS-1$ //$NON-NLS-2$
            rs = connection.getMetaData().getTables(null, null, "%", tableTypes); //$NON-NLS-1$
            List<String> tables = new ArrayList<String>();
            
            while (rs.next()) {
                String name = rs.getString("TABLE_NAME"); //$NON-NLS-1$
                String schema = rs.getString("TABLE_SCHEM"); //$NON-NLS-1$
                if (schema != null) {
                    name = schema + "." + name; //$NON-NLS-1$
                }
                tables.add(name); 
            }
            
            wizardImport.getData().setJdbcTables(tables);
            return true;
        } catch (SQLException e) {
            setErrorMessage(Resources.getMessage("ImportWizardPageJDBC.41")); //$NON-NLS-1$
            return false;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                /* Ignore silently */
            }
        }
    }
}
