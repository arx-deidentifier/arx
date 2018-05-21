/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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

package org.deidentifier.arx.gui.view.impl.risk;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.eclipse.swt.widgets.Composite;

/**
 * Layouts the risk analysis perspective.
 *
 * @author Fabian Prasser
 */
public class LayoutRisksTop extends LayoutRisksAbstract {

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     * @param target
     * @param reset
     */
    public LayoutRisksTop(final Composite parent,
                          final Controller controller,
                          final ModelPart target,
                          final ModelPart reset) {

        super(parent, controller, target == ModelPart.INPUT, true);
        
        registerView(0, new ViewRisksRiskDistributionPlot(createTab(Resources.getMessage("RiskAnalysis.4"), "help.risk.classsizes"), controller, target, reset)); //$NON-NLS-1$ //$NON-NLS-2$
        registerView(1, new ViewRisksRiskDistributionTable(createTab(Resources.getMessage("RiskAnalysis.0"), "help.risk.classsizes"), controller, target, reset)); //$NON-NLS-1$ //$NON-NLS-2$
        registerView(2, new ViewRisksQuasiIdentifiersTable(createTab(Resources.getMessage("RiskAnalysis.15"), "help.risk.quasiidentifiers"), controller, target, reset)); //$NON-NLS-1$ //$NON-NLS-2$
        registerView(3, new ViewRisksReIdentification(createTab(Resources.getMessage("RiskAnalysis.32"), "help.risk.reidentification"), controller, target, reset)); //$NON-NLS-1$ //$NON-NLS-2$
        
        if (target == ModelPart.INPUT) {
            new ViewRisksHIPAAIdentifiersTable(createTab(Resources.getMessage("RiskAnalysis.26"), "help.risk.hipaa"), controller, target, reset); //$NON-NLS-1$ //$NON-NLS-2$
        }
        
        setSelectionIdex(0);
    }
}
