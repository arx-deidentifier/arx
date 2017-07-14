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

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.ARXClassificationConfiguration;
import org.deidentifier.arx.ARXLogisticRegressionConfiguration;
import org.deidentifier.arx.ARXLogisticRegressionConfiguration.PriorFunction;
import org.deidentifier.arx.ARXNaiveBayesConfiguration;
import org.deidentifier.arx.ARXNaiveBayesConfiguration.Type;
import org.deidentifier.arx.ARXRandomForestConfiguration;
import org.deidentifier.arx.ARXSVMConfiguration;
import org.deidentifier.arx.ARXSVMConfiguration.Kernel;
import org.deidentifier.arx.ARXSVMConfiguration.MulticlassType;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.resources.Resources;
import org.eclipse.swt.widgets.Shell;

import de.linearbits.preferences.PreferenceBoolean;
import de.linearbits.preferences.PreferenceDouble;
import de.linearbits.preferences.PreferenceInteger;
import de.linearbits.preferences.PreferenceSelection;
import de.linearbits.preferences.PreferencesDialog;

/**
 * This class implements a dialog for editing a classification configuration
 *
 * @author Johanna Eicher
 */
public class DialogClassificationConfiguration extends PreferencesDialog {

    /** Model */
    private final Model             model;

    /**
     * Creates an instance.
     * 
     * @param parent
     * @param model
     */
    public DialogClassificationConfiguration(Shell parent,
                                             Model model) {
        super(parent,
              Resources.getMessage("DialogClassificationConfiguration.0"), //$NON-NLS-1$
              Resources.getMessage("DialogClassificationConfiguration.1")); //$NON-NLS-1$
        this.model = model;
        ARXClassificationConfiguration config = this.model.getClassificationModel().getCurrentConfiguration();
        
        if (config instanceof ARXLogisticRegressionConfiguration) {
            createContentForLogisticRegression((ARXLogisticRegressionConfiguration) config);
        } else if (config instanceof ARXNaiveBayesConfiguration) {
            createContentForNaiveBayes((ARXNaiveBayesConfiguration) config);
        } else if (config instanceof ARXRandomForestConfiguration) {
            createContentForRandomForest((ARXRandomForestConfiguration) config);
        } else if (config instanceof ARXSVMConfiguration) {
            createContentForSVM((ARXSVMConfiguration) config);
        } else {
            throw new IllegalArgumentException("Unknown classification configuration");
        }
    }

    /**
     * Create content for logistic regression.
     * @param config
     */
    private void createContentForLogisticRegression(ARXLogisticRegressionConfiguration config) {
        this.addCategory(Resources.getMessage("DialogClassificationConfiguration.2")); //$NON-NLS-1$
                  
        // Alpha
        this.addPreference(new PreferenceDouble(Resources.getMessage("ViewClassificationAttributes.11")) { //$NON-NLS-1$
            protected Double getValue() { return config.getAlpha(); }
            protected void setValue(Object t) { config.setAlpha((Double) t); }});
        
        // Decay exponent
        this.addPreference(new PreferenceDouble(Resources.getMessage("ViewClassificationAttributes.12")) { //$NON-NLS-1$
            protected Double getValue() { return config.getDecayExponent(); }
            protected void setValue(Object t) { config.setDecayExponent((Double) t); }});
        
        // Deterministic
        this.addPreference(new PreferenceBoolean(Resources.getMessage("ViewClassificationAttributes.13")) { //$NON-NLS-1$
            protected Boolean getValue() { return config.isDeterministic(); }
            protected void setValue(Object t) { config.setDeterministic((Boolean) t); }});
        
        // Lambda
        this.addPreference(new PreferenceDouble(Resources.getMessage("ViewClassificationAttributes.14")) { //$NON-NLS-1$
            protected Double getValue() { return config.getLambda(); }
            protected void setValue(Object t) { config.setLambda((Double) t); }});
        
        // Learning rate
        this.addPreference(new PreferenceDouble(Resources.getMessage("ViewClassificationAttributes.15")) { //$NON-NLS-1$
            protected Double getValue() { return config.getLearningRate(); }
            protected void setValue(Object t) { config.setLearningRate((Double) t); }});
        
        // Maximal number of records
        this.addPreference(new PreferenceInteger(Resources.getMessage("ViewClassificationAttributes.16")) { //$NON-NLS-1$
            protected Integer getValue() { return config.getMaxRecords(); }
            protected void setValue(Object t) { config.setMaxRecords((Integer) t); }});

        // Number of folds
        this.addPreference(new PreferenceInteger(Resources.getMessage("ViewClassificationAttributes.17")) { //$NON-NLS-1$
            protected Integer getValue() { return config.getNumFolds(); }
            protected void setValue(Object t) { config.setNumFolds((Integer) t); }});
        
        // Prior function
        this.addPreference(new PreferenceSelection(Resources.getMessage("ViewClassificationAttributes.18"), getPriorFunctions()) { //$NON-NLS-1$
            protected String getValue() { return config.getPriorFunction().name(); }
            protected void setValue(Object arg0) { config.setPriorFunction(PriorFunction.valueOf((String) arg0)); }
        });
        
        // Seed
        this.addPreference(new PreferenceInteger(Resources.getMessage("ViewClassificationAttributes.19")) { //$NON-NLS-1$
            protected Integer getValue() { return config.getSeed(); }
            protected void setValue(Object t) { config.setSeed((Integer) t); }});
        
        // Step offset
        this.addPreference(new PreferenceInteger(Resources.getMessage("ViewClassificationAttributes.20")) { //$NON-NLS-1$
            protected Integer getValue() { return config.getStepOffset(); }
            protected void setValue(Object t) { config.setStepOffset((Integer) t); }});

        // Vector length
        this.addPreference(new PreferenceInteger(Resources.getMessage("ViewClassificationAttributes.21")) { //$NON-NLS-1$
            protected Integer getValue() { return config.getVectorLength(); }
            protected void setValue(Object t) { config.setVectorLength((Integer) t); }});
    }

    /**
     * Create content for naive bayes.
     * @param config
     */
    private void createContentForNaiveBayes(ARXNaiveBayesConfiguration config) {
        this.addCategory(Resources.getMessage("DialogClassificationConfiguration.3")); //$NON-NLS-1$
        
        // Deterministic
        this.addPreference(new PreferenceBoolean(Resources.getMessage("ViewClassificationAttributes.13")) { //$NON-NLS-1$
            protected Boolean getValue() { return config.isDeterministic(); }
            protected void setValue(Object t) { config.setDeterministic((Boolean) t); }});
        
        // Maximal number of records
        this.addPreference(new PreferenceInteger(Resources.getMessage("ViewClassificationAttributes.16")) { //$NON-NLS-1$
            protected Integer getValue() { return config.getMaxRecords(); }
            protected void setValue(Object t) { config.setMaxRecords((Integer) t); }});
        
        // Number of folds
        this.addPreference(new PreferenceInteger(Resources.getMessage("ViewClassificationAttributes.17")) { //$NON-NLS-1$
            protected Integer getValue() { return config.getNumFolds(); }
            protected void setValue(Object t) { config.setNumFolds((Integer) t); }});
        
        // Seed
        this.addPreference(new PreferenceInteger(Resources.getMessage("ViewClassificationAttributes.19")) { //$NON-NLS-1$
            protected Integer getValue() { return config.getSeed(); }
            protected void setValue(Object t) { config.setSeed((Integer) t); }});
        
        // Sigma
        this.addPreference(new PreferenceDouble(Resources.getMessage("ViewClassificationAttributes.22")) { //$NON-NLS-1$
            protected Double getValue() { return config.getSigma(); }
            protected void setValue(Object t) { config.setSigma((Double) t); }});
        
        // Type
        this.addPreference(new PreferenceSelection(Resources.getMessage("ViewClassificationAttributes.18"), getTypes()) { //$NON-NLS-1$
            protected String getValue() { return config.getType().name(); }
            protected void setValue(Object arg0) { config.setType(Type.valueOf((String) arg0)); }
        });
        
        // Vector length
        this.addPreference(new PreferenceInteger(Resources.getMessage("ViewClassificationAttributes.21")) { //$NON-NLS-1$
            protected Integer getValue() { return config.getVectorLength(); }
            protected void setValue(Object t) { config.setVectorLength((Integer) t); }});
        
    }

    /**
     * Create content for random forest.
     * @param config
     */
    private void createContentForRandomForest(ARXRandomForestConfiguration config) {
        this.addCategory(Resources.getMessage("DialogClassificationConfiguration.4")); //$NON-NLS-1$
        
        // Deterministic
        this.addPreference(new PreferenceBoolean(Resources.getMessage("ViewClassificationAttributes.13")) { //$NON-NLS-1$
            protected Boolean getValue() { return config.isDeterministic(); }
            protected void setValue(Object t) { config.setDeterministic((Boolean) t); }});
        
        // Maximal number of records
        this.addPreference(new PreferenceInteger(Resources.getMessage("ViewClassificationAttributes.16")) { //$NON-NLS-1$
            protected Integer getValue() { return config.getMaxRecords(); }
            protected void setValue(Object t) { config.setMaxRecords((Integer) t); }});
        
        // Number of folds
        this.addPreference(new PreferenceInteger(Resources.getMessage("ViewClassificationAttributes.17")) { //$NON-NLS-1$
            protected Integer getValue() { return config.getNumFolds(); }
            protected void setValue(Object t) { config.setNumFolds((Integer) t); }});
        
        // Number of trees
        this.addPreference(new PreferenceInteger(Resources.getMessage("ViewClassificationAttributes.24")) { //$NON-NLS-1$
            protected Integer getValue() { return config.getNumberOfTrees(); }
            protected void setValue(Object t) { config.setNumberOfTrees((Integer) t); }});
        
        // Seed
        this.addPreference(new PreferenceInteger(Resources.getMessage("ViewClassificationAttributes.19")) { //$NON-NLS-1$
            protected Integer getValue() { return config.getSeed(); }
            protected void setValue(Object t) { config.setSeed((Integer) t); }});
        
        // Vector length
        this.addPreference(new PreferenceInteger(Resources.getMessage("ViewClassificationAttributes.21")) { //$NON-NLS-1$
            protected Integer getValue() { return config.getVectorLength(); }
            protected void setValue(Object t) { config.setVectorLength((Integer) t); }});
        
    }

    /**
     * Create content for SVM.
     * @param config
     */
    private void createContentForSVM(ARXSVMConfiguration config) {
        this.addCategory(Resources.getMessage("DialogClassificationConfiguration.5")); //$NON-NLS-1$
        
        // C
        this.addPreference(new PreferenceDouble(Resources.getMessage("ViewClassificationAttributes.25")) { //$NON-NLS-1$
            protected Double getValue() { return config.getC(); }
            protected void setValue(Object t) { config.setC((Double) t); }});
        
        // Deterministic
        this.addPreference(new PreferenceBoolean(Resources.getMessage("ViewClassificationAttributes.13")) { //$NON-NLS-1$
            protected Boolean getValue() { return config.isDeterministic(); }
            protected void setValue(Object t) { config.setDeterministic((Boolean) t); }});
        
        // Kernel degree
        this.addPreference(new PreferenceInteger(Resources.getMessage("ViewClassificationAttributes.26")) { //$NON-NLS-1$
            protected Integer getValue() { return config.getKernelDegree(); }
            protected void setValue(Object t) { config.setKernelDegree((Integer) t); }});
        
        // Kernel sigma
        this.addPreference(new PreferenceDouble(Resources.getMessage("ViewClassificationAttributes.27")) { //$NON-NLS-1$
            protected Double getValue() { return config.getKernelSigma(); }
            protected void setValue(Object t) { config.setKernelSigma((Double) t); }});
        
        // Kernel type
        this.addPreference(new PreferenceSelection(Resources.getMessage("ViewClassificationAttributes.18"), getKernelTypes()) { //$NON-NLS-1$
            protected String getValue() { return config.getKernelType().name(); }
            protected void setValue(Object arg0) { config.setKernelType(Kernel.valueOf((String) arg0)); }
        });
        
        // Maximal number of records
        this.addPreference(new PreferenceInteger(Resources.getMessage("ViewClassificationAttributes.16")) { //$NON-NLS-1$
            protected Integer getValue() { return config.getMaxRecords(); }
            protected void setValue(Object t) { config.setMaxRecords((Integer) t); }});

        // Multiclass type
        this.addPreference(new PreferenceSelection(Resources.getMessage("ViewClassificationAttributes.18"), getMulticlassTypes()) { //$NON-NLS-1$
            protected String getValue() { return config.getMulticlassType().name(); }
            protected void setValue(Object arg0) { config.setMulticlassType(MulticlassType.valueOf((String) arg0)); }
        });
        
        // Number of folds
        this.addPreference(new PreferenceInteger(Resources.getMessage("ViewClassificationAttributes.17")) { //$NON-NLS-1$
            protected Integer getValue() { return config.getNumFolds(); }
            protected void setValue(Object t) { config.setNumFolds((Integer) t); }});
        
        // Seed
        this.addPreference(new PreferenceInteger(Resources.getMessage("ViewClassificationAttributes.19")) { //$NON-NLS-1$
            protected Integer getValue() { return config.getSeed(); }
            protected void setValue(Object t) { config.setSeed((Integer) t); }});
        
        // Vector length
        this.addPreference(new PreferenceInteger(Resources.getMessage("ViewClassificationAttributes.21")) { //$NON-NLS-1$
            protected Integer getValue() { return config.getVectorLength(); }
            protected void setValue(Object t) { config.setVectorLength((Integer) t); }});
    }
    
    /**
     * Creates a string array of type {@link Kernel}
     * @return
     */
    private String[] getKernelTypes() {
        List<String> result = new ArrayList<String>();
        for (Kernel type : Kernel.values()) {
            result.add(type.name());
        }
        return result.toArray(new String[result.size()]);
    }
    
    /**
     * Creates a string array of type {@link MulticlassType}
     * @return
     */
    private String[] getMulticlassTypes() {
        List<String> result = new ArrayList<String>();
        for (MulticlassType type : MulticlassType.values()) {
            result.add(type.name());
        }
        return result.toArray(new String[result.size()]);
    }

    /**
     * Creates a string array of type {@link PriorFunction}
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
     * Creates a string array of type {@link Type}
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
