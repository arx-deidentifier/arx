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

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.common.ComponentRiskMonitor;
import org.deidentifier.arx.risk.RiskQuestionnaire;
import org.deidentifier.arx.risk.RiskQuestionnaireSection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * The monitor visualization
 * 
 * @author Thomas Guenzel
 * @author Fabian Prasser
 */
public class RiskWizardVisualizationMonitor extends RiskWizardVisualization {

    /** Contains the monitors for each section */
    private List<ComponentRiskMonitor> monitors;

    /** Overall monitor, showing the overall score */
    private ComponentRiskMonitor       totalMonitor;

    /**
     * Create a new monitor visualization
     * 
     * @param parent
     *            the parent
     * @param controller
     *            the controller
     * @param checklist
     *            the checklist to use
     */
    public RiskWizardVisualizationMonitor(Composite parent,
                                          Controller controller,
                                          RiskQuestionnaire checklist) {
        super(parent, controller, checklist);
    }

    /**
     * Update the UI when the weights change
     */
    @Override
    public void updateWeights() {
        RiskQuestionnaireSection sections[] = checklist.getSections();
        for (int i = 0; i < sections.length; i++) {
            RiskQuestionnaireSection s = sections[i];

            ComponentRiskMonitor riskMonitor = monitors.get(i);
            riskMonitor.setRisk(1.0 - ((s.getScore() / 2.0) + 0.5));
        }

        totalMonitor.setRisk(1.0 - ((checklist.getScore() / 2.0) + 0.5));
    }

    /**
     * Creates the view containing the different monitors
     */
    @Override
    protected void createVisualization() {
        RiskQuestionnaireSection sections[] = this.checklist.getSections();
        monitors = new ArrayList<ComponentRiskMonitor>();

        GridLayout layout = SWTUtil.createGridLayoutWithEqualWidth(sections.length);
        layout.marginHeight = 0;
        layout.marginTop = 0;
        layout.marginBottom = 0;
        layout.makeColumnsEqualWidth = true;
        this.setLayout(layout);

        for (int i = 0; i < sections.length; i++) {
            String title = sections[i].getTitle();

            ComponentRiskMonitor riskMonitor = new ComponentRiskMonitor(this,
                                                                        this.controller,
                                                                        title,
                                                                        title);
            riskMonitor.setLayoutData(SWTUtil.createFillGridData());
            riskMonitor.setRisk(0.5);
            monitors.add(riskMonitor);
        }

        String totalTitle = Resources.getMessage("RiskWizard.2");
        totalMonitor = new ComponentRiskMonitor(this, this.controller, totalTitle, totalTitle);
        totalMonitor.setLayoutData(SWTUtil.createFillGridData());
        totalMonitor.setRisk(0.5);

        GridData gridData = new GridData();
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.verticalAlignment = SWT.FILL;
        gridData.minimumHeight = 150;
        gridData.horizontalSpan = layout.numColumns;
        totalMonitor.setLayoutData(gridData);
    }
}
