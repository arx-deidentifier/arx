/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
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
public class LayoutRisksBottom extends LayoutRisksAbstract {

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     * @param target
     * @param reset
     */
    public LayoutRisksBottom(final Composite parent,
                            final Controller controller,
                            final ModelPart target,
                            final ModelPart reset) {
        
        super(parent, controller);

        registerView(0, new ViewRisksBasicEstimates(createTab(Resources.getMessage("RiskAnalysis.5")), controller, target, reset)); //$NON-NLS-1$
        registerView(1, new ViewRisksPlotUniquenessEstimates(createTab(Resources.getMessage("RiskAnalysis.13")), controller, target, reset, false)); //$NON-NLS-1$
        registerView(2, new ViewRisksPlotUniquenessEstimates(createTab(Resources.getMessage("RiskAnalysis.24")), controller, target, reset, true)); //$NON-NLS-1$

        if (target == ModelPart.INPUT) {
            new ViewRisksPopulationModel(createTab(Resources.getMessage("RiskAnalysis.16")), controller); //$NON-NLS-1$
            new ViewRisksQuasiIdentifiers(createTab(Resources.getMessage("RiskAnalysis.23")), controller); //$NON-NLS-1$
        } 
        
        setSelectionIdex(0);
    }
}
