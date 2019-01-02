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
package org.deidentifier.arx.gui.view.impl.attributes;

import java.util.Arrays;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelAttributes.ViewAttributesType;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.common.ClipboardHandlerTable;
import org.deidentifier.arx.gui.view.impl.common.ComponentStatusLabelProgressProvider;
import org.deidentifier.arx.gui.view.impl.common.async.Analysis;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContext;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisManager;
import org.deidentifier.arx.risk.RiskEstimateBuilderInterruptible;
import org.deidentifier.arx.risk.RiskModelMSUColumnStatistics;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import de.linearbits.swt.table.DynamicTable;
import de.linearbits.swt.table.DynamicTableColumn;

/**
 * This view displays statistics about column contributions.
 *
 * @author Fabian Prasser
 */
public class ViewAttributesMSUColumnContributions extends ViewAttributes<AnalysisContextAttributes> {

    /** Label */
    private static final String LABEL_ATTRIBUTE         = Resources.getMessage("RiskAnalysisMSU.1");
    /** Label */
    private static final String LABEL_CONTRIBUTION      = Resources.getMessage("RiskAnalysisMSU.2");
    /** Label */
    private static final String LABEL_AVERAGE_SIZE      = Resources.getMessage("RiskAnalysisMSU.3");
    /** Label */
    private static final String LABEL_NO_MSUS_FOUND     = Resources.getMessage("RiskAnalysisMSU.8");

    /** View */
    private Composite           root;

    /** View */
    private DynamicTable        tableAttributes;

    /** Internal stuff. */
    private AnalysisManager     manager;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     * @param target
     * @param reset
     */
    public ViewAttributesMSUColumnContributions(final Composite parent,
                         final Controller controller,
                         final ModelPart target,
                         final ModelPart reset) {
        
        super(parent, controller, target, reset);
        this.manager = new AnalysisManager(parent.getDisplay());
        controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        controller.addListener(ModelPart.POPULATION_MODEL, this);
    }
    
    @Override
    public void update(ModelEvent event) {
        super.update(event);
        if (event.part == ModelPart.ATTRIBUTE_TYPE || event.part == ModelPart.POPULATION_MODEL) {
            triggerUpdate();
        }
    }
    
    /**
     * Clears the given table
     * @param table
     */
    private void clearTable(DynamicTable table) {
        if (table == null) {
            return;
        }
        for (TableItem i : table.getItems()) {
            i.dispose();
        }
    }

    /**
     * Creates a new table
     * @param root
     * @param title
     * @return
     */
    private DynamicTable createTable(Composite root, String[] columns, String[] bars) {
        DynamicTable table = SWTUtil.createTableDynamic(root, SWT.BORDER | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setMenu(new ClipboardHandlerTable(table).getMenu());
        String width = String.valueOf((int) (100d / (double) columns.length)) + "%"; //$NON-NLS-1$
        for (String column : columns) {
            DynamicTableColumn c = new DynamicTableColumn(table, SWT.LEFT);
            if (Arrays.asList(bars).contains(column)) {
                SWTUtil.createColumnWithBarCharts(table, c);
            }
            c.setWidth(width, "100px"); //$NON-NLS-1$
            c.setText(column);
        }
        for (final TableColumn col : table.getColumns()) {
            col.pack();
        }
        table.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        SWTUtil.createGenericTooltip(table);
        return table;
    }

    @Override
    protected Control createControl(Composite parent) {

        // Root
        this.root = new Composite(parent, SWT.NONE);
        this.root.setLayout(SWTUtil.createGridLayout(1));
        
        // Table
        this.tableAttributes = createTable(root, new String[]{LABEL_ATTRIBUTE, LABEL_CONTRIBUTION, LABEL_AVERAGE_SIZE}, new String[]{LABEL_CONTRIBUTION});
        this.tableAttributes.setLayoutData(SWTUtil.createFillGridData());
        
        // Done
        return this.root;
    }

    @Override
    protected AnalysisContextAttributes createViewConfig(AnalysisContext context) {
        return new AnalysisContextAttributes(context);
    }

    @Override
    protected void doReset() {
        if (this.manager != null) {
            this.manager.stop();
        }
        root.setRedraw(false);    
        this.clearTable(tableAttributes);
        root.setRedraw(true);
        setStatusEmpty();
    }

    @Override
    protected void doUpdate(final AnalysisContextAttributes context) {

        // Enable/disable
        final RiskEstimateBuilderInterruptible builder = getBuilder(context);
        if (!this.isEnabled() || builder == null) {
            if (manager != null) {
                manager.stop();
            }
            this.setStatusEmpty();
            return;
        }

        // Create an analysis
        Analysis analysis = new Analysis() {

            private boolean  stopped = false;
            private double[] columnContribution;
            private double[] columnAverageKeySize;
            private String[] attributes;
            
            @Override
            public int getProgress() {
                return builder == null ? 0 : builder.getProgress();
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

                // Disable redraw
                root.setRedraw(false);
                
                // Clear table
                clearTable(tableAttributes);

                // Create entries for table
                for (int i=0; i<columnContribution.length; i++) {
                    TableItem item = new TableItem(tableAttributes, SWT.NONE);
                    item.setText(0, attributes[i]);
                    if (Double.isNaN(columnContribution[i])) {
                        item.setText(1, LABEL_NO_MSUS_FOUND);
                    } else {
                        item.setData("1", columnContribution[i]); //$NON-NLS-1$                
                    }
                    if (Double.isNaN(columnAverageKeySize[i])) {
                        item.setText(2, LABEL_NO_MSUS_FOUND);
                    } else {
                        item.setText(2, SWTUtil.getPrettyString(columnAverageKeySize[i]));                    
                    }
                }
                
                // Enable
                root.layout();
                root.setRedraw(true);
                setStatusDone();
            }

            @Override
            public void onInterrupt() {
                if (!isEnabled() || !isValid()) {
                    setStatusEmpty();
                } else {
                    setStatusWorking();
                }
            }

            @Override
            public void run() throws InterruptedException {

                // Timestamp
                long time = System.currentTimeMillis();

                // Perform work
                RiskModelMSUColumnStatistics model = builder.getMSUColumnStatistics(controller.getModel().getRiskModel().getMaxKeySize(),
                                                                                    controller.getModel().getRiskModel().isSdcMicroScores());

                // Create array
                columnContribution = model.getColumnKeyContributions();
                columnAverageKeySize = model.getColumnAverageKeySize();
                attributes = model.getAttributes();
              
                // Our users are patient
                while (System.currentTimeMillis() - time < MINIMAL_WORKING_TIME && !stopped) {
                    Thread.sleep(10);
                }
            }

            @Override
            public void stop() {
                if (builder != null) builder.interrupt();
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
    
    @Override
    protected ViewAttributesType getViewType() {
        return ViewAttributesType.COLUMN_CONTRIBUTIONS;
    }
    
    /**
     * Is an analysis running
     */
    protected boolean isRunning() {
        return manager != null && manager.isRunning();
    }
}
