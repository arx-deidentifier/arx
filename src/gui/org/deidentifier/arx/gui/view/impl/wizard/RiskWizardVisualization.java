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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.risk.RiskQuestionnaire;

/**
 * Base class for the visualizations
 * 
 * @author Thomas Guenzel
 * @author Fabian Prasser
 */
public abstract class RiskWizardVisualization extends Composite {

    /** Checklist used for the visualization */
    protected RiskQuestionnaire  checklist;
    /** Controller */
    protected Controller controller;

    /**
     * Create a new visualization for the checklist
     * 
     * @param parent
     *            the parent
     * @param controller
     *            the controller
     * @param checklist
     *            the checklist
     */
    public RiskWizardVisualization(Composite parent, Controller controller, RiskQuestionnaire checklist) {
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
     * Used in subclasses to respond to changes
     */
    public abstract void updateWeights();

    /**
     * Used in subclasses for the initial setup
     */
    protected abstract void createVisualization();
}
