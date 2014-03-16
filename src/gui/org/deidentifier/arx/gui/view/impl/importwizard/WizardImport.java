package org.deidentifier.arx.gui.view.impl.importwizard;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.view.impl.importwizard.WizardImportData.source;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;


public class WizardImport extends Wizard {

    private WizardImportData data = new WizardImportData(this);

    private Controller controller;
    private Model model;

    private WizardImportSourcePage sourcePage;
    private WizardImportCsvPage csvPage;
    private WizardImportColumnPage columnPage;
    private WizardImportPreviewPage previewPage;
    private WizardImportJdbcPage jdbcPage;
    private WizardImportTablePage tablePage;

    private IWizardPage currentPage;


    public Controller getController()
    {

        return controller;

    }

    public Model getModel()
    {

        return model;

    }

    public WizardImport(Controller controller, Model model)
    {

        setWindowTitle("Import data wizard");

        this.controller = controller;

    }

    @Override
    public void addPages()
    {

        sourcePage = new WizardImportSourcePage(data);
        addPage(sourcePage);

        csvPage = new WizardImportCsvPage(data);
        addPage(csvPage);

        columnPage = new WizardImportColumnPage(data);
        addPage(columnPage);

        previewPage = new WizardImportPreviewPage(data);
        addPage(previewPage);

        jdbcPage = new WizardImportJdbcPage(data);
        addPage(jdbcPage);

        tablePage = new WizardImportTablePage(data);
        addPage(tablePage);

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

            }

        } else if (currentPage == csvPage) {

            return columnPage;

        } else if (currentPage == columnPage) {

            return previewPage;

        } else if (currentPage == jdbcPage) {

            return tablePage;

        } else if (currentPage == tablePage) {

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
