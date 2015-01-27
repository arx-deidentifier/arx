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

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.view.impl.wizard.ImportWizardModel.SourceType;
import org.deidentifier.arx.io.ImportColumn;
import org.deidentifier.arx.io.ImportConfiguration;
import org.deidentifier.arx.io.ImportConfigurationCSV;
import org.deidentifier.arx.io.ImportConfigurationExcel;
import org.deidentifier.arx.io.ImportConfigurationJDBC;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;

/**
 * Wizard guiding the user through the process of importing data
 * 
 * The user is taken through the process of importing data into the GUI step by
 * step. All necessary information is asked for (e.g. source type, appropriate
 * details for each source, etc.), too.
 * 
 * Refer to {@link ImportWizardPageSource} for details about which source types
 * are supported and to the appropriate page(s) itself for more details about a
 * specific source type.
 * 
 * @author Karol Babioch
 * @author Fabian Prasser
 */
public class ImportWizard extends ARXWizard<ImportConfiguration> {

    /** Reference to container storing all the data gathered by the wizard. */
    private ImportWizardModel       data          = new ImportWizardModel();

    /** Reference of controller being used by this wizard. */
    private Controller              controller;

    /** Reference of model being used by this wizard. */
    private Model                   model;

    /*
     * All of the pages provided by this wizard
     */
    /**  TODO */
    private ImportWizardPageSource  sourcePage;
    
    /**  TODO */
    private ImportWizardPageCSV     csvPage;
    
    /**  TODO */
    private ImportWizardPageColumns columnPage;
    
    /**  TODO */
    private ImportWizardPagePreview previewPage;
    
    /**  TODO */
    private ImportWizardPageJDBC    jdbcPage;
    
    /**  TODO */
    private ImportWizardPageTable   tablePage;
    
    /**  TODO */
    private ImportWizardPageExcel   xlsPage;

    /**
     * Holds reference to the page currently being shown
     * 
     * This is set within {@link #getNextPage(IWizardPage)} and later on used by
     * {@link #canFinish()} to determine whether the wizard can be finished.
     */
    private IWizardPage             currentPage;

    /**
     * Configuration representing all of the choices that were made
     * 
     * This configuration is the result of the whole wizard process. It will be
     * created once the wizard is about to finish {@link #performFinish()} and
     * can be accessed by {@link #getResultingConfiguration()}.
     */
    private ImportConfiguration           configuration = null;

    /**
     * Creates a new data import wizard and sets the window title.
     *
     * @param controller Reference to controller
     * @param model Reference to model
     */
    public ImportWizard(Controller controller, Model model) {

        setWindowTitle("Import data");
        this.setDefaultPageImageDescriptor(ImageDescriptor.createFromImage(controller.getResources()
                                                                           .getImage("import.png"))); //$NON-NLS-1$
        this.controller = controller;
        this.model = model;
    }

    /**
     * Adds all of the available pages to the wizard.
     *
     * @note Note that for reasons of simplicity all pages are directly added
     *       here. The page ordering is handled by {@link #getNextPage(IWizardPage)}.
     */
    @Override
    public void addPages() {

        sourcePage = new ImportWizardPageSource(this);
        addPage(sourcePage);

        csvPage = new ImportWizardPageCSV(this);
        addPage(csvPage);

        columnPage = new ImportWizardPageColumns(this);
        addPage(columnPage);

        previewPage = new ImportWizardPagePreview(this);
        addPage(previewPage);

        jdbcPage = new ImportWizardPageJDBC(this);
        addPage(jdbcPage);

        tablePage = new ImportWizardPageTable(this);
        addPage(tablePage);

        xlsPage = new ImportWizardPageExcel(this);
        addPage(xlsPage);
    }

    /**
     * Determines when the wizard should be finishable
     * 
     * The wizard can only be finished on the {@link #previewPage preview page}.
     * This makes sure that the user is signs off on the settings previously
     * made.
     *
     * @return
     * @see {@link #performFinish()}
     */
    @Override
    public boolean canFinish() {
        return this.currentPage == previewPage;
    }

    /**
     * Handles the correct ordering of wizard pages
     * 
     * This method makes sure that the correct page is shown once the user hits
     * the "next" button. The page flow depends <code>currentPage</code> and the
     * selected {@link ImportWizardModel#getSourceType() sourceType}.
     * 
     * @param currentPage
     *            The page that is currently being shown
     * 
     * @return The page that will be shown next
     */
    @Override
    public IWizardPage getNextPage(IWizardPage currentPage) {

        this.currentPage = currentPage;
        if (currentPage == sourcePage) {
            SourceType src = data.getSourceType();
            if (src == SourceType.CSV) {
                return csvPage;
            } else if (src == SourceType.JDBC) {
                return jdbcPage;
            } else if (src == SourceType.EXCEL) {
                return xlsPage;
            }
        } else if (currentPage == csvPage) {
            return columnPage;
        } else if (currentPage == columnPage) {
            return previewPage;
        } else if (currentPage == jdbcPage) {
            return tablePage;
        } else if (currentPage == tablePage) {
            return columnPage;
        } else if (currentPage == xlsPage) {
            return columnPage;
        }

        return null;
    }

    /**
     * Returns a reference to DataSourceConfiguration
     * 
     * The wizard will built an appropriate {@link ImportConfiguration} object once it
     * is about to finish {@link #performFinish()}. This object can then be
     * retrieved using this method.
     *
     * @return {@link #configuration} The resulting data source configuration
     * @note Note however, that the return value might be null, when the wizard
     *       wasn't completed successfully.
     */
    public ImportConfiguration getResult() {
        return configuration;
    }

    /**
     * 
     * Cancel pressed.
     *
     * @return
     */
    @Override
    public boolean performCancel() {
        try {
            if (data.getJdbcConnection() != null && !data.getJdbcConnection().isClosed()) {
                data.getJdbcConnection().close();
            }
        } catch (Exception e) { /* Die silently */ }
        return true;
    }
    
    /**
     * Gets executed once the wizard is about to finish
     * 
     * This will build an appropriate {@link ImportConfiguration} object, depending
     * upon the {@link ImportWizardModel#getSourceType() source type} and the
     * choices the user made during the process of the wizard.
     * 
     * {@link #configuration} will hold a reference of the object. This can be
     * retrieved later on by {@link #getResultingConfiguration()}.
     *
     * @return
     * @see {@link #getResultingConfiguration()}
     */
    @Override
    public boolean performFinish() {

        if (data.getSourceType() == SourceType.CSV) {
            
            configuration = new ImportConfigurationCSV(data.getFileLocation(),
                                                     data.getCsvSeparator(),
                                                     data.getFirstRowContainsHeader());

        } else if (data.getSourceType() == SourceType.EXCEL) {

            configuration = new ImportConfigurationExcel(data.getFileLocation(),
                                                       data.getExcelSheetIndex(),
                                                       data.getFirstRowContainsHeader());

        } else if (data.getSourceType() == SourceType.JDBC) {

            configuration = new ImportConfigurationJDBC(data.getJdbcConnection(),
                                                  data.getSelectedJdbcTable());

        } else {
            throw new RuntimeException("Configuration type not supported");
        }

        for (ImportColumn c : data.getEnabledColumns()) {
            configuration.addColumn(c);
        }
        
        if (data.getSourceType() != SourceType.JDBC) {
            try {
                if (data.getJdbcConnection() != null && !data.getJdbcConnection().isClosed()) {
                    data.getJdbcConnection().close();
                }
            } catch (Exception e) { /* Die silently */ }
        }
        
        return true;
    }

    /**
     * Returns a reference to the controller being used by this wizard.
     *
     * @return
     */
    Controller getController() {
        return controller;
    }

    /**
     * Returns a reference to the object containing the gathered data.
     *
     * @return
     */
    ImportWizardModel getData() {
        return data;
    }

    /**
     * Returns a reference to the model being used by this wizard.
     *
     * @return
     */
    Model getModel() {
        return model;
    }
}
