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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deidentifier.arx.ARXFeatureScaling;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.utility.LayoutUtility.ViewUtilityType;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.nebula.widgets.pagination.IPageLoader;
import org.eclipse.nebula.widgets.pagination.PageableController;
import org.eclipse.nebula.widgets.pagination.collections.PageListHelper;
import org.eclipse.nebula.widgets.pagination.collections.PageResult;
import org.eclipse.nebula.widgets.pagination.table.PageableTable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * This view allows to select a set of attributes for classification analysis
 * 
 * @author Fabian Prasser
 * @author Johanna Eicher
 */
public class ViewStatisticsClassificationAttributes implements IView, ViewStatisticsBasic {

    /**
     * Page loader
     * @author Fabian Prasser
     */
    private class AttributesPageLoader implements IPageLoader<PageResult<String>> {

        @Override
        public PageResult<String> loadPage(PageableController controller) {
            if (state == null || state.attributes == null || state.attributes.isEmpty()) {
                return PageListHelper.createPage(new ArrayList<String>(), controller);
            } else {
                return PageListHelper.createPage(state.attributes, controller);
            }
        }
    }

    /**
     * Internal state management
     * 
     * @author Fabian Prasser
     */
    private class State {

        /** Data */
        private final List<String>               attributes        = new ArrayList<String>();
        /** Data */
        private final Map<String, AttributeType> types             = new HashMap<>();
        /** Data */
        private final Map<String, DataType<?>>   dtypes            = new HashMap<>();
        /** Data */
        private final Set<String>                features          = new HashSet<String>();
        /** Data */
        private final Set<String>                classes           = new HashSet<String>();
        /** Data */
        private final Set<String>                responseVariables = new HashSet<String>();
        /** Data */
        private final Map<String, String>        scaling           = new HashMap<>();

        /**
         * Creates a new instance
         * 
         * @param handle
         * @param definition
         * @param featureScaling 
         */
        private State(DataHandle handle, DataDefinition definition, ARXFeatureScaling featureScaling) {

            for (int col = 0; col < handle.getNumColumns(); col++) {
                String attribute = handle.getAttributeName(col);
                attributes.add(attribute);
                types.put(attribute, definition.getAttributeType(attribute));
                dtypes.put(attribute, definition.getDataType(attribute));
                scaling.put(attribute, featureScaling.getScalingFunction(attribute));
            }
            features.addAll(model.getSelectedFeatures());
            classes.addAll(model.getSelectedClasses());
            responseVariables.addAll(definition.getResponseVariables());
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            State other = (State) obj;
            if (attributes == null) {
                if (other.attributes != null) return false;
            } else if (!attributes.equals(other.attributes)) return false;
            if (classes == null) {
                if (other.classes != null) return false;
            } else if (!classes.equals(other.classes)) return false;
            if (features == null) {
                if (other.features != null) return false;
            } else if (!features.equals(other.features)) return false;
            if (responseVariables == null) {
                if (other.responseVariables != null) return false;
            } else if (!responseVariables.equals(other.responseVariables)) return false;
            if (types == null) {
                if (other.types != null) return false;
            } else if (!types.equals(other.types)) return false;
            if (dtypes == null) {
                if (other.dtypes != null) return false;
            } else if (!dtypes.equals(other.dtypes)) return false;
            if (scaling == null) {
                if (other.scaling != null) return false;
            } else if (!scaling.equals(other.scaling)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
            result = prime * result + ((classes == null) ? 0 : classes.hashCode());
            result = prime * result + ((features == null) ? 0 : features.hashCode());
            result = prime * result + ((responseVariables == null) ? 0 : responseVariables.hashCode());
            result = prime * result + ((types == null) ? 0 : types.hashCode());
            result = prime * result + ((dtypes == null) ? 0 : dtypes.hashCode());
            result = prime * result + ((scaling == null) ? 0 : scaling.hashCode());
            return result;
        }
    }

    /** Label */
    private final String        LABEL_SELECT               = Resources.getMessage("ViewClassificationAttributes.39"); //$NON-NLS-1$
    /** Label */
    private final String        LABEL_DESELECT             = Resources.getMessage("ViewClassificationAttributes.40"); //$NON-NLS-1$
    /** Label */
    private final String        LABEL_SELECT_ALL           = Resources.getMessage("ViewClassificationAttributes.4"); //$NON-NLS-1$
    /** Label */
    private final String        LABEL_SELECT_NONE          = Resources.getMessage("ViewClassificationAttributes.38"); //$NON-NLS-1$
    /** Label */
    private static final String LABEL_CATEGORICAL          = Resources.getMessage("ViewClassificationAttributes.2"); //$NON-NLS-1$
    /** Label */
    private static final String LABEL_FEATURE_SCALING      = Resources.getMessage("ViewClassificationAttributes.41"); //$NON-NLS-1$
    /** Label */
    private static final String LABEL_FEATURE_SCALING_EDIT = Resources.getMessage("ViewClassificationAttributes.42"); //$NON-NLS-1$
    /** Controller */
    private final Controller    controller;
    /** View */
    private final Composite     root;
    /** View */
    private final PageableTable features;
    /** View */
    private final PageableTable classes;
    /** Model */
    private Model               model;
    /** State */
    private State               state;

    /**
     * Creates a new instance.
     * 
     * @param parent
     * @param controller
     */
    public ViewStatisticsClassificationAttributes(final Composite parent,
                                                  final Controller controller) {
        
        controller.addListener(ModelPart.INPUT, this);
        controller.addListener(ModelPart.MODEL, this);
        controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        controller.addListener(ModelPart.ATTRIBUTE_TYPE_BULK_UPDATE, this);
        controller.addListener(ModelPart.DATA_TYPE, this);
        controller.addListener(ModelPart.OUTPUT, this);
        controller.addListener(ModelPart.RESPONSE_VARIABLES, this);
        
        this.controller = controller;

        // Create group
        root = parent;
        root.setLayout(GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(true).create());

        Label label = new Label(parent, SWT.LEFT);
        label.setText(Resources.getMessage("ViewClassificationAttributes.0")); //$NON-NLS-1$
        label.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        label = new Label(parent, SWT.LEFT);
        label.setText(Resources.getMessage("ViewClassificationAttributes.1")); //$NON-NLS-1$
        label.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        
        // Create pageable table
        features = SWTUtil.createPageableTableViewer(parent, SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.BORDER, true, false);
        features.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).span(1, 1).create());
        features.getViewer().setContentProvider(new ArrayContentProvider());
        features.setPageLoader(new AttributesPageLoader());

        // Table
        Table featuresTable = features.getViewer().getTable();
        SWTUtil.createGenericTooltip(featuresTable);
        GridData gd = SWTUtil.createFillGridData();
        gd.heightHint = 100;
        featuresTable.setLayoutData(gd);
        featuresTable.setHeaderVisible(true);
        
        // Menu
        features.getViewer().getTable().addMenuDetectListener(new MenuDetectListener() {
            @Override
            public void menuDetected(MenuDetectEvent e) {
                
                // Check selection
               int index = features.getViewer().getTable().getSelectionIndex();
               if (state == null || index == -1) {
                 return; 
               }
               
               // Create and show context menu
               showFeaturesMenu(features.getViewer(), (String) features.getViewer().getTable().getItem(index).getData()); 
            }
        });

        // Menu
        features.getViewer().getTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent arg0) {

                // Check selection
                int index = features.getViewer().getTable().getSelectionIndex();
                if (arg0.button != 1 || state == null || index == -1) { 
                    return;
                }
                
                // Update
                TableItem item = features.getViewer().getTable().getItem(index);
                String attribute = (String)item.getData();
                if (state.features.contains(attribute)) {
                    state.features.remove(attribute);
                } else {
                    state.features.add(attribute);
                }
                
                // Propagate
                features.refreshPage();
                model.setSelectedFeatures(state.features);
                controller.update(new ModelEvent(ViewStatisticsClassificationAttributes.this, ModelPart.CLASSIFICATION_CONFIGURATION, null));
            }
        });
        
        // Column
        createColumn(features, Resources.getMessage("ViewClassificationAttributes.100"), 50, new ColumnLabelProvider() { //$NON-NLS-1$
            @Override
            public Image getImage(Object element) {
                if (state == null) {
                    return null;
                } else if (state.features.contains(element)){
                    return controller.getResources().getManagedImage("yes.png"); //$NON-NLS-1$
                } else {
                    return controller.getResources().getManagedImage("no.png"); //$NON-NLS-1$
                }
            }
            @Override
            public String getText(Object element) {
                return null;
            }
        });

        // Column
        createColumn(features, Resources.getMessage("ViewClassificationAttributes.103"), 50, new ColumnLabelProvider() { //$NON-NLS-1$
            @Override
            public Image getImage(Object element) {
                if (state == null) {
                    return null;
                } else {
                    String attribute = (String)element;
                    AttributeType type = state.types.get(attribute);
                    return controller.getResources().getImage(type, state.responseVariables.contains(attribute));
                }
            }
            @Override
            public String getText(Object element) {
                return null;
            }
        });

        // Column
        createColumn(features, Resources.getMessage("ViewClassificationAttributes.104"), 100, new ColumnLabelProvider() { //$NON-NLS-1$
            @Override
            public String getText(Object element) {
                if (state == null) {
                    return null;
                } else {
                    return (String)element;
                }
            }
        });
        
        // Column
        createColumn(features, Resources.getMessage("ViewClassificationAttributes.105"), 100, new ColumnLabelProvider() { //$NON-NLS-1$
            @Override
            public String getText(Object element) {
                if (state == null) {
                    return null;
                } else {
                    String function = state.scaling.get((String)element);
                    if (function == null || function.equals("")) { //$NON-NLS-1$
                        function = LABEL_CATEGORICAL;
                    }
                    return function;
                }
            }
        });
        
        // Create pageable table
        classes = SWTUtil.createPageableTableViewer(parent, SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.BORDER, true, false);
        classes.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).span(1, 1).create());
        classes.getViewer().setContentProvider(new ArrayContentProvider());
        classes.setPageLoader(new AttributesPageLoader());

        // Table
        Table classesTable = classes.getViewer().getTable();
        SWTUtil.createGenericTooltip(classesTable);
        gd = SWTUtil.createFillGridData();
        gd.heightHint = 100;
        classesTable.setLayoutData(gd);
        classesTable.setHeaderVisible(true);

        // Menu
        classes.getViewer().getTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent arg0) {

                // Check selection
                int index = classes.getViewer().getTable().getSelectionIndex();
                if (arg0.button != 1 || state == null || index == -1) { 
                    return;
                }
                
                // Update
                TableItem item = classes.getViewer().getTable().getItem(index);
                String attribute = (String)item.getData();
                if (state.classes.contains(attribute)) {
                    state.classes.remove(attribute);
                } else {
                    state.classes.add(attribute);
                }
                
                // Propagate
                classes.refreshPage();
                model.setSelectedClasses(state.classes);
                controller.update(new ModelEvent(ViewStatisticsClassificationAttributes.this, ModelPart.CLASSIFICATION_CONFIGURATION, null));
            }
        });
        
        // Menu
        classes.getViewer().getTable().addMenuDetectListener(new MenuDetectListener() {
            @Override
            public void menuDetected(MenuDetectEvent e) {
                
                // Check selection
               int index = classes.getViewer().getTable().getSelectionIndex();
               if (state == null || index == -1) {
                 return; 
               }
               
               // Create and show context menu
               showClassesMenu(classes.getViewer(), (String) classes.getViewer().getTable().getItem(index).getData()); 
            }
        });

        // Column
        createColumn(classes, Resources.getMessage("ViewClassificationAttributes.106"), 50, new ColumnLabelProvider() { //$NON-NLS-1$
            @Override
            public Image getImage(Object element) {
                if (state == null) {
                    return null;
                } else if (state.classes.contains(element)){
                    return controller.getResources().getManagedImage("yes.png"); //$NON-NLS-1$
                } else {
                    return controller.getResources().getManagedImage("no.png"); //$NON-NLS-1$
                }
            }
            @Override
            public String getText(Object element) {
                return null;
            }
        });

        // Column
        createColumn(classes, Resources.getMessage("ViewClassificationAttributes.109"), 50, new ColumnLabelProvider() { //$NON-NLS-1$
            @Override
            public Image getImage(Object element) {
                if (state == null) {
                    return null;
                } else {
                    String attribute = (String)element;
                    AttributeType type = state.types.get(attribute);
                    return controller.getResources().getImage(type, state.responseVariables.contains(attribute));
                }
            }
            @Override
            public String getText(Object element) {
                return null;
            }
        });

        // Column
        createColumn(classes, Resources.getMessage("ViewClassificationAttributes.104"), 100, new ColumnLabelProvider() { //$NON-NLS-1$
            @Override
            public String getText(Object element) {
                if (state == null) {
                    return null;
                } else {
                    return (String)element;
                }
            }
        });
        
        features.setCurrentPage(0);
        features.refreshPage();
        classes.setCurrentPage(0);
        classes.refreshPage();
    
        // Reset view
        reset();
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    @Override
    public Composite getParent() {
        return this.root;
    }
    
    /**
     * Returns the type
     * @return
     */
    public ViewUtilityType getType() {
        return ViewUtilityType.CLASSIFICATION;
    }
    
    @Override
    public void reset() {
        state = null;
        features.refreshPage();
        features.setCurrentPage(0);
        classes.refreshPage();
        classes.setCurrentPage(0);
        SWTUtil.disable(root);
    }

    @Override
    public void update(final ModelEvent event) {
        if (event.part == ModelPart.MODEL) {
           this.model = (Model) event.data;
           update();
        } else if (event.part == ModelPart.INPUT ||
                   event.part == ModelPart.ATTRIBUTE_TYPE ||
                   event.part == ModelPart.ATTRIBUTE_TYPE_BULK_UPDATE || 
                   event.part == ModelPart.OUTPUT ||
                   event.part == ModelPart.DATA_TYPE ||
                   event.part == ModelPart.RESPONSE_VARIABLES) {
           update();
        }
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
                                           int width,
                                           ColumnLabelProvider provider) {
        
        TableViewerColumn column = new TableViewerColumn(table.getViewer(), SWT.NONE);
        column.setLabelProvider(provider);
        TableColumn tColumn = column.getColumn();
        tColumn.setToolTipText(name);
        tColumn.setText(name);
        tColumn.setWidth(width);
        tColumn.setResizable(true);
        return column;
    }

    /**
     * Shows context menu for classes
     * @param viewer
     * @param data
     */
    private void showClassesMenu(TableViewer viewer, final String data) {

        // Menu
        Menu menu = new Menu(viewer.getTable());
        viewer.getTable().setMenu(menu);
        
        // Item
        MenuItem item = new MenuItem(menu, SWT.NONE);
        item.setText(LABEL_SELECT);
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
               if (state != null && !state.classes.contains(data)) {
                   state.classes.add(data);
                   classes.refreshPage();
                   model.setSelectedClasses(state.classes);
                   controller.update(new ModelEvent(ViewStatisticsClassificationAttributes.this, ModelPart.CLASSIFICATION_CONFIGURATION, null));
               }
            }
        });
        
        // Item
        item = new MenuItem(menu, SWT.NONE);
        item.setText(LABEL_DESELECT);
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                if (state != null && state.classes.contains(data)) {
                    state.classes.remove(data);
                    classes.refreshPage();
                    model.setSelectedClasses(state.classes);
                    controller.update(new ModelEvent(ViewStatisticsClassificationAttributes.this, ModelPart.CLASSIFICATION_CONFIGURATION, null));
                }
            }
        });
        
        // Separate
        new MenuItem(menu, SWT.SEPARATOR);

        // Item
        item = new MenuItem(menu, SWT.NONE);
        item.setText(LABEL_SELECT_ALL);
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
               if (state != null && state.classes.size() != state.attributes.size()) {
                   state.classes.addAll(state.attributes);
                   classes.refreshPage();
                   model.setSelectedClasses(state.classes);
                   controller.update(new ModelEvent(ViewStatisticsClassificationAttributes.this, ModelPart.CLASSIFICATION_CONFIGURATION, null));
               }
            }
        });
        
        // Item
        item = new MenuItem(menu, SWT.NONE);
        item.setText(LABEL_SELECT_NONE);
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                if (state != null && state.classes.size() != 0) {
                    state.classes.clear();
                    classes.refreshPage();
                    model.setSelectedClasses(state.classes);
                    controller.update(new ModelEvent(ViewStatisticsClassificationAttributes.this, ModelPart.CLASSIFICATION_CONFIGURATION, null));
                }
            }
        });
        
        // Show
        menu.setVisible(true);
    }

    /**
     * Shows context menu for features
     * @param viewer
     * @param data
     */
    private void showFeaturesMenu(TableViewer viewer, final String data) {

        // Menu
        Menu menu = new Menu(viewer.getTable());
        viewer.getTable().setMenu(menu);
        
        // Item
        MenuItem item = new MenuItem(menu, SWT.NONE);
        item.setText(LABEL_SELECT);
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
               if (state != null && !state.features.contains(data)) {
                   state.features.add(data);
                   features.refreshPage();
                   model.setSelectedFeatures(state.features);
                   controller.update(new ModelEvent(ViewStatisticsClassificationAttributes.this, ModelPart.CLASSIFICATION_CONFIGURATION, null));
               }
            }
        });
        
        // Item
        item = new MenuItem(menu, SWT.NONE);
        item.setText(LABEL_DESELECT);
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                if (state != null && state.features.contains(data)) {
                    state.features.remove(data);
                    features.refreshPage();
                    model.setSelectedFeatures(state.features);
                    controller.update(new ModelEvent(ViewStatisticsClassificationAttributes.this, ModelPart.CLASSIFICATION_CONFIGURATION, null));
                }
            }
        });
        
        // Separate
        new MenuItem(menu, SWT.SEPARATOR);

        // Item
        item = new MenuItem(menu, SWT.NONE);
        item.setText(LABEL_SELECT_ALL);
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
               if (state != null && state.features.size() != state.attributes.size()) {
                   state.features.addAll(state.attributes);
                   features.refreshPage();
                   model.setSelectedFeatures(state.features);
                   controller.update(new ModelEvent(ViewStatisticsClassificationAttributes.this, ModelPart.CLASSIFICATION_CONFIGURATION, null));
               }
            }
        });
        
        // Item
        item = new MenuItem(menu, SWT.NONE);
        item.setText(LABEL_SELECT_NONE);
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                if (state != null && state.features.size() != 0) {
                    state.features.clear();
                    features.refreshPage();
                    model.setSelectedFeatures(state.features);
                    controller.update(new ModelEvent(ViewStatisticsClassificationAttributes.this, ModelPart.CLASSIFICATION_CONFIGURATION, null));
                }
            }
        });

        // Only for numeric attributes
        boolean enabled = (state != null && state.dtypes.get(data) != DataType.STRING);

        // Separate
        new MenuItem(menu, SWT.SEPARATOR);

        // Submenu
        Menu featureScaling = new Menu(menu);
        featureScaling.setEnabled(enabled);
        item = new MenuItem(menu, SWT.CASCADE);
        item.setText(LABEL_FEATURE_SCALING); // $NON-NLS-1$
        item.setMenu(featureScaling);
        item.setEnabled(enabled);

        // Items
        for (final String _function : new String[] { "x", //$NON-NLS-1$
                                                     "x^2", //$NON-NLS-1$
                                                     "sqrt(x)", //$NON-NLS-1$
                                                     "log(x)", //$NON-NLS-1$
                                                     "2^x", //$NON-NLS-1$
                                                     "1/x", //$NON-NLS-1$
                                                     LABEL_CATEGORICAL }) {

            item = new MenuItem(featureScaling, SWT.NONE);
            item.setText(_function);
            item.setEnabled(enabled);
            item.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent arg0) {
                    String function = model.getClassificationModel().getFeatureScaling().getScalingFunction(data);
                    if ((function == null && _function != null) || !_function.equals(function)) {
                        if (_function.equals(LABEL_CATEGORICAL)) {
                            model.getClassificationModel().setScalingFunction(data, null);
                            state.scaling.put(data, null);
                        } else {
                            model.getClassificationModel().setScalingFunction(data, _function);
                            state.scaling.put(data, _function);
                        }
                        features.refreshPage();
                        controller.update(new ModelEvent(ViewStatisticsClassificationAttributes.this, ModelPart.CLASSIFICATION_CONFIGURATION, null));
                    }
                }
            });
        }

        // Item
        item = new MenuItem(featureScaling, SWT.NONE);
        item.setText(LABEL_FEATURE_SCALING_EDIT);
        item.setEnabled(enabled);
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                if (state != null) {
                    String function = model.getClassificationModel().getFeatureScaling().getScalingFunction(data);
                    if (function == null || function.equals("")) { //$NON-NLS-1$
                        function = LABEL_CATEGORICAL;
                    }
                    String _function = controller.actionShowInputDialog(features.getShell(),
                                                                        Resources.getMessage("ViewClassificationAttributes.113"), //$NON-NLS-1$
                                                                        Resources.getMessage("ViewClassificationAttributes.114"), //$NON-NLS-1$
                                                                        function,
                                                                        new IInputValidator() {
                                                                            @Override
                                                                            public String isValid(String arg0) {
                                                                                return model.getClassificationModel()
                                                                                            .getFeatureScaling()
                                                                                            .isValidScalingFunction(arg0) ||
                                                                                       arg0.equals(LABEL_CATEGORICAL) ? null : Resources.getMessage("ViewClassificationAttributes.115"); //$NON-NLS-1$
                                                                            }
                                                                        });
                    if (!function.equals(_function)) {
                        if (_function == null || function.equals("") || function.equals(LABEL_CATEGORICAL)) { //$NON-NLS-1$
                            model.getClassificationModel().setScalingFunction(data, null);
                            state.scaling.put(data, null);
                        } else {
                            model.getClassificationModel().setScalingFunction(data, function);
                            state.scaling.put(data, function);
                        }
                        features.refreshPage();
                        controller.update(new ModelEvent(ViewStatisticsClassificationAttributes.this,
                                                         ModelPart.CLASSIFICATION_CONFIGURATION,
                                                         null));
                    }
                }
            }
        });
        
        // Show
        menu.setVisible(true);
    }
    
    /**
     * Updates the view.
     * 
     * @param node
     */
    private void update() {

        // Check
        if (model == null || model.getInputConfig() == null || model.getInputConfig().getInput() == null) {
            return;
        }
        
        // Create state
        DataDefinition definition = model.getOutputDefinition() == null ? model.getInputDefinition() : model.getOutputDefinition();
        DataHandle handle = model.getOutput() != null ? model.getOutput() : model.getInputConfig().getInput().getHandle();
        State state = new State(handle, definition, model.getClassificationModel().getFeatureScaling());
        
        // Check again
        if (this.state == null || !this.state.equals(state)) {
            this.state = state;
        } else {
            return;
        }

        // Update
        features.refreshPage();
        classes.refreshPage();
        features.setCurrentPage(0);
        classes.setCurrentPage(0);
        SWTUtil.enable(root);
    }
}