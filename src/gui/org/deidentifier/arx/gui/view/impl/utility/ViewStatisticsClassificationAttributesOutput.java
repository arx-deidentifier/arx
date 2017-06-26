/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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

package org.deidentifier.arx.gui.view.impl.utility;


import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.common.ComponentStatusLabel;
import org.deidentifier.arx.gui.view.impl.utility.LayoutUtility.ViewUtilityType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * This view shows a hint message regarding attribute selection for classification analysis
 * 
 * @author Johanna Eicher
 */
public class ViewStatisticsClassificationAttributesOutput implements IView, ViewStatisticsBasic {

    /** View */
    private final Composite  root;

    /**
     * Creates a new instance.
     * 
     * @param parent
     * @param controller
     */
    public ViewStatisticsClassificationAttributesOutput(final Composite parent,
                                    final Controller controller) {
        this.root = parent;
        
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new FillLayout());
        ComponentStatusLabel label = new ComponentStatusLabel(composite, SWT.CENTER);
        label.setText(Resources.getMessage("ViewClassificationAttributes.3"));
        StackLayout layout = (StackLayout) this.root.getLayout();
        layout.topControl = composite;
    }

    @Override
    public void dispose() {
        // nothing to do
    }

    @Override
    public Composite getParent() {
        return this.root;
    }

    /**
     * Returns the type
     * 
     * @return
     */
    public ViewUtilityType getType() {
        return ViewUtilityType.CLASSIFICATION;
    }

    @Override
    public void reset() {
        // nothing to do
    }

    @Override
    public void update(ModelEvent event) {
        // nothing to do
    }
}
