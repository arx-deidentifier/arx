/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2020 Fabian Prasser and contributors
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
    private boolean                         flashAlgorithmAvailable;
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
    /** View */    
    private Button                          radioAlgorithmFlashOptimal;
    /** View */
    private Button                          radioAlgorithmGenetic;
    /** View */
    private Button                          radioAlgorithmLightningTopDown;
    /** View */
    private Button                          radioAlgorithmLightning;
    /** View */
    private Button                          radioAlgorithmFlashHeuristic;
    /** View */    
    private Button                          btnGlobalTransformation;
    /** View */
    private Button                          btnLocalTransformation;

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
        this.flashAlgorithmAvailable = model.getSolutionSpaceSize() <= model.getHeuristicSearchThreshold();
        for (PrivacyCriterion c : model.getInputConfig().getCriteria()) {
            if (!c.isLocalRecodingSupported()) {
                this.localRecodingAvailable = false;
            }
            if (!c.isOptimalSearchSupported()) {
                this.flashAlgorithmAvailable = false;
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
    private void checkAndUpdateModel() {
        
        // State
        String error = null;

        // Set algorithm
        if (radioAlgorithmFlashOptimal.isEnabled() && radioAlgorithmFlashOptimal.getSelection()) {
            configuration.setSearchType(SearchType.OPTIMAL);
        } else if (radioAlgorithmGenetic.isEnabled() && radioAlgorithmGenetic.getSelection()) {
            configuration.setSearchType(SearchType.HEURISTIC_GENETIC);
        } else if (radioAlgorithmLightningTopDown.isEnabled() && radioAlgorithmLightningTopDown.getSelection()) {
            configuration.setSearchType(SearchType.HEURISTIC_TOP_DOWN);
        } else if (radioAlgorithmLightning.isEnabled() && radioAlgorithmLightning.getSelection()) {
            configuration.setSearchType(SearchType.HEURISTIC_BOTTOM_UP);
        } else if (radioAlgorithmFlashHeuristic.isEnabled() && radioAlgorithmFlashHeuristic.getSelection()) {
            configuration.setSearchType(SearchType.HEURISTIC_BINARY);
        } else {
            error = Resources.getMessage("DialogAnonymization.21"); //$NON-NLS-1$
        }
        
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
        
        if (btnLocalTransformation.isEnabled() && btnLocalTransformation.getSelection()) {
            configuration.setTransformationType(TransformationType.LOCAL);
        } else if (btnGlobalTransformation.isEnabled() && btnGlobalTransformation.getSelection()) {
            configuration.setTransformationType(TransformationType.GLOBAL);
        } else {
            error = Resources.getMessage("DialogAnonymization.22"); //$NON-NLS-1$
        }
        
        // Handle parameter
        if (getHeuristicSearchStepLimit() == null) {
            error = Resources.getMessage("DialogAnonymization.8"); //$NON-NLS-1$
            configuration.setHeuristicSearchStepLimit(0);
        } else {
            configuration.setHeuristicSearchStepLimit(getHeuristicSearchStepLimit());
        }

        // Handle parameter
        configuration.setTimeLimitEnabled(radioTimeLimit.getSelection());
        configuration.setStepLimitEnabled(radioStepLimit.getSelection());
        
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
    	 * Creation and layout 
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
        this.radioAlgorithmFlashOptimal = new Button(group1, SWT.RADIO);
        this.radioAlgorithmFlashOptimal.setText(Resources.getMessage("DialogAnonymization.11")); //$NON-NLS-1$

        // Radio - best-effort binary
        this.radioAlgorithmFlashHeuristic = new Button(group1, SWT.RADIO);
        this.radioAlgorithmFlashHeuristic.setText(Resources.getMessage("DialogAnonymization.19")); //$NON-NLS-1$
        
        // Radio - Heuristic
        this.radioAlgorithmLightning = new Button(group1, SWT.RADIO);
        this.radioAlgorithmLightning.setText(Resources.getMessage("DialogAnonymization.16")); //$NON-NLS-1$

        // Radio - best-effort binary
        this.radioAlgorithmLightningTopDown = new Button(group1, SWT.RADIO);
        this.radioAlgorithmLightningTopDown.setText(Resources.getMessage("DialogAnonymization.20")); //$NON-NLS-1$
        
        // Radio - Genetic
        this.radioAlgorithmGenetic = new Button(group1, SWT.RADIO);
        this.radioAlgorithmGenetic.setText(Resources.getMessage("DialogAnonymization.17")); //$NON-NLS-1$
        
        // Group - Limits
        Group group2 = new Group(parent, SWT.SHADOW_ETCHED_IN);
        group2.setText(Resources.getMessage("DialogAnonymization.18")); //$NON-NLS-1$
        GridData data2 = SWTUtil.createFillGridData();
        data2.horizontalIndent = 5;
        group2.setLayoutData(data2);
        group2.setLayout(GridLayoutFactory.swtDefaults().numColumns(2).create());
        
        // Checkbox - step limit
        this.radioStepLimit = new Button(group2, SWT.RADIO);
        this.radioStepLimit.setText(Resources.getMessage("DialogAnonymization.7"));
        
        // Text - step limit
        this.txtHeuristicSearchStepLimit = new Text(group2, SWT.BORDER);
        this.txtHeuristicSearchStepLimit.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
        
        // Checkbox - time limit
        this.radioTimeLimit = new Button(group2, SWT.RADIO);
        this.radioTimeLimit.setText(Resources.getMessage("DialogAnonymization.2"));
        
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
        this.btnGlobalTransformation = new Button(group3, SWT.RADIO);
        this.btnGlobalTransformation.setText(Resources.getMessage("DialogAnonymization.10")); //$NON-NLS-1$
        this.btnGlobalTransformation.setLayoutData(GridDataFactory.swtDefaults().span(2, 1).create());

        // Radio - local transformation
        this.btnLocalTransformation = new Button(group3, SWT.RADIO);
        this.btnLocalTransformation.setText(Resources.getMessage("DialogAnonymization.3")); //$NON-NLS-1$

        // Tet - number iterations
        this.textNumIterations = new Text(group3, SWT.BORDER);
        this.textNumIterations.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());

        /*
         * Set initial values 
         */
        
        // Search strategy radio buttons
        switch(configuration.getSearchType()) {
        case HEURISTIC_BINARY: radioAlgorithmFlashHeuristic.setSelection(true); break;
        case HEURISTIC_BOTTOM_UP: radioAlgorithmLightning.setSelection(true); break;
        case HEURISTIC_TOP_DOWN: radioAlgorithmLightningTopDown.setSelection(true); break;
        case HEURISTIC_GENETIC: radioAlgorithmGenetic.setSelection(true); break;
		default:
            this.radioAlgorithmFlashOptimal.setSelection(true);
            this.radioStepLimit.setEnabled(false);
            this.radioTimeLimit.setEnabled(false);
            this.txtHeuristicSearchStepLimit.setEnabled(false);
            this.txtHeuristicSearchTimeLimit.setEnabled(false);
        }
        
        // Limits
        this.txtHeuristicSearchStepLimit.setText(String.valueOf(configuration.getHeuristicSearchStepLimit()));
        this.txtHeuristicSearchTimeLimit.setText(String.valueOf(configuration.getHeuristicSearchTimeLimit()));
        this.radioStepLimit.setSelection(configuration.isStepLimitEnabled());
        this.radioTimeLimit.setSelection(configuration.isTimeLimitEnabled());
        
        // Transformation types
        switch(configuration.getTransformationType()) {
            case GLOBAL: btnGlobalTransformation.setSelection(true); break;
            case LOCAL: btnLocalTransformation.setSelection(true); break;
        }
        this.textNumIterations.setText(String.valueOf(configuration.getNumIterations()));
        
        /*
         * Customize according to currently available settings
         */
        
        // Recoding
        if (this.localRecodingAvailable) {
            this.btnLocalTransformation.setEnabled(true);
        } else {
            this.btnLocalTransformation.setEnabled(false);
            this.btnGlobalTransformation.setSelection(true);
            this.configuration.setTransformationType(TransformationType.GLOBAL);
            this.textNumIterations.setEnabled(false);
            createMessage(group3, 2, Resources.getMessage("DialogAnonymization.12")); //$NON-NLS-1$
        }

        // Optimal search
        if (!this.flashAlgorithmAvailable) {
                        
            this.radioStepLimit.setEnabled(true);
            this.txtHeuristicSearchStepLimit.setEnabled(true);
            this.radioTimeLimit.setEnabled(true);
            this.txtHeuristicSearchTimeLimit.setEnabled(true);
            this.radioAlgorithmFlashOptimal.setEnabled(false);
            this.radioAlgorithmFlashOptimal.setSelection(false);
            this.radioAlgorithmFlashHeuristic.setEnabled(false);
            this.radioAlgorithmFlashHeuristic.setSelection(false);
            if (configuration.getSearchType() == SearchType.OPTIMAL || configuration.getSearchType() == SearchType.HEURISTIC_BINARY) {
                configuration.setSearchType(SearchType.HEURISTIC_TOP_DOWN);
                this.radioAlgorithmLightning.setSelection(true);
            }
            
            // Message
            createMessage(group1, 3, Resources.getMessage("DialogAnonymization.13")); //$NON-NLS-1$
        }
        
        // Time and step limit
        if (configuration.getSearchType() == SearchType.OPTIMAL && this.flashAlgorithmAvailable) {
            this.radioStepLimit.setEnabled(false);
            this.txtHeuristicSearchStepLimit.setEnabled(false);
            this.radioTimeLimit.setEnabled(false);
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
            this.txtHeuristicSearchStepLimit.setEnabled(true);
            this.radioTimeLimit.setEnabled(true);
            this.txtHeuristicSearchTimeLimit.setEnabled(true);
        }

        /*
         * Handle events
         */
        
        // Radio listener
        this.radioAlgorithmFlashOptimal.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                if (radioAlgorithmFlashOptimal.getSelection()) {
                    radioStepLimit.setEnabled(false);
                    radioTimeLimit.setEnabled(false);
                    txtHeuristicSearchStepLimit.setEnabled(false);
                    txtHeuristicSearchTimeLimit.setEnabled(false);
                }
                checkAndUpdateModel();
            }
        });
        
        // Radio listener
        this.radioAlgorithmFlashHeuristic.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                if (radioAlgorithmFlashHeuristic.getSelection()) {
                    radioStepLimit.setEnabled(true);
                    radioTimeLimit.setEnabled(true);
                    txtHeuristicSearchStepLimit.setEnabled(true);
                    txtHeuristicSearchTimeLimit.setEnabled(true);
                }
                checkAndUpdateModel();
            }
        });
        
        // Radio listener
        this.radioAlgorithmLightning.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                if (radioAlgorithmLightning.getSelection()) {
                    radioStepLimit.setEnabled(true);
                    radioTimeLimit.setEnabled(true);
                    txtHeuristicSearchStepLimit.setEnabled(true);
                    txtHeuristicSearchTimeLimit.setEnabled(true);
                }
                checkAndUpdateModel();
            }
        });
            
        // Radio listener
        this.radioAlgorithmLightningTopDown.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                if (radioAlgorithmLightningTopDown.getSelection()) {
                    radioStepLimit.setEnabled(true);
                    radioTimeLimit.setEnabled(true);
                    txtHeuristicSearchStepLimit.setEnabled(true);
                    txtHeuristicSearchTimeLimit.setEnabled(true);
                }
                checkAndUpdateModel();
            }
        });
        
        // Radio listener
        this.radioAlgorithmGenetic.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                if (radioAlgorithmGenetic.getSelection()) {
                    radioStepLimit.setEnabled(true);
                    radioTimeLimit.setEnabled(true);
                    txtHeuristicSearchStepLimit.setEnabled(true);
                    txtHeuristicSearchTimeLimit.setEnabled(true);
                }
                checkAndUpdateModel();
            }
        });
   
        this.txtHeuristicSearchStepLimit.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent arg0) {
                radioStepLimit.setSelection(true);
                checkAndUpdateModel();
            }
        });
        
        this.txtHeuristicSearchTimeLimit.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent arg0) {
                radioTimeLimit.setSelection(true);
                checkAndUpdateModel();
            }
        });
        
        this.btnGlobalTransformation.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                checkAndUpdateModel();
            }
        });
        
        this.btnLocalTransformation.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                checkAndUpdateModel();
            }
        });
        
        this.textNumIterations.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent arg0) {
                btnLocalTransformation.setSelection(true);
                btnGlobalTransformation.setSelection(false);
                checkAndUpdateModel();
            }
        });
        
        // Done
        applyDialogFont(base);
        checkAndUpdateModel();
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