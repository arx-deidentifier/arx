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

/**
 * This view displays settings for all attributes.
 *
 * @author Fabian Prasser
 */
public class ViewDataDefinition implements IView {

    /**  TODO */
    private final Controller           controller;
    
    /**  TODO */
    private final CTabFolder           folder;
    
    /**  TODO */
    private final Map<Integer, String> names = new HashMap<Integer, String>();
    
    /**  TODO */
    private final Set<IView>           views = new HashSet<IView>();
    
    /**  TODO */
    private Model                      model;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     */
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

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IView#dispose()
     */
    @Override
    public void dispose() {
        controller.removeListener(this);
        for (IView view : views){
            view.dispose();
        }
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IView#reset()
     */
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

    /**
     * Handle a selection event.
     */
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

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IView#update(org.deidentifier.arx.gui.model.ModelEvent)
     */
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
