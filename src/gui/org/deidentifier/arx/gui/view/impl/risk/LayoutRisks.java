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

package org.deidentifier.arx.gui.view.impl.risk;

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
 * This class layouts the risk analysis view.
 *
 * @author Fabian Prasser
 */
public class LayoutRisks implements ILayout {

    /** Constant */
    private static final int          WEIGHT_TOP    = 75;
    /** Constant */
    private static final int          WEIGHT_BOTTOM = 25;
    /** Constant */
    private static final int          WEIGHT_LEFT   = 50;
    /** Constant */
    private static final int          WEIGHT_RIGHT  = 50;

    /** View */
    private final Composite           centerLeft;
    /** View */
    private final Composite           centerRight;
    /** View */
    private final Composite           bottomLeft;
    /** View */
    private final Composite           bottomRight;
    /** View */
    private final SashForm            centerSash;
    /** View */
    private final LayoutRisksAbstract layoutBottomLeft;
    /** View */
    private final LayoutRisksAbstract layoutBottomRight;
    /** View */
    private final LayoutRisksTop      layoutTopLeft;
    /** View */
    private final LayoutRisksTop      layoutTopRight;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     */
    public LayoutRisks(final Composite parent, final Controller controller) {

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
        layoutTopLeft = new LayoutRisksTop(centerLeft,
                                          controller,
                                          ModelPart.INPUT,
                                          null);
        layoutTopRight = new LayoutRisksTop(centerRight,
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
        layoutBottomLeft = new LayoutRisksBottom(bottomLeft,
                                          controller,
                                          ModelPart.INPUT,
                                          null);
        layoutBottomRight = new LayoutRisksBottom(bottomRight,
                                           controller,
                                           ModelPart.OUTPUT,
                                           ModelPart.INPUT);
        // Sync folders
        layoutBottomLeft.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                layoutBottomRight.setSelectionIdex(layoutBottomLeft.getSelectionIndex());
                
                if (layoutBottomLeft.getSelectionIndex() == 4) {
                    layoutTopLeft.setSelectionIdex(2);
                    layoutTopRight.setSelectionIdex(2);
                } else if (layoutBottomLeft.getSelectionIndex() == 0) {
                    layoutTopLeft.setSelectionIdex(3);
                    layoutTopRight.setSelectionIdex(3);
                }
                
                controller.update(new ModelEvent(this, ModelPart.SELECTED_RISK_VISUALIZATION, null));
            }
        });
        layoutBottomRight.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                layoutBottomLeft.setSelectionIdex(layoutBottomRight.getSelectionIndex());
                
                if (layoutBottomRight.getSelectionIndex() == 0) {
                    layoutTopLeft.setSelectionIdex(3);
                    layoutTopRight.setSelectionIdex(3);
                }
                
                controller.update(new ModelEvent(this, ModelPart.SELECTED_RISK_VISUALIZATION, null));
            }
        });
        
        layoutTopLeft.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                layoutTopRight.setSelectionIdex(layoutTopLeft.getSelectionIndex());
                
                if (layoutTopLeft.getSelectionIndex() == 2) {
                    layoutBottomLeft.setSelectionIdex(4);
                } else if (layoutTopLeft.getSelectionIndex() == 3) {
                    layoutBottomLeft.setSelectionIdex(0);
                    layoutBottomRight.setSelectionIdex(0);
                }
                
                controller.update(new ModelEvent(this, ModelPart.SELECTED_RISK_VISUALIZATION, null));
            }
        });
        layoutTopRight.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                layoutTopLeft.setSelectionIdex(layoutTopRight.getSelectionIndex());

                if (layoutTopRight.getSelectionIndex() == 2) {
                    layoutBottomLeft.setSelectionIdex(4);
                } else if (layoutTopRight.getSelectionIndex() == 3) {
                    layoutBottomLeft.setSelectionIdex(0);
                    layoutBottomRight.setSelectionIdex(0);
                }
                
                controller.update(new ModelEvent(this, ModelPart.SELECTED_RISK_VISUALIZATION, null));
            }
        });

        // Set sash weights
        centerSash.setWeights(new int[] { WEIGHT_TOP, WEIGHT_BOTTOM });
        bottomSash.setWeights(new int[] { WEIGHT_LEFT, WEIGHT_RIGHT });
        center.setWeights(new int[] { WEIGHT_LEFT, WEIGHT_RIGHT });
    }
}