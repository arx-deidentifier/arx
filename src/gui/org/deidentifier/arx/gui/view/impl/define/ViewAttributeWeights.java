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

package org.deidentifier.arx.gui.view.impl.define;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.common.DelayedChangeListener;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.nebula.widgets.pagination.IPageLoader;
import org.eclipse.nebula.widgets.pagination.PageableController;
import org.eclipse.nebula.widgets.pagination.collections.PageListHelper;
import org.eclipse.nebula.widgets.pagination.collections.PageResult;
import org.eclipse.nebula.widgets.pagination.table.PageableTable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import de.linearbits.swt.widgets.Knob;
import de.linearbits.swt.widgets.KnobColorProfile;
import de.linearbits.swt.widgets.KnobRange;

/**
 * This class allows to define weights for attributes.
 *
 * @author Fabian Prasser
 */
public class ViewAttributeWeights implements IView {

    /**
     * Page loader
     * @author Fabian Prasser
     */
    private class AttributePageLoader implements IPageLoader<PageResult<String>> {

        @Override
        public PageResult<String> loadPage(PageableController controller) {
            if (sortedAttributes == null) {
                return PageListHelper.createPage(new ArrayList<String>(), controller);
            } else {
                return PageListHelper.createPage(sortedAttributes, controller);
            }
        }
    }

    /** Constant */
    private static final int        MIN_SPACE        = 60;

    /** Constant */
    private static final int        MIN_KNOB         = 30;

    /** Controller. */
    private Controller              controller       = null;

    /** Model. */
    private Model                   model            = null;

    /** Model */
    private List<String>            sortedAttributes = new ArrayList<String>();

    /** Model. */
    private final Set<String>       attributes       = new HashSet<String>();

    /** View. */
    private Composite               panel            = null;

    /** View. */
    private final ScrolledComposite root;

    /** Color profile */
    private final KnobColorProfile  defaultColorProfile;

    /** Color profile */
    private final KnobColorProfile  focusedColorProfile;
    
    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     */
    public ViewAttributeWeights(final Composite parent, final Controller controller) {

        // Register
        this.controller = controller;
        this.controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        this.controller.addListener(ModelPart.ATTRIBUTE_TYPE_BULK_UPDATE, this);
        this.controller.addListener(ModelPart.MODEL, this);
        this.controller.addListener(ModelPart.INPUT, this);
        
        // Color profiles
        this.defaultColorProfile = KnobColorProfile.createDefaultSystemProfile(parent.getDisplay());
        this.focusedColorProfile = KnobColorProfile.createFocusedBlueRedProfile(parent.getDisplay());
        
        this.root = new ScrolledComposite(parent, SWT.H_SCROLL);
        this.root.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent arg0) {
                if (defaultColorProfile != null && !defaultColorProfile.isDisposed()) {
                    defaultColorProfile.dispose();
                }
                if (focusedColorProfile != null && !focusedColorProfile.isDisposed()) {
                    focusedColorProfile.dispose();
                }
            }
        });
        this.root.getHorizontalBar().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				root.redraw();
			}
		});
        this.root.pack();
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    @Override
    public void reset() {
        root.setRedraw(false);
        if (panel != null && !panel.isDisposed()) {
            panel.dispose();
            panel = null;
        }
        attributes.clear();
        sortedAttributes.clear();
        root.setRedraw(true);
    }

    @Override
    public void update(ModelEvent event) {

        if (event.part == ModelPart.MODEL) {
            this.model = (Model)event.data;
        } 
        
        if (event.part == ModelPart.MODEL ||
            event.part == ModelPart.INPUT) {
            reset();
        } 
        
        if (event.part == ModelPart.ATTRIBUTE_TYPE ||
            event.part == ModelPart.ATTRIBUTE_TYPE_BULK_UPDATE ||
            event.part == ModelPart.MODEL) {
            if (model != null) {
                updateControls();
            }
        }
    }

    /**
     * Creates a new column
     * @param table
     * @param name
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
        tColumn.setWidth(200);
        tColumn.setResizable(true);
        return column;
    }

    /**
     * Updates the controls
     */
    private void updateControls() {

        // Create ordered list of QIs
        DataDefinition definition = model.getInputDefinition();
        if (definition != null) {
            Set<String> _qis = definition.getQuasiIdentifyingAttributes();
            
            // Break if nothing has changed
            if (this.attributes.equals(_qis)) {
                return;
            }
            
            this.sortedAttributes = new ArrayList<String>();
            DataHandle handle = model.getInputConfig().getInput().getHandle();
            for (int i=0; i<handle.getNumColumns(); i++){
                String attr = handle.getAttributeName(i);
                if (_qis.contains(attr)){
                    sortedAttributes.add(attr);
                }
            }
            this.attributes.clear();
            this.attributes.addAll(sortedAttributes);
        }

        // Check
        if (root == null || root.isDisposed()) {
            return;
        }
        
        root.setRedraw(false);
        
        // Dispose widgets
        if (panel != null) {
            panel.dispose();
            panel = null;
        }

        // Create new widget
        panel = new Composite(root, SWT.NONE);
        panel.setLayoutData(GridDataFactory.swtDefaults().grab(true, true).align(SWT.FILL, SWT.CENTER).create());
        
        // For handling high-dimensional data
        final int MAX_KNOBS = 32;
        
        // High-dimensional setting
        if (sortedAttributes.size() > MAX_KNOBS) {

            // Create layout
            panel.setLayout(SWTUtil.createGridLayout(1, true));

            // Viewer
            final PageableTable paginationTable = SWTUtil.createPageableTableViewer(panel, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE | SWT.V_SCROLL | SWT.VIRTUAL, true, false);
            paginationTable.getViewer().setContentProvider(new ArrayContentProvider());
            paginationTable.setPageLoader(new AttributePageLoader());
            paginationTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

            // Table
            Table table = paginationTable.getViewer().getTable();
            SWTUtil.createGenericTooltip(table);
            GridData gd = SWTUtil.createFillGridData();
            gd.heightHint = 100;
            table.setLayoutData(gd);
            table.setHeaderVisible(true);

            // Columns
            createColumn(paginationTable, Resources.getMessage("ViewAttributeWeights.1"), new ColumnLabelProvider() { //$NON-NLS-1$
                @Override
                public String getText(Object element) {
                    return (String) element;
                }
            });
            createColumn(paginationTable, Resources.getMessage("ViewAttributeWeights.2"), new ColumnLabelProvider() { //$NON-NLS-1$
                @Override
                public String getText(Object element) {
                    return SWTUtil.getPrettyString(model.getInputConfig().getAttributeWeight((String) element));
                }
            });

            // Context menu
            paginationTable.getViewer().getTable().addMenuDetectListener(new MenuDetectListener() {
                @Override
                public void menuDetected(MenuDetectEvent e) {
                    // Check selection
                    final Table table = paginationTable.getViewer().getTable();
                    int index = table.getSelectionIndex();
                    if (index == -1) { return; }

                    // Create and show context menu
                    final String attribute = (String) table.getItem(index).getData();
                    final String weight = String.valueOf(model.getInputConfig().getAttributeWeight(attribute));
                    Menu menu = new Menu(table);
                    table.setMenu(menu);

                    MenuItem item = new MenuItem(menu, SWT.NONE);
                    item.setText(Resources.getMessage("ViewAttributeWeights.3")); //$NON-NLS-1$
                    item.addSelectionListener(new SelectionAdapter() {
                        @Override
                        public void widgetSelected(SelectionEvent arg0) {
                            String result = controller.actionShowInputDialog(table.getShell(),
                                                                             Resources.getMessage("ViewAttributeWeights.5"), //$NON-NLS-1$
                                                                             Resources.getMessage("ViewAttributeWeights.4") + attribute, //$NON-NLS-1$
                                                                             weight);
                            if (result != null) {
                                double _weight = -1d;
                                try {
                                    _weight = Double.valueOf(result);
                                } catch (Exception e) {
                                    // Ignore
                                }
                                if (_weight >= 0d && _weight <= 1d) {
                                    model.getInputConfig().setAttributeWeight(attribute, _weight);
                                    paginationTable.getViewer().update(attribute, null);
                                }
                            }
                        }
                    });
                }
            });
            
            // Update
            paginationTable.setCurrentPage(0);
            
        // Low-dimensional setting
        } else {
                
            // Create layout
            panel.setLayout(GridLayoutFactory.swtDefaults().numColumns(sortedAttributes.size()).margins(0, 0).equalWidth(true).create());
            
            // Create composites
            List<Composite> composites = new ArrayList<Composite>();
            for (int i = 0; i < sortedAttributes.size(); i++) {
                Composite c = new Composite(panel, SWT.NONE);
                c.setLayoutData(GridDataFactory.fillDefaults()
                                               .grab(true, true)
                                               .align(SWT.FILL, SWT.CENTER)
                                               .create());
                c.setLayout(GridLayoutFactory.swtDefaults().numColumns(1).margins(2, 0).create());
                composites.add(c);
            }
            
            // Create labels
            for (int i = 0; i < sortedAttributes.size(); i++) {
                Label label = new Label(composites.get(i), SWT.CENTER);
                label.setText(sortedAttributes.get(i));
                label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
            }
            
            // Create knob widgets
            List<Knob<Double>> knobs = new ArrayList<Knob<Double>>();
            for (int i = 0; i < sortedAttributes.size(); i++) {
                Knob<Double> knob = new Knob<Double>(composites.get(i),
                                                     SWT.NULL,
                                                     new KnobRange.Double(0d, 1d));
                knob.setLayoutData(GridDataFactory.swtDefaults()
                                                  .grab(false, false)
                                                  .align(SWT.CENTER, SWT.CENTER)
                                                  .hint(MIN_KNOB, MIN_KNOB)
                                                  .create());
                knob.setDefaultColorProfile(defaultColorProfile);
                knob.setFocusedColorProfile(focusedColorProfile);
                knobs.add(knob);
            }
    
            // Create labels
            for (int i = 0; i < sortedAttributes.size(); i++) {
                
                final Label label = new Label(composites.get(i), SWT.CENTER);
                label.setText("0.0"); //$NON-NLS-1$
                label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
                
                final String attribute = sortedAttributes.get(i);
                final Knob<Double> knob = knobs.get(i);
                knob.addSelectionListener(new SelectionAdapter(){
                    public void widgetSelected(SelectionEvent arg0) {
                        
                        double value = knob.getValue();
                        label.setText(SWTUtil.getPrettyString(value));
                        label.setToolTipText(String.valueOf(value));
                        
                        try {
                            
                            // Correctly indicate weights slightly > 0
                            double parsedValue = Double.valueOf(SWTUtil.getPrettyString(value)).doubleValue();
                            if (parsedValue == 0d && value > 0d) {
                                label.setText(">0"); //$NON-NLS-1$
                            }
                            
                            // Correctly indicate weights slightly < 1
                            if (parsedValue == 1d && value < 1d) {
                                label.setText("<1"); //$NON-NLS-1$
                            }
                            
                        } catch (Exception e) {
                            // Drop silently
                        }
                        
                        if (model != null && model.getInputConfig() != null) {
                            model.getInputConfig().setAttributeWeight(attribute, value);
                        }
                    }
                });
                knob.addSelectionListener(new DelayedChangeListener(100) {
                    @Override public void delayedEvent() {
                        controller.update(new ModelEvent(ViewAttributeWeights.this, ModelPart.ATTRIBUTE_WEIGHT, model.getInputConfig().getAttributeWeight(attribute)));
                    }
                });
            }
            
            // Set values
            for(int i=0; i<sortedAttributes.size(); i++){
                if (model != null && model.getInputConfig() != null) {
                    knobs.get(i).setValue(model.getInputConfig().getAttributeWeight(sortedAttributes.get(i)));
                }
            }
        }

        // Update root composite
        root.setContent(panel);
        root.setExpandHorizontal(true);
        root.setExpandVertical(true);
        if (sortedAttributes.size() <= MAX_KNOBS) {
            root.setMinWidth(MIN_SPACE * sortedAttributes.size());
        }
        root.setVisible(!sortedAttributes.isEmpty());
        root.layout(true, true);    
        root.setRedraw(true);
        root.redraw();
    }
}
