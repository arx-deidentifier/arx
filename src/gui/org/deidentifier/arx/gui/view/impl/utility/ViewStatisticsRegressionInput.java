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
import org.deidentifier.arx.gui.view.impl.common.ComponentStatusLabelProgressProvider;
import org.deidentifier.arx.gui.view.impl.common.ComponentTable;
import org.deidentifier.arx.gui.view.impl.common.async.Analysis;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContext;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisManager;
import org.deidentifier.arx.gui.view.impl.common.table.CTConfiguration;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * This view displays a statistics about classification
 *
 * @author Fabian Prasser
 */
public class ViewStatisticsRegressionInput  extends ViewStatistics<AnalysisContextClassification> {

    /** View */
    private ComponentTable  table;

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

        // Configure table
        CTConfiguration config = new CTConfiguration(parent, CTConfiguration.STYLE_TABLE);
        config.setHorizontalAlignment(SWT.CENTER);
        config.setCellSelectionEnabled(false);
        config.setColumnSelectionEnabled(false);
        config.setRowSelectionEnabled(false);
        config.setColumnHeaderLayout(CTConfiguration.COLUMN_HEADER_LAYOUT_FILL_EQUAL);
        config.setRowHeaderLayout(CTConfiguration.ROW_HEADER_LAYOUT_DEFAULT);

        this.table = new ComponentTable(parent, SWT.NONE, config);
        return this.table.getControl();
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
        table.clear();
        setStatusEmpty();
    }

    @Override
    protected void doUpdate(final AnalysisContextClassification context) {

        // The statistics builder
        final StatisticsBuilderInterruptible builder = context.handle.getStatistics().getInterruptibleInstance();
        final String[] features = context.model.getSelectedFeatures().toArray(new String[0]);
        final String[] classes = context.model.getSelectedClasses().toArray(new String[0]);
        final double fraction = context.handle.getNumRows() > context.model.getClassificationMaxRecords() ?
                                (double)context.model.getClassificationMaxRecords() / (double)context.handle.getNumRows() : 1d;
        final Integer seed = context.model.getClassificationSeed();
        
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

                if (stopped) {
                    return;
                }

                // Now update the table
                table.setData(new IDataProvider() {
                    public int getColumnCount() {
                        return 4;
                    }
                    public Object getDataValue(int arg0, int arg1) {
                        switch(arg0){
                        case 0:
                            return classes[arg1];
                        case 1:
                            return String.valueOf(classNumbers.get(arg1));
                        case 2:
                            return SWTUtil.getPrettyString(baselineAccuracies.get(arg1)*100d)+"%"; //$NON-NLS-1$
                        case 3:
                            return SWTUtil.getPrettyString(accuracies.get(arg1)*100d)+"%"; //$NON-NLS-1$
                        }
                        return "";
                    }
                    public int getRowCount() {
                        return classes.length;
                    }
                    public void setDataValue(int arg0, int arg1, Object arg2) { 
                        /* Ignore */
                    }
                }, new String[] { Resources.getMessage("ViewStatisticsClassificationInput.0"), //$NON-NLS-1$
                                  Resources.getMessage("ViewStatisticsClassificationInput.2"), //$NON-NLS-1$
                                  Resources.getMessage("ViewStatisticsClassificationInput.3"), //$NON-NLS-1$
                                  Resources.getMessage("ViewStatisticsClassificationInput.1") }); //$NON-NLS-1$
                setStatusDone();
            }

            @Override
            public void onInterrupt() {
                setStatusWorking();
            }

            @Override
            public void run() throws InterruptedException {
                
                // Timestamp
                long time = System.currentTimeMillis();
                
                // Do work
                for (String clazz : classes) {
                    accuracies.add(builder.getClassificationPerformance(features,
                                                                        clazz,
                                                                        seed,
                                                                        fraction).getFractionCorrect());
                    progress++;
                    if (stopped) {
                        break;
                    }
                    baselineAccuracies.add(builder.getClassificationPerformance(clazz,
                                                                       seed,
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
