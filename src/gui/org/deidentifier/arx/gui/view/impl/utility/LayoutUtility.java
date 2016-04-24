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

package org.deidentifier.arx.gui.view.impl.utility;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.ILayout;
import org.deidentifier.arx.gui.view.impl.common.ViewData;
import org.deidentifier.arx.gui.view.impl.common.ViewDataInput;
import org.deidentifier.arx.gui.view.impl.common.ViewDataOutput;
import org.eclipse.nebula.widgets.nattable.coordinate.PixelCoordinate;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * This class layouts the analysis view.
 *
 * @author Fabian Prasser
 */
public class LayoutUtility implements ILayout {

    public static enum ViewUtilityType {
        CLASSIFICATION,
        LOGISTIC_REGRESSION,
        DATA,
        CONTINGENCY,
        CONTINGENCY_TABLE,
        HISTOGRAM,
        HISTOGRAM_TABLE,
        EQUIVALENCE_CLASSES,
        SUMMARY,
        PROPERTIES,
        LOCAL_RECODING
    }

    /**
     * A runnable for synchronizing both tables.
     *
     * @author Fabian Prasser
     */
    private class Synchronizer implements Runnable {

        /**  TODO */
        final ViewData in;
        
        /**  TODO */
        final ViewData out;
        
        /**  TODO */
        Boolean        stop     = false;
        
        /**  TODO */
        Runnable       runnable = null;

        /**
         * Creates a new instance.
         *
         * @param in
         * @param out
         */
        public Synchronizer(final ViewData in, final ViewData out) {
            this.in = in;
            this.out = out;
            runnable = new Runnable() {
                @Override
                public void run() {
                    ViewportLayer outLayer = out.getViewportLayer();
                    ViewportLayer inLayer = in.getViewportLayer();
                    PixelCoordinate coordinate = inLayer.getOrigin();
                    final int y = coordinate.getY();
                    final int x = coordinate.getX();
                    outLayer.setOriginY(y);
                    outLayer.setOriginX(x);
                }
            };
            new Thread(this).start();
        }

        /**
         * Returns the input view.
         *
         * @return
         */
        public ViewData getIn() {
            return in;
        }

        @Override
        public void run() {
            final long time = System.currentTimeMillis();
            while (!stop && ((System.currentTimeMillis() - time) < 1000)) {
                if ((in != null) && (out != null)) {
                    try {
                        if (Display.getCurrent() != null) {
                            runnable.run();
                        } else {
                            Display.getDefault().syncExec(runnable);
                        }
                    } catch (final Exception e) {
                        // Die silently
                    } 
                }
                try {
                    Thread.sleep(10);
                } catch (final InterruptedException e) {
                    // Die silently
                }
            }
            synchronizer = null;
            synchronized (monitor) {
                monitor.notify();
            }
        }

        /**
         * 
         */
        public void stop() {
            stop = true;
            synchronized (monitor) {
                try {
                    while (synchronizer != null){
                        monitor.wait();
                    }
                } catch (final InterruptedException e) {
                    // Die silently
                }
            }
        }
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

    /** View */
    private Synchronizer                  synchronizer  = null;

    /** View */
    private String[]                      monitor       = new String[0];

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
        dataInputView = new ViewDataInput(centerLeft,
                                          controller, 
                                          "help.utility.data"); //$NON-NLS-1$
        dataOutputView = new ViewDataOutput(centerRight,
                                            controller, 
                                            "help.utility.data"); //$NON-NLS-1$

        // Sync tables
        dataInputView.addScrollBarListener(new Listener() {
            @Override
            public void handleEvent(final Event arg0) {
                PixelCoordinate coordinate = dataInputView.getViewportLayer().getOrigin();
                final int row = coordinate.getY();
                final int col = coordinate.getX();
                if (dataOutputView != null) {
                    dataOutputView.getViewportLayer().setOriginY(row);
                    dataOutputView.getViewportLayer().setOriginX(col);
                    synchronize(dataInputView, dataOutputView);
                }
            }
        });
        dataOutputView.addScrollBarListener(new Listener() {
            @Override
            public void handleEvent(final Event arg0) {
                PixelCoordinate coordinate = dataOutputView.getViewportLayer().getOrigin();
                final int row = coordinate.getY();
                final int col = coordinate.getX();
                if (dataInputView != null) {
                    dataInputView.getViewportLayer().setOriginY(row);
                    dataInputView.getViewportLayer().setOriginX(col);
                    synchronize(dataOutputView, dataInputView);
                }
            }
        });
        
        Composite classificationInput = dataInputView.createAdditionalItem(Resources.getMessage("StatisticsView.10"), "help.utility.accuracy"); //$NON-NLS-1$ //$NON-NLS-2$
        classificationInput.setLayout(new FillLayout());
        new ViewStatisticsLogisticRegressionInput(classificationInput, controller);
        
        Composite classificationOutput = dataOutputView.createAdditionalItem(Resources.getMessage("StatisticsView.10"), "help.utility.accuracy"); //$NON-NLS-1$ //$NON-NLS-2$
        classificationOutput.setLayout(new FillLayout());
        new ViewStatisticsLogisticRegressionOutput(classificationOutput, controller);

        // Create bottom composite
        final Composite compositeBottom = new Composite(centerSash, SWT.NONE);
        compositeBottom.setLayout(new FillLayout());
        final SashForm bottomSash = new SashForm(compositeBottom,
                                                 SWT.HORIZONTAL | SWT.SMOOTH);

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
                
                // Hack to show classification stuff
                if (dataInputView.getSelectionIndex()==1) {
                    statisticsInputLayout.setSelectedView(ViewUtilityType.CLASSIFICATION);
                    statisticsOutputLayout.setSelectedView(ViewUtilityType.CLASSIFICATION);
                }
                // Hack to update visualizations
                controller.update(new ModelEvent(this, ModelPart.SELECTED_UTILITY_VISUALIZATION, null));
            }
        });
        dataOutputView.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                dataInputView.setSelectionIndex(dataOutputView.getSelectionIndex());
                
                // Hack to show classification stuff
                if (dataOutputView.getSelectionIndex()==1) {
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
    }

    /**
     * Synchronizes the tables for another second.
     *
     * @param in
     * @param out
     */
    protected void synchronize(final ViewData in, final ViewData out) {

        synchronized (this) {
            if ((synchronizer != null) && (synchronizer.getIn() != in)) {
                synchronizer.stop();
                synchronizer = null;
            }

            if (synchronizer == null) {
                synchronizer = new Synchronizer(in, out);
            }
        }
    }
}