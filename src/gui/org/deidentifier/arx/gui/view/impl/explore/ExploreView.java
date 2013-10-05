/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.arx.gui.view.impl.explore;

import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelNodeFilter;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IAttachable;
import org.deidentifier.arx.gui.view.def.IView.ModelEvent;
import org.deidentifier.arx.gui.view.def.IView.ModelEvent.EventTarget;
import org.deidentifier.arx.gui.view.impl.common.TitledFolder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class ExploreView implements IAttachable {

    private final Composite root;
    private ModelNodeFilter filter;
    private ARXResult       result;

    public ExploreView(final Composite parent, final Controller controller) {

        // Create top composite
        root = new Composite(parent, SWT.NONE);
        root.setLayoutData(SWTUtil.createFillGridData());
        root.setLayout(SWTUtil.createGridLayout(1));
        root.addListener(SWT.Show, new Listener() {
            @Override
            public void handleEvent(final Event arg0) {
                if ((result != null) && (filter != null)) {
                    controller.update(new ModelEvent(this,
                                                     EventTarget.FILTER,
                                                     filter));
                }
            }
        });

        // Create top composite
        TitledFolder folder = new TitledFolder(root, controller, null, "id-30"); //$NON-NLS-1$
        folder.setLayoutData(SWTUtil.createFillGridData());
        
        // Lattice
        Composite item1 = folder.createItem(Resources.getMessage("ExploreView.0"), //$NON-NLS-1$ 
                                            controller.getResources().getImage("explore_lattice.png")); //$NON-NLS-1$
        
        item1.setLayoutData(SWTUtil.createFillGridData());
        new LatticeView(item1, controller);
        
        // List
        Composite item2 = folder.createItem(Resources.getMessage("ExploreView.2"), //$NON-NLS-1$ 
                                            controller.getResources().getImage("explore_list.png")); //$NON-NLS-1$
        
        item2.setLayoutData(SWTUtil.createFillGridData());
        new ListView(item2, controller);
        
        // Select
        folder.setSelection(0);

        // Create bottom composite
        final Composite bottom = new Composite(root, SWT.NONE);
        bottom.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        final GridLayout bottomLayout = SWTUtil.createGridLayout(3);
        bottomLayout.makeColumnsEqualWidth = true;
        bottom.setLayout(bottomLayout);

        // Create viewers
        new NodeFilterView(bottom, controller);
        new NodeClipboardView(bottom, controller);
        new NodePropertiesView(bottom, controller);

    }

    @Override
    public Control getControl() {
        return root;
    }
}
