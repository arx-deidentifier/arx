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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.Pair;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.common.ClipboardHandlerTable;
import org.deidentifier.arx.risk.RiskEstimator;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.mihalis.opal.dynamictablecolumns.DynamicTable;
import org.mihalis.opal.dynamictablecolumns.DynamicTableColumn;

/**
 * This view displays information about the quasi identifiers
 * 
 * @author Fabian Prasser
 */
public class ViewQuasiIdentifierRisks implements IView {

    /** Controller */
    private final Controller         controller;

    /** View */
    private final Composite          root;

    /** View */
    private DynamicTable             table;

    /** View */
    private List<TableItem>          items    = new ArrayList<TableItem>();

    /** View */
    private List<TableColumn>        columns  = new ArrayList<TableColumn>();

    /** Model */
    private Model                    model    = null;

    /** Model */
    private int                      size     = 0;

    /** Model */
    private Set<Set<String>>         powerset = new HashSet<Set<String>>();

    /** Model */
    private Map<Set<String>, Double> scores   = new HashMap<Set<String>, Double>();
    
    /**
     * Creates a new instance.
     * 
     * @param parent
     * @param controller
     */
    public ViewQuasiIdentifierRisks(final Composite parent,
                                  final Controller controller) {

        controller.addListener(ModelPart.INPUT, this);
        controller.addListener(ModelPart.MODEL, this);
        this.controller = controller;

        // Create group
        root = parent;
        root.setLayout(GridLayoutFactory.swtDefaults().numColumns(1).create());

        create(root);
        reset();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.arx.gui.view.def.IView#dispose()
     */
    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.arx.gui.view.def.IView#reset()
     */
    @Override
    public void reset() {
        powerset.clear();
        scores.clear();
        size = 0;
        if (table != null) {
            table.setRedraw(false);
            for (final TableItem i : items) {
                i.dispose();
            }
            table.setRedraw(true);
            table.dispose();
        }
        items.clear();
        columns.clear();
        SWTUtil.disable(root);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deidentifier.arx.gui.view.def.IView#update(org.deidentifier.arx.gui
     * .model.ModelEvent)
     */
    @Override
    public void update(final ModelEvent event) {
        if (event.part == ModelPart.MODEL) {
            this.model = (Model) event.data;
        } else if (event.part == ModelPart.INPUT) {
            if (model != null && model.getInputConfig() != null &&
                model.getInputConfig().getInput() != null) {
                
                DataHandle handle = model.getInputConfig().getInput().getHandle();
                Set<String> attrs = new HashSet<String>();
                for (int i=0; i<handle.getNumColumns(); i++) {
                    attrs.add(handle.getAttributeName(i));
                }
                this.powerset = powerSet(attrs);
                this.size = attrs.size();
                
                this.scores.clear();
                int count = 0;
                for (Set<String> set : powerset) {
                    
                    System.out.println((double)(count++) / (double) powerset.size() * 100d + "%");
                    
                    if (!set.isEmpty()) {
                        
                        RiskEstimator estimator = model.getInputConfig()
                                                 .getInput()
                                                 .getHandle()
                                                 .getRiskEstimator(set);
                        double score = 0d;
                        if (estimator.getSampleUniquesRisk() != 0d) {
                            score = estimator.getPopulationUniquesRisk();
                        }
                        scores.put(set, score);
                    }
                }
                update();
            } else {
                reset();
            }
        }
    }

    /**
     * Creates the required controls.
     * 
     * @param parent
     */
    private void create(final Composite parent) {
//
//        table = new DynamicTable(parent, SWT.SINGLE | SWT.BORDER |
//                                         SWT.V_SCROLL | SWT.FULL_SELECTION);
//        table.setHeaderVisible(true);
//        table.setLinesVisible(true);
//        final GridData gdata = SWTUtil.createFillGridData();
//        table.setLayoutData(gdata);
//        table.setMenu(new ClipboardHandlerTable(table).getMenu());
//
//        DynamicTableColumn c = new DynamicTableColumn(table, SWT.LEFT);
//        c.setWidth("50%", "100px"); //$NON-NLS-1$ //$NON-NLS-2$
//        c.setText(Resources.getMessage("ViewSampleDistribution.6")); //$NON-NLS-1$
//        columns.add(c);
//        c = new DynamicTableColumn(table, SWT.LEFT);
//        c.setWidth("50%", "100px"); //$NON-NLS-1$ //$NON-NLS-2$
//        c.setText(Resources.getMessage("ViewSampleDistribution.7")); //$NON-NLS-1$
//        columns.add(c);
//        for (final TableColumn col : columns) {
//            col.pack();
//        }
    }

    /**
     * Updates the view.
     * 
     * @param node
     */
    private void update() {
        
        if (table != null) {
            table.setRedraw(false);
            for (final TableItem i : items) {
                i.dispose();
            }
            table.setRedraw(true);
            items.clear();
            columns.clear();
            table.dispose();
        }
        

        table = new DynamicTable(root, SWT.SINGLE | SWT.BORDER |
                                       SWT.V_SCROLL | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        final GridData gdata = SWTUtil.createFillGridData();
        table.setLayoutData(gdata);
        table.setMenu(new ClipboardHandlerTable(table).getMenu());

        double columnsize = (int)(1.0d / (double)size * 100d);
        
        for (int i=0; i<size; i++) {
            DynamicTableColumn c = new DynamicTableColumn(table, SWT.LEFT);
            c.setWidth(columnsize+"%", "10px"); //$NON-NLS-1$ //$NON-NLS-2$
            c.setText("Attribute "+i); //$NON-NLS-1$
            columns.add(c);
        }
        
        DynamicTableColumn c = new DynamicTableColumn(table, SWT.LEFT);
        c.setWidth(columnsize+"%", "10px"); //$NON-NLS-1$ //$NON-NLS-2$
        c.setText("Score"); //$NON-NLS-1$
        columns.add(c);
        
        for (final TableColumn col : columns) {
            col.pack();
        }

        table.setRedraw(false);
        
        // For all sizes
        for (int i = 1; i <= size; i++) {
            
            System.out.println((double)i / (double) size * 100d + "%");
            
            List<Pair<Set<String>, Double>> list = new ArrayList<Pair<Set<String>, Double>>();
            
            Set<Set<String>> elements = new HashSet<Set<String>>();
            for (Set<String> set : powerset) {
                if (set.size() == i && !elements.contains(set)) {
                    double score = 0d;
                    double count = 0;
                    for (Set<String> other : powerset) {
                        if (other.containsAll(set)) {
                            score += scores.get(other);
                            count ++;
                        }
                    }
                    score /= count;
                    list.add(new Pair<Set<String>, Double>(set, score));
                }
            }
            
            Collections.sort(list, new Comparator<Pair<Set<String>, Double>>(){
                public int compare(Pair<Set<String>, Double> o1,
                                   Pair<Set<String>, Double> o2) {
                    return -o1.getSecond().compareTo(o2.getSecond());
                }
            });
            
            for (Pair<Set<String>, Double> entry : list) {
                createItem(entry.getFirst(), entry.getSecond());
            }
        }
        
        table.setRedraw(true);
        table.redraw();
        SWTUtil.enable(table);
    }
    
    /**
     * Creates a table item
     * @param label
     * @param value
     */
    private void createItem(Set<String> label, double value) {
        TableItem item = new TableItem(table, SWT.NONE);
        int idx = 0;
        List<String> list = new ArrayList<String>();
        list.addAll(label);
        Collections.sort(list);
        for (String s : list) {
            
            item.setText(idx++, s);
        }
        item.setText(columns.size()-1, String.valueOf(value));
        items.add(item);
    }
    
    /**
     * Returns the power set
     * @param originalSet
     * @return
     */
    public static <T> Set<Set<T>> powerSet(Set<T> originalSet) {
        Set<Set<T>> sets = new HashSet<Set<T>>();
        if (originalSet.isEmpty()) {
            sets.add(new HashSet<T>());
            return sets;
        }
        List<T> list = new ArrayList<T>(originalSet);
        T head = list.get(0);
        Set<T> rest = new HashSet<T>(list.subList(1, list.size())); 
        for (Set<T> set : powerSet(rest)) {
            Set<T> newSet = new HashSet<T>();
            newSet.add(head);
            newSet.addAll(set);
            sets.add(newSet);
            sets.add(set);
        }       
        return sets;
    }
}
