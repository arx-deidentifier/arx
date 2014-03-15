package org.deidentifier.arx.gui.view.impl.importwizard;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.eclipse.jface.wizard.Wizard;


public class WizardImport extends Wizard {

    private WizardImportData data = new WizardImportData(this);

    private Controller controller;
    private Model model;

    private WizardImportSourcePage sourcePage;
    private WizardImportCsvPage csvPage;
    private WizardImportColumnPage columnPage;
    private WizardImportPreviewPage previewPage;


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

    }

    @Override
    public boolean performFinish()
    {

        // TODO: By now everything is known to actually import the data ...

        return true;

    }

}
