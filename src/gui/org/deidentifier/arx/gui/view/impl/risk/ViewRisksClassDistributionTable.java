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
package org.deidentifier.arx.gui.view.impl.risk;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.model.ModelRisk.ViewRiskType;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.common.ClipboardHandlerTable;
import org.deidentifier.arx.gui.view.impl.common.ComponentStatusLabelProgressProvider;
import org.deidentifier.arx.gui.view.impl.common.async.Analysis;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContext;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisManager;
import org.deidentifier.arx.risk.RiskEstimateBuilderInterruptible;
import org.deidentifier.arx.risk.RiskModelEquivalenceClasses;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableItem;
import de.linearbits.swttable.DynamicTable;
import de.linearbits.swttable.DynamicTableColumn;

/**
 * This view displays basic risk estimates.
 *
 * @author Fabian Prasser
 */
public class ViewRisksClassDistributionTable extends ViewRisks<AnalysisContextRisk> {

    /** View */
    private Composite                root;

    /** View */
    private DynamicTable             table;

    /** View */
    private List<TableItem>          items;

    /** View */
    private List<DynamicTableColumn> columns;

    /** View */
    private DecimalFormat            format;

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
    public ViewRisksClassDistributionTable(final Composite parent,
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
    
    @Override
    protected Control createControl(Composite parent) {

        this.items = new ArrayList<TableItem>();
        this.columns = new ArrayList<DynamicTableColumn>();
        this.format = new DecimalFormat("##0.00000");

        this.root = new Composite(parent, SWT.NONE);
        this.root.setLayout(new FillLayout());
        
        table = new DynamicTable(root, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setMenu(new ClipboardHandlerTable(table).getMenu());

        DynamicTableColumn c = new DynamicTableColumn(table, SWT.LEFT);
        c.setWidth("33%", "100px"); //$NON-NLS-1$ //$NON-NLS-2$
        c.setText(Resources.getMessage("RiskAnalysis.1")); //$NON-NLS-1$
        columns.add(c);
        c = new DynamicTableColumn(table, SWT.LEFT);
        c.setWidth("33%", "100px"); //$NON-NLS-1$ //$NON-NLS-2$
        c.setText(Resources.getMessage("RiskAnalysis.2")); //$NON-NLS-1$
        columns.add(c);
        c = new DynamicTableColumn(table, SWT.LEFT);
        c.setWidth("33%", "100px"); //$NON-NLS-1$ //$NON-NLS-2$
        c.setText(Resources.getMessage("RiskAnalysis.3")); //$NON-NLS-1$
        columns.add(c);
        for (final DynamicTableColumn col : columns) {
            col.pack();
        }
        SWTUtil.createGenericTooltip(table);
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
        table.setRedraw(false);
        for (final TableItem i : items) {
            i.dispose();
        }
        items.clear();
        table.setRedraw(true);
        setStatusEmpty();
    }


    @Override
    protected void doUpdate(final AnalysisContextRisk context) {

        // Enable/disable
        if (!this.isEnabled()) {
            if (manager != null) {
                manager.stop();
            }
            this.setStatusEmpty();
            return;
        }

        // Create an analysis
        Analysis analysis = new Analysis() {

            // The statistics builder
            RiskEstimateBuilderInterruptible builder = getBuilder(context);

            private boolean                  stopped = false;
            private int[]                    distribution;
            private double                   numClasses;

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

                // Update chart
                for (final TableItem i : items) {
                    i.dispose();
                }
                items.clear();

                // Create entries
                for (int i = 0; i < distribution.length; i += 2) {
                    TableItem item = new TableItem(table, SWT.NONE);
                    item.setText(0, String.valueOf(distribution[i]));
                    item.setText(1, String.valueOf(distribution[i + 1]));
                    item.setText(2, format.format((double) distribution[i + 1] / numClasses * 100d));
                    items.add(item);
                }

                root.layout();
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
                RiskModelEquivalenceClasses model = builder.getEquivalenceClassModel();
                distribution = model.getEquivalenceClasses();
                numClasses = model.getNumClasses();

                // Our users are patient
                while (System.currentTimeMillis() - time < MINIMAL_WORKING_TIME && !stopped) {
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
