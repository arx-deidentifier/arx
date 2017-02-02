/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.risk.RiskQuestionnaire;
import org.deidentifier.arx.risk.RiskQuestionnaireSection;
import org.deidentifier.arx.risk.RiskQuestionnaireWeights;
import org.eclipse.jface.wizard.Wizard;

/**
 * The questionnaire wizard for evaluating data sharing risks
 * 
 * @author Thomas Guenzel
 * @author Fabian Prasser
 */
public class RiskWizard extends Wizard {

    /** Array containing each section's wizard page */
    protected RiskWizardPageSection[]  pages;

    /** Final page showing the evaluation */
    protected RiskWizardPageEvaluation evaluationPage;

    /** The questionnaire used for the wizard */
    private RiskQuestionnaire                  checklist;

    /** Controller */
    private Controller                 controller;

    /**
     * Create a new questionnaire wizard
     * 
     * @param checklist
     * @param controller
     */
    public RiskWizard(RiskQuestionnaire checklist, Controller controller) {
        super();

        this.checklist = checklist;
        // try to restore a saved version of the weights
        Model model = controller.getModel();
        if (model != null) {
            RiskQuestionnaireWeights savedModel = model.getRiskWizardModel();
            if (savedModel != null) {
                this.checklist.setWeightConfiguration(savedModel);
            }
        }
        this.controller = controller;
        this.setWindowTitle(Resources.getMessage("RiskWizard.0"));
    }

    /**
     * Adds the necessary pages to the wizard
     */
    @Override
    public void addPages() {
        // add a page for each section
        RiskQuestionnaireSection[] sections = checklist.getSections();
        pages = new RiskWizardPageSection[sections.length];
        for (int i = 0; i < sections.length; i++) {
            RiskQuestionnaireSection s = sections[i];
            RiskWizardPageSection p = new RiskWizardPageSection(s);
            this.addPage(p);
            pages[i] = p;
        }

        // add the final evaluation page
        evaluationPage = new RiskWizardPageEvaluation(checklist, controller);
        this.addPage(evaluationPage);
    }

    /**
     * Called when the dialog is finished, saves the current weights
     */
    @Override
    public boolean performFinish() {
        this.controller.getModel().setRiskWizardModel(this.checklist.getWeightConfiguration());
        return true;
    }

    /**
     * Updates the weights for each section and the evaluation
     */
    protected void updateWeights() {
        for (RiskWizardPageSection page : pages) {
            page.updateWeights();
        }
        evaluationPage.updateWeights();
    }
}
