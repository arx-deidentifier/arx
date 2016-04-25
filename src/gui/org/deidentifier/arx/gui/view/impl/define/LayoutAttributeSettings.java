/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2016 Fabian Prasser, Florian Kohlmayer and contributors
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

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.ILayout;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolder;
import org.eclipse.swt.widgets.Composite;

/**
 * This view displays settings for all attributes.
 *
 * @author Fabian Prasser
 */
public class LayoutAttributeSettings implements ILayout {

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     */
    public LayoutAttributeSettings(final Composite parent,
                                   final Controller controller) {

        // Create the tab folder
        ComponentTitledFolder folder = new ComponentTitledFolder(parent, controller, null, "id-1");
        folder.setLayoutData(SWTUtil.createFillGridData());

        // First view
        Composite composite1 = folder.createItem(Resources.getMessage("LayoutAttributeMetadata.0"), null); //$NON-NLS-1$
        composite1.setLayout(SWTUtil.createGridLayout(1));
        new ViewAttributeTransformation(composite1, controller);

        // Second view
        composite1 = folder.createItem(Resources.getMessage("LayoutAttributeMetadata.1"), null); //$NON-NLS-1$
        composite1.setLayout(SWTUtil.createGridLayout(1));
        new ViewAttributeList(composite1, controller);
        
        // Select
        folder.setSelection(0);
    }
}