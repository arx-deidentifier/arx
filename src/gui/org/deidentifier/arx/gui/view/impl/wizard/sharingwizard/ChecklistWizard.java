package org.deidentifier.arx.gui.view.impl.wizard.sharingwizard;

import org.eclipse.jface.wizard.Wizard;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelRiskWizard;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.impl.wizard.sharingwizard.checklist.*;

/**
 * The checklist wizard for evaluating the data sharing risk
 *
 */
public class ChecklistWizard extends Wizard {
	
	/**
	 * array containing each section's wizard page
	 */
	protected SectionPage[] pages;
	
	/**
	 * final page showing the evaluation
	 */
	protected EvaluationPage evaluationPage;
	
	/**
	 * the checklist used for the wizard
	 */
	private Checklist checklist;
	
	/**
	 * the arx controller
	 */
	private Controller controller;

	/**
	 * create a new checklist wizard
	 * @param checklist the checklist to use
	 * @param controller the controller opening the wizard 
	 */
	public ChecklistWizard(Checklist checklist, Controller controller) {
		super();
		
		this.checklist = checklist;
		// try to restore a saved version of the weights
		Model model = controller.getModel();
		if(model != null) {
			ModelRiskWizard savedModel = model.getRiskWizardModel();
			if(savedModel != null) {
				System.out.println(savedModel);
				this.checklist.setWeightConfiguration(savedModel);
			}
		}
		this.controller = controller;
		this.setWindowTitle(Resources.getMessage("RiskWizard.0"));
	}
	
	/**
	 * adds the necessary pages to the wizard
	 */
	@Override
	public void addPages() {
		// add a page for each section
		Section[] sections = checklist.getSections();
		pages = new SectionPage[sections.length];
		for(int i = 0; i < sections.length; i++) {
			Section s = sections[i];
			SectionPage p = new SectionPage(s);
			this.addPage(p);
			pages[i] = p;
		}
		
		// add the final evaluation page
		evaluationPage = new EvaluationPage(checklist, controller);
		this.addPage(evaluationPage);
	}

	/**
	 * called when the dialog is finished, saves the current weights
	 */
	@Override
	public boolean performFinish() {
		// Print the result to the console
		//this.pages = null;
		//return false;
		this.controller.getModel().setRiskWizardModel(this.checklist.getWeightConfiguration());
		//System.out.println(checklist);
		return true;
	}
	
	/**
	 * updates the weights for each section and the evaluation
	 */
	protected void updateWeights() {
		for(SectionPage page : pages) {
			page.updateWeights();
		}
		evaluationPage.updateWeights();
	}
	
}
