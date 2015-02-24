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

import java.util.HashMap;
import java.util.Map;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelRisk.ViewRisk;
import org.deidentifier.arx.gui.view.def.ILayout;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolder;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolderButton;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolItem;

/**
 * Base class for layouts in this perspective
 * @author Fabian Prasser
 */
public class LayoutRisksAbstract implements ILayout {

    /** View */
    private final ComponentTitledFolder folder;
    /** Model */
    private final Map<Integer, ViewRisk> views = new HashMap<Integer, ViewRisk>();

    /**
     * Creates a new instance
     * @param parent
     * @param controller
     */
    public LayoutRisksAbstract(Composite parent, Controller controller) {
        folder = new ComponentTitledFolder(parent, controller, null, null);
    }
    /**
     * Creates a new instance
     * @param parent
     * @param controller
     * @param buttonBar
     */
    public LayoutRisksAbstract(Composite parent, Controller controller, ComponentTitledFolderButton buttonBar) {
        folder = new ComponentTitledFolder(parent, controller, buttonBar, null);
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
     * Returns the according view type
     * @param index
     * @return
     */
    public ViewRisk getViewTypeForSelectionIndex(final int index) {
        return this.views.get(index);
    }

    /**
     * Sets the selection index.
     *
     * @param index
     */
    public void setSelectionIdex(final int index) {
        folder.setSelection(index);
    }
    
    /**
     * Creates a new tab
     * @param title
     * @return
     */
    protected Composite createTab(String title) {
        final Composite item = folder.createItem(title, null); 
        item.setLayout(new FillLayout());
        return item;
    }
    
    /**
     * Returns the associated button item
     * @param label
     * @return
     */
    protected ToolItem getButtonItem(String label) {
        return folder.getButtonItem(label);
    }

    /**
     * Registers a new view
     * @param index
     * @param view
     */
    protected void registerView(int index, ViewRisks<?> view) {
        this.views.put(index, view.getViewType());
    }
}
