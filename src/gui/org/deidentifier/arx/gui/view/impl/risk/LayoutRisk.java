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
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.ILayout;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * This class layouts the risk analysis view.
 *
 * @author Fabian Prasser
 */
public class LayoutRisk implements ILayout {

    /** Constant*/
    private static final int       WEIGHT_TOP    = 75;
    
    /** Constant*/
    private static final int       WEIGHT_BOTTOM = 25;
    
    /** Constant*/
    private static final int       WEIGHT_LEFT   = 50;
    
    /** Constant*/
    private static final int       WEIGHT_RIGHT  = 50;
    
    /** View*/
    private final Composite        bottomLeft;
    
    /** View*/
    private final Composite        bottomRight;
    
    /** View*/
    private final SashForm         centerSash;
    
    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     */
    public LayoutRisk(final Composite parent, final Controller controller) {

        // Create the SashForm with HORIZONTAL
        centerSash = new SashForm(parent, SWT.VERTICAL);
        centerSash.setLayoutData(SWTUtil.createFillGridData());
        centerSash.setLayout(SWTUtil.createGridLayout(1));
        
        // Create center composite
        ComponentTitledFolder folder = new ComponentTitledFolder(centerSash, controller, null, "id-1030", true); //$NON-NLS-1$
        folder.setLayoutData(SWTUtil.createFillGridData());
        
        // Lattice
        Composite item1 = folder.createItem(Resources.getMessage("ViewSampleDistribution.4"), //$NON-NLS-1$ 
                                            controller.getResources().getImage("explore_lattice.png")); //$NON-NLS-1$
        
        item1.setLayoutData(SWTUtil.createFillGridData());
        new ViewDistributionPlot(item1, controller);
        
        // List
        Composite item2 = folder.createItem(Resources.getMessage("ViewSampleDistribution.0"), //$NON-NLS-1$ 
                                            controller.getResources().getImage("explore_list.png")); //$NON-NLS-1$
        
        item2.setLayoutData(SWTUtil.createFillGridData());
        new ViewDistributionTable(item2, controller);
        folder.setSelection(0);

        
        // Create bottom composite
        final Composite compositeBottom = new Composite(centerSash, SWT.NONE);
        compositeBottom.setLayout(new FillLayout());
        final SashForm bottomSash = new SashForm(compositeBottom,
                                                 SWT.HORIZONTAL | SWT.SMOOTH);

        bottomLeft = new Composite(bottomSash, SWT.NONE);
        bottomLeft.setLayout(new FillLayout());

        bottomRight = new Composite(bottomSash, SWT.NONE);
        bottomRight.setLayout(new FillLayout());

        // --------------------------
        // STATISTICS VIEWS
        // --------------------------

        new ViewRisks(bottomLeft, controller);
        new ViewUniquesPlot(bottomRight, controller);
        
        // Set sash weights
        centerSash.setWeights(new int[] { WEIGHT_TOP, WEIGHT_BOTTOM });
        bottomSash.setWeights(new int[] { WEIGHT_LEFT, WEIGHT_RIGHT });
    }
}