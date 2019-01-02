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
package org.deidentifier.arx.gui.view.impl.attributes;

import java.util.List;

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
import org.deidentifier.arx.risk.RiskModelAttributes;
import org.deidentifier.arx.risk.RiskModelAttributes.QuasiIdentifierRisk;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import de.linearbits.swt.table.DynamicTable;
import de.linearbits.swt.table.DynamicTableColumn;

/**
 * This view displays basic risk analyses of attributes.
 *
 * @author Fabian Prasser
 * @author Maximilian Zitzmann
 */
public class ViewAttributesQuasiIdentifiersTable extends ViewAttributes<AnalysisContextAttributes> {

    /** View */
    private DynamicTable          table;

    /** Internal stuff. */
    private final AnalysisManager manager;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     * @param target
     * @param reset
     */
    public ViewAttributesQuasiIdentifiersTable(final Composite parent,
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
     *
     * @param risks
     */
    private void createItem(QuasiIdentifierRisk risks) {
        TableItem item = new TableItem(table, SWT.NONE);
        List<String> list = risks.getIdentifier();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            builder.append(list.get(i));
            if (i < list.size() - 1) {
                builder.append(", "); //$NON-NLS-1$
            }
        }
        item.setText(0, builder.toString());
        item.setData("1", risks.getDistinction()); //$NON-NLS-1$
        item.setData("2", risks.getSeparation()); //$NON-NLS-1$
    }

    @Override
    protected Control createControl(Composite parent) {

        /* View */
        Composite root = new Composite(parent, SWT.NONE);
        root.setLayout(new FillLayout());

        table = SWTUtil.createTableDynamic(root, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setMenu(new ClipboardHandlerTable(table).getMenu());

        DynamicTableColumn c = new DynamicTableColumn(table, SWT.LEFT);
        c.setWidth("50%"); //$NON-NLS-1$ //$NON-NLS-2$
        c.setText(Resources.getMessage("RiskAnalysis.19")); //$NON-NLS-1$
        c.setResizable(true);

        c = new DynamicTableColumn(table, SWT.LEFT);
        SWTUtil.createColumnWithBarCharts(table, c);
        c.setWidth("10%"); //$NON-NLS-1$ //$NON-NLS-2$
        c.setText(Resources.getMessage("RiskAnalysis.43")); //$NON-NLS-1$
        c.setResizable(true);

        c = new DynamicTableColumn(table, SWT.LEFT);
        SWTUtil.createColumnWithBarCharts(table, c);
        c.setWidth("10%"); //$NON-NLS-1$ //$NON-NLS-2$
        c.setText(Resources.getMessage("RiskAnalysis.44")); //$NON-NLS-1$
        c.setResizable(true);

        for (final TableColumn col : table.getColumns()) {
            col.pack();
        }
        SWTUtil.createGenericTooltip(table);
        return root;
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
        table.setRedraw(false);
        for (final TableItem i : table.getItems()) {
            i.dispose();
        }
        table.setRedraw(true);
        setStatusEmpty();
    }

    @Override
    protected void doUpdate(final AnalysisContextAttributes context) {
        
        // Enable/disable
        final RiskEstimateBuilderInterruptible builder = getBuilder(context, context.context.getModel().getSelectedQuasiIdentifiers());
        if (!this.isEnabled() || builder == null) {
            if (manager != null) {
                manager.stop();
            }
            this.setStatusEmpty();
            return;
        }

        // Create an analysis
        Analysis analysis = new Analysis() {

            // The statistics builder
            private boolean stopped = false;
            private RiskModelAttributes risks;

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

                // Disable drawing
                table.setRedraw(false);
                
                // Update chart
                for (final TableItem i : table.getItems()) {
                    i.dispose();
                }
                
                // Create table items
                for (QuasiIdentifierRisk item : risks.getAttributeRisks()) {
                    createItem(item);
                }
                
                // Pack columns
                for (final TableColumn col : table.getColumns()) {
                    col.pack();
                }
                
                // Layout
                table.layout();

                // Enable drawing and redraw
                table.setRedraw(true);
                table.redraw();
                table.getParent().pack();

                // Update status
                if (risks.getAttributeRisks().length == 0) {
                    setStatusEmpty();
                } else {
                    setStatusDone();
                }
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
                risks = builder.getAttributeRisks();

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
        return new ComponentStatusLabelProgressProvider() {
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
        return ViewAttributesType.ATTRIBUTES;
    }

    /**
     * Is an analysis running
     */
    protected boolean isRunning() {
        return manager != null && manager.isRunning();
    }
}