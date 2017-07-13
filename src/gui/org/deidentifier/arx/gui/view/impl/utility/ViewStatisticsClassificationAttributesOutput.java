/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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


import org.deidentifier.arx.ARXClassificationConfiguration;
import org.deidentifier.arx.ARXLogisticRegressionConfiguration;
import org.deidentifier.arx.ARXNaiveBayesConfiguration;
import org.deidentifier.arx.ARXRandomForestConfiguration;
import org.deidentifier.arx.ARXSVMConfiguration;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.utility.LayoutUtility.ViewUtilityType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import de.linearbits.swt.table.DynamicTable;
import de.linearbits.swt.table.DynamicTableColumn;

/**
 * This view shows a hint message regarding attribute selection for classification analysis
 * 
 * @author Johanna Eicher
 */
public class ViewStatisticsClassificationAttributesOutput implements IView, ViewStatisticsBasic {

    /** View */
    private final Composite  root;
    /** Controller */
    private final Controller controller;
    /** Model */
    private Model            model;
    /** Widget */
    private Combo            combo;
    /** View */
    private DynamicTable     table;

    /**
     * Creates a new instance.
     * 
     * @param parent
     * @param controller
     */
    public ViewStatisticsClassificationAttributesOutput(final Composite parent,
                                    final Controller controller) {
        
        this.controller = controller;
        this.controller.addListener(ModelPart.MODEL, this);
        
        this.root = parent;
        this.root.setLayout(SWTUtil.createGridLayout(1));
        
        // Composite for classifier combo
        final Composite composite = new Composite(this.root, SWT.NONE);
        composite.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        composite.setLayout(SWTUtil.createGridLayout(3, false));
        final Label label = new Label(composite, SWT.PUSH);
        label.setText(Resources.getMessage("ViewClassificationAttributes.4")); //$NON-NLS-1$
        // Combo for selecting classifier
        this.combo = new Combo(composite, SWT.READ_ONLY);
        this.combo.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        this.combo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                if (combo.getSelectionIndex() >= 0) {
                    String type = combo.getItem(combo.getSelectionIndex());
                    ARXClassificationConfiguration configCurrent = getClassificationConfig(type);
                    model.getClassificationModel().setCurrentConfiguration(configCurrent);
                    updateTable(configCurrent);
                    controller.update(new ModelEvent(ViewStatisticsClassificationAttributesOutput.this,
                                                     ModelPart.STATISTICAL_CLASSIFIER,
                                                     null));
                }
            }
        });
        // Button to prompt edit dialog
        Button button = new Button(composite, SWT.PUSH);
        button.setText(Resources.getMessage("ViewClassificationAttributes.30")); //$NON-NLS-1$
        button.setToolTipText(Resources.getMessage("ViewClassificationAttributes.31")); //$NON-NLS-1$
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
               controller.actionShowClassificationConfigurationDialog();
            }
        });

        // Table for displaying classifier parameters/values
        this.table = SWTUtil.createTableDynamic(root, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
        this.table.setLayoutData(SWTUtil.createFillGridData(2));
        this.table.setLinesVisible(true);
        this.table.setHeaderVisible(true);
        DynamicTableColumn c = new DynamicTableColumn(this.table, SWT.LEFT);
        c.setWidth("50%", "100px");
        c.setText(Resources.getMessage("ViewClassificationAttributes.5")); //$NON-NLS-1$
        c = new DynamicTableColumn(this.table, SWT.LEFT);
        c.setWidth("50%", "100px");
        c.setText(Resources.getMessage("ViewClassificationAttributes.6")); //$NON-NLS-1$
        for (final TableColumn col : this.table.getColumns()) {
            col.pack();
        }
        SWTUtil.createGenericTooltip(this.table);
    }
    
    /**
     * Creates table items for each parameter of a logistic regression
     * configuration.
     * 
     * @param config
     */
    private void createItemsForLogisticRegression(ARXLogisticRegressionConfiguration config) {
        TableItem item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.11")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getAlpha()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.12")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getDecayExponent()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.13")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.isDeterministic()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.14")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getLambda()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.15")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getLearningRate()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.16")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getMaxRecords()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.17")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getNumFolds()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.18")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getPriorFunction()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.19")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getSeed()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.20")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getStepOffset()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.21")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getVectorLength()));
    }
    

    /**
     * Creates table items for each parameter of a naive bayes configuration
     * 
     * @param config
     */
    private void createItemsForNaiveBayes(ARXNaiveBayesConfiguration config) {
        TableItem item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.13")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.isDeterministic()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.16")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getMaxRecords()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.17")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getNumFolds()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.19")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getSeed()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.22")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getSigma()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.23")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getType()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.21")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getVectorLength()));
    }

    /**
     * Creates table items for each parameter of a random forest configuration
     * 
     * @param config
     */
    private void createItemsForRandomForest(ARXRandomForestConfiguration config) {
        TableItem item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.13")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.isDeterministic()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.16")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getMaxRecords()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.17")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getNumFolds()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.24")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getNumberOfTrees()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.19")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getSeed()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.21")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getVectorLength()));
    }

    /**
     * Creates table items for each parameter of a SVM configuration
     * 
     * @param config
     */
    private void createItemsForSVM(ARXSVMConfiguration config) {
        TableItem item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.25")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getC()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.13")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.isDeterministic()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.26")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getKernelDegree()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.27")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getKernelSigma()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.28")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getKernelType()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.16")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getMaxRecords()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.29")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getMulticlassType()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.17")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getNumFolds()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.19")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getSeed()));

        item = new TableItem(this.table, SWT.NONE);
        item.setText(0, Resources.getMessage("ViewClassificationAttributes.21")); //$NON-NLS-1$
        item.setText(1, SWTUtil.getPrettyString(config.getVectorLength()));
    }

    @Override
    public void dispose() {
        this.controller.removeListener(this);
    }

    /**
     * Return the classification configuration for this type.
     * @param type
     * @return
     */
    private ARXClassificationConfiguration getClassificationConfig(String type) {
        if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException("Unknown statistical classifier " + type);
        }
        // Logistic regression
        if (type.equals(Resources.getMessage("ViewClassificationAttributes.7"))) {  //$NON-NLS-1$
            return this.model.getClassificationModel().getLogisticRegressionConfiguration();
        }
        // Naive bayes
        if (type.equals(Resources.getMessage("ViewClassificationAttributes.8"))) {  //$NON-NLS-1$
            return this.model.getClassificationModel().getNaiveBayesConfiguration();
        }
        // Random forest
        if (type.equals(Resources.getMessage("ViewClassificationAttributes.9"))) {  //$NON-NLS-1$
            return this.model.getClassificationModel().getRandomForestConfiguration();
        }
        // SVM
        if (type.equals(Resources.getMessage("ViewClassificationAttributes.10"))) {  //$NON-NLS-1$
            return this.model.getClassificationModel().getSVMConfiguration();
        }
        return null;
    }
    
    /**
     * Return label for classifier based on configuration
     * 
     * @param config
     * @return
     */
    private String getLabel(ARXClassificationConfiguration config) {
        // Logistic regression
        if (config instanceof ARXLogisticRegressionConfiguration) { return Resources.getMessage("ViewClassificationAttributes.7"); }
        // Naive Bayes
        if (config instanceof ARXNaiveBayesConfiguration) { return Resources.getMessage("ViewClassificationAttributes.8"); }
        // Random forest
        if (config instanceof ARXRandomForestConfiguration) { return Resources.getMessage("ViewClassificationAttributes.9"); }
        // SVM
        if (config instanceof ARXSVMConfiguration) { return Resources.getMessage("ViewClassificationAttributes.10"); }
        return null;
    }

    @Override
    public Composite getParent() {
        return this.root;
    }

    /**
     * Returns the type
     * 
     * @return
     */
    public ViewUtilityType getType() {
        return ViewUtilityType.CLASSIFICATION;
    }
    
    @Override
    public void reset() {
        for(TableItem item : this.table.getItems()){
            item.dispose();
        }
        this.table.removeAll();
        SWTUtil.disable(this.root);
    }

    @Override
    public void update(ModelEvent event) {
        if (event.part == ModelPart.MODEL) {
            this.model = (Model) event.data;
            // Create items
            final String[] classifiers = new String[4];
            classifiers[0] = getLabel(this.model.getClassificationModel().getLogisticRegressionConfiguration());
            classifiers[1] = getLabel(this.model.getClassificationModel().getNaiveBayesConfiguration());
            classifiers[2] = getLabel(this.model.getClassificationModel().getRandomForestConfiguration());
            classifiers[3] = getLabel(this.model.getClassificationModel().getSVMConfiguration());
            this.combo.setItems(classifiers);
            // Select LR by default
            this.combo.select(0);
            updateTable(this.model.getClassificationModel().getLogisticRegressionConfiguration());
        }
    }

    /**
     * Updates the view.
     */
    private void updateTable(ARXClassificationConfiguration config){
        // Check
        if (this.model == null || config == null) {
            return;
        }
        
        // Clear
        this.root.setRedraw(false);
        for(TableItem item : this.table.getItems()){
            item.dispose();
        }
        
        // Create items based on config
        if (config instanceof ARXLogisticRegressionConfiguration) {
            createItemsForLogisticRegression((ARXLogisticRegressionConfiguration) config);
        } else if (config instanceof ARXNaiveBayesConfiguration) {
            createItemsForNaiveBayes((ARXNaiveBayesConfiguration) config);
        } else if (config instanceof ARXRandomForestConfiguration) {
            createItemsForRandomForest((ARXRandomForestConfiguration) config);
        } else if (config instanceof ARXSVMConfiguration) {
            createItemsForSVM((ARXSVMConfiguration) config);
        }
        
        this.root.setRedraw(true);
        SWTUtil.enable(this.root);
    }
}
