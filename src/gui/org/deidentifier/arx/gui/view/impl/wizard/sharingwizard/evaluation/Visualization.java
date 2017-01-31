package org.deidentifier.arx.gui.view.impl.wizard.sharingwizard.evaluation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.view.impl.wizard.sharingwizard.checklist.Checklist;

public abstract class Visualization extends Composite {
	protected Checklist checklist;
	protected Controller controller;
	
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
	
	protected void createVisualization() {
		
	}
	
	public void updateWeights() {
		
	}

}
