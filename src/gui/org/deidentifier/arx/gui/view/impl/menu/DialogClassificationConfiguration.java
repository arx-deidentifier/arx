package org.deidentifier.arx.gui.view.impl.menu;

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

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.ARXClassificationConfiguration;
import org.deidentifier.arx.aggregates.ClassificationConfigurationLogisticRegression;
import org.deidentifier.arx.aggregates.ClassificationConfigurationLogisticRegression.PriorFunction;
import org.deidentifier.arx.aggregates.ClassificationConfigurationNaiveBayes;
import org.deidentifier.arx.aggregates.ClassificationConfigurationNaiveBayes.Type;
import org.deidentifier.arx.aggregates.ClassificationConfigurationRandomForest;
import org.deidentifier.arx.aggregates.ClassificationConfigurationRandomForest.SplitRule;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.def.IDialog;
import org.eclipse.swt.widgets.Shell;

import de.linearbits.preferences.PreferenceDouble;
import de.linearbits.preferences.PreferenceInteger;
import de.linearbits.preferences.PreferenceSelection;
import de.linearbits.preferences.PreferencesDialog;

/**
 * This class implements a dialog for editing a classification configuration
 *
 * @author Johanna Eicher
 * @author Fabian Prasser
 */
public class DialogClassificationConfiguration implements IDialog {

    /** Window */
    private final PreferencesDialog                 dialog;
    /** Config */
    private final ARXClassificationConfiguration<?> config;

    /**
     * Creates a new instance
     * @param parent
     * @param config
     */
    public DialogClassificationConfiguration(Shell parent,
                                             ARXClassificationConfiguration<?> config) {

        this.dialog = new PreferencesDialog(parent,
                                            Resources.getMessage("DialogClassificationConfiguration.0"), //$NON-NLS-1$
                                            Resources.getMessage("DialogClassificationConfiguration.1")); //$NON-NLS-1$
        
        this.config = config;

        if (config instanceof ClassificationConfigurationLogisticRegression) {
            createContentForLogisticRegression((ClassificationConfigurationLogisticRegression) config);
        } else if (config instanceof ClassificationConfigurationNaiveBayes) {
            createContentForNaiveBayes((ClassificationConfigurationNaiveBayes) config);
        } else if (config instanceof ClassificationConfigurationRandomForest) {
            createContentForRandomForest((ClassificationConfigurationRandomForest) config);
        } else {
            throw new IllegalArgumentException("Unknown classification configuration");
        }
    }

    /**
     * Open the dialog.
     */
    public void open() {
        this.dialog.open();
    }
    
    /**
     * Creates content for logistic regression
     * @param config
     */
    private void createContentForLogisticRegression(final ClassificationConfigurationLogisticRegression config) {
        this.dialog.addCategory(Resources.getMessage("DialogClassificationConfiguration.2")); //$NON-NLS-1$
                  
        // Alpha
        this.dialog.addPreference(new PreferenceDouble(Resources.getMessage("ViewClassificationAttributes.11"), 0d, 1d, ClassificationConfigurationLogisticRegression.DEFAULT_ALPHA) { //$NON-NLS-1$
            protected Double getValue() { return config.getAlpha(); }
            protected void setValue(Object t) { config.setAlpha((Double)t); }});
        
        // Decay exponent
        this.dialog.addPreference(new PreferenceDouble(Resources.getMessage("ViewClassificationAttributes.12"), Double.MIN_VALUE, Double.MAX_VALUE, ClassificationConfigurationLogisticRegression.DEFAULT_DECAY_EXPONENT) { //$NON-NLS-1$
            protected Double getValue() { return config.getDecayExponent(); }
            protected void setValue(Object t) { config.setDecayExponent((Double)t); }});
        
        // Lambda
        this.dialog.addPreference(new PreferenceDouble(Resources.getMessage("ViewClassificationAttributes.14"), 0d, Double.MAX_VALUE, ClassificationConfigurationLogisticRegression.DEFAULT_LAMBDA) { //$NON-NLS-1$
            protected Double getValue() { return config.getLambda(); }
            protected void setValue(Object t) { config.setLambda((Double)t); }});
        
        // Learning rate
        this.dialog.addPreference(new PreferenceDouble(Resources.getMessage("ViewClassificationAttributes.15"), 0d, Double.MAX_VALUE, ClassificationConfigurationLogisticRegression.DEFAULT_LEARNING_RATE) { //$NON-NLS-1$
            protected Double getValue() { return config.getLearningRate(); }
            protected void setValue(Object t) { config.setLearningRate((Double)t); }});
        
        // Prior function
        this.dialog.addPreference(new PreferenceSelection(Resources.getMessage("ViewClassificationAttributes.18"), getPriorFunctions(), ClassificationConfigurationLogisticRegression.DEFAULT_PRIOR.toString()) { //$NON-NLS-1$
            protected String getValue() { return config.getPriorFunction().name(); }
            protected void setValue(Object arg0) { config.setPriorFunction(PriorFunction.valueOf((String)arg0));  }
        });
        
        // Step offset
        this.dialog.addPreference(new PreferenceInteger(Resources.getMessage("ViewClassificationAttributes.20"), 0, Integer.MAX_VALUE, ClassificationConfigurationLogisticRegression.DEFAULT_STEP_OFFSET) { //$NON-NLS-1$
            protected Integer getValue() { return config.getStepOffset(); }
            protected void setValue(Object t) { config.setStepOffset((Integer)t); }});
    }
    
    /**
     * Creates content for naive bayes.
     * @param config
     */
    private void createContentForNaiveBayes(final ClassificationConfigurationNaiveBayes config) {
        this.dialog.addCategory(Resources.getMessage("DialogClassificationConfiguration.3")); //$NON-NLS-1$
        
        // Sigma
        this.dialog.addPreference(new PreferenceDouble(Resources.getMessage("ViewClassificationAttributes.22"), 0d, Double.MAX_VALUE, ClassificationConfigurationNaiveBayes.DEFAULT_SIGMA) { //$NON-NLS-1$
            protected Double getValue() { return config.getSigma(); }
            protected void setValue(Object t) { config.setSigma((Double)t); }});
        
        // Type
        this.dialog.addPreference(new PreferenceSelection(Resources.getMessage("ViewClassificationAttributes.23"), getTypes(), ClassificationConfigurationNaiveBayes.DEFAULT_TYPE.toString()) { //$NON-NLS-1$
            protected String getValue() { return config.getType().name(); }
            protected void setValue(Object arg0) { config.setType(Type.valueOf((String)arg0));  }
        });
    }

    /**
     * Creates content for random forest
     * @param config
     */
    private void createContentForRandomForest(final ClassificationConfigurationRandomForest config) {
        this.dialog.addCategory(Resources.getMessage("DialogClassificationConfiguration.4")); //$NON-NLS-1$
        
        // Number of trees
        this.dialog.addPreference(new PreferenceInteger(Resources.getMessage("ViewClassificationAttributes.24"), 1, Integer.MAX_VALUE, ClassificationConfigurationRandomForest.DEFAULT_NUMBER_OF_TREES) { //$NON-NLS-1$
            protected Integer getValue() { return config.getNumberOfTrees(); }
            protected void setValue(Object t) { config.setNumberOfTrees((Integer)t); }});
        
        // Number of variables to split
        this.dialog.addPreference(new PreferenceInteger(Resources.getMessage("ViewClassificationAttributes.33"), 0, Integer.MAX_VALUE, ClassificationConfigurationRandomForest.DEFAULT_NUMBER_OF_VARIABLES_TO_SPLIT) { //$NON-NLS-1$
            protected Integer getValue() { return config.getNumberOfVariablesToSplit(); }
            protected void setValue(Object t) { config.setNumberOfVariablesToSplit((Integer)t); }});

        // Minimum size of leaf nodes
        this.dialog.addPreference(new PreferenceInteger(Resources.getMessage("ViewClassificationAttributes.34"), 1, Integer.MAX_VALUE, ClassificationConfigurationRandomForest.DEFAULT_MINIMUM_SIZE_OF_LEAF_NODES) { //$NON-NLS-1$
            protected Integer getValue() { return config.getMinimumSizeOfLeafNodes(); }
            protected void setValue(Object t) { config.setMinimumSizeOfLeafNodes((Integer)t); }});

        // Maximum number of leaf nodes
        this.dialog.addPreference(new PreferenceInteger(Resources.getMessage("ViewClassificationAttributes.35"), 2, Integer.MAX_VALUE, ClassificationConfigurationRandomForest.DEFAULT_MAXMIMUM_NUMBER_OF_LEAF_NODES) { //$NON-NLS-1$
            protected Integer getValue() { return config.getMaximumNumberOfLeafNodes(); }
            protected void setValue(Object t) { config.setMaximumNumberOfLeafNodes((Integer)t); }});

        // Subsample
        this.dialog.addPreference(new PreferenceDouble(Resources.getMessage("ViewClassificationAttributes.36"), 0d, 1d, ClassificationConfigurationRandomForest.DEFAULT_SUBSAMPLE) { //$NON-NLS-1$
            protected Double getValue() { return config.getSubsample(); }
            protected void setValue(Object t) { config.setSubsample((Double)t); }});
        
        // Split rule
        this.dialog.addPreference(new PreferenceSelection(Resources.getMessage("ViewClassificationAttributes.37"), getSplitRules(), ClassificationConfigurationRandomForest.DEFAULT_SPLIT_RULE.toString()) { //$NON-NLS-1$
            protected String getValue() { return config.getSplitRule().name(); }
            protected void setValue(Object arg0) { config.setSplitRule(SplitRule.valueOf((String)arg0));  }
        });
    }
    
    /**
     * Creates a list of prior functions
     * @return
     */
    private String[] getPriorFunctions() {
        List<String> result = new ArrayList<String>();
        for (PriorFunction function : PriorFunction.values()) {
            result.add(function.name());
        }
        return result.toArray(new String[result.size()]);
    }
    
    /**
     * Returns a modified configuration, if available, <code>null</code>, otherwise.
     * @return
     */
    public ARXClassificationConfiguration<?> getResult() {
        if (dialog.getReturnCode() == PreferencesDialog.OK) {
            return config;
        } else {
            return null;
        }
    }
    
    /**
     * Creates a list split rules
     * @return
     */
    private String[] getSplitRules() {
        List<String> result = new ArrayList<String>();
        for (SplitRule rule : SplitRule.values()) {
            result.add(rule.name());
        }
        return result.toArray(new String[result.size()]);
    }
    
    /**
     * Creates a list of types
     * @return
     */
    private String[] getTypes() {
        List<String> result = new ArrayList<String>();
        for (Type type : Type.values()) {
            result.add(type.name());
        }
        return result.toArray(new String[result.size()]);
    }
}