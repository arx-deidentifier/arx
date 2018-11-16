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

package org.deidentifier.arx.gui.view.impl.masking;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.ILayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * This class layouts the masking view.
 *
 * @author Karol Babioch
 * @author Sandro Schaeffler
 * @author Peter Bock
 */
public class LayoutMasking implements ILayout {

    /**
     * Creates an instance.
     * 
     * @param parent
     * @param controller
     */
    public LayoutMasking(final Composite parent, final Controller controller) {

        // Split the layout in the middle (top and bottom) with SashForm
        SashForm sashMid = new SashForm(parent, SWT.VERTICAL | SWT.SMOOTH);
        sashMid.setLayoutData(SWTUtil.createFillGridData());

        // Split the top half in the center (left and right) with SashForm
        SashForm sashTop = new SashForm(sashMid, SWT.HORIZONTAL | SWT.SMOOTH);
        sashTop.setLayoutData(SWTUtil.createFillGridData());

        // Create top left composite
        Composite compositeTopLeft = new Composite(sashTop, SWT.NONE);
        compositeTopLeft.setLayoutData(SWTUtil.createFillGridData());
        compositeTopLeft.setLayout(SWTUtil.createGridLayout(1));

        // Create top center composite
        Composite compositeTopCenter = new Composite(sashTop, SWT.NONE);
        compositeTopCenter.setLayoutData(new FillLayout());
        compositeTopCenter.setLayout(SWTUtil.createGridLayout(1));

        // Create top right composite
        Composite compositeTopRight = new Composite(sashTop, SWT.NONE);
        compositeTopRight.setLayoutData(SWTUtil.createFillGridData());
        compositeTopRight.setLayout(SWTUtil.createGridLayout(1));

        // Split the bottom half in the center (left and right) with SashForm
        SashForm sashBottom = new SashForm(sashMid, SWT.HORIZONTAL | SWT.SMOOTH);

        // Create bottom left composite
        Composite compositeBottomLeft = new Composite(sashBottom, SWT.NONE);
        compositeBottomLeft.setLayoutData(SWTUtil.createFillGridData());
        compositeBottomLeft.setLayout(SWTUtil.createGridLayout(1));

        // Create bottom right composite
        Composite compositeBottomRight = new Composite(sashBottom, SWT.NONE);
        compositeBottomRight.setLayoutData(SWTUtil.createFillGridData());
        compositeBottomRight.setLayout(SWTUtil.createGridLayout(1));

        // Set SashForm weights
        sashMid.setWeights(new int[] { 50, 50 });
        sashTop.setWeights(new int[] { 50, 50, 0 });
        sashBottom.setWeights(new int[] { 50, 50 });

        // Add views and sub-layouts
        new ViewAttributeConfiguration(compositeTopLeft, controller);
        new ViewMaskingConfiguration(compositeTopCenter, controller);
        new ViewVariableConfiguration(compositeBottomLeft, controller);
        new ViewVariableDistribution(compositeBottomRight, controller);
    }

}
