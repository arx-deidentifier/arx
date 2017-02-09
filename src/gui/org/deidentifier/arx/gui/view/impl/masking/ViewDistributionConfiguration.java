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
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolder;
import org.deidentifier.arx.masking.RandomVariable;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * This view allows to configure the variable distribution
 *
 * @author Karol Babioch
 */
public class ViewDistributionConfiguration implements IView {

    private Controller controller;

    private TableViewer tableViewer;

    private Label l;


    public ViewDistributionConfiguration(final Composite parent, final Controller controller) {

        this.controller = controller;

        build(parent);

        this.controller.addListener(ModelPart.MASKING_VARIABLE_SELECTED, this);

    }

    private void build(Composite parent) {

        // Create title bar
        ComponentTitledFolder folder = new ComponentTitledFolder(parent, controller, null, null);
        folder.setLayoutData(SWTUtil.createFillGridData());
        Composite composite = folder.createItem("Distribution configuration", null);
        composite.setLayout(SWTUtil.createGridLayout(1));
        folder.setSelection(0);

        // Only a placeholder for now
        // TODO Replace this depending on the masking operation and attribute
        l = new Label(composite, SWT.HORIZONTAL);
        l.setText("Select a variable to configure distribution of");

    }

    @Override
    public void dispose() {

        controller.removeListener(this);

    }

    @Override
    public void reset() {

    }

    @Override
    public void update(ModelEvent event) {

        RandomVariable variable = (RandomVariable)event.data;
        l.setText("Configuring variable: " + variable.getName());

    }

}
