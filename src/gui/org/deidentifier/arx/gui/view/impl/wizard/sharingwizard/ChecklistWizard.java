package org.deidentifier.arx.gui.view.impl.wizard.sharingwizard;

import org.eclipse.jface.wizard.Wizard;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.view.impl.wizard.sharingwizard.checklist.*;


public class ChecklistWizard extends Wizard {

	protected SectionPage[] pages;
	protected EvaluationPage evaluationPage;
	private Checklist checklist;
	private Controller controller;

	/**
	 * create a new checklist wizard
	 * @param checklist the checklist to use
	 * @param controller the controller opening the wizard 
	 */
	public ChecklistWizard(Checklist checklist, Controller controller) {
		super();
		
		this.checklist = checklist;
		this.controller = controller;
		this.setWindowTitle("Checklist Wizard");
	}
	

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
		checklist.saveWeightDefaults();
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
