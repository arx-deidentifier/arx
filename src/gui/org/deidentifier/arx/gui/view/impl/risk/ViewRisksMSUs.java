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
import org.deidentifier.arx.gui.model.ModelRisk.ViewRiskType;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.common.ClipboardHandlerTable;
import org.deidentifier.arx.gui.view.impl.common.ComponentStatusLabelProgressProvider;
import org.deidentifier.arx.gui.view.impl.common.async.Analysis;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContext;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisManager;
import org.deidentifier.arx.risk.RiskEstimateBuilderInterruptible;
import org.deidentifier.arx.risk.RiskModelMSU;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import de.linearbits.swt.table.DynamicTable;
import de.linearbits.swt.table.DynamicTableColumn;

/**
 * This view displays statistics about MSUs.
 *
 * @author Fabian Prasser
 */
public class ViewRisksMSUs extends ViewRisks<AnalysisContextRisk> {

    /** View */
    private Composite                root;

    /** View */
    private DynamicTable             tableMSUSize;

    /** View */
    private DynamicTable             tableColumnContribution;

    /** View */
    private DynamicTable             tableColumnAverageKeySize;

    /** Internal stuff. */
    private AnalysisManager          manager;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     * @param target
     * @param reset
     */
    public ViewRisksMSUs(final Composite parent,
                                   final Controller controller,
                                   final ModelPart target,
                                   final ModelPart reset) {
        
        super(parent, controller, target, reset);
        this.manager = new AnalysisManager(parent.getDisplay());
        controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
    }
    
    @Override
    public void update(ModelEvent event) {
        super.update(event);
        if (event.part == ModelPart.ATTRIBUTE_TYPE) {
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
        for( TableItem i : table.getItems()) {
            i.dispose();
        }
    }

    /**
     * Creates a new table
     * @param root
     * @param title
     * @return
     */
    private DynamicTable createTable(Composite root, String title) {
        Label label = new Label(root, SWT.NONE);
        label.setLayoutData(SWTUtil.createNoFillGridData());
        label.setText(title);
        DynamicTable table = SWTUtil.createTableDynamic(root, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setMenu(new ClipboardHandlerTable(table).getMenu());
        DynamicTableColumn c = new DynamicTableColumn(table, SWT.LEFT);
        c.setWidth("50%", "100px"); //$NON-NLS-1$ //$NON-NLS-2$
        c.setText("Key"); //$NON-NLS-1$
        c = new DynamicTableColumn(table, SWT.LEFT);
        SWTUtil.createColumnWithBarCharts(table, c);
        c.setWidth("50%", "100px"); //$NON-NLS-1$ //$NON-NLS-2$
        c.setText("Value"); //$NON-NLS-1$
        for (final TableColumn col : table.getColumns()) {
            col.pack();
        }
        SWTUtil.createGenericTooltip(table);
        table.setLayoutData(SWTUtil.createFillGridData(0));
        return table;
    }

    /**
     * Fills the table
     * @param table
     * @param data
     */
    private void fillTableRelative(DynamicTable table, double[] data) {
        for (int i=0; i<data.length; i++) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(0, String.valueOf(i+1));
            item.setData("1", data[i]);
        }
    }

    /**
     * Fills the table
     * @param table
     * @param data
     */
    private void fillTableAbsolute(DynamicTable table, double[] data) {
        for (int i=0; i<data.length; i++) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(0, String.valueOf(i+1));
            item.setText(1, SWTUtil.getPrettyString(data[i]));
        }
    }

    @Override
    protected Control createControl(Composite parent) {

        this.root = new Composite(parent, SWT.NONE);
        this.root.setLayout(SWTUtil.createGridLayout(1));
        this.tableMSUSize = createTable(root, "Size distribution of MSUs");
        this.tableColumnContribution = createTable(root, "Column contributions");
        this.tableColumnAverageKeySize = createTable(root, "Average MSU size");
        return this.root;
    }

    @Override
    protected AnalysisContextRisk createViewConfig(AnalysisContext context) {
        return new AnalysisContextRisk(context);
    }

    @Override
    protected void doReset() {
        if (this.manager != null) {
            this.manager.stop();
        }
        root.setRedraw(false);
        clearTable(tableMSUSize);
        clearTable(tableColumnAverageKeySize);
        clearTable(tableColumnContribution);
        root.setRedraw(true);
        setStatusEmpty();
    }


    @Override
    protected void doUpdate(final AnalysisContextRisk context) {

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
            private double[] msuSizeDistribution;
            private double[] columnContribution;
            private double[] columnAverageKeySize;
            
            @Override
            public int getProgress() {
                return 0;
            }

            @Override
            public void onError() {
                setStatusEmpty();
            }

            @Override
            public void onFinish() {

                if (stopped || !isEnabled()) {
                    return;
                }
                

                root.setRedraw(false);
                // Clear
                clearTable(tableColumnAverageKeySize);
                clearTable(tableColumnContribution);
                clearTable(tableMSUSize);

                // Create entries
                fillTableAbsolute(tableColumnAverageKeySize, columnAverageKeySize);
                fillTableRelative(tableColumnContribution, columnContribution);
                fillTableRelative(tableMSUSize, msuSizeDistribution);
               
                root.layout();
                setStatusDone();
                root.setRedraw(true);
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
                RiskModelMSU model = builder.getMSUStatistics();

                // Create array
                msuSizeDistribution = model.getMSUSizeDistribution();
                columnContribution = model.getColumnKeyContributions();
                columnAverageKeySize = model.getColumnKeyAverageSize();
              
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
        return null;
    }

    @Override
    protected ViewRiskType getViewType() {
        return ViewRiskType.CLASSES_TABLE;
    }

    /**
     * Is an analysis running
     */
    protected boolean isRunning() {
        return manager != null && manager.isRunning();
    }
}
