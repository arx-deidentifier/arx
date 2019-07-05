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
package org.deidentifier.arx.gui.view.impl.utility;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;
import org.deidentifier.arx.aggregates.StatisticsBuilderInterruptible;
import org.deidentifier.arx.aggregates.StatisticsQuality;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.common.ClipboardHandlerTable;
import org.deidentifier.arx.gui.view.impl.common.ComponentStatusLabelProgressProvider;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledSeparator;
import org.deidentifier.arx.gui.view.impl.common.async.Analysis;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContext;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.nebula.widgets.pagination.IPageLoader;
import org.eclipse.nebula.widgets.pagination.PageableController;
import org.eclipse.nebula.widgets.pagination.collections.PageListHelper;
import org.eclipse.nebula.widgets.pagination.collections.PageResult;
import org.eclipse.nebula.widgets.pagination.table.PageableTable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import de.linearbits.swt.table.DynamicTable;
import de.linearbits.swt.table.DynamicTableColumn;

/**
 * This view displays results from different quality models
 *
 * @author Fabian Prasser
 */
public class ViewStatisticsQuality extends ViewStatistics<AnalysisContextQuality> {

    /**
     * Page loader
     * @author Fabian Prasser
     */
    private class QualityPageLoader implements IPageLoader<PageResult<Pair<String, StatisticsQuality>>> {

        @Override
        public PageResult<Pair<String, StatisticsQuality>> loadPage(PageableController controller) {
            if (data == null || data.getAttributes().isEmpty()) {
                return PageListHelper.createPage(new ArrayList<Pair<String, StatisticsQuality>>(), controller);
            } else {
                List<Pair<String, StatisticsQuality>> list = new ArrayList<>();
                for (String attribute : data.getAttributes()) {
                    list.add(new Pair<>(attribute, data));
                }
                return PageListHelper.createPage(list, controller);
            }
        }
    }

    /** Model */
    private StatisticsQuality data;

    /** View */
    private Composite         root;

    /** View */
    private SashForm          sash;

    /** View */
    private PageableTable     table;

    /** View */
    private DynamicTable      table2;

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
    public ViewStatisticsQuality(final Composite parent,
                                 final Controller controller,
                                 final ModelPart target,
                                 final ModelPart reset) {
        
        super(parent, controller, target, reset, false);
        this.manager = new AnalysisManager(parent.getDisplay());
    }
    
    @Override
    public LayoutUtility.ViewUtilityType getType() {
        return LayoutUtility.ViewUtilityType.QUALITY_MODELS;
    }

    /**
     * Creates a new column
     * @param table
     * @param name
     * @param width
     * @param provider
     */
    private TableViewerColumn createColumn(PageableTable table,
                                           String name, 
                                           ColumnLabelProvider provider) {
        
        TableViewerColumn column = new TableViewerColumn(table.getViewer(), SWT.NONE);
        column.setLabelProvider(provider);
        TableColumn tColumn = column.getColumn();
        tColumn.setToolTipText(name);
        tColumn.setText(name);
        tColumn.setWidth(30);
        tColumn.setResizable(false);
        return column;
    }

    /**
     * Second set of columns
     * @return
     */
    private List<String> getColumns2() {
        List<String> result = new ArrayList<>();
        result.add(Resources.getMessage("ViewStatisticsQuality.6")); //$NON-NLS-1$
        result.add(Resources.getMessage("ViewStatisticsQuality.7")); //$NON-NLS-1$
        return result;
    }

    /**
     * Returns a formatted string for the value
     * @param item
     * @param index
     * @param value
     * @return
     */
    private void setNumericValueAtIndex(TableItem item, int index, double value) {
        if (Double.isNaN(value)) {
            item.setText(index, Resources.getMessage("ViewStatisticsQuality.8")); //$NON-NLS-1$
        } else {
            item.setData(String.valueOf(index), value);
        }
    }

    @Override
    protected Control createControl(Composite parent) {
        
        // Root
        this.root = new Composite(parent, SWT.NONE);
        this.root.setLayout(new FillLayout());
        
        // Sash
        this.sash = new SashForm(this.root, SWT.VERTICAL);

        // Upper
        Composite upper = new Composite(sash, SWT.NONE);
        upper.setLayout(SWTUtil.createGridLayout(1));
        ComponentTitledSeparator separator1 = new ComponentTitledSeparator(upper, SWT.NONE);
        separator1.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        separator1.setText(Resources.getMessage("ViewStatisticsQuality.9")); //$NON-NLS-1$
        
        // Add to upper
        this.table = SWTUtil.createPageableTableViewer(upper, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION, true, true);
        this.table.setLayoutData(SWTUtil.createFillGridData());
        this.table.getViewer().setContentProvider(new ArrayContentProvider());
        this.table.setPageLoader(new QualityPageLoader());

        // Table
        Table tTable = this.table.getViewer().getTable();
        SWTUtil.createGenericTooltip(tTable);
        GridData gd = SWTUtil.createFillGridData();
        gd.heightHint = 100;
        tTable.setLayoutData(gd);
        tTable.setHeaderVisible(true);
        tTable.setMenu(new ClipboardHandlerTable(tTable).getMenu());
        
        // Create column: attribute
        createColumn(this.table, Resources.getMessage("ViewStatisticsQuality.0"), //$NON-NLS-1$
                     new ColumnLabelProvider() {
                        @Override
                        @SuppressWarnings("unchecked")
                        public String getText(Object element) {
                            Pair<String, StatisticsQuality> data = (Pair<String, StatisticsQuality>)element;
                            return data.getFirst();
                        }            
        });
        // Create column: data type
        createColumn(this.table, Resources.getMessage("ViewStatisticsQuality.1"), //$NON-NLS-1$
                     new ColumnLabelProvider() {
                        @Override
                        @SuppressWarnings("unchecked")
                        public String getText(Object element) {
                            Pair<String, StatisticsQuality> data = (Pair<String, StatisticsQuality>)element;
                            return data.getSecond().getDataType(data.getFirst()).getDescription().getLabel();
                        }            
        });
        // Create column: missings
        createColumn(this.table, Resources.getMessage("ViewStatisticsQuality.2"), //$NON-NLS-1$
                     new ColumnLabelProvider() {
                        @Override
                        @SuppressWarnings("unchecked")
                        public String getText(Object element) {
                            Pair<String, StatisticsQuality> data = (Pair<String, StatisticsQuality>)element;
                            return SWTUtil.getPrettyString(data.getSecond().getMissings().getValue(data.getFirst()) * 100d) + "%"; //$NON-NLS-1$
                        }            
        });
        
        // Output only
        if (getTarget() == ModelPart.OUTPUT) {
            // Create column
            createColumn(this.table, Resources.getMessage("ViewStatisticsQuality.3"), //$NON-NLS-1$
                         new ColumnLabelProvider() {
                            @Override
                            @SuppressWarnings("unchecked")
                            public String getText(Object element) {
                                Pair<String, StatisticsQuality> data = (Pair<String, StatisticsQuality>)element;
                                return SWTUtil.getPrettyString(data.getSecond().getGeneralizationIntensity().getValue(data.getFirst()) * 100d) + "%"; //$NON-NLS-1$
                            }            
            });
            // Create column
            createColumn(this.table, Resources.getMessage("ViewStatisticsQuality.4"), //$NON-NLS-1$
                         new ColumnLabelProvider() {
                            @Override
                            @SuppressWarnings("unchecked")
                            public String getText(Object element) {
                                Pair<String, StatisticsQuality> data = (Pair<String, StatisticsQuality>)element;
                                return SWTUtil.getPrettyString(data.getSecond().getGranularity().getValue(data.getFirst()) * 100d) + "%"; //$NON-NLS-1$
                            }            
            });
            // Create column
            createColumn(this.table, Resources.getMessage("ViewStatisticsQuality.5"), //$NON-NLS-1$
                         new ColumnLabelProvider() {
                            @Override
                            @SuppressWarnings("unchecked")
                            public String getText(Object element) {
                                Pair<String, StatisticsQuality> data = (Pair<String, StatisticsQuality>)element;
                                return SWTUtil.getPrettyString(data.getSecond().getNonUniformEntropy().getValue(data.getFirst()) * 100d) + "%"; //$NON-NLS-1$
                            }            
            });
            // Create column
            createColumn(this.table, Resources.getMessage("ViewStatisticsQuality.18"), //$NON-NLS-1$
                         new ColumnLabelProvider() {
                            @Override
                            @SuppressWarnings("unchecked")
                            public String getText(Object element) {
                                Pair<String, StatisticsQuality> data = (Pair<String, StatisticsQuality>)element;
                                return SWTUtil.getPrettyString(data.getSecond().getAttributeLevelSquaredError().getValue(data.getFirst()) * 100d) + "%"; //$NON-NLS-1$
                            }            
            });
        }
        
        // Init
        this.table.setCurrentPage(0);
        this.table.refreshPage();
        
        // Pack
        for (final TableColumn col : table.getViewer().getTable().getColumns()) {
            col.pack();
        }
        SWTUtil.createGenericTooltip(table.getViewer().getTable());

        // Add to lower
        if (getTarget() == ModelPart.OUTPUT) {
    
            // Lower
            Composite lower = new Composite(sash, SWT.NONE);
            lower.setLayout(SWTUtil.createGridLayout(1));
            ComponentTitledSeparator separator2 = new ComponentTitledSeparator(lower, SWT.NONE);
            separator2.setLayoutData(SWTUtil.createFillHorizontallyGridData());
            separator2.setText(Resources.getMessage("ViewStatisticsQuality.10")); //$NON-NLS-1$
            
            // Create table
            this.table2 = SWTUtil.createTableDynamic(lower, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
            this.table2.setHeaderVisible(true);
            this.table2.setLinesVisible(true);
            this.table2.setMenu(new ClipboardHandlerTable(table2).getMenu());
            this.table2.setLayoutData(SWTUtil.createFillGridData());
            
            // Create columns
            List<String> columns2 = getColumns2();
            int perc2 = (int)Math.floor(100d / (double)columns2.size());
            for (String column : columns2) {
                DynamicTableColumn c = new DynamicTableColumn(this.table2, SWT.LEFT);
                c.setWidth(perc2 + "%", "30px"); //$NON-NLS-1$ //$NON-NLS-2$
                c.setText(column); //$NON-NLS-1$
                SWTUtil.createColumnWithBarCharts(table2, c);
            }

            // Pack
            for (final TableColumn col : table2.getColumns()) {
                col.pack();
            }
            SWTUtil.createGenericTooltip(table2);
        }
        
        // Configure & return
        if (getTarget() == ModelPart.OUTPUT) {
            sash.setWeights(new int[] {2, 1});
        } else {
            sash.setWeights(new int[] {2});
        }
        this.root.layout();
        this.root.pack();
        return this.root;
    }

    @Override
    protected AnalysisContextQuality createViewConfig(AnalysisContext context) {
        return new AnalysisContextQuality(context);
    }

    @Override
    protected void doReset() {
        if (this.manager != null) {
            this.manager.stop();
        }
        data = null;
        table.setCurrentPage(0);
        table.refreshPage();
        if (table2 != null) {
            table2.setRedraw(false);
            for (final TableItem i : table2.getItems()) {
                i.dispose();
            }
            table2.setRedraw(true);
        }
        setStatusEmpty();
    }

    @Override
    protected void doUpdate(AnalysisContextQuality context) {

        // The statistics builder
        final StatisticsBuilderInterruptible builder = context.handle.getStatistics().getInterruptibleInstance();
        
        // Create an analysis
        Analysis analysis = new Analysis(){
            
            private boolean           stopped = false;
            private StatisticsQuality quality;

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

                // Check
                if (stopped || !isEnabled()) {
                    return;
                }

                // Update
                data = quality;
                table.setCurrentPage(0);
                table.refreshPage();
                
                // Lower table
                if (table2 != null) {

                    // Prepare
                    table2.setRedraw(false);
                    
                    // Clear
                    for (final TableItem i : table2.getItems()) {
                        i.dispose();
                    }
                    
                    TableItem item = new TableItem(table2, SWT.NONE);
                    item.setText(0, Resources.getMessage("ViewStatisticsQuality.11")); //$NON-NLS-1$
                    setNumericValueAtIndex(item, 1, quality.getGeneralizationIntensity().getArithmeticMean(false));
                    
                    item = new TableItem(table2, SWT.NONE);
                    item.setText(0, Resources.getMessage("ViewStatisticsQuality.12")); //$NON-NLS-1$
                    setNumericValueAtIndex(item, 1, quality.getGranularity().getArithmeticMean(false));
                    
                    item = new TableItem(table2, SWT.NONE);
                    item.setText(0, Resources.getMessage("ViewStatisticsQuality.13")); //$NON-NLS-1$
                    setNumericValueAtIndex(item, 1, quality.getNonUniformEntropy().getArithmeticMean(false));
                    
                    item = new TableItem(table2, SWT.NONE);
                    item.setText(0, Resources.getMessage("ViewStatisticsQuality.14")); //$NON-NLS-1$
                    setNumericValueAtIndex(item, 1, quality.getDiscernibility().getValue());
                    
                    item = new TableItem(table2, SWT.NONE);
                    item.setText(0, Resources.getMessage("ViewStatisticsQuality.15")); //$NON-NLS-1$
                    setNumericValueAtIndex(item, 1, quality.getAverageClassSize().getValue());
                    
                    item = new TableItem(table2, SWT.NONE);
                    item.setText(0, Resources.getMessage("ViewStatisticsQuality.17")); //$NON-NLS-1$
                    setNumericValueAtIndex(item, 1, quality.getRecordLevelSquaredError().getValue());

                    item = new TableItem(table2, SWT.NONE);
                    item.setText(0, Resources.getMessage("ViewStatisticsQuality.19")); //$NON-NLS-1$
                    setNumericValueAtIndex(item, 1, quality.getAttributeLevelSquaredError().getArithmeticMean(false));

                    item = new TableItem(table2, SWT.NONE);
                    item.setText(0, Resources.getMessage("ViewStatisticsQuality.20")); //$NON-NLS-1$
                    setNumericValueAtIndex(item, 1, quality.getSSESST().getValue());
                    
                    // Done
                    table2.setRedraw(true);
                    table2.layout();
                }
                
                // Dynamic weights
                if (getTarget() == ModelPart.OUTPUT) {
                    
                    int upperWeight = table.getViewer().getTable().getItemCount();
                    int lowerWeight = table2.getItemCount();
                    if (upperWeight > 2 * lowerWeight) {
                        upperWeight = 2;
                        lowerWeight = 1;
                    }                     
                    sash.setWeights(new int[] {upperWeight, lowerWeight});
                } else {
                    sash.setWeights(new int[] {2});
                }
                
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
                
                // Perform work
                this.quality = builder.getQualityStatistics();

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