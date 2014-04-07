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

package org.deidentifier.arx.gui.view.impl.wizard.importdata;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.view.impl.wizard.importdata.ImportData.SourceType;
import org.deidentifier.arx.io.CSVFileConfiguration;
import org.deidentifier.arx.io.DataSourceConfiguration;
import org.deidentifier.arx.io.ExcelFileConfiguration;
import org.deidentifier.arx.io.importdata.Column;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;


/**
 * Wizard guiding the user through the process of importing data
 *
 * The user is taken through the process of importing data into the GUI step by
 * step. All necessary information is asked for (e.g. source type, appropriate
 * details for each source, etc.), too.
 *
 * Refer to {@link SourcePage} for details about which source types are
 * supported and to the appropriate page(s) itself for more details about a
 * specific source type.
 */
public class ImportDataWizard extends Wizard {

    /**
     * Reference to container storing all the data gathered by the wizard
     */
    private ImportData data = new ImportData();

    /**
     * Reference of controller being used by this wizard
     */
    private Controller controller;

    /**
     * Reference of model being used by this wizard
     */
    private Model model;

    /*
     * All of the pages provided by this wizard
     */
    private SourcePage sourcePage;
    private CsvPage csvPage;
    private ColumnPage columnPage;
    private PreviewPage previewPage;
    private JdbcPage jdbcPage;
    private TablePage tablePage;
    private ExcelPage xlsPage;

    /**
     * Holds reference to the page currently being shown
     *
     * This is set within {@link #getNextPage(IWizardPage)} and later on used
     * by {@link #canFinish()} to determine whether the wizard can be finished.
     */
    private IWizardPage currentPage;

    /**
     * Configuration representing all of the choices that were made
     *
     * This configuration is the result of the whole wizard process. It will be
     * created once the wizard is about to finish {@link #performFinish()} and
     * can be accessed by {@link #getResultingConfiguration()}.
     */
    private DataSourceConfiguration configuration = null;


    /**
     * Returns a reference to the controller being used by this wizard
     */
    Controller getController()
    {

        return controller;

    }

    /**
     * Returns a reference to the model being used by this wizard
     */
    Model getModel()
    {

        return model;

    }

    /**
     * Returns a reference to the object containing the gathered data
     */
    ImportData getData() {

        return data;

    }

    /**
     * Creates a new data import wizard and sets the window title
     *
     * @param controller Reference to controller
     * @param model Reference to model
     */
    public ImportDataWizard(Controller controller, Model model)
    {

        setWindowTitle("Import data wizard");

        this.controller = controller;
        this.model = model;

    }

    /**
     * Adds all of the available pages to the wizard
     *
     * @note Note that for reasons of simplicity all pages are directly added
     * here. The page ordering is handled by {@link #getNextPage(IWizardPage)}.
     *
     * TODO Add pages in a more elegant way, e.g. using an array and loop
     */
    @Override
    public void addPages()
    {

        sourcePage = new SourcePage(this);
        addPage(sourcePage);

        csvPage = new CsvPage(this);
        addPage(csvPage);

        columnPage = new ColumnPage(this);
        addPage(columnPage);

        previewPage = new PreviewPage(this);
        addPage(previewPage);

        jdbcPage = new JdbcPage(this);
        addPage(jdbcPage);

        tablePage = new TablePage(this);
        addPage(tablePage);

        xlsPage = new ExcelPage(this);
        addPage(xlsPage);

    }

    /**
     * Handles the correct ordering of wizard pages
     *
     * This method makes sure that the correct page is shown once the user hits
     * the "next" button. The page flow depends <code>currentPage</code> and
     * the selected {@link ImportData#getSourceType() sourceType}.
     *
     * @param currentPage The page that is currently being shown
     *
     * @return The page that will be shown next
     *
     * TODO Implement in a more elegant way
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
     * Determines when the wizard should be finishable
     *
     * The wizard can only be finished on the
     * {@link #previewPage preview page}. This makes sure that the user is
     * signs off on the settings previously made.
     *
     * @see {@link #performFinish()}
     */
    @Override
    public boolean canFinish() {

        return this.currentPage == previewPage;

    }

    /**
     * Gets executed once the wizard is about to finish
     *
     * This will build an appropriate {@link DataSourceConfiguration} object,
     * depending upon the {@link ImportData#getSourceType() source type} and
     * the choices the user made during the process of the wizard.
     *
     * {@link #configuration} will hold a reference of the object. This can be
     * retrieved later on by {@link #getResultingConfiguration()}.
     *
     * @see {@link #getResultingConfiguration()}
     */
    @Override
    public boolean performFinish()
    {

        if (data.getSourceType() == SourceType.CSV) {

            configuration = new CSVFileConfiguration(data.getFileLocation(), data.getCsvSeparator(), data.getFirstRowContainsHeader());

        } else if (data.getSourceType() == SourceType.EXCEL) {

            configuration = new ExcelFileConfiguration(data.getFileLocation(), data.getExcelSheetIndex(), data.getFirstRowContainsHeader());

        } else {

            throw new RuntimeException("File configuration not supported");

        }

        for (Column c : data.getEnabledColumns()) {

            configuration.addColumn(c);

        }

        return true;

    }

    /**
     * Returns a reference to DataSourceConfiguration
     *
     * The wizard will built an appropriate {@link DataSourceConfiguration}
     * object once it is about to finish {@link #performFinish()}. This object
     * can then be retrieved using this method.
     *
     * @note Note however, that the return value might be null, when the wizard
     * wasn't completed successfully.
     *
     * @return {@link #configuration} The resulting data source configuration
     */
    public DataSourceConfiguration getResultingConfiguration() {

        return configuration;

    }

}
