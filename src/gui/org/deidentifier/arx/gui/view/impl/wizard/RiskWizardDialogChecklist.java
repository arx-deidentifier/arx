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

import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.risk.RiskQuestionnaire;
import org.deidentifier.arx.risk.RiskQuestionnaireWeights;
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

/**
 * The ChecklistDialog is the dialog presented for the wizard
 * 
 * @author Thomas Guenzel
 * @author Fabian Prasser
 */
public class RiskWizardDialogChecklist extends WizardDialog {

    /** Widget */
    private Button    weightEditButton;

    /** Widget */
    private Button    loadButton;

    /** Widget */
    private Button    saveButton;

    /** Model */
    private RiskQuestionnaire checklist;

    /**
     * creates a new checklist dialog for a specified checklist
     * 
     * @param checklist
     *            the checklist to use
     * @param parentShell
     *            the parent for this dialog
     * @param controller
     *            the arx controller
     * @param newWizard
     *            the wizard
     */
    public RiskWizardDialogChecklist(RiskQuestionnaire checklist, Shell parentShell, IWizard newWizard) {
        super(parentShell, newWizard);
        this.checklist = checklist;
    }

    /**
     * create a custom button bar, with the load/store/edit buttons for the
     * weight profiles
     */
    @Override
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

        weightEditButton = new Button(parent, SWT.CHECK);
        weightEditButton.setText(Resources.getMessage("RiskWizard.6"));

        final RiskWizardDialogChecklist reference = this;

        loadButton = new Button(parent, SWT.PUSH);
        loadButton.setText(Resources.getMessage("RiskWizard.7"));
        loadButton.setVisible(false);
        loadButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(loadButton.getShell(), SWT.OPEN);
                dialog.setFilterExtensions(new String[] { "*.txt", "*.properties", "*.weights" });
                dialog.setFilterPath("config/weights");
                String result = dialog.open();
                if (result != null) {
                    updateWeightConfig(result);
                }
            }
        });
        layout.numColumns++;

        saveButton = new Button(parent, SWT.PUSH);
        saveButton.setText(Resources.getMessage("RiskWizard.8"));
        saveButton.setVisible(false);
        saveButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(saveButton.getShell(), SWT.SAVE);
                dialog.setFileName("weights.txt");
                dialog.setFilterExtensions(new String[] { "*.txt", "*.properties", "*.weights" });
                dialog.setFilterPath("config/weights");
                String result = dialog.open();
                if (result != null) {
                    RiskQuestionnaireWeights wc = checklist.getWeightConfiguration();
                    wc.save(result);
                }
            }
        });
        layout.numColumns++;

        Listener listener = new Listener() {
            @Override
            public void handleEvent(Event event) {
                reference.setWeightsEditable(weightEditButton.getSelection());
            }
        };

        weightEditButton.addListener(SWT.Selection, listener);

        // Add a placeholder label that uses the empty space
        layout.numColumns++;
        Label placeholder = new Label(parent, 1);
        placeholder.setText("");
        placeholder.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));

        super.createButtonsForButtonBar(parent);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Control ctrl = super.createDialogArea(parent);
        getProgressMonitor();
        return ctrl;
    }

    @Override
    protected IProgressMonitor getProgressMonitor() {
        // remove progress monitor, taken from
        // http://commercialjavaproducts.blogspot.de/2010/11/remove-progress-monitor-part-from-jface.html
        ProgressMonitorPart monitor = (ProgressMonitorPart) super.getProgressMonitor();
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.heightHint = 0;
        monitor.setLayoutData(gridData);
        monitor.setVisible(false);
        return monitor;
    }

    /**
     * enable or disable the edit mode
     * 
     * @param weightsEditable
     *            whether the weights should be changeable
     */
    protected void setWeightsEditable(boolean weightsEditable) {
        loadButton.setVisible(weightsEditable);
        saveButton.setVisible(weightsEditable);
        IWizardPage pages[] = this.getWizard().getPages();
        for (IWizardPage page : pages) {
            if (page instanceof RiskWizardPageSection) {
                RiskWizardPageSection sectionPage = (RiskWizardPageSection) page;
                sectionPage.setWeightEditable(weightsEditable);
            }
        }
        this.dialogArea.update();
    }

    /**
     * updates the current weight configuration
     * 
     * @param result
     *            the weight configuration to load
     */
    protected void updateWeightConfig(String result) {
        checklist.setWeightConfiguration(new RiskQuestionnaireWeights(result));
        IWizard wizard = this.getWizard();
        if (wizard instanceof RiskWizard) {
            RiskWizard casted = (RiskWizard) wizard;
            casted.updateWeights();
        }
    }
}
