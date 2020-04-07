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

package org.deidentifier.arx.gui.view.impl.menu;

import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelAnonymizationConfiguration;
import org.deidentifier.arx.gui.model.ModelAnonymizationConfiguration.SearchType;
import org.deidentifier.arx.gui.model.ModelAnonymizationConfiguration.TransformationType;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * A dialog for defining parameters of the anonymization method
 * 
 * @author Fabian Prasser
 * @author Thierry Meurers
 */
public class DialogAnonymization extends TitleAreaDialog {

    /** Model */
    private String                          title;
    /** Model */
    private String                          message;
    /** Model */
    private boolean                         optimalSearchAvailable;
    /** Model */
    private boolean                         heuristicSearchStepLimitAvailable;
    /** Model */
    private boolean                         heuristicSearchTimeLimitAvailable;
    /** Model */
    private boolean                         localRecodingAvailable;

    /** View */
    private Button                          okButton;
    /** View */
    private Text                            txtHeuristicSearchTimeLimit;
    /** View */
    private Text                            txtHeuristicSearchStepLimit;
    /** View */
    private Text                            textNumIterations;
    /** View */    
    private Button                          radioTimeLimit;
    /** View */
    private Button                          radioStepLimit;

    /** Result */
    private ModelAnonymizationConfiguration configuration;
    /** Result */
    private boolean                         configurationValid;

    /**
     * Creates a new instance
     * 
     * @param shell
     * @param model
     */
    public DialogAnonymization(Shell shell, Model model) {
        super(shell);
        this.title = Resources.getMessage("DialogAnonymization.0"); //$NON-NLS-1$
        this.message = Resources.getMessage("DialogAnonymization.1"); //$NON-NLS-1$
        this.configuration = model.getAnonymizationConfiguration();
        this.configurationValid = true;
        
        // Determine available search options
        this.localRecodingAvailable = true;
        this.heuristicSearchStepLimitAvailable = true;
        this.heuristicSearchTimeLimitAvailable = true;
        this.optimalSearchAvailable = model.getSolutionSpaceSize() <= model.getHeuristicSearchThreshold();
        for (PrivacyCriterion c : model.getInputConfig().getCriteria()) {
            if (!c.isLocalRecodingSupported()) {
                this.localRecodingAvailable = false;
            }
            if (!c.isOptimalSearchSupported()) {
                this.optimalSearchAvailable = false;
            }
            if (!c.isHeuristicSearchSupported()) {
                this.heuristicSearchStepLimitAvailable = false;
            }
            if (!c.isHeuristicSearchWithTimeLimitSupported()) {
                this.heuristicSearchTimeLimitAvailable = false;
            }
        }
    }

    /**
     * Returns the parameters selected by the user. Returns <code>null</code> if the selection has been canceled.
     * 
     * @return the value
     */
    public ModelAnonymizationConfiguration getResult() {
        if (configurationValid) {
            return configuration;
        } else {
            return null;
        }
    }

    /**
     * Checks if all input is valid
     */
    private void checkValidity() {
        
        // State
        String error = null;
        
        // Handle parameter
        if (getHeuristicSearchTimeLimit() == null) {
            error = Resources.getMessage("DialogAnonymization.5"); //$NON-NLS-1$
            configuration.setHeuristicSearchTimeLimit(0);
        } else {
            configuration.setHeuristicSearchTimeLimit(getHeuristicSearchTimeLimit());
        }
        
        // Handle parameter
        if (getNumIterations() == null) {
            error = Resources.getMessage("DialogAnonymization.4"); //$NON-NLS-1$
            configuration.setNumIterations(0);
        } else {
            configuration.setNumIterations(getNumIterations());
        }
        
        // Handle parameter
        if (getHeuristicSearchStepLimit() == null) {
            error = Resources.getMessage("DialogAnonymization.8"); //$NON-NLS-1$
            configuration.setHeuristicSearchStepLimit(0);
        } else {
            configuration.setHeuristicSearchStepLimit(getHeuristicSearchStepLimit());
        }

        // Handle parameter
        if (!radioTimeLimit.isEnabled() || !radioTimeLimit.getSelection()) {
            configuration.setTimeLimitEnabled(false);
        } else {
            configuration.setTimeLimitEnabled(true);
        }
        
        if (!radioStepLimit.isEnabled() || !radioStepLimit.getSelection()) {
            configuration.setStepLimitEnabled(false);
        } else {
            configuration.setStepLimitEnabled(true);
        }

        // Update state
        setErrorMessage(error);
        if (okButton != null) {
            configurationValid = error == null;
            okButton.setEnabled(error == null);
        }
    }
    
    /**
     * Adds a message to the given group
     * @param group
     * @param span
     * @param message
     */
    private void createMessage(Group group, int span, String message) {
        Label label = new Label(group, SWT.NONE);
        label.setText(Resources.getMessage("DialogAnonymization.14") + message); //$NON-NLS-1$
        label.setLayoutData(GridDataFactory.fillDefaults().span(span, 1).create());
    }

   /**
    * Converts to int, returns null if not valid
    * @return
    */
   private Integer getHeuristicSearchStepLimit() {
    
       int value = 0;
       try {
           value = Integer.valueOf(txtHeuristicSearchStepLimit.getText());
       } catch (Exception e) {
           return null;
       }
       if (value > 0d && value < Integer.MAX_VALUE) {
           return value;
       } else {
           return null;
       }
   }

    /**
     * Converts to double, returns null if not valid
     * @return
     */
    private Double getHeuristicSearchTimeLimit() {

        double value = 0d;
        try {
            value = Double.valueOf(txtHeuristicSearchTimeLimit.getText());
        } catch (Exception e) {
            return null;
        }
        if (value > 0d && value < ((double)Integer.MAX_VALUE / 10000d)) {
            return value;
        } else {
            return null;
        }
    }
   
    /**
     * Converts to double, returns null if not valid
     * @return
     */
    private Integer getNumIterations() {
        int value = 0;
        try {
            value = Integer.valueOf(textNumIterations.getText());
        } catch (Exception e) {
            return null;
        }
        if (value > 0d) {
            return value;
        } else {
            return null;
        }
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            configurationValid = true;
        } else if (buttonId == IDialogConstants.CANCEL_ID) {
            configurationValid = false;
        }
        super.buttonPressed(buttonId);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setImages(Resources.getIconSet(newShell.getDisplay()));
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }
    
    @Override
    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);
        setTitle(title); 
        setMessage(message);
        return contents;
    }

    @Override
    protected Control createDialogArea(Composite parent) {

    	/** 
    	 * Initialization 
    	 * */
        Composite composite = (Composite) super.createDialogArea(parent);        
        Composite base = new Composite(composite, SWT.NONE);
        base.setLayoutData(SWTUtil.createFillGridData());
        base.setLayout(SWTUtil.createGridLayout(1, false));
        
        // Group - search strategy
        Group group1 = new Group(parent, SWT.SHADOW_ETCHED_IN);
        group1.setText(Resources.getMessage("DialogAnonymization.6")); //$NON-NLS-1$
        GridData data1 = SWTUtil.createFillGridData();
        data1.horizontalIndent = 5;
        group1.setLayoutData(data1);
        group1.setLayout(GridLayoutFactory.swtDefaults().numColumns(3).create());

        // Radio - optimal
        final Button radioAlgorithmFlashOptimal = new Button(group1, SWT.RADIO);
        radioAlgorithmFlashOptimal.setText(Resources.getMessage("DialogAnonymization.11")); //$NON-NLS-1$
        //radio11.setLayoutData(GridDataFactory.swtDefaults().span(3, 1).create());

        // Radio - best-effort binary
        final Button radioAlgorithmFlashHeuristic = new Button(group1, SWT.RADIO);
        radioAlgorithmFlashHeuristic.setText(Resources.getMessage("DialogAnonymization.19")); //$NON-NLS-1$
        
        // Radio - Heuristic
        final Button radioAlgorithmLightning = new Button(group1, SWT.RADIO);
        radioAlgorithmLightning.setText(Resources.getMessage("DialogAnonymization.16")); //$NON-NLS-1$

        // Radio - best-effort binary
        final Button radioAlgorithmLightningTopDown = new Button(group1, SWT.RADIO);
        radioAlgorithmLightningTopDown.setText(Resources.getMessage("DialogAnonymization.20")); //$NON-NLS-1$
        
        // Radio - Genetic
        final Button radioAlgorithmGenetic = new Button(group1, SWT.RADIO);
        radioAlgorithmGenetic.setText(Resources.getMessage("DialogAnonymization.17")); //$NON-NLS-1$
        
        // Group - Limitations
        Group group2 = new Group(parent, SWT.SHADOW_ETCHED_IN);
        group2.setText(Resources.getMessage("DialogAnonymization.18")); //$NON-NLS-1$
        GridData data2 = SWTUtil.createFillGridData();
        data2.horizontalIndent = 5;
        group2.setLayoutData(data2);
        group2.setLayout(GridLayoutFactory.swtDefaults().numColumns(2).create());
        
        // Checkbox - step limit
        radioStepLimit = new Button(group2, SWT.RADIO);
        radioStepLimit.setText(Resources.getMessage("DialogAnonymization.7"));
        
        // Text - step limit
        this.txtHeuristicSearchStepLimit = new Text(group2, SWT.BORDER);
        this.txtHeuristicSearchStepLimit.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
        
        // Checkbox - time limit
        radioTimeLimit = new Button(group2, SWT.RADIO);
        radioTimeLimit.setText(Resources.getMessage("DialogAnonymization.2"));
        
        // Text - time limit
        this.txtHeuristicSearchTimeLimit = new Text(group2, SWT.BORDER);
        this.txtHeuristicSearchTimeLimit.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
        
        // Group - transformation model
        Group group3 = new Group(parent, SWT.SHADOW_ETCHED_IN);
        group3.setText(Resources.getMessage("DialogAnonymization.9")); //$NON-NLS-1$
        GridData data3 = SWTUtil.createFillGridData();
        data3.horizontalIndent = 5;
        group3.setLayoutData(data3);
        group3.setLayout(GridLayoutFactory.swtDefaults().numColumns(2).create());

        // Radio - global transformation
        final Button btnGlobalTransformation = new Button(group3, SWT.RADIO);
        btnGlobalTransformation.setText(Resources.getMessage("DialogAnonymization.10")); //$NON-NLS-1$
        btnGlobalTransformation.setLayoutData(GridDataFactory.swtDefaults().span(2, 1).create());

        // Radio - local transformation
        final Button btnLocalTransformation = new Button(group3, SWT.RADIO);
        btnLocalTransformation.setText(Resources.getMessage("DialogAnonymization.3")); //$NON-NLS-1$

        // Tet - number iterations
        this.textNumIterations = new Text(group3, SWT.BORDER);
        this.textNumIterations.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());

        // Search strategy radio buttons
        switch(configuration.getSearchType()) {
        case HEURISTIC_BINARY: radioAlgorithmFlashHeuristic.setSelection(true); break;
        case HEURISTIC_BOTTOM_UP: radioAlgorithmLightning.setSelection(true); break;
        case HEURISTIC_TOP_DOWN: radioAlgorithmLightningTopDown.setSelection(true); break;
        case HEURISTIC_GENETIC: radioAlgorithmGenetic.setSelection(true); break;
		default:
			radioAlgorithmFlashOptimal.setSelection(true);
			radioStepLimit.setSelection(false);
            radioStepLimit.setEnabled(false);
            radioTimeLimit.setSelection(false);
            radioTimeLimit.setEnabled(false);
            txtHeuristicSearchStepLimit.setEnabled(false);
            txtHeuristicSearchTimeLimit.setEnabled(false);
        }
        
        // text - step limit
        this.txtHeuristicSearchStepLimit.setText(String.valueOf(configuration.getHeuristicSearchStepLimit()));
        // text - time limit
        this.txtHeuristicSearchTimeLimit.setText(String.valueOf(configuration.getHeuristicSearchTimeLimit()));
        
        // Transformation type radio buttons
        switch(configuration.getTransformationType()) {
            case GLOBAL: btnGlobalTransformation.setSelection(true); break;
            case LOCAL: btnLocalTransformation.setSelection(true); break;
        }
        
        // text - number iterations
        this.textNumIterations.setText(String.valueOf(configuration.getNumIterations()));
        
        // Show message
        if (this.localRecodingAvailable) {
            btnLocalTransformation.setEnabled(true);
        } else {
            btnLocalTransformation.setEnabled(false);
            btnGlobalTransformation.setSelection(true);
            configuration.setTransformationType(TransformationType.GLOBAL);
            textNumIterations.setEnabled(false);
            createMessage(group3, 2, Resources.getMessage("DialogAnonymization.12")); //$NON-NLS-1$
        }
        
        // Radio listener
        radioAlgorithmFlashOptimal.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                if (radioAlgorithmFlashOptimal.getSelection()) {
                    configuration.setSearchType(SearchType.OPTIMAL);
                    radioStepLimit.setSelection(false);
                    radioStepLimit.setEnabled(false);
                    radioTimeLimit.setSelection(false);
                    radioTimeLimit.setEnabled(false);
                    txtHeuristicSearchStepLimit.setEnabled(false);
                    txtHeuristicSearchTimeLimit.setEnabled(false);
                }
            }
        });
        
        // Radio listener
        radioAlgorithmFlashHeuristic.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                if (radioAlgorithmFlashHeuristic.getSelection()) {
                    configuration.setSearchType(SearchType.HEURISTIC_BINARY);
                    radioStepLimit.setEnabled(true);
                    radioTimeLimit.setEnabled(true);
                    txtHeuristicSearchStepLimit.setEnabled(true);
                    txtHeuristicSearchTimeLimit.setEnabled(true);
                }
            }
        });

        // Radio listener
        radioAlgorithmLightning.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                if (radioAlgorithmLightning.getSelection()) {
                    configuration.setSearchType(SearchType.HEURISTIC_BOTTOM_UP);
                    radioStepLimit.setEnabled(true);
                    radioTimeLimit.setEnabled(true);
                    txtHeuristicSearchStepLimit.setEnabled(true);
                    txtHeuristicSearchTimeLimit.setEnabled(true);
                    
                }
            }
        });

        // Radio listener
        radioAlgorithmLightningTopDown.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                if (radioAlgorithmLightningTopDown.getSelection()) {
                    configuration.setSearchType(SearchType.HEURISTIC_TOP_DOWN);
                    radioStepLimit.setEnabled(true);
                    radioTimeLimit.setEnabled(true);
                    txtHeuristicSearchStepLimit.setEnabled(true);
                    txtHeuristicSearchTimeLimit.setEnabled(true);
                    
                }
            }
        });
        
        // Radio listener
        radioAlgorithmGenetic.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                if (radioAlgorithmGenetic.getSelection()) {
                    configuration.setSearchType(SearchType.HEURISTIC_GENETIC);
                    radioStepLimit.setEnabled(true);
                    radioTimeLimit.setEnabled(true);
                    txtHeuristicSearchStepLimit.setEnabled(true);
                    txtHeuristicSearchTimeLimit.setEnabled(true);
                }
            }
        });
       
        this.txtHeuristicSearchStepLimit.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent arg0) {
                radioStepLimit.setSelection(true);
                checkValidity();
            }
        });
        
        this.txtHeuristicSearchTimeLimit.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent arg0) {
                radioTimeLimit.setSelection(true);
                checkValidity();
            }
        });
        
        btnGlobalTransformation.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                if (btnGlobalTransformation.getSelection()) {
                    configuration.setTransformationType(TransformationType.GLOBAL);
                }
            }
        });
        
        btnLocalTransformation.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                if (btnLocalTransformation.getSelection()) {
                    configuration.setTransformationType(TransformationType.LOCAL);
                }
            }
        });
        
        this.textNumIterations.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent arg0) {
                btnLocalTransformation.setSelection(true);
                btnGlobalTransformation.setSelection(false);
                configuration.setTransformationType(TransformationType.LOCAL);
                checkValidity();
            }
        });
        
        // Prepare radio buttons
        if (!this.optimalSearchAvailable) {
                        
            this.radioStepLimit.setEnabled(true);
            this.txtHeuristicSearchStepLimit.setEnabled(true);
            this.radioTimeLimit.setEnabled(true);
            this.txtHeuristicSearchTimeLimit.setEnabled(true);
            radioAlgorithmFlashOptimal.setEnabled(false);
            radioAlgorithmFlashOptimal.setSelection(false);

            if (configuration.getSearchType() == SearchType.OPTIMAL) {
                configuration.setSearchType(SearchType.HEURISTIC_TOP_DOWN);
                radioAlgorithmLightning.setSelection(true);
                
                if (!configuration.isTimeLimitEnabled() && !configuration.isStepLimitEnabled()) {
                    configuration.setTimeLimitEnabled(true);
                }
            }
            
            // Message
            createMessage(group1, 3, Resources.getMessage("DialogAnonymization.13")); //$NON-NLS-1$
        }
        
        // Time and step limit
        if (configuration.getSearchType() == SearchType.OPTIMAL && this.optimalSearchAvailable) {
            this.radioStepLimit.setEnabled(false);
            this.radioStepLimit.setSelection(false);
            this.txtHeuristicSearchStepLimit.setEnabled(false);
            this.radioTimeLimit.setEnabled(false);
            this.radioTimeLimit.setSelection(false);
            this.txtHeuristicSearchTimeLimit.setEnabled(false);
        } else if (!this.heuristicSearchStepLimitAvailable) {
            this.radioStepLimit.setEnabled(false);
            this.radioStepLimit.setSelection(false);
            this.txtHeuristicSearchStepLimit.setEnabled(false);
            this.radioTimeLimit.setEnabled(true);
            this.radioTimeLimit.setSelection(true);
            this.txtHeuristicSearchTimeLimit.setEnabled(true);
        } else if (!this.heuristicSearchTimeLimitAvailable) {
            this.radioStepLimit.setEnabled(true);
            this.radioStepLimit.setSelection(true);
            this.txtHeuristicSearchStepLimit.setEnabled(true);
            this.radioTimeLimit.setEnabled(false);
            this.radioTimeLimit.setSelection(false);
            this.txtHeuristicSearchTimeLimit.setEnabled(false);
        } else {
            this.radioStepLimit.setEnabled(true);
            this.radioStepLimit.setSelection(true);
            this.txtHeuristicSearchStepLimit.setEnabled(true);
            this.radioTimeLimit.setEnabled(true);
            this.radioTimeLimit.setSelection(true);
            this.txtHeuristicSearchTimeLimit.setEnabled(true);
        }

        // Configuration
        radioTimeLimit.setSelection(configuration.isTimeLimitEnabled());
        radioStepLimit.setSelection(configuration.isStepLimitEnabled());
        
        // Prepare
        applyDialogFont(base);
        checkValidity();
        return composite;
    }

    @Override
    protected ShellListener getShellListener() {
        return new ShellAdapter() {
            @Override
            public void shellClosed(final ShellEvent event) {
                configurationValid = false;
                setReturnCode(Window.CANCEL);
            }
        };
    }
}