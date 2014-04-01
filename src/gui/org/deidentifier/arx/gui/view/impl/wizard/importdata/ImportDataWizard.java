package org.deidentifier.arx.gui.view.impl.wizard.importdata;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.view.impl.wizard.importdata.ImportData.SourceType;
import org.deidentifier.arx.io.importdata.CSVConfiguration;
import org.deidentifier.arx.io.importdata.Column;
import org.deidentifier.arx.io.importdata.DataSourceConfiguration;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;


/**
 * Wizard guiding the user through the process of importing data
 *
 * The user is taken through the process of importing data into the GUI step by
 * step. All necessary information is asked for (e.g. source type, appropriate
 * details for each source, etc.), too.
 *
 * Multiple source types are supported:
 *
 * <ul>
 *  <li>{@link CsvPage} CSV</li>
 *  <li>{@link JdbcPage} Database (JDBC)</li>
 *  <li>{@link XlsPage} Excel (XLS)</li>
 * </ul>
 *
 * Refer to appropriate page(s) for more details about a specific source type.
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
    private XlsPage xlsPage;

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

        xlsPage = new XlsPage(this);
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

            } else if (src == SourceType.XLS) {

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

            try {

                CSVConfiguration config = new CSVConfiguration(data.getFileLocation(), data.getCsvSeparator(), data.getFirstRowContainsHeader());

                for (Column c : data.getEnabledColumns()) {

                    config.addColumn(c);

                }

                this.configuration = config;

            } catch (Exception e) {

                // TODO: There should be no need to catch exceptions
                this.configuration = null;

            }

        } else {

            // TODO: Implement

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
