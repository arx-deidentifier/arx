/*
 * ARX: Powerful Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.arx.gui.view.impl.analyze;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.ILayout;
import org.deidentifier.arx.gui.view.impl.common.ViewData;
import org.deidentifier.arx.gui.view.impl.common.ViewDataInput;
import org.deidentifier.arx.gui.view.impl.common.ViewDataOutput;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

/**
 * This class layouts the analysis view
 * @author Fabian Prasser
 */
public class LayoutAnalyze implements ILayout {

    /**
     * A runnable for synchronizing both tables
     * @author Fabian Prasser
     */
    private class Synchronizer implements Runnable {

        final ViewData in;
        final ViewData out;
        
        Boolean        stop     = false;
        Runnable       runnable = null;

        /**
         * Creates a new instance
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
                    outLayer.setOriginRowPosition(inLayer.getOriginRowPosition());
                    outLayer.setOriginColumnPosition(inLayer.getOriginColumnPosition());
                }
            };
            new Thread(this).start();
        }

        /**
         * Returns the input view
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

    private static final int       WEIGHT_TOP    = 75;
    private static final int       WEIGHT_BOTTOM = 25;
    private static final int       WEIGHT_LEFT   = 50;
    private static final int       WEIGHT_RIGHT  = 50;

    private final Composite        centerLeft;
    private final Composite        centerRight;
    private final Composite        bottomLeft;
    private final Composite        bottomRight;
    private final SashForm         centerSash;
    
    private final ViewData         dataInputView;
    private final ViewData         dataOutputView;

    private final LayoutStatistics statisticsInputLayout;
    private final LayoutStatistics statisticsOutputLayout;

    private Synchronizer           synchronizer  = null;
    private String[]               monitor = new String[0];

    /**
     * Creates a new instance
     * @param parent
     * @param controller
     */
    public LayoutAnalyze(final Composite parent, final Controller controller) {

        // Create the SashForm with HORIZONTAL
        centerSash = new SashForm(parent, SWT.VERTICAL);
        centerSash.setLayoutData(SWTUtil.createFillGridData());
        
        // Create center composite
        final Composite center = new Composite(centerSash, SWT.NONE);
        center.setLayoutData(SWTUtil.createFillGridData());
        center.setLayout(SWTUtil.createGridLayout(2));

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
                                          controller);
        dataOutputView = new ViewDataOutput(centerRight,
                                            controller);

        // Sync tables
        dataInputView.addScrollBarListener(new Listener() {
            @Override
            public void handleEvent(final Event arg0) {
                final int row = dataInputView.getViewportLayer()
                                             .getOriginRowPosition();
                final int col = dataInputView.getViewportLayer()
                                             .getOriginColumnPosition();
                if (dataOutputView != null) {
                    dataOutputView.getViewportLayer().setOriginRowPosition(row);
                    dataOutputView.getViewportLayer().setOriginColumnPosition(col);
                    synchronize(dataInputView, dataOutputView);
                }
            }
        });
        dataOutputView.addScrollBarListener(new Listener() {
            @Override
            public void handleEvent(final Event arg0) {
                final int row = dataOutputView.getViewportLayer()
                                              .getOriginRowPosition();
                final int col = dataOutputView.getViewportLayer()
                                              .getOriginColumnPosition();
                if (dataInputView != null) {
                    dataInputView.getViewportLayer().setOriginRowPosition(row);
                    dataInputView.getViewportLayer()
                                 .setOriginColumnPosition(col);
                    synchronize(dataOutputView, dataInputView);
                }
            }
        });

        // Create bottom composite
        final Composite compositeBottom = new Composite(centerSash, SWT.NONE);
        compositeBottom.setLayout(new FillLayout());
        final SashForm bottomSash = new SashForm(compositeBottom,
                                                 SWT.HORIZONTAL | SWT.SMOOTH);

        bottomLeft = new Composite(bottomSash, SWT.NONE);
        bottomLeft.setLayout(new FillLayout());

        bottomRight = new Composite(bottomSash, SWT.NONE);
        bottomRight.setLayout(new FillLayout());

        statisticsInputLayout = new LayoutStatistics(bottomLeft,
                                                 controller,
                                                 ModelPart.INPUT,
                                                 null);
        statisticsOutputLayout = new LayoutStatistics(bottomRight,
                                                  controller,
                                                  ModelPart.OUTPUT,
                                                  ModelPart.INPUT);

        // Sync folders
        statisticsInputLayout.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                statisticsOutputLayout.setSelectionIdex(statisticsInputLayout.getSelectionIndex());
            }
        });
        statisticsOutputLayout.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                statisticsInputLayout.setSelectionIdex(statisticsOutputLayout.getSelectionIndex());
            }
        });

        // Set sash weights
        centerSash.setWeights(new int[] { WEIGHT_TOP, WEIGHT_BOTTOM });
        bottomSash.setWeights(new int[] { WEIGHT_LEFT, WEIGHT_RIGHT });
    }

    /**
     * Synchronizes the tables for another second
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