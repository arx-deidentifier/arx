/*
 * FLASH: Efficient, Stable and Optimal Data Anonymization
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

package org.deidentifier.flash.gui.view.impl.explore;

import org.deidentifier.flash.FLASHResult;
import org.deidentifier.flash.gui.Controller;
import org.deidentifier.flash.gui.SWTUtil;
import org.deidentifier.flash.gui.resources.Resources;
import org.deidentifier.flash.gui.view.def.IAttachable;
import org.deidentifier.flash.gui.view.def.IView.ModelEvent;
import org.deidentifier.flash.gui.view.def.IView.ModelEvent.EventTarget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class ExploreView implements IAttachable {

    private final Composite top;
    private NodeFilter      filter;
    private FLASHResult     result;

    public ExploreView(final Composite parent, final Controller controller) {

        // Create top composite
        top = new Composite(parent, SWT.NONE);
        final GridData topLayoutData = SWTUtil.createFillGridData();
        top.setLayoutData(topLayoutData);
        top.setLayout(new GridLayout());
        top.addListener(SWT.Show, new Listener() {
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
        final CTabFolder folder = new CTabFolder(top, SWT.BOTTOM | SWT.BORDER);
        final GridData g = SWTUtil.createFillGridData();
        folder.setLayoutData(g);
        folder.setUnselectedCloseVisible(false);
        folder.setSimple(false);
        folder.setTabHeight(25);

        // Prevent closing
        folder.addCTabFolder2Listener(new CTabFolder2Adapter() {
            @Override
            public void close(final CTabFolderEvent event) {
                event.doit = false;
            }
        });

        // Lattice
        final IAttachable v1 = new LatticeView(folder, controller);
        final CTabItem item1 = new CTabItem(folder, SWT.NONE);
        item1.setText(Resources.getMessage("ExploreView.0")); //$NON-NLS-1$
        item1.setControl(v1.getControl());
        item1.setShowClose(false);
        item1.setImage(controller.getResources()
                                 .getImage("explore_lattice.png")); //$NON-NLS-1$

        // List
        final IAttachable v2 = new ListView(folder, controller);
        final CTabItem item2 = new CTabItem(folder, SWT.NONE);
        item2.setText(Resources.getMessage("ExploreView.2")); //$NON-NLS-1$
        item2.setControl(v2.getControl());
        item2.setShowClose(false);
        item2.setImage(controller.getResources().getImage("explore_list.png")); //$NON-NLS-1$

        // Select
        folder.setSelection(0);

        // Create bottom composite
        final Composite bottom = new Composite(top, SWT.NONE);
        final GridData bottomLayoutData = SWTUtil.createFillHorizontallyGridData();
        bottom.setLayoutData(bottomLayoutData);
        final GridLayout bottomLayout = new GridLayout();
        bottomLayout.numColumns = 3;
        bottomLayout.makeColumnsEqualWidth = true;
        bottomLayout.marginLeft = 0;
        bottomLayout.marginRight = 0;
        bottomLayout.marginWidth = 0;
        bottom.setLayout(bottomLayout);

        // Create viewers
        new NodeFilterView(bottom, controller);
        new NodeClipboardView(bottom, controller);
        new NodePropertiesView(bottom, controller);

    }

    @Override
    public Control getControl() {
        return top;
    }
}
