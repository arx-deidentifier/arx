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

package org.deidentifier.arx.gui.view.impl.masking;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * This view allows to configure a particular masking operation (i.e. set parameters).
 *
 * @author Karol Babioch
 */
public class ViewMaskingConfiguration implements IView {

    private Controller controller;

    private Composite parentComposite;


    public ViewMaskingConfiguration(final Composite parent, final Controller controller) {

        this.controller = controller;
        this.parentComposite = build(parent);

    }


    private Composite build(Composite parent) {

        // Title bar
        ComponentTitledFolder folder = new ComponentTitledFolder(parent, controller, null, null);
        folder.setLayoutData(SWTUtil.createFillGridData());

        // First tab
        Composite composite = folder.createItem("Masking configuration", null);
        composite.setLayout(SWTUtil.createGridLayout(1));
        folder.setSelection(0);

        // Only a placeholder for now
        // TODO Replace this depending on the masking operation and attribute
        Label l1 = new Label(composite, SWT.HORIZONTAL);
        l1.setText("Options depending on selected attribute and masking type");

        return composite;

    }


    @Override
    public void dispose() {

    }

    @Override
    public void reset() {

    }

    @Override
    public void update(ModelEvent event) {

    }

}
