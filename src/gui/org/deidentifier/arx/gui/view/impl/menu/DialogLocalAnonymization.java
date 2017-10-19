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

package org.deidentifier.arx.gui.view.impl.menu;

import org.apache.commons.math3.util.Pair;
import org.deidentifier.arx.gui.model.Model;
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
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * A dialog for defining parameters of the local anonymization method
 * 
 * @author Fabian Prasser
 */
public class DialogLocalAnonymization extends TitleAreaDialog {

    /** Model */
    private String title;
    /** Model */
    private String message;

    /** View */
    private Button okButton;
    /** View */
    private Text   txtMaxTimePerIteration;
    /** View */
    private Text   txtMinRecordsPerIteration;

    /** Result */
    private Double maxTimePerIteration    = 0d;
    /** Result */
    private Double minRecordsPerIteration = 0.01d;

    /**
     * Creates a new instance
     * 
     * @param shell
     * @param model
     */
    public DialogLocalAnonymization(Shell shell, Model model) {
        super(shell);
        this.title = Resources.getMessage("DialogLocalAnonymization.0"); //$NON-NLS-1$
        this.message = Resources.getMessage("DialogLocalAnonymization.1"); //$NON-NLS-1$
        this.maxTimePerIteration = (double)model.getInputConfig().getHeuristicSearchTimeLimit() / 1000d;
        this.minRecordsPerIteration = model.getLocalRecodingModel().getMinRecordsPerIteration();
    }

    /**
     * Returns the parameters selected by the user. Returns a
     * pair. First: max. time per iteration. Second: min. records per iteration.
     * 
     * @return the value
     */
    public Pair<Double, Double> getResult() {
        if (minRecordsPerIteration == null || maxTimePerIteration == null) {
            return null;
        } else {
            return new Pair<>(maxTimePerIteration, minRecordsPerIteration);
        }
    }

    /**
     * Checks if all input is valid
     */
    private void checkValidity() {
        if (getMaxTimePerIteration() == null) {
            setErrorMessage(Resources.getMessage("DialogLocalAnonymization.5")); //$NON-NLS-1$
            if (okButton != null) {
                okButton.setEnabled(false);
            }
        } else if (getMinRecordsPerIteration() == null) {
            setErrorMessage(Resources.getMessage("DialogLocalAnonymization.4")); //$NON-NLS-1$
            if (okButton != null) {
                okButton.setEnabled(false);
            }
        } else {
            setErrorMessage(null);
            if (okButton != null) {
                okButton.setEnabled(true);
            }
        }
    }
    
    /**
     * Converts to double, returns null if not valid
     * @return
     */
    private Double getMaxTimePerIteration() {
        double value = 0d;
        try {
            value = Double.valueOf(txtMaxTimePerIteration.getText());
        } catch (Exception e) {
            return null;
        }
        if (((int)value) > 0d) {
            return value;
        } else {
            return null;
        }
    }

    /**
     * Converts to double, returns null if not valid
     * @return
     */
    private Double getMinRecordsPerIteration() {
        double value = 0d;
        try {
            value = Double.valueOf(txtMinRecordsPerIteration.getText());
        } catch (Exception e) {
            return null;
        }
        if (value > 0d && value <1d) {
            return value;
        } else {
            return null;
        }
    }
   
    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            maxTimePerIteration = getMaxTimePerIteration();
            minRecordsPerIteration = getMinRecordsPerIteration();
        } else if (buttonId == IDialogConstants.CANCEL_ID) {
            maxTimePerIteration = null;
            minRecordsPerIteration = null;
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

        Composite composite = (Composite) super.createDialogArea(parent);        
        Composite base = new Composite(composite, SWT.NONE);
        base.setLayoutData(SWTUtil.createFillGridData());
        base.setLayout(GridLayoutFactory.swtDefaults().numColumns(2).create());

        // Time
        Label label1 = new Label(base, SWT.NONE);
        label1.setText(Resources.getMessage("DialogLocalAnonymization.2")); //$NON-NLS-1$

        this.txtMaxTimePerIteration = new Text(base, SWT.BORDER);
        this.txtMaxTimePerIteration.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
        this.txtMaxTimePerIteration.setText(String.valueOf(maxTimePerIteration));
        this.txtMaxTimePerIteration.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent arg0) {
                checkValidity();
            }
        });
        
        // Records
        Label label2 = new Label(base, SWT.NONE);
        label2.setText(Resources.getMessage("DialogLocalAnonymization.3")); //$NON-NLS-1$

        this.txtMinRecordsPerIteration = new Text(base, SWT.BORDER);
        this.txtMinRecordsPerIteration.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
        this.txtMinRecordsPerIteration.setText(String.valueOf(minRecordsPerIteration));
        this.txtMinRecordsPerIteration.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent arg0) {
                checkValidity();
            }
        });

        applyDialogFont(base);
        checkValidity();
        return composite;
    }

    @Override
    protected ShellListener getShellListener() {
        return new ShellAdapter() {
            @Override
            public void shellClosed(final ShellEvent event) {
                maxTimePerIteration = null;
                minRecordsPerIteration = null;
                setReturnCode(Window.CANCEL);
            }
        };
    }
}
