/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.arx.gui.view.impl.define;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

public class ViewDataDefinition implements IView {

    private final Controller           controller;
    private final CTabFolder           folder;
    private final Map<Integer, String> names = new HashMap<Integer, String>();
    private final Set<IView>           views = new HashSet<IView>();
    private Model                      model;

    public ViewDataDefinition(final Composite parent,
                              final Controller controller) {

        // Register
        this.controller = controller;
        this.controller.addListener(ModelPart.SELECTED_ATTRIBUTE, this);
        this.controller.addListener(ModelPart.INPUT, this);
        this.controller.addListener(ModelPart.MODEL, this);

        // Create the tab folder
        folder = new CTabFolder(parent, SWT.TOP | SWT.BORDER | SWT.FLAT);
        folder.setUnselectedCloseVisible(false);
        folder.setSimple(false);
        folder.setTabHeight(25);
        final GridData tabData = SWTUtil.createFillGridData();
        tabData.grabExcessVerticalSpace = true;
        folder.setLayoutData(tabData);

        // Create help button
        SWTUtil.createHelpButton(controller, folder, "id-1"); //$NON-NLS-1$

        // Prevent closing
        folder.addCTabFolder2Listener(new CTabFolder2Adapter() {
            @Override
            public void close(final CTabFolderEvent event) {
                event.doit = false;
            }
        });

        folder.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                selectionEvent();
            }
        });
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
        for (IView view : views){
            view.dispose();
        }
    }

    @Override
    public void reset() {
        folder.setRedraw(false);
        for (final CTabItem tab : folder.getItems()) {
            tab.dispose();
        }
        // TODO: Is this enough cleanup?
        for (final IView v : views) {
            v.dispose();
        }
        names.clear();
        views.clear();
        folder.setRedraw(true);
        folder.redraw();
    }

    private void selectionEvent() {
        int index = folder.getSelectionIndex();
        if (index >= 0) {
            final String name = names.get(index);
            if (model != null) {
                model.setSelectedAttribute(name);
                controller.update(new ModelEvent(this,
                                                 ModelPart.SELECTED_ATTRIBUTE,
                                                 name));
            }
        }
    }

    @Override
    public void update(final ModelEvent event) {
        if (event.part == ModelPart.SELECTED_ATTRIBUTE) {
            for (final CTabItem item : folder.getItems()) {
                if (item.getText().equals(event.data)) {
                    folder.setSelection(item);
                    break;
                }
            }
        } else if (event.part == ModelPart.MODEL) {
            model = (Model) event.data;
        } else if (event.part == ModelPart.INPUT) {
            reset();
            folder.setRedraw(false);
            for (int i = 0; i < model.getInputConfig()
                                     .getInput()
                                     .getHandle()
                                     .getNumColumns(); i++) {
                final String col = model.getInputConfig()
                                        .getInput()
                                        .getHandle()
                                        .getAttributeName(i);
                final IView l = new ViewAttributeDefinition(folder,
                                                            col,
                                                            controller);
                l.update(new ModelEvent(this, ModelPart.MODEL, model));
                l.update(new ModelEvent(this, ModelPart.INPUT, event.data));
                names.put(i, col);
                views.add(l);
            }
            folder.setRedraw(true);
            folder.redraw();
        }
    }
}
