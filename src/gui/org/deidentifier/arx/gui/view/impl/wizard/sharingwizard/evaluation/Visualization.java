package org.deidentifier.arx.gui.view.impl.wizard.sharingwizard.evaluation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.view.impl.wizard.sharingwizard.checklist.Checklist;

/**
 * base class for the visualizations
 *
 */
public abstract class Visualization extends Composite {
	/**
	 * the checklist used for the visualization
	 */
	protected Checklist checklist;
	
	protected Controller controller;
	
	/**
	 * create a new visualization for the checklist
	 * @param parent the parent
	 * @param controller the controller
	 * @param checklist the checklist
	 */
	public Visualization(Composite parent, Controller controller, Checklist checklist) {
		super(parent, SWT.NO_SCROLL);
		
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		this.setLayoutData(gridData);
		
		this.checklist = checklist;
		this.controller = controller;
		this.createVisualization();
	}
	
	/**
	 * used in subclasses for the initial setup
	 */
	protected void createVisualization() {
		
	}
	
	/**
	 * used in subclasses to respond to changes
	 */
	public void updateWeights() {
		
	}

}
