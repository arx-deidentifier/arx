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
import org.deidentifier.arx.risk.HIPAAIdentifierMatch;
import org.deidentifier.arx.risk.RiskEstimateBuilderInterruptible;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import de.linearbits.swt.table.DynamicTable;
import de.linearbits.swt.table.DynamicTableColumn;

/**
 * This view displays the identified Safe Harbor identifier.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class ViewAttributesHIPAAIdentifiersTable extends ViewAttributes<AnalysisContextAttributes> {

    /** View */
    private Composite       root;

    /** View */
    private DynamicTable    table;

    /** Internal stuff. */
    private AnalysisManager manager;
    
    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     * @param target
     * @param reset
     */
    public ViewAttributesHIPAAIdentifiersTable(final Composite parent,
                                    final Controller controller,
                                    final ModelPart target,
                                    final ModelPart reset) {
                                    
        super(parent, controller, target, reset);
        this.manager = new AnalysisManager(parent.getDisplay());
    }
    
    @Override
    public void update(ModelEvent event) {
        super.update(event);
        if (event.part == ModelPart.ATTRIBUTE_TYPE) {
            triggerUpdate();
        }
    }
    
    /**
     * Creates a table item
     * @param risks
     */
    private void createItem(HIPAAIdentifierMatch identifier) {
        final TableItem item = new TableItem(table, SWT.NONE);
        item.setText(0, identifier.getColumn());
        item.setText(1, format(identifier.getIdentifier().toString()));
        item.setText(2, identifier.getInstance());
        item.setText(3, format(identifier.getMatchType().toString()));
        if (identifier.getConfidence() != null) {
            item.setData("4", identifier.getConfidence());
        } else {
            item.setText(4, identifier.getValue());
        }
    }
    
    /**
     * Converts the given value
     * @param value
     * @return
     */
    private String format(String value) {
        value = value.toLowerCase().replace("_", " ");
        value = value.substring(0, 1).toUpperCase() + value.substring(1);
        return value;
    }
    
    @Override
    protected Control createControl(Composite parent) {
        
        this.root = new Composite(parent, SWT.NONE);
        this.root.setLayout(new FillLayout());
        
        table = SWTUtil.createTableDynamic(root, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setMenu(new ClipboardHandlerTable(table).getMenu());
        
        DynamicTableColumn c = new DynamicTableColumn(table, SWT.LEFT);
        c.setWidth("20%%"); //$NON-NLS-1$
        c.setText(Resources.getMessage("RiskAnalysis.27")); //$NON-NLS-1$
        c.setResizable(true);
        c = new DynamicTableColumn(table, SWT.LEFT);
        c.setWidth("20%"); //$NON-NLS-1$
        c.setText(Resources.getMessage("RiskAnalysis.28")); //$NON-NLS-1$
        c.setResizable(true);
        c = new DynamicTableColumn(table, SWT.LEFT);
        c.setWidth("20%%"); //$NON-NLS-1$
        c.setText(Resources.getMessage("RiskAnalysis.31")); //$NON-NLS-1$
        c.setResizable(true);
        c = new DynamicTableColumn(table, SWT.LEFT);
        c.setWidth("20%"); //$NON-NLS-1$
        c.setText(Resources.getMessage("RiskAnalysis.29")); //$NON-NLS-1$
        c.setResizable(true);
        c = new DynamicTableColumn(table, SWT.LEFT);
        SWTUtil.createColumnWithBarCharts(table, c);
        c.setWidth("20%"); //$NON-NLS-1$
        c.setText(Resources.getMessage("RiskAnalysis.30")); //$NON-NLS-1$
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
            
            private boolean stopped = false;
            private HIPAAIdentifierMatch[] matches;
            
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
                
                // Check
                if (stopped || !isEnabled()) {
                    return;
                }

                // Disable drawing
                table.setRedraw(false);
                
                // Update chart
                for (final TableItem i : table.getItems()) {
                    i.dispose();
                }
                
                // For all identifiers
                for (HIPAAIdentifierMatch item : matches) {
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

                // Set status
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
                matches = builder.getHIPAAIdentifiers();
                
                // Our users are patient
                while (System.currentTimeMillis() - time < MINIMAL_WORKING_TIME && !stopped) {
                    Thread.sleep(10);
                }
            }
            
            @Override
            public void stop() {
                if (builder != null)
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
    protected ViewAttributesType getViewType() {
        return ViewAttributesType.HIPAA_ATTRIBUTES;
    }
    
    /**
     * Is an analysis running
     */
    protected boolean isRunning() {
        return manager != null && manager.isRunning();
    }
}
