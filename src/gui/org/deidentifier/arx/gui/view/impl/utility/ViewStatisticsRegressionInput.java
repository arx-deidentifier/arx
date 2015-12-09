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

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.aggregates.StatisticsBuilderInterruptible;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.common.ClipboardHandlerTable;
import org.deidentifier.arx.gui.view.impl.common.ComponentStatusLabelProgressProvider;
import org.deidentifier.arx.gui.view.impl.common.async.Analysis;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContext;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import de.linearbits.swt.table.DynamicTable;
import de.linearbits.swt.table.DynamicTableColumn;

/**
 * This view displays a statistics about classification
 *
 * @author Fabian Prasser
 */
public class ViewStatisticsRegressionInput extends ViewStatistics<AnalysisContextClassification> {

    /** View */
    private DynamicTable    table;
    /** View */
    private Composite       root;

    /** Internal stuff. */
    private AnalysisManager manager;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     */
    public ViewStatisticsRegressionInput(final Composite parent,
                                             final Controller controller) {

        super(parent, controller, ModelPart.INPUT, null, false);
        this.manager = new AnalysisManager(parent.getDisplay());
        controller.addListener(ModelPart.SELECTED_FEATURES_OR_CLASSES, this);
        controller.addListener(ModelPart.DATA_TYPE, this);
    }

    @Override
    public LayoutUtility.ViewUtilityType getType() {
        return LayoutUtility.ViewUtilityType.LOGISTIC_REGRESSION;
    }
    @Override
    public void update(ModelEvent event) {
        super.update(event);
        if (event.part == ModelPart.SELECTED_FEATURES_OR_CLASSES ||
            event.part == ModelPart.DATA_TYPE) {
            triggerUpdate();
        }
    }

    @Override
    protected Control createControl(Composite parent) {

        this.root = new Composite(parent, SWT.NONE);
        this.root.setLayout(new FillLayout());
        
        table = SWTUtil.createTableDynamic(root, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setMenu(new ClipboardHandlerTable(table).getMenu());
        
        DynamicTableColumn c = new DynamicTableColumn(table, SWT.LEFT);
        c.setWidth("20%", "100px"); //$NON-NLS-1$ //$NON-NLS-2$
        c.setText(Resources.getMessage("ViewStatisticsClassificationInput.0")); //$NON-NLS-1$
        c = new DynamicTableColumn(table, SWT.LEFT);
        c.setWidth("20%", "100px"); //$NON-NLS-1$ //$NON-NLS-2$
        c.setText(Resources.getMessage("ViewStatisticsClassificationInput.2")); //$NON-NLS-1$
        c = new DynamicTableColumn(table, SWT.LEFT);
        SWTUtil.createColumnWithBarCharts(table, c);
        c.setWidth("20%", "100px"); //$NON-NLS-1$ //$NON-NLS-2$
        c.setText(Resources.getMessage("ViewStatisticsClassificationInput.3")); //$NON-NLS-1$
        c = new DynamicTableColumn(table, SWT.LEFT);
        SWTUtil.createColumnWithBarCharts(table, c);
        c.setWidth("20%", "100px"); //$NON-NLS-1$ //$NON-NLS-2$
        c.setText(Resources.getMessage("ViewStatisticsClassificationInput.1")); //$NON-NLS-1$
        c = new DynamicTableColumn(table, SWT.LEFT);
        SWTUtil.createColumnWithBarCharts(table, c);
        c.setWidth("20%", "100px"); //$NON-NLS-1$ //$NON-NLS-2$
        c.setText(Resources.getMessage("ViewStatisticsClassificationInput.5")); //$NON-NLS-1$
        for (final TableColumn col : table.getColumns()) {
            col.pack();
        }
        SWTUtil.createGenericTooltip(table);
        return this.root;
    }

    @Override
    protected AnalysisContextClassification createViewConfig(AnalysisContext context) {
        return new AnalysisContextClassification(context);
    }

    @Override
    protected void doReset() {
        if (this.manager != null) {
            this.manager.stop();
        }
        table.setRedraw(false);
        for (final TableItem i : table.getItems()) {
            i.dispose();
        }
        table.setRedraw(true);
        setStatusEmpty();
    }

    @Override
    protected void doUpdate(final AnalysisContextClassification context) {

        // The statistics builder
        final StatisticsBuilderInterruptible builder = context.handle.getStatistics().getInterruptibleInstance();
        final String[] features = context.model.getSelectedFeatures().toArray(new String[0]);
        final String[] classes = context.model.getSelectedClasses().toArray(new String[0]);
        final double fraction = context.handle.getNumRows() > context.model.getClassificationModel().getMaximalNumberOfRecords() ?
                                (double)context.model.getClassificationModel().getMaximalNumberOfRecords() / (double)context.handle.getNumRows() : 1d;
        final Integer seed = context.model.getClassificationModel().getSeed();
        final boolean ignoreSuppressedRecords = context.model.getClassificationModel().isIgnoreSuppressedRecords();
        
        // Create an analysis
        Analysis analysis = new Analysis(){

            private boolean       stopped            = false;
            private List<Double>  accuracies         = new ArrayList<>();
            private List<Double>  baselineAccuracies = new ArrayList<>();
            private List<Integer> classNumbers       = new ArrayList<>();
            private int           progress           = 0;

            @Override
            public int getProgress() {
                return (int)((double)progress / (double)classes.length / 3d * 100d);
            }
            
            @Override
            public void onError() {
                setStatusEmpty();
            }

            @Override
            public void onFinish() {

                // Check
                if (stopped || !isEnabled()) {
                    return;
                }

                // Update chart
                for (final TableItem i : table.getItems()) {
                    i.dispose();
                }

                // Create entries
                for (int i = 0; i < classes.length; i++) {
                    TableItem item = new TableItem(table, SWT.NONE);
                    item.setText(0, classes[i]);
                    item.setText(1, String.valueOf(classNumbers.get(i)));
                    item.setData("2", baselineAccuracies.get(i));
                    item.setData("3", accuracies.get(i));
                    item.setData("4", accuracies.get(i) - baselineAccuracies.get(i));
                }

                // Status
                root.layout();
                setStatusDone();
            }

            @Override
            public void onInterrupt() {
                if (!isEnabled()) {
                    setStatusEmpty();
                } else {
                    setStatusWorking();
                }
            }

            @Override
            public void run() throws InterruptedException {
                
                // Timestamp
                long time = System.currentTimeMillis();
                
                // Do work
                for (String clazz : classes) {
                    baselineAccuracies.add(builder.getClassificationPerformance(clazz,
                                                                                seed,
                                                                                ignoreSuppressedRecords,
                                                                                fraction).getFractionCorrect());
                    progress++;
                    if (stopped) {
                        break;
                    }
                    accuracies.add(builder.getClassificationPerformance(features,
                                                                        clazz,
                                                                        seed,
                                                                        ignoreSuppressedRecords,
                                                                        fraction).getFractionCorrect());
                    progress++;
                    if (stopped) {
                        break;
                    }
                    classNumbers.add(builder.getDistinctValues(context.handle.getColumnIndexOf(clazz)).length);
                    progress++;
                    if (stopped) {
                        break;
                    }
                }

                // Our users are patient
                while (System.currentTimeMillis() - time < MINIMAL_WORKING_TIME && !stopped){
                    Thread.sleep(10);
                }
            }

            @Override
            public void stop() {
                builder.interrupt();
                this.stopped = true;
            }
        };
        
        this.manager.start(analysis);
    }

    @Override
    protected ComponentStatusLabelProgressProvider getProgressProvider() {
        return new ComponentStatusLabelProgressProvider(){
            public int getProgress() {
                if (manager == null) {
                    return 0;
                } else {
                    return manager.getProgress();
                }
            }
        };
    }

    /**
     * Is an analysis running
     */
    protected boolean isRunning() {
        return manager != null && manager.isRunning();
    }
}
