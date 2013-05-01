/*
 * FLASH: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.flash.gui.view.impl.analyze;

import org.deidentifier.flash.gui.Controller;
import org.deidentifier.flash.gui.SWTUtil;
import org.deidentifier.flash.gui.resources.Resources;
import org.deidentifier.flash.gui.view.def.IAttachable;
import org.deidentifier.flash.gui.view.def.IDataView;
import org.deidentifier.flash.gui.view.def.IStatisticsView;
import org.deidentifier.flash.gui.view.def.IView.ModelEvent.EventTarget;
import org.deidentifier.flash.gui.view.impl.common.DataView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;

public class AnalyzeView implements IAttachable {

    private class Synchronizer implements Runnable {
        final IDataView in;
        final IDataView out;
        Boolean         stop     = false;
        Runnable        runnable = null;

        public Synchronizer(final IDataView in, final IDataView out) {
            this.in = in;
            this.out = out;
            runnable = new Runnable() {
                @Override
                public void run() {
                    out.getViewportLayer()
                       .setOriginRowPosition(in.getViewportLayer()
                                               .getOriginRowPosition());
                    out.getViewportLayer()
                       .setOriginColumnPosition(in.getViewportLayer()
                                                  .getOriginColumnPosition());
                }
            };
            new Thread(this).start();
        }

        public IDataView getIn() {
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
                    } // Catch nattable bugs
                }
                try {
                    Thread.sleep(10);
                } catch (final InterruptedException e) {
                }
            }
            synchronizer = null;
            synchronized (stop) {
                stop.notify();
            }
        }

        public void stop() {
            stop = true;
            synchronized (stop) {
                try {
                    stop.wait();
                } catch (final InterruptedException e) {
                }
            }
        }
    }

    private static final String   TEXT_CENTER_RIGHT = Resources.getMessage("AnalyzeView.0"); //$NON-NLS-1$
    private static final String   TEXT_CENTER_LEFT  = Resources.getMessage("AnalyzeView.1"); //$NON-NLS-1$
    private static final int      WEIGHT_TOP        = 75;

    private static final int      WEIGHT_BOTTOM     = 25;
    private static final int      WEIGHT_LEFT       = 50;

    private static final int      WEIGHT_RIGHT      = 50;
    private final Group           centerLeft;
    private final Group           centerRight;
    private final Composite           bottomLeft;

    private final Composite           bottomRight;
    private final IDataView       dataInputView;
    private final IDataView       dataOutputView;

    private final IStatisticsView statisticsInputView;

    private final IStatisticsView statisticsOutputView;

    private final SashForm        centerSash;

    private Synchronizer          synchronizer      = null;

    public AnalyzeView(final Composite parent, final Controller controller) {

        // Create the SashForm with HORIZONTAL
        centerSash = new SashForm(parent, SWT.VERTICAL);

        // Create center composite
        final Composite center = new Composite(centerSash, SWT.NONE);
        // center.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        final GridLayout centerLayout = new GridLayout();
        centerLayout.numColumns = 2;
        center.setLayout(centerLayout);

        // Create left composite
        centerLeft = new Group(center, SWT.NONE);
        centerLeft.setText(TEXT_CENTER_LEFT);
        centerLeft.setLayoutData(SWTUtil.createFillGridData());
        final GridLayout leftLayout = new GridLayout();
        leftLayout.numColumns = 1;
        centerLeft.setLayout(leftLayout);

        // Create right composite
        centerRight = new Group(center, SWT.NONE);
        centerRight.setText(TEXT_CENTER_RIGHT);
        centerRight.setLayoutData(SWTUtil.createFillGridData());
        final GridLayout rightLayout = new GridLayout();
        rightLayout.numColumns = 1;
        centerRight.setLayout(rightLayout);

        // Create views
        dataInputView = new DataView(centerLeft,
                                     controller,
                                     EventTarget.INPUT,
                                     null);
        dataOutputView = new DataView(centerRight,
                                      controller,
                                      EventTarget.OUTPUT,
                                      EventTarget.INPUT);

        // Sync tables
        dataInputView.addSelectionListener(new Listener() {
            @Override
            public void handleEvent(final Event arg0) {
                final int row = dataInputView.getViewportLayer()
                                             .getOriginRowPosition();
                final int col = dataInputView.getViewportLayer()
                                             .getOriginColumnPosition();
                if (dataOutputView != null) {
                    dataOutputView.getViewportLayer().setOriginRowPosition(row);
                    dataOutputView.getViewportLayer()
                                  .setOriginColumnPosition(col);
                    synchronize(dataInputView, dataOutputView);
                }
            }
        });
        dataOutputView.addSelectionListener(new Listener() {
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

        statisticsInputView = new StatisticsView(bottomLeft,
                                                 controller,
                                                 EventTarget.INPUT,
                                                 null);
        statisticsOutputView = new StatisticsView(bottomRight,
                                                  controller,
                                                  EventTarget.OUTPUT,
                                                  EventTarget.INPUT);

        // Sync folders
        statisticsInputView.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                statisticsOutputView.setSelectionIdex(statisticsInputView.getSelectionIndex());
            }
        });
        statisticsOutputView.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                statisticsInputView.setSelectionIdex(statisticsOutputView.getSelectionIndex());
            }
        });

        // Set sash weights
        centerSash.setWeights(new int[] { WEIGHT_TOP, WEIGHT_BOTTOM });
        bottomSash.setWeights(new int[] { WEIGHT_LEFT, WEIGHT_RIGHT });
    }

    @Override
    public Control getControl() {
        return centerSash;
    }

    /**
     * Synchronizes the tables for another second
     * 
     * @param in
     * @param out
     */
    protected void synchronize(final IDataView in, final IDataView out) {

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
