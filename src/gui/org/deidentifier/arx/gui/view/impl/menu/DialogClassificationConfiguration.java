package org.deidentifier.arx.gui.view.impl.menu;

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

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.ARXClassificationConfiguration;
import org.deidentifier.arx.aggregates.ClassificationConfigurationLogisticRegression;
import org.deidentifier.arx.aggregates.ClassificationConfigurationLogisticRegression.PriorFunction;
import org.deidentifier.arx.aggregates.ClassificationConfigurationNaiveBayes;
import org.deidentifier.arx.aggregates.ClassificationConfigurationNaiveBayes.Type;
import org.deidentifier.arx.aggregates.ClassificationConfigurationRandomForest;
import org.deidentifier.arx.aggregates.ClassificationConfigurationSVM;
import org.deidentifier.arx.aggregates.ClassificationConfigurationSVM.Kernel;
import org.deidentifier.arx.aggregates.ClassificationConfigurationSVM.MulticlassType;
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
        } else if (config instanceof ClassificationConfigurationSVM) {
            createContentForSVM((ClassificationConfigurationSVM) config);
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
        this.dialog.addPreference(new PreferenceDouble(Resources.getMessage("ViewClassificationAttributes.12"), -1d, 0d, ClassificationConfigurationLogisticRegression.DEFAULT_DECAY_EXPONENT) { //$NON-NLS-1$
            protected Double getValue() { return config.getDecayExponent(); }
            protected void setValue(Object t) { config.setDecayExponent((Double)t); }});
        
        // Lambda
        this.dialog.addPreference(new PreferenceDouble(Resources.getMessage("ViewClassificationAttributes.14"), Double.MAX_VALUE, ClassificationConfigurationLogisticRegression.DEFAULT_LAMBDA) { //$NON-NLS-1$
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
    }
    
    /**
     * Creates content for SVM
     * @param config
     */
    private void createContentForSVM(final ClassificationConfigurationSVM config) {
        this.dialog.addCategory(Resources.getMessage("DialogClassificationConfiguration.5")); //$NON-NLS-1$
        
        // C
        this.dialog.addPreference(new PreferenceDouble(Resources.getMessage("ViewClassificationAttributes.25"), 0d, Double.MAX_VALUE, ClassificationConfigurationSVM.DEFAULT_C) { //$NON-NLS-1$
            protected Double getValue() { return config.getC(); }
            protected void setValue(Object t) { config.setC((Double)t); }});
        
        // Kernel degree
        this.dialog.addPreference(new PreferenceInteger(Resources.getMessage("ViewClassificationAttributes.26"), 0, Integer.MAX_VALUE, ClassificationConfigurationSVM.DEFAULT_KERNEL_DEGREE) { //$NON-NLS-1$
            protected Integer getValue() { return config.getKernelDegree(); }
            protected void setValue(Object t) { config.setKernelDegree((Integer)t); }});

        // Kernel sigma
        this.dialog.addPreference(new PreferenceDouble(Resources.getMessage("ViewClassificationAttributes.27"), 0d, Double.MAX_VALUE, ClassificationConfigurationSVM.DEFAULT_KERNEL_SIGMA) { //$NON-NLS-1$
            protected Double getValue() { return config.getKernelSigma(); }
            protected void setValue(Object t) { config.setKernelSigma((Double)t); }});
        
        // Kernel type
        this.dialog.addPreference(new PreferenceSelection(Resources.getMessage("ViewClassificationAttributes.28"), getKernels(), ClassificationConfigurationSVM.DEFAULT_KERNEL_TYPE.toString()) { //$NON-NLS-1$
            protected String getValue() { return config.getKernelType().name(); }
            protected void setValue(Object arg0) { config.setKernelType(Kernel.valueOf((String)arg0));  }
        });

        // Multiclass type
        this.dialog.addPreference(new PreferenceSelection(Resources.getMessage("ViewClassificationAttributes.29"), getMulticlassTypes(), ClassificationConfigurationSVM.DEFAULT_MULTICLASS_TYPE.toString()) { //$NON-NLS-1$
            protected String getValue() { return config.getMulticlassType().name(); }
            protected void setValue(Object arg0) { config.setMulticlassType(MulticlassType.valueOf((String)arg0));  }
        });
    }
    
    /**
     * Creates a list of kernels
     * @return
     */
    private String[] getKernels() {
        List<String> result = new ArrayList<String>();
        for (Kernel kernel : Kernel.values()) {
            result.add(kernel.name());
        }
        return result.toArray(new String[result.size()]);
    }

    /**
     * Creates a list of multiclass types
     * @return
     */
    private String[] getMulticlassTypes() {
        List<String> result = new ArrayList<String>();
        for (MulticlassType multiclassType : MulticlassType.values()) {
            result.add(multiclassType.name());
        }
        return result.toArray(new String[result.size()]);
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