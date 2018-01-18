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
package org.deidentifier.arx.gui.view.impl.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A stack layout for multiple columns
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class ComponentMultiStack {

    /** Base */
    private final Composite                 base;
    /** Each cell */
    private List<Composite>                 stacks   = new ArrayList<Composite>();
    /** Layout for each cell */
    private List<StackLayout>               layouts  = new ArrayList<StackLayout>();
    /** Children for each cell */
    private Map<StackLayout, List<Control>> children = new HashMap<StackLayout, List<Control>>();

    /**
     * Base composite. Should have a reasonable layout, e.g., GridLayout
     * @param composite
     */
    public ComponentMultiStack(Composite base) {
        this.base = base;
    }
    
    /**
     * Creates a new entry
     * @param layoutData
     * @return
     */
    public Composite create(Object layoutData) {
        
        StackLayout layout = new StackLayout();
        Composite composite = new Composite(base, SWT.NONE);
        composite.setLayoutData(layoutData);
        composite.setLayout(layout);
        stacks.add(composite);
        layouts.add(layout);
        return composite;
    }
    
    /**
     * Collect information about children
     */
    public void pack() {
        for (int i=0; i<stacks.size(); i++) {
            List<Control> children = new ArrayList<Control>();
            for (Control c : stacks.get(i).getChildren()) {
                children.add(c);
            }
            this.children.put(layouts.get(i), children);
        }
    }
    
    /**
     * Sets the layer
     * @param layer
     */
    public void setLayer(int layer) {
        for (int i=0; i<layouts.size(); i++) {
            StackLayout layout = layouts.get(i);
            if (layer < 0 || layer > children.get(layout).size()) {
                throw new IllegalArgumentException("Layout out of range");
            }
            layout.topControl = this.children.get(layout).get(layer);
        }
        for (Composite c : stacks) {
            c.layout();
        }
    }
}
