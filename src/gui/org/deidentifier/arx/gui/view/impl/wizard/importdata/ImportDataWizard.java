package org.deidentifier.arx.gui.view.impl.wizard.importdata;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.view.impl.wizard.importdata.ImportData.DataSourceType;
import org.deidentifier.arx.io.importdata.CSVConfiguration;
import org.deidentifier.arx.io.importdata.Column;
import org.deidentifier.arx.io.importdata.DataSourceConfiguration;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;


/**
 * Wizard guiding the user through the process of importing data into the GUI
 *
 * The user is taken through the process of importing data into the GUI step by
 * step. This includes the selection of the data source as well as the inquiry
 * for all of the information coming along with it.
 *
 * Multiple sources are supported for the import are supported:
 *
 * <ul>
 *  <li>{@link CsvPage} CSV</li>
 *  <li>{@link JdbcPage} Database (JDBC)</li>
 *  <li>{@link XlsPage} Excel</li>
 * </ul>
 *
 * Refer to appropriate page(s) for more details about a specific source.
 */
public class ImportDataWizard extends Wizard {

    /**
     * Object storing data gathered by the wizard
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
     * This is set within {@link #getNextPage(IWizardPage)} and later used by
     * {@link #canFinish()}.
     */
    private IWizardPage currentPage;


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
     * Adds pages to the wizard.
     *
     * @note Note that for reasons of simplicity all pages are directly added
     * here. The page ordering is handled by {@link #getNextPage(IWizardPage)}.
     *
     * TODO Add pages in a more elegant way
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
     * Handles the correct ordering of wizard pages.
     *
     * As {@link #addPage(IWizardPage)} simply adds all of the wizard pages,
     * this method makes sure that the correct one is shown next. The process
     * flow depends upon the selected source and the <code>currentPage</code>.
     *
     * @param currentPage The page currently being shown
     *
     * @return The page that will be shown next
     *
     * TODO Implement in a more elegant way
     */
    @Override
    public IWizardPage getNextPage(IWizardPage currentPage) {

        this.currentPage = currentPage;

        if (currentPage == sourcePage) {

            DataSourceType src = data.getSource();

            if (src == DataSourceType.CSV) {

                return csvPage;

            } else if (src == DataSourceType.JDBC) {

                return jdbcPage;

            } else if (src == DataSourceType.XLS) {

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
     * Indicates whether finish button should be clickable.
     *
     * The wizard can only be finished after the user has seen the preview on
     * the {@link #previewPage}.
     */
    @Override
    public boolean canFinish() {

        return this.currentPage == previewPage;

    }
    
    DataSourceConfiguration result = null;

    @Override
    public boolean performFinish()
    {
        if (data.getSource() == DataSourceType.CSV) {
            try {
                CSVConfiguration config = new CSVConfiguration(data.getFileLocation(),
                                                                data.getCsvSeparator(),
                                                                data.getFirstRowContainsHeader());
                
                for (Column c : data.getEnabledColumns()) {
                    config.addColumn(c);
                }
                
                this.result = config;
            } catch (Exception e) {
                // TODO: There should be no need to catch exceptions
                this.result = null;
            }
            
        } else {
            // TODO: Implement
        }
        return true;
    }
    
    /**
     * Returns the result
     * @return
     */
    public DataSourceConfiguration getResult() {
        return result;
    }
}
