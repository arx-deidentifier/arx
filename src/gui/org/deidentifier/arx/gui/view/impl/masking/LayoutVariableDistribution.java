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
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.ILayout;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolder;
import org.eclipse.swt.widgets.Composite;

/**
 * This class layouts the variable distribution
 *
 * @author Karol Babioch
 */
public class LayoutVariableDistribution implements ILayout {

    public LayoutVariableDistribution(final Composite parent, final Controller controller) {

        // Create the tab folder
        ComponentTitledFolder folder = new ComponentTitledFolder(parent, controller, null, null); // TODO Assign help id
        folder.setLayoutData(SWTUtil.createFillGridData());

        // Plot view
        Composite compositePlot = folder.createItem("Distribution plot", null);
        compositePlot.setLayout(SWTUtil.createGridLayout(1));

        // Table view
        Composite compositeTable = folder.createItem("Distribution table", null);
        compositeTable.setLayout(SWTUtil.createGridLayout(1));

        // Select distribution plot view by default
        folder.setSelection(0);

        // Create sub-views
        new ViewVariableDistributionPlot(compositePlot, controller);
        new ViewVariableDistributionTable(compositeTable, controller);

    }

}
