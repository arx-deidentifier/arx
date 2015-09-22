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

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.model.ModelRisk.ViewRiskType;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.common.ClipboardHandlerTable;
import org.deidentifier.arx.gui.view.impl.common.ComponentStatusLabelProgressProvider;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContext;
import org.deidentifier.arx.risk.hipaa.SafeHarborValidator;
import org.deidentifier.arx.risk.hipaa.Warning;
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
public class ViewSafeHarborAttributesTable extends ViewRisks<AnalysisContextRisk> {
    
    /** View */
    private Composite root;
    
    /** View */
    private DynamicTable table;
    
    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     * @param target
     * @param reset
     */
    public ViewSafeHarborAttributesTable(final Composite parent,
                                         final Controller controller,
                                         final ModelPart target,
                                         final ModelPart reset) {
                                         
        super(parent, controller, target, reset);
        controller.addListener(ModelPart.INPUT, this);
    }
    
    @Override
    public void update(ModelEvent event) {
        super.update(event);
        if (event.part == ModelPart.INPUT) {
            triggerUpdate();
        }
    }
    
    /**
     * Creates a table item
     * @param risks
     */
    private void createItem(Warning identifier) {
        final TableItem item = new TableItem(table, SWT.NONE);
        item.setText(0, String.valueOf(identifier.getColumn()));
        item.setText(1, identifier.getIdentifier().toString());
        item.setText(2, identifier.getValue());
    }
    
    @Override
    protected Control createControl(Composite parent) {
        
        this.root = new Composite(parent, SWT.NONE);
        this.root.setLayout(new FillLayout());
        
        table = SWTUtil.createTableDynamic(root, SWT.SINGLE | SWT.BORDER |
                                                 SWT.V_SCROLL | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setMenu(new ClipboardHandlerTable(table).getMenu());
        
        DynamicTableColumn c = new DynamicTableColumn(table, SWT.LEFT);
        c.setWidth("30%"); //$NON-NLS-1$ //$NON-NLS-2$
        c.setText(Resources.getMessage("RiskAnalysis.27")); //$NON-NLS-1$
        c.setResizable(true);
        c = new DynamicTableColumn(table, SWT.LEFT);
        c.setWidth("30%"); //$NON-NLS-1$ //$NON-NLS-2$
        c.setText(Resources.getMessage("RiskAnalysis.28")); //$NON-NLS-1$
        c.setResizable(true);
        c = new DynamicTableColumn(table, SWT.LEFT);
        c.setWidth("30%"); //$NON-NLS-1$ //$NON-NLS-2$
        c.setText(Resources.getMessage("RiskAnalysis.29")); //$NON-NLS-1$
        c.setResizable(true);
        for (final TableColumn col : table.getColumns()) {
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
        table.setRedraw(false);
        for (final TableItem i : table.getItems()) {
            i.dispose();
        }
        table.setRedraw(true);
        setStatusEmpty();
    }
    
    @Override
    protected void doUpdate(final AnalysisContextRisk context) {
        setStatusDone();
        
        final Warning[] identifiers = SafeHarborValidator.validate(context.handle);
        
        // Update chart
        for (final TableItem i : table.getItems()) {
            i.dispose();
        }
        
        // For all identifiers
        for (Warning item : identifiers) {
            createItem(item);
        }
        
        for (final TableColumn col : table.getColumns()) {
            col.pack();
        }
        
        table.layout();
        table.redraw();
        setStatusDone();

    }
    
    @Override
    protected ComponentStatusLabelProgressProvider getProgressProvider() {
        return new ComponentStatusLabelProgressProvider() {
            public int getProgress() {
                return 100;
            }
        };
    }
    
    @Override
    protected ViewRiskType getViewType() {
        return ViewRiskType.ATTRIBUTES;
    }
    
    @Override
    protected boolean isRunning() {
        return false;
    }
}
