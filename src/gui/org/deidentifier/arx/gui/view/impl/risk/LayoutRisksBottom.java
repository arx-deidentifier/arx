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
import org.deidentifier.arx.gui.view.def.ILayout;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolder;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * Layouts the risk analysis perspective.
 *
 * @author Fabian Prasser
 */
public class LayoutRisksBottom implements ILayout {

    /**  TODO */
    private final ComponentTitledFolder folder;
    
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

        // Create the tab folder
        folder = new ComponentTitledFolder(parent, controller, null, null);
        final Composite item1 = folder.createItem(Resources.getMessage("RiskAnalysis.5"), null); //$NON-NLS-1$ 
        item1.setLayout(new FillLayout());
        final Composite item3 = folder.createItem(Resources.getMessage("RiskAnalysis.13"), null); //$NON-NLS-1$ 
        item3.setLayout(new FillLayout());

        
        // Create the views
        new ViewRisksBasicEstimates(item1, controller, target, reset);
        new ViewRisksPlotUniquenessEstimates(item3, controller, target, reset);
        
        if (target == ModelPart.INPUT) {
            final Composite item2 = folder.createItem(Resources.getMessage("RiskAnalysis.16"), null); //$NON-NLS-1$ 
            item2.setLayout(new FillLayout());
            new ViewRisksPopulationModel(item2, controller);
        } 
        
        folder.setSelection(0);
    }

    /**
     * Adds a selection listener.
     *
     * @param listener
     */
    public void addSelectionListener(final SelectionListener listener) {
        folder.addSelectionListener(listener);
    }

    /**
     * Returns the selection index.
     *
     * @return
     */
    public int getSelectionIndex() {
        return folder.getSelectionIndex();
    }

    /**
     * Sets the selection index.
     *
     * @param index
     */
    public void setSelectionIdex(final int index) {
        folder.setSelection(index);
    }
}
