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

package org.deidentifier.arx.gui.view.impl.attributes;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.ILayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * This class layouts the attributes analysis view.
 *
 * @author Fabian Prasser
 */
public class LayoutAttributes implements ILayout {

    /** Constant */
    private static final int               WEIGHT_TOP    = 75;
    /** Constant */
    private static final int               WEIGHT_BOTTOM = 25;
    /** Constant */
    private static final int               WEIGHT_LEFT   = 50;
    /** Constant */
    private static final int               WEIGHT_RIGHT  = 50;

    /** View */
    private final Composite                centerLeft;
    /** View */
    private final Composite                centerRight;
    /** View */
    private final Composite                bottomLeft;
    /** View */
    private final Composite                bottomRight;
    /** View */
    private final SashForm                 centerSash;
    /** View */
    private final LayoutAttributesAbstract layoutBottomLeft;
    /** View */
    private final LayoutAttributesAbstract layoutBottomRight;
    /** View */
    private final LayoutAttributesTop      layoutTopLeft;
    /** View */
    private final LayoutAttributesTop      layoutTopRight;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     */
    public LayoutAttributes(final Composite parent, final Controller controller) {

        // Create the SashForm with HORIZONTAL
        centerSash = new SashForm(parent, SWT.VERTICAL);
        centerSash.setLayoutData(SWTUtil.createFillGridData());
        
        // Create center composite
        SashForm center = new SashForm(centerSash, SWT.HORIZONTAL | SWT.SMOOTH);
        center.setLayoutData(SWTUtil.createFillGridData());

        // Create left composite
        centerLeft = new Composite(center, SWT.NONE);
        centerLeft.setLayoutData(SWTUtil.createFillGridData());
        centerLeft.setLayout(new FillLayout());

        // Create right composite
        centerRight = new Composite(center, SWT.NONE);
        centerRight.setLayoutData(SWTUtil.createFillGridData());
        centerRight.setLayout(new FillLayout());

        // Create views
        layoutTopLeft = new LayoutAttributesTop(centerLeft,
                                          controller,
                                          ModelPart.INPUT,
                                          null);
        layoutTopRight = new LayoutAttributesTop(centerRight,
                                           controller,
                                           ModelPart.OUTPUT,
                                           ModelPart.INPUT);

        // Create bottom composite
        final Composite compositeBottom = new Composite(centerSash, SWT.NONE);
        compositeBottom.setLayout(new FillLayout());
        final SashForm bottomSash = new SashForm(compositeBottom,
                                                 SWT.HORIZONTAL | SWT.SMOOTH);

        bottomLeft = new Composite(bottomSash, SWT.NONE);
        bottomLeft.setLayout(new FillLayout());

        bottomRight = new Composite(bottomSash, SWT.NONE);
        bottomRight.setLayout(new FillLayout());

        // Create views
        layoutBottomLeft = new LayoutAttributesBottom(bottomLeft,
                                          controller,
                                          ModelPart.INPUT,
                                          null);
        layoutBottomRight = new LayoutAttributesBottom(bottomRight,
                                           controller,
                                           ModelPart.OUTPUT,
                                           ModelPart.INPUT);
        // Sync folders
        layoutBottomLeft.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                
                // Synchronize left and right side
                layoutBottomRight.setSelectionIdex(layoutBottomLeft.getSelectionIndex());
                
                controller.update(new ModelEvent(this, ModelPart.SELECTED_ATTRIBUTES_VISUALIZATION, null));
            }
        });
        layoutBottomRight.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                
                // Synchronize left and right side
                layoutBottomLeft.setSelectionIdex(layoutBottomRight.getSelectionIndex());

                // Synchronize "Quasi-identifiers"
                if (layoutBottomRight.getSelectionIndex() == 0) {
                    layoutTopLeft.setSelectionIdex(0);
                    layoutTopRight.setSelectionIdex(0);
                }
                
                controller.update(new ModelEvent(this, ModelPart.SELECTED_ATTRIBUTES_VISUALIZATION, null));
            }
        });
        
        layoutTopLeft.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                
                // Synchronize left and right
                layoutTopRight.setSelectionIdex(layoutTopLeft.getSelectionIndex());

                // Synchronize "Quasi-identifiers"
                if (layoutTopLeft.getSelectionIndex() == 0) {
                    layoutBottomRight.setSelectionIdex(0);
                }
                
                controller.update(new ModelEvent(this, ModelPart.SELECTED_ATTRIBUTES_VISUALIZATION, null));
            }
        });
        layoutTopRight.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                
                // Synchronize left and right
                layoutTopLeft.setSelectionIdex(layoutTopRight.getSelectionIndex());

                // Synchronize "Quasi-identifiers"
                if (layoutTopRight.getSelectionIndex() == 0) {
                    layoutBottomRight.setSelectionIdex(0);
                }
                
                controller.update(new ModelEvent(this, ModelPart.SELECTED_ATTRIBUTES_VISUALIZATION, null));
            }
        });

        // Set sash weights
        centerSash.setWeights(new int[] { WEIGHT_TOP, WEIGHT_BOTTOM });
        bottomSash.setWeights(new int[] { WEIGHT_LEFT, WEIGHT_RIGHT });
        center.setWeights(new int[] { WEIGHT_LEFT, WEIGHT_RIGHT });

        // Fix resize bug
        SWTUtil.fixOSXSashBug(centerSash);
        SWTUtil.fixOSXSashBug(bottomSash);
        SWTUtil.fixOSXSashBug(center);
    }
}