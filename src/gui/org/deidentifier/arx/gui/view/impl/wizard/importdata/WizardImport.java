package org.deidentifier.arx.gui.view.impl.wizard.importdata;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.view.impl.wizard.importdata.WizardImportData.source;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;


public class WizardImport extends Wizard {

    private WizardImportData data = new WizardImportData();

    private Controller controller;
    private Model model;

    private WizardImportSourcePage sourcePage;
    private WizardImportCsvPage csvPage;
    private WizardImportColumnPage columnPage;
    private WizardImportPreviewPage previewPage;
    private WizardImportJdbcPage jdbcPage;
    private WizardImportTablePage tablePage;
    private WizardImportXlsPage xlsPage;


    private IWizardPage currentPage;

    Controller getController()
    {

        return controller;

    }

    Model getModel()
    {

        return model;

    }

    WizardImportData getData() {

        return data;

    }

    public WizardImport(Controller controller, Model model)
    {

        setWindowTitle("Import data wizard");

        this.controller = controller;
        this.model = model;

    }

    @Override
    public void addPages()
    {

        sourcePage = new WizardImportSourcePage(this);
        addPage(sourcePage);

        csvPage = new WizardImportCsvPage(this);
        addPage(csvPage);

        columnPage = new WizardImportColumnPage(this);
        addPage(columnPage);

        previewPage = new WizardImportPreviewPage(this);
        addPage(previewPage);

        jdbcPage = new WizardImportJdbcPage(this);
        addPage(jdbcPage);

        tablePage = new WizardImportTablePage(this);
        addPage(tablePage);

        xlsPage = new WizardImportXlsPage(this);
        addPage(xlsPage);

    }

    @Override
    public IWizardPage getNextPage(IWizardPage currentPage) {

        this.currentPage = currentPage;

        if (currentPage == sourcePage) {

            source src = data.getSource();

            if (src == source.CSV) {

                return csvPage;

            } else if (src == source.JDBC) {

                return jdbcPage;

            } else if (src == source.XLS) {

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

    @Override
    public boolean canFinish() {

        return this.currentPage == previewPage;

    }

    @Override
    public boolean performFinish()
    {

        // TODO: By now everything is known to actually import the data ...

        return true;

    }

}
