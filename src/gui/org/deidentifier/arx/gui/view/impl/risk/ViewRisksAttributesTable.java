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
import java.util.Collections;
import java.util.List;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.model.ModelRisk.ViewRisk;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.common.ClipboardHandlerTable;
import org.deidentifier.arx.gui.view.impl.common.ComponentStatusLabelProgressProvider;
import org.deidentifier.arx.gui.view.impl.common.async.Analysis;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContext;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisManager;
import org.deidentifier.arx.risk.RiskEstimateBuilderInterruptible;
import org.deidentifier.arx.risk.RiskModelAttributes;
import org.deidentifier.arx.risk.RiskModelAttributes.QuasiIdentifierRisks;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.mihalis.opal.dynamictablecolumns.DynamicTable;
import org.mihalis.opal.dynamictablecolumns.DynamicTableColumn;

/**
 * This view displays basic risk estimates.
 *
 * @author Fabian Prasser
 */
public class ViewRisksAttributesTable extends ViewRisks<AnalysisContextRisk> {

    /** View */
    private DecimalFormat     format = new DecimalFormat("##0.000");

    /** View */
    private Composite         root;

    /** View */
    private DynamicTable      table;

    /** View */
    private List<TableItem>   items;

    /** View */
    private List<TableColumn> columns;

    /** Internal stuff. */
    private AnalysisManager   manager;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     * @param target
     * @param reset
     */
    public ViewRisksAttributesTable(final Composite parent,
                                    final Controller controller,
                                    final ModelPart target,
                                    final ModelPart reset) {
        
        super(parent, controller, target, reset);
        controller.addListener(ModelPart.SELECTED_QUASI_IDENTIFIERS, this);
        controller.addListener(ModelPart.POPULATION_MODEL, this);
        this.manager = new AnalysisManager(parent.getDisplay());
    }
    
    @Override
    public void update(ModelEvent event) {
        super.update(event);
        if (event.part == ModelPart.SELECTED_QUASI_IDENTIFIERS || event.part == ModelPart.POPULATION_MODEL) {
            triggerUpdate();
        }
    }

    /**
     * Creates a table item
     * @param risks
     */
    private void createItem(QuasiIdentifierRisks risks) {
        final TableItem item = new TableItem(table, SWT.NONE);
        List<String> list = new ArrayList<String>();
        list.addAll(risks.getIdentifier());
        Collections.sort(list);
        StringBuilder builder = new StringBuilder();
        for (int i=0; i<list.size(); i++) {
            builder.append(list.get(i));
            if (i < list.size() - 1){
                builder.append(", ");
            }
        }
        item.setText(0, builder.toString());
        item.setText(1, format.format(risks.getFractionOfUniqueTuples() * 100d));
        item.setText(2, format.format(risks.getHighestReidentificationRisk() * 100d));
        item.setText(3, format.format(risks.getAverageReidentificationRisk() * 100d));

        items.add(item);
    }

    @Override
    protected Control createControl(Composite parent) {

        this.root = new Composite(parent, SWT.NONE);
        this.root.setLayout(new FillLayout());
        
        this.items = new ArrayList<TableItem>();
        this.columns = new ArrayList<TableColumn>();

        table = new DynamicTable(root, SWT.SINGLE | SWT.BORDER |
                                       SWT.V_SCROLL | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setMenu(new ClipboardHandlerTable(table).getMenu());

        DynamicTableColumn c = new DynamicTableColumn(table, SWT.LEFT);
        c.setWidth("70%"); //$NON-NLS-1$ //$NON-NLS-2$
        c.setText(Resources.getMessage("RiskAnalysis.19")); //$NON-NLS-1$
        c.setResizable(true);
        columns.add(c);
        c = new DynamicTableColumn(table, SWT.LEFT);
        c.setWidth("10%"); //$NON-NLS-1$ //$NON-NLS-2$
        c.setText(Resources.getMessage("RiskAnalysis.20")); //$NON-NLS-1$
        c.setResizable(true);
        columns.add(c);
        c = new DynamicTableColumn(table, SWT.LEFT);
        c.setWidth("10%"); //$NON-NLS-1$ //$NON-NLS-2$
        c.setText(Resources.getMessage("RiskAnalysis.21")); //$NON-NLS-1$
        c.setResizable(true);
        columns.add(c);
        c = new DynamicTableColumn(table, SWT.LEFT);
        c.setWidth("10%"); //$NON-NLS-1$ //$NON-NLS-2$
        c.setText(Resources.getMessage("RiskAnalysis.22")); //$NON-NLS-1$
        c.setResizable(true);
        columns.add(c);
        for (final TableColumn col : columns) {
            col.pack();
        }
        SWTUtil.createGenericTooltip(table);
        return root;
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
        table.setRedraw(true);
        items.clear();
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
            RiskEstimateBuilderInterruptible builder = getBuilder(context, context.context.getModel().getSelectedQuasiIdentifiers());
            private boolean                  stopped = false;
            private RiskModelAttributes      risks;
            
            @Override
            public int getProgress() {
                return builder.getProgress();
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

                // For all sizes
                for (QuasiIdentifierRisks item : risks.getAttributeRisks()) {
                    createItem(item);
                }

                for (final TableColumn col : columns) {
                    col.pack();
                }

                if (risks.getAttributeRisks().length==0) {
                    setStatusEmpty();
                } else {
                    setStatusDone();
                }

                table.layout();
                table.redraw();
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

                // Perform work
                risks = builder.getPopulationBasedAttributeRisks();

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
    protected ViewRisk getViewType() {
        return ViewRisk.ATTRIBUTES;
    }

    /**
     * Is an analysis running
     */
    protected boolean isRunning() {
        return manager != null && manager.isRunning();
    }
}
