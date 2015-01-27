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

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.ILayout;
import org.deidentifier.arx.gui.view.impl.common.ViewDataInput;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * This class implements the layout for the data definition perspective.
 *
 * @author Fabian Prasser
 */
public class LayoutDefinition implements ILayout {

    /**  TODO */
    private final Composite center;
    
    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     */
    public LayoutDefinition(final Composite parent, final Controller controller) {

		// Define
        Composite compositeLeft;
		Composite compositeRight;
		Composite compositeTopRight;
		Composite compositeBottomRight;

        // Create center composite
        center = new Composite(parent, SWT.NONE);
        center.setLayoutData(SWTUtil.createFillGridData());
        final GridLayout centerLayout = SWTUtil.createGridLayout(2);
        centerLayout.makeColumnsEqualWidth = true;
        center.setLayout(centerLayout);

        // Create left composite
        compositeLeft = new Composite(center, SWT.NONE);
        compositeLeft.setLayoutData(SWTUtil.createFillGridData());
        compositeLeft.setLayout(SWTUtil.createGridLayout(1));

        // Create right composite
        compositeRight = new Composite(center, SWT.NONE);
        compositeRight.setLayoutData(SWTUtil.createFillGridData());
        compositeRight.setLayout(SWTUtil.createGridLayout(1));
        
        // Create top-right composite
        compositeTopRight = new Composite(compositeRight, SWT.NONE);
        compositeTopRight.setLayoutData(SWTUtil.createFillGridData());
        compositeTopRight.setLayout(SWTUtil.createGridLayout(1));

        // Create bottom-right composite
        compositeBottomRight = new Composite(compositeRight, SWT.NONE);
        compositeBottomRight.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        compositeBottomRight.setLayout(SWTUtil.createGridLayout(1));

        // Create views
        new ViewDataInput(compositeLeft, controller); 
        new ViewSubsetDefinition(compositeLeft, controller);
        new ViewDataDefinition(compositeTopRight, controller);
        new ViewGeneralSettings(compositeBottomRight, controller);
    }
}
