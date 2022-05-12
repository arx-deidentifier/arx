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
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.risk.RiskQuestionnaire;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Final page containing the two different visualizations
 * 
 * @author Thomas Guenzel
 * @author Fabian Prasser
 */
public class RiskWizardPageEvaluation extends WizardPage {

    /** The checklist */
    private RiskQuestionnaire               checklist;
    /** The current visualization */
    private RiskWizardVisualization visualization;
    /** Widget */
    private Composite               rootComposite;
    /** Widget */
    private Controller              controller;

    /**
     * create the evaluation page for the checklist
     * 
     * @param checklist
     *            the checklist to use
     * @param controller
     *            the arx controller
     */
    protected RiskWizardPageEvaluation(RiskQuestionnaire checklist, Controller controller) {
        super(Resources.getMessage("RiskWizard.9"));

        this.checklist = checklist;
        this.controller = controller;
        this.setTitle(Resources.getMessage("RiskWizard.10"));
        this.setDescription(Resources.getMessage("RiskWizard.11"));
    }

    /**
     * creates the control and adds the visualization selection top bar
     */
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

        this.showMonitorVisualization();

        setControl(rootComposite);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            this.updateWeights();
        }
    }

    /**
     * creates the top bar used for switching between visualizations
     * 
     * @param parent
     *            the parent composite
     * @param span
     *            the span to use for the bar
     * @return
     */
    private Composite createTopBar(Composite parent, int span) {
        Composite c = new Composite(parent, SWT.NO_BACKGROUND);

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
        weightLabel.setText(Resources.getMessage("RiskWizard.19"));
        FontDescriptor boldDescriptor = FontDescriptor.createFrom(weightLabel.getFont())
                                                      .setStyle(SWT.BOLD);
        Font boldFont = boldDescriptor.createFont(weightLabel.getDisplay());
        weightLabel.setFont(boldFont);

        final Combo visualizationDropDown = new Combo(c, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
        final String monitorTitle = Resources.getMessage("RiskWizard.12");
        final String stacksTitle = Resources.getMessage("RiskWizard.13");
        visualizationDropDown.add(monitorTitle);
        visualizationDropDown.add(stacksTitle);
        visualizationDropDown.setText(monitorTitle);
        visualizationDropDown.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String selected = visualizationDropDown.getText();
                if (selected.equals(stacksTitle)) {
                    showStacksVisualization();
                } else if (selected.equals(monitorTitle)) {
                    showMonitorVisualization();
                }
            }
        });

        Label separator = new Label(c, SWT.SEPARATOR | SWT.HORIZONTAL);
        GridData sepData = new GridData();
        sepData.horizontalSpan = layout.numColumns;
        sepData.grabExcessHorizontalSpace = true;
        sepData.horizontalAlignment = GridData.FILL;
        separator.setLayoutData(sepData);

        return c;
    }

    /**
     * remove the current visualization from the UI
     */
    private void removeVisualization() {
        if (this.visualization != null) {
            this.visualization.dispose();
        }
    }

    /**
     * set the current visualization and update the UI
     * 
     * @param visualization
     *            the visualization to change to
     */
    private void setVisualization(RiskWizardVisualization visualization) {
        removeVisualization();
        this.visualization = visualization;
        updateWeights();
        this.rootComposite.layout();
    }

    /**
     * change to monitor visualization
     */
    private void showMonitorVisualization() {
        setVisualization(new RiskWizardVisualizationMonitor(rootComposite, controller, checklist));
    }

    /**
     * change to stacks visualization
     */
    private void showStacksVisualization() {
        setVisualization(new RiskWizardVisualizationStack(rootComposite, controller, checklist));
    }

    /**
     * update the current visualization when the weights change
     */
    protected void updateWeights() {
        visualization.updateWeights();
    }

}
