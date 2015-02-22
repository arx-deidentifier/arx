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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.view.impl.common.ClipboardHandlerTable;
import org.deidentifier.arx.gui.view.impl.common.async.Analysis;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContext;
import org.deidentifier.arx.gui.view.impl.utility.AnalysisManager;
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
        this.manager = new AnalysisManager(parent.getDisplay());
    }
    
    @Override
    protected Control createControl(Composite parent) {

        this.items = new ArrayList<TableItem>();
        this.columns = new ArrayList<TableColumn>();

        this.root = new Composite(parent, SWT.NONE);
        this.root.setLayout(new FillLayout());
        
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
        if (table != null && !table.isDisposed()) {
            table.setRedraw(false);
            for (final TableItem i : items) {
                i.dispose();
            }
            for (TableColumn c : columns) {
                c.dispose();
            }
            table.setRedraw(true);
            table.dispose();
        }
        items.clear();
        columns.clear();
    }

    /**
     * Creates a table item
     * @param risks
     */
    private void createItem(QuasiIdentifierRisks risks) {
        TableItem item = new TableItem(table, SWT.NONE);
        int idx = 0;
        List<String> list = new ArrayList<String>();
        list.addAll(risks.getIdentifier());
        Collections.sort(list);
        for (String s : list) {
            item.setText(idx++, s);
        }
        item.setText(table.getColumnCount()-3, String.valueOf(risks.getFractionOfUniqueTuples()));
        item.setText(table.getColumnCount()-2, String.valueOf(risks.getHighestReidentificationRisk()));
        item.setText(table.getColumnCount()-1, String.valueOf(risks.getAverageReidentificationRisk()));
        
        items.add(item);
    }

    @Override
    protected void doUpdate(final AnalysisContextRisk context) {

            // Create an analysis
            Analysis analysis = new Analysis(){

            // The statistics builder
            RiskEstimateBuilderInterruptible builder = context.handle.getRiskEstimator(
                                                                     context.context.getModel().getPopulationModel().getModel(),
                                                                     context.context.getContext().definition.getQuasiIdentifyingAttributes()).getInterruptibleInstance();
            
            private boolean  stopped = false;
            private RiskModelAttributes risks;

            @Override
            public void onError() {
                setStatusEmpty();
            }

            @Override
            public void onFinish() {

                if (stopped) {
                    return;
                }

                // Update chart
                if (table != null && !table.isDisposed()) {
                    for (final TableItem i : items) {
                        i.dispose();
                    }
                    items.clear();
                    for (TableColumn c : columns) {
                        c.dispose();
                    }
                    columns.clear();
                    table.dispose();
                }
                

                table = new DynamicTable(root, SWT.SINGLE | SWT.BORDER |
                                               SWT.V_SCROLL | SWT.FULL_SELECTION);
                table.setHeaderVisible(true);
                table.setLinesVisible(true);
                table.setMenu(new ClipboardHandlerTable(table).getMenu());

                double columnsize = (int)(1.0d / (double)(this.risks.getNumIdentifiers()+3) * 100d);
                
                for (int i=0; i<this.risks.getNumIdentifiers(); i++) {
                    DynamicTableColumn c = new DynamicTableColumn(table, SWT.LEFT);
                    c.setWidth(columnsize+"%", "10px"); //$NON-NLS-1$ //$NON-NLS-2$
                    c.setText("Attribute "+i); //$NON-NLS-1$
                    columns.add(c);
                }
                
                DynamicTableColumn c = new DynamicTableColumn(table, SWT.LEFT);
                c.setWidth(columnsize+"%", "10px"); //$NON-NLS-1$ //$NON-NLS-2$
                c.setText("Score"); //$NON-NLS-1$
                columns.add(c);

                c = new DynamicTableColumn(table, SWT.LEFT);
                c.setWidth(columnsize+"%", "10px"); //$NON-NLS-1$ //$NON-NLS-2$
                c.setText("Highest"); //$NON-NLS-1$
                columns.add(c);

                c = new DynamicTableColumn(table, SWT.LEFT);
                c.setWidth(columnsize+"%", "10px"); //$NON-NLS-1$ //$NON-NLS-2$
                c.setText("Avg"); //$NON-NLS-1$
                columns.add(c);
                
                for (final TableColumn col : columns) {
                    col.pack();
                }
                
                // For all sizes
                for (QuasiIdentifierRisks item : risks.getAttributeRisks()) {
                    createItem(item);
                }
                
                setStatusDone();

                root.layout();
                root.redraw();
            }

            @Override
            public void onInterrupt() {
                setStatusWorking();
            }

            @Override
            public void run() throws InterruptedException {
                
                // Timestamp
                long time = System.currentTimeMillis();
                
                // Perform work
                risks = builder.getPopulationBasedAttributeRisks();

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
}
