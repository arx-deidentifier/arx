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

package org.deidentifier.arx.gui.view.impl.explore;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.ILayout;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * This class layouts the exploration view.
 *
 * @author Fabian Prasser
 */
public class LayoutExplore implements ILayout {

    /**  TODO */
    private final Composite root;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     */
    public LayoutExplore(final Composite parent, final Controller controller) {

        // Create top composite
        root = new Composite(parent, SWT.NONE);
        root.setLayoutData(SWTUtil.createFillGridData());
        root.setLayout(SWTUtil.createGridLayout(1));
 
        // Create top composite
        ComponentTitledFolder folder = new ComponentTitledFolder(root, controller, null, "id-30"); //$NON-NLS-1$
        folder.setLayoutData(SWTUtil.createFillGridData());
        
        // Lattice
        Composite item1 = folder.createItem(Resources.getMessage("ExploreView.0"), //$NON-NLS-1$ 
                                            controller.getResources().getImage("explore_lattice.png")); //$NON-NLS-1$
        
        item1.setLayoutData(SWTUtil.createFillGridData());
        new ViewLattice(item1, controller);
        
        // List
        Composite item2 = folder.createItem(Resources.getMessage("ExploreView.2"), //$NON-NLS-1$ 
                                            controller.getResources().getImage("explore_list.png")); //$NON-NLS-1$
        
        item2.setLayoutData(SWTUtil.createFillGridData());
        new ViewList(item2, controller);

        // Tiles
        Composite item3 = folder.createItem(Resources.getMessage("ExploreView.3"), //$NON-NLS-1$ 
                                            controller.getResources().getImage("explore_tiles.png")); //$NON-NLS-1$
        
        item3.setLayoutData(SWTUtil.createFillGridData());
        new ViewTiles(item3, controller);
        
        // Select
        folder.setSelection(0);

        // Create bottom composite
        final Composite bottom = new Composite(root, SWT.NONE);
        bottom.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        final GridLayout bottomLayout = SWTUtil.createGridLayout(3);
        bottomLayout.makeColumnsEqualWidth = true;
        bottom.setLayout(bottomLayout);

        // Create viewers
        new ViewFilter(bottom, controller);
        new ViewClipboard(bottom, controller);
        new ViewProperties(bottom, controller);
    }
}
