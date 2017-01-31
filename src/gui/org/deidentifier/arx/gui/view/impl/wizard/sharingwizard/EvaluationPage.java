package org.deidentifier.arx.gui.view.impl.wizard.sharingwizard;



import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.swtchart.Chart;

import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.common.ComponentRiskMonitor;

import org.deidentifier.arx.gui.view.impl.wizard.sharingwizard.checklist.Checklist;
import org.deidentifier.arx.gui.view.impl.wizard.sharingwizard.checklist.Section;
import org.deidentifier.arx.gui.view.impl.wizard.sharingwizard.checklist.WeightConfiguration;
import org.deidentifier.arx.gui.view.impl.wizard.sharingwizard.evaluation.MonitorVisualization;
import org.deidentifier.arx.gui.view.impl.wizard.sharingwizard.evaluation.StackVisualization;
import org.deidentifier.arx.gui.view.impl.wizard.sharingwizard.evaluation.Visualization;

public class EvaluationPage extends WizardPage {
	private Checklist checklist;
	
	private Label fileLabel;
	private Composite topBar;
	
	private Visualization visualization;
	private Composite rootComposite;
	
	private Controller controller;

	protected EvaluationPage(Checklist checklist, Controller controller) {
		super("Evaluation");

		this.checklist = checklist;
		this.controller = controller;
		this.setTitle("Risk Evaluation");
		this.setDescription("This is the risk evaluation based on your answers.");
	}

	@Override
	public void createControl(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 0;
		layout.marginTop = 0;
		layout.marginBottom = 0;
		layout.verticalSpacing = 0;
		layout.makeColumnsEqualWidth = true;

		rootComposite = new Composite(parent, SWT.NO_BACKGROUND);
		rootComposite.setLayout(layout);
		GridData rootData = new GridData();
		rootData.grabExcessHorizontalSpace = true;
		rootData.horizontalAlignment = GridData.FILL;
		rootData.grabExcessVerticalSpace = true;
		rootData.verticalAlignment = GridData.FILL;
		rootComposite.setLayoutData(rootData);

		createTopBar(rootComposite, layout.numColumns);

		//		rootComposite.setBackground(rootComposite.getDisplay().getSystemColor(SWT.COLOR_BLUE));
		
		this.showMonitorVisualization();
		//
		//this.visualization = new StackVisualization(rootComposite, checklist);

		setControl(rootComposite);
	}


	private Composite createTopBar(Composite parent, int span) {
		Composite c = new Composite(parent, SWT.NO_BACKGROUND);

		//		c.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_CYAN));

		GridData cData = new GridData();
		cData.horizontalSpan = span;
		cData.horizontalAlignment = GridData.FILL;
		c.setLayoutData(cData);

		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		c.setLayout(layout);


		Label weightLabel = new Label(c, SWT.LEFT);
		GridData weightData = new GridData();
		weightData.grabExcessHorizontalSpace = true;
		weightLabel.setLayoutData(weightData);
		weightLabel.setText("Visualization:");
		FontDescriptor boldDescriptor = FontDescriptor.createFrom(weightLabel.getFont()).setStyle(SWT.BOLD);
		Font boldFont = boldDescriptor.createFont(weightLabel.getDisplay());
		weightLabel.setFont(boldFont);
		fileLabel = weightLabel;
		
		final Combo visualizationDropDown = new Combo(c, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		visualizationDropDown.add("Monitor");
		visualizationDropDown.add("Stacks");
		visualizationDropDown.setText("Monitor");
		visualizationDropDown.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String selected = visualizationDropDown.getText(); 
				if(selected.equals("Stacks")) {
					System.out.println("Select Stacks");
					showStacksVisualization();
				} else if(selected.equals("Monitor")) {
					System.out.println("Select Monitor");
					showMonitorVisualization();
				}
			}
		});

		/*Button load = new Button (c, SWT.PUSH);
		load.setText("Load");
		load.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(load.getShell(), SWT.OPEN);
				dialog.setFilterExtensions(new String [] {"*.txt","*.properties","*.weights"});
				dialog.setFilterPath("config/weights");
				String result = dialog.open();
				if(result != null) {
					updateWeightConfig(result);
				}
			}
		});
		Button save = new Button (c, SWT.PUSH);
		save.setText("Save");
		save.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(load.getShell(), SWT.SAVE);
				String name = checklist.getWeightConfiguration().getName();
				if(name == "Last used") {
					name = "last_used.txt";
				}
				dialog.setFileName(name);
				//dialog.setFilterExtensions(new String [] {"*.txt","*.properties","*.weights"});
				dialog.setFilterPath("config/weights");
				String result = dialog.open();
				if(result != null) {
					WeightConfiguration wc = checklist.getWeightConfiguration();
					wc.save(result);
					updateBar();
				}
			}
		});*/

		Label separator = new Label(c, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData sepData = new GridData();
		sepData.horizontalSpan = layout.numColumns;
		sepData.grabExcessHorizontalSpace = true;
		sepData.horizontalAlignment = GridData.FILL;
		separator.setLayoutData(sepData);

		topBar = c;
		return c;
	}


	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if(visible) {
			this.updateWeights();
		}
	}
	
	protected void updateWeights() {
		visualization.updateWeights();
	}
	
	
	private void showMonitorVisualization() {
		setVisualization(new MonitorVisualization(rootComposite, controller, checklist));
	}
	
	private void showStacksVisualization() {
		setVisualization(new StackVisualization(rootComposite, controller, checklist));
	}
	
	private void setVisualization(Visualization visualization) {
		removeVisualization();
		this.visualization = visualization;
		updateWeights();
		this.rootComposite.layout();
	}
	
	
	
	private void removeVisualization() {
		if(this.visualization != null) {
			this.visualization.dispose();
		}
	}
	
	
}
