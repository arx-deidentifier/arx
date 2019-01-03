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
        
        super(parent, controller, target == ModelPart.INPUT, false);
        registerView(0, new ViewRisksOverview(createTab(Resources.getMessage("RiskAnalysis.5"), "help.risk.reidentification"), controller, target, reset)); //$NON-NLS-1$ //$NON-NLS-2$
        registerView(1, new ViewRisksPopulationUniques(createTab(Resources.getMessage("RiskAnalysis.24"), "help.risk.uniques"), controller, target, reset)); //$NON-NLS-1$ //$NON-NLS-2$
        setSelectionIdex(0);
    }
}
