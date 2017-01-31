package org.deidentifier.arx.gui.view.impl.wizard.sharingwizard;

import org.eclipse.jface.wizard.Wizard;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.view.impl.wizard.sharingwizard.checklist.*;


public class ChecklistWizard extends Wizard {

	protected SectionPage[] pages;
	protected EvaluationPage evaluationPage;
	private Checklist checklist;
	private Controller controller;

	public ChecklistWizard(Checklist checklist, Controller controller) {
		super();
		
//		setNeedsProgressMonitor(true);
		this.checklist = checklist;
		this.controller = controller;
		this.setWindowTitle("Checklist Wizard");
	}
	

	@Override
	public void addPages() {
		
		Section[] sections = checklist.getSections();
		pages = new SectionPage[sections.length];
		for(int i = 0; i < sections.length; i++) {
			Section s = sections[i];
			SectionPage p = new SectionPage(s);
			this.addPage(p);
			pages[i] = p;
		}
		
		evaluationPage = new EvaluationPage(checklist, controller);
		this.addPage(evaluationPage);
	}

	@Override
	public boolean performFinish() {
		// Print the result to the console
		//this.pages = null;
		//return false;
		checklist.saveWeightDefaults();
		System.out.println(checklist);
		return true;
	}
	
	protected void updateWeights() {
		for(SectionPage page : pages) {
			page.updateWeights();
		}
		evaluationPage.updateWeights();
	}
	
}
