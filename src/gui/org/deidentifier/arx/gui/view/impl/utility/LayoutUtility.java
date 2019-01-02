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

package org.deidentifier.arx.gui.view.impl.utility;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.ILayout;
import org.deidentifier.arx.gui.view.impl.common.DelayedChangeListener;
import org.deidentifier.arx.gui.view.impl.common.ViewData;
import org.deidentifier.arx.gui.view.impl.common.ViewDataInput;
import org.deidentifier.arx.gui.view.impl.common.ViewDataOutput;
import org.eclipse.nebula.widgets.nattable.coordinate.PixelCoordinate;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * This class layouts the analysis view.
 *
 * @author Fabian Prasser
 */
public class LayoutUtility implements ILayout {

    /**
     * Type of view which is displayed
     * 
     * @author Fabian Prasser
     */
    public static enum ViewUtilityType {
        CLASSIFICATION,
        CLASSIFICATION_PRECISION_RECALL,
        DATA,
        CONTINGENCY,
        CONTINGENCY_TABLE,
        HISTOGRAM,
        HISTOGRAM_TABLE,
        EQUIVALENCE_CLASSES,
        SUMMARY,
        PROPERTIES,
        LOCAL_RECODING,
        QUALITY_MODELS
    }

    /** Constant */
    private static final int              WEIGHT_TOP    = 75;

    /** Constant */
    private static final int              WEIGHT_BOTTOM = 25;

    /** Constant */
    private static final int              WEIGHT_LEFT   = 50;

    /** Constant */
    private static final int              WEIGHT_RIGHT  = 50;

    /** View */
    private final Composite               centerLeft;

    /** View */
    private final Composite               centerRight;

    /** View */
    private final Composite               bottomLeft;

    /** View */
    private final Composite               bottomRight;

    /** View */
    private final SashForm                centerSash;

    /** View */
    private final ViewData                dataInputView;

    /** View */
    private final ViewData                dataOutputView;

    /** View */
    private final LayoutUtilityStatistics statisticsInputLayout;

    /** View */
    private final LayoutUtilityStatistics statisticsOutputLayout;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     */
    public LayoutUtility(final Composite parent, final Controller controller) {

        // Create the SashForm with HORIZONTAL
        centerSash = new SashForm(parent, SWT.VERTICAL);
        centerSash.setLayoutData(SWTUtil.createFillGridData());
        
        // Create center composite
        final Composite center = new Composite(centerSash, SWT.NONE);
        center.setLayoutData(SWTUtil.createFillGridData());
        center.setLayout(SWTUtil.createGridLayoutWithEqualWidth(2));

        // Create left composite
        centerLeft = new Composite(center, SWT.NONE);
        centerLeft.setLayoutData(SWTUtil.createFillGridData());
        centerLeft.setLayout(SWTUtil.createGridLayout(1));

        // Create right composite
        centerRight = new Composite(center, SWT.NONE);
        centerRight.setLayoutData(SWTUtil.createFillGridData());
        centerRight.setLayout(SWTUtil.createGridLayout(1));

        // Create views
        dataInputView = new ViewDataInput(centerLeft, controller, "help.utility.data"); //$NON-NLS-1$
        dataOutputView = new ViewDataOutput(centerRight, controller, "help.utility.data"); //$NON-NLS-1$

        // Sync tables
        dataInputView.addScrollBarListener(new Listener() {
            @Override public void handleEvent(final Event arg0) {
                synchronize(dataInputView, dataOutputView);
            }
        });
        dataInputView.addScrollBarListener(new DelayedChangeListener(100) {
            @Override public void delayedEvent() {
                synchronize(dataInputView, dataOutputView);
            }
        });        
        dataOutputView.addScrollBarListener(new Listener() {
            @Override public void handleEvent(final Event arg0) {
                synchronize(dataOutputView, dataInputView);
            }
        });
        dataOutputView.addScrollBarListener(new DelayedChangeListener(100) {
            @Override public void delayedEvent() {
                synchronize(dataOutputView, dataInputView);
            }
        });
        
        Composite classificationInput = dataInputView.createAdditionalItem(Resources.getMessage("StatisticsView.10"), "help.utility.accuracy"); //$NON-NLS-1$ //$NON-NLS-2$
        classificationInput.setLayout(new FillLayout());
        ViewStatisticsClassificationInput viewClassificationInput = new ViewStatisticsClassificationInput(classificationInput, controller);
        
        Composite classificationOutput = dataOutputView.createAdditionalItem(Resources.getMessage("StatisticsView.10"), "help.utility.accuracy"); //$NON-NLS-1$ //$NON-NLS-2$
        classificationOutput.setLayout(new FillLayout());
        ViewStatisticsClassificationOutput viewClassificationOutput = new ViewStatisticsClassificationOutput(classificationOutput, controller);

        Composite qualityInput = dataInputView.createAdditionalItem(Resources.getMessage("StatisticsView.11"), "help.utility.quality"); //$NON-NLS-1$ //$NON-NLS-2$
        qualityInput.setLayout(new FillLayout());
        new ViewStatisticsQuality(qualityInput, controller, ModelPart.INPUT, ModelPart.INPUT);
        
        Composite qualityOutput = dataOutputView.createAdditionalItem(Resources.getMessage("StatisticsView.11"), "help.utility.quality"); //$NON-NLS-1$ //$NON-NLS-2$
        qualityOutput.setLayout(new FillLayout());
        new ViewStatisticsQuality(qualityOutput, controller, ModelPart.OUTPUT, ModelPart.INPUT);

        // Create bottom composite
        final Composite compositeBottom = new Composite(centerSash, SWT.NONE);
        compositeBottom.setLayout(new FillLayout());
        final SashForm bottomSash = new SashForm(compositeBottom, SWT.HORIZONTAL | SWT.SMOOTH);

        bottomLeft = new Composite(bottomSash, SWT.NONE);
        bottomLeft.setLayout(new FillLayout());

        bottomRight = new Composite(bottomSash, SWT.NONE);
        bottomRight.setLayout(new FillLayout());

        statisticsInputLayout = new LayoutUtilityStatistics(bottomLeft,
                                                 controller,
                                                 ModelPart.INPUT,
                                                 null);
        statisticsOutputLayout = new LayoutUtilityStatistics(bottomRight,
                                                  controller,
                                                  ModelPart.OUTPUT,
                                                  ModelPart.INPUT);

        // Sync folders
        dataInputView.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                dataOutputView.setSelectionIndex(dataInputView.getSelectionIndex());
                
                // Hack to show summary for input
                if (dataInputView.getSelectionIndex() == 0) {
                    statisticsInputLayout.setSelectedView(ViewUtilityType.SUMMARY);
                    statisticsOutputLayout.setSelectedView(ViewUtilityType.SUMMARY);
                }
                // Hack to show classification stuff
                if (dataInputView.getSelectionIndex() == 1) {
                    statisticsInputLayout.setSelectedView(ViewUtilityType.CLASSIFICATION);
                    statisticsOutputLayout.setSelectedView(ViewUtilityType.CLASSIFICATION);
                }

                // Hack to update visualizations
                controller.update(new ModelEvent(this, ModelPart.SELECTED_UTILITY_VISUALIZATION, null));
            }
        });
        viewClassificationInput.setOtherView(viewClassificationOutput);
        viewClassificationOutput.setOtherView(viewClassificationInput);
        dataOutputView.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                dataInputView.setSelectionIndex(dataOutputView.getSelectionIndex());
                
                // Hack to show summary for output
                if (dataOutputView.getSelectionIndex() == 0) {
                    statisticsInputLayout.setSelectedView(ViewUtilityType.SUMMARY);
                    statisticsOutputLayout.setSelectedView(ViewUtilityType.SUMMARY);
                }
                // Hack to show classification stuff
                if (dataOutputView.getSelectionIndex() == 1) {
                    statisticsInputLayout.setSelectedView(ViewUtilityType.CLASSIFICATION);
                    statisticsOutputLayout.setSelectedView(ViewUtilityType.CLASSIFICATION);
                }

                // Hack to update visualizations
                controller.update(new ModelEvent(this, ModelPart.SELECTED_UTILITY_VISUALIZATION, null));
            }
        });
        statisticsInputLayout.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                statisticsOutputLayout.setSelectedView(statisticsInputLayout.getSelectedView());

                // Hack to show classification stuff
                if (statisticsInputLayout.getSelectedView() == ViewUtilityType.CLASSIFICATION) {
                    dataOutputView.setSelectionIndex(1);
                    dataInputView.setSelectionIndex(1);
                }
                
                // Hack to update visualizations
                controller.update(new ModelEvent(this, ModelPart.SELECTED_UTILITY_VISUALIZATION, null));
            }
        });
        statisticsOutputLayout.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                statisticsInputLayout.setSelectedView(statisticsOutputLayout.getSelectedView());

                // Hack to show classification stuff
                if (statisticsOutputLayout.getSelectedView() == ViewUtilityType.CLASSIFICATION) {
                    dataOutputView.setSelectionIndex(1);
                    dataInputView.setSelectionIndex(1);
                }
                
                // Hack to update visualizations
                controller.update(new ModelEvent(this, ModelPart.SELECTED_UTILITY_VISUALIZATION, null));
            }
        });
        statisticsInputLayout.setItemVisibilityListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                statisticsOutputLayout.setVisibleItems(statisticsInputLayout.getVisibleItems());
                // Hack to update visualizations
                controller.update(new ModelEvent(this, ModelPart.SELECTED_UTILITY_VISUALIZATION, null));
            }
        });
        statisticsOutputLayout.setItemVisibilityListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                statisticsInputLayout.setVisibleItems(statisticsOutputLayout.getVisibleItems());
                // Hack to update visualizations
                controller.update(new ModelEvent(this, ModelPart.SELECTED_UTILITY_VISUALIZATION, null));
            }
        });

        // Set sash weights
        centerSash.setWeights(new int[] { WEIGHT_TOP, WEIGHT_BOTTOM });
        bottomSash.setWeights(new int[] { WEIGHT_LEFT, WEIGHT_RIGHT });
        
        // Fix resize bug
        SWTUtil.fixOSXSashBug(centerSash);
        SWTUtil.fixOSXSashBug(bottomSash);
    }

    /**
     * Synchronizes the position of both tables
     * @param in
     * @param out
     * @return
     */
    private void synchronize(ViewData in, ViewData out) {
        if (in == null || out == null) {
            return;
        }
        PixelCoordinate coordinate = in.getViewportLayer().getOrigin();
        final int x = coordinate.getY();
        final int y = coordinate.getX();
        out.getViewportLayer().setOriginY(x);
        out.getViewportLayer().setOriginX(y);
    }
}
