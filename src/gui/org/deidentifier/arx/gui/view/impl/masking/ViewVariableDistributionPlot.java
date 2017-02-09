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
import org.deidentifier.arx.gui.view.def.IView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.swtchart.Chart;

/**
 * This implements the distribution table plot
 *
 * @author Karol Babioch
 */
public class ViewVariableDistributionPlot implements IView {

    private Controller controller;
    private Label l;

    public ViewVariableDistributionPlot(final Composite parent, final Controller controller) {

        this.controller = controller;

        build(parent);

        this.controller.addListener(ModelPart.MASKING_VARIABLE_SELECTED, this);

    }

    private void build(Composite parent) {

        // Create chart
        Chart chart = new Chart(parent, SWT.NONE);

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
