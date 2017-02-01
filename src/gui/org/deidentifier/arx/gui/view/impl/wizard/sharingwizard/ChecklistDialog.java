package org.deidentifier.arx.gui.view.impl.wizard.sharingwizard;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.view.impl.wizard.sharingwizard.checklist.Checklist;
import org.deidentifier.arx.gui.view.impl.wizard.sharingwizard.checklist.WeightConfiguration;


public class ChecklistDialog extends WizardDialog {
	private Button weightButton;
	private Button loadButton;
	private Button saveButton;
	private Checklist checklist;
	
	private Controller controller;
	
	public ChecklistDialog(Checklist checklist, Shell parentShell, Controller controller, IWizard newWizard) {
		super(parentShell, newWizard);
		this.checklist = checklist;
		this.controller = controller;
	}
	
	/**
	 * create a custom button bar, with the load/store/edit buttons for the weight profiles
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		// this code creates the button on the left side (settings)
		GridLayout layout = (GridLayout) parent.getLayout();
		layout.horizontalSpacing = 0;

		// adjust the parent to use the full width
		GridData gridData = (GridData) parent.getLayoutData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.minimumWidth = 650;
		gridData.horizontalAlignment = GridData.FILL;
		
		// add the settings button
		layout.numColumns++;
		
		weightButton = new Button(parent, SWT.CHECK);
		weightButton.setText("Edit");
		
		final ChecklistDialog reference = this;
		
		loadButton = new Button(parent, SWT.PUSH);
		loadButton.setText("Load");
		loadButton.setVisible(false);
		loadButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(loadButton.getShell(), SWT.OPEN);
				dialog.setFilterExtensions(new String [] {"*.txt","*.properties","*.weights"});
				dialog.setFilterPath("config/weights");
				String result = dialog.open();
				if(result != null) {
					updateWeightConfig(result);
				}
			}
		});
		layout.numColumns++;
		
		saveButton = new Button(parent, SWT.PUSH);
		saveButton.setText("Save");
		saveButton.setVisible(false);
		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(saveButton.getShell(), SWT.SAVE);
				String name = checklist.getWeightConfiguration().getName();
				if(name == "Last used") {
					name = "last_used.txt";
				}
				dialog.setFileName(name);
				dialog.setFilterExtensions(new String [] {"*.txt","*.properties","*.weights"});
				dialog.setFilterPath("config/weights");
				String result = dialog.open();
				if(result != null) {
					WeightConfiguration wc = checklist.getWeightConfiguration();
					wc.save(result);
				}
			}
		});
		layout.numColumns++;
		
		Listener listener = new Listener() {
		      public void handleEvent(Event event) {
		        reference.setWeightsEditable(weightButton.getSelection());
		      }
		    };
		
		weightButton.addListener(SWT.Selection, listener);
		
		
		// add a placeholder label that uses the empty space
		layout.numColumns++;
		Label placeholder = new Label(parent, 1);
		placeholder.setText("");
		placeholder.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		
		
		super.createButtonsForButtonBar(parent);
	}
	
	/**
	 * enable or disable the edit mode
	 * @param weightsEditable whether the weights should be changeable
	 */
	protected void setWeightsEditable(boolean weightsEditable) {
		loadButton.setVisible(weightsEditable);
		saveButton.setVisible(weightsEditable);
		//System.out.println("Editable: "+weightsEditable);
		IWizardPage pages[] = this.getWizard().getPages();
		for(IWizardPage page : pages) {
			if(page instanceof SectionPage) {
				SectionPage sectionPage = (SectionPage)page;
				sectionPage.setWeightEditable(weightsEditable);
			}
		}
		this.dialogArea.update();
	}

	/**
	 * updates the current weight configuration
	 * @param result the weight configuration to load
	 */
	protected void updateWeightConfig(String result) {
		checklist.setWeightConfiguration(new WeightConfiguration(result));
		IWizard wizard = this.getWizard();
		if(wizard instanceof ChecklistWizard) {
			ChecklistWizard casted = (ChecklistWizard)wizard;
			casted.updateWeights();
		}
	}
	
	// remove progress monitor, taken from
	// http://commercialjavaproducts.blogspot.de/2010/11/remove-progress-monitor-part-from-jface.html
	
	@Override
    protected Control createDialogArea(Composite parent) {
        Control ctrl = super.createDialogArea(parent);
        getProgressMonitor();
        return ctrl;
    }
    
    @Override
    protected IProgressMonitor getProgressMonitor() {
        ProgressMonitorPart monitor = (ProgressMonitorPart) super.getProgressMonitor();
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.heightHint = 0;
        monitor.setLayoutData(gridData);
        monitor.setVisible(false);
        return monitor;
    }
	
	
}
