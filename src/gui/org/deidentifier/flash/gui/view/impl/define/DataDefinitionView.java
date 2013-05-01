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

package org.deidentifier.flash.gui.view.impl.define;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deidentifier.flash.gui.Controller;
import org.deidentifier.flash.gui.Model;
import org.deidentifier.flash.gui.SWTUtil;
import org.deidentifier.flash.gui.view.def.IView;
import org.deidentifier.flash.gui.view.def.IView.ModelEvent.EventTarget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

public class DataDefinitionView implements IView {

    private final Controller           controller;
    private final CTabFolder           tabFolder;
    private final Map<Integer, String> names = new HashMap<Integer, String>();
    private final Set<IView>           views = new HashSet<IView>();
    private Model                      model;

    public DataDefinitionView(final Composite parent,
                              final Controller controller) {

        // Register
        this.controller = controller;
        this.controller.addListener(EventTarget.SELECTED_ATTRIBUTE, this);
        this.controller.addListener(EventTarget.INPUT, this);
        this.controller.addListener(EventTarget.MODEL, this);

        // Create the tab folder
        tabFolder = new CTabFolder(parent, SWT.TOP | SWT.BORDER | SWT.FLAT);
        tabFolder.setUnselectedCloseVisible(false);
        tabFolder.setSimple(true);
        tabFolder.setTabHeight(25);
        final GridData tabData = SWTUtil.createFillGridData();
        tabData.grabExcessVerticalSpace = true;
        tabFolder.setLayoutData(tabData);

        // Prevent closing
        tabFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
            @Override
            public void close(final CTabFolderEvent event) {
                event.doit = false;
            }
        });

        tabFolder.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                selectionEvent();
            }
        });
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    @Override
    public void reset() {
        tabFolder.setRedraw(false);
        for (final CTabItem tab : tabFolder.getItems()) {
            tab.dispose();
        }
        // TODO: Is this enough cleanup?
        for (final IView v : views) {
            v.dispose();
        }
        names.clear();
        views.clear();
        tabFolder.setRedraw(true);
        tabFolder.redraw();
    }

    private void selectionEvent() {
        int index = tabFolder.getSelectionIndex();
        if (index >= 0) {
            final String name = names.get(index);
            if (model != null) {
                model.setSelectedAttribute(name);
                controller.update(new ModelEvent(this,
                                                 EventTarget.SELECTED_ATTRIBUTE,
                                                 name));
            }
        }
    }

    @Override
    public void update(final ModelEvent event) {
        if (event.target == EventTarget.SELECTED_ATTRIBUTE) {
            for (final CTabItem item : tabFolder.getItems()) {
                if (item.getText().equals(event.data)) {
                    tabFolder.setSelection(item);
                    break;
                }
            }
        } else if (event.target == EventTarget.MODEL) {
            model = (Model) event.data;
        } else if (event.target == EventTarget.INPUT) {
            reset();
            tabFolder.setRedraw(false);
            for (int i = 0; i < model.getInputConfig()
                                     .getInput()
                                     .getHandle()
                                     .getNumColumns(); i++) {
                final String col = model.getInputConfig()
                                        .getInput()
                                        .getHandle()
                                        .getAttributeName(i);
                final IView l = new AttributeDefinitionView(tabFolder,
                                                            col,
                                                            controller);
                l.update(new ModelEvent(this, EventTarget.MODEL, model));
                l.update(new ModelEvent(this, EventTarget.INPUT, event.data));
                names.put(i, col);
                views.add(l);
            }
            tabFolder.setRedraw(true);
            tabFolder.redraw();
        }
    }
}
