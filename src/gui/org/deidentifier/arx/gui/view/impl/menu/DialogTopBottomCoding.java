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

import org.apache.commons.math3.util.Pair;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
 * A dialog for defining thresholds for top and bottom coding
 * 
 * @author Fabian Prasser
 */
public class DialogTopBottomCoding extends TitleAreaDialog {

    /** Model */
    private String                                             title;
    /** Model */
    private String                                             message;
    /** Model */
    private Pair<Pair<String, Boolean>, Pair<String, Boolean>> value = null;
    /** Model */
    private DataType<?>                                        type;

    /** View */
    private Button                                             okButton;
    /** View */
    private Text                                               bottomThresholdText;
    /** View */
    private Text                                               topThresholdText;
    /** View */
    private Button                                             bottomInclusiveCheckbox;
    /** View */
    private Button                                             topInclusiveCheckbox;
    /** View */
    private Label                                              errorMessageLabel;

    /**
     * Creates a new instance
     * 
     * @param parentShell
     * @param type
     */
    public DialogTopBottomCoding(Shell parentShell,
                                 DataType<?> type) {
        super(parentShell);
        this.title = Resources.getMessage("DialogTopBottomCoding.0"); //$NON-NLS-1$
        this.type = type;
        this.message = Resources.getMessage("DialogTopBottomCoding.1"); //$NON-NLS-1$
    }

    /**
     * Returns a pair containing the bottom threshold+inclusive and the top threshold + inclusive.
     * Either bottom or top may be null if they have not been defined.
     * 
     * @return the value
     */
    public Pair<Pair<String, Boolean>, Pair<String, Boolean>> getValue() {
        return value;
    }

    @Override
    public void setErrorMessage(String message) {
        
        // Check
        if (this.errorMessageLabel.isDisposed()) return;
        
        // Set
        if (message != null) {
            this.errorMessageLabel.setText(message);
        } else {
            this.errorMessageLabel.setText(""); //$NON-NLS-1$
        }

        // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=113643
        Control button = getButton(IDialogConstants.OK_ID);
        if (button != null) {
            button.setEnabled(message == null);
        }
    }

    /**
     * Checks if all input is valid
     */
    private void checkValidity() {

        if (!bottomThresholdText.getText().equals("") && //$NON-NLS-1$
            !type.isValid(bottomThresholdText.getText())) {
            setErrorMessage(Resources.getMessage("DialogTopBottomCoding.3")); //$NON-NLS-1$
        } else if (!topThresholdText.getText().equals("") && //$NON-NLS-1$
                   !type.isValid(topThresholdText.getText())) {
            setErrorMessage(Resources.getMessage("DialogTopBottomCoding.5")); //$NON-NLS-1$
        } else if (bottomThresholdText.getText().equals("") && topThresholdText.getText().equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
            setErrorMessage(Resources.getMessage("DialogTopBottomCoding.12")); //$NON-NLS-1$
        } else {
            setErrorMessage(null);
        }
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            Pair<String, Boolean> pair1 = null;
            Pair<String, Boolean> pair2 = null;
            if (!bottomThresholdText.getText().equals("") && //$NON-NLS-1$
                type.isValid(bottomThresholdText.getText())) {
                pair1 = new Pair<>(bottomThresholdText.getText(), bottomInclusiveCheckbox.getSelection());
            }
            if (!topThresholdText.getText().equals("") && //$NON-NLS-1$
                type.isValid(topThresholdText.getText())) {
                pair2 = new Pair<>(topThresholdText.getText(), topInclusiveCheckbox.getSelection());
            }
            value = new Pair<Pair<String, Boolean>, Pair<String, Boolean>>(pair1, pair2);
        } else {
            value = null;
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
        okButton = createButton(parent,
                                IDialogConstants.OK_ID,
                                IDialogConstants.OK_LABEL,
                                true);
        okButton.setEnabled(false);
        createButton(parent,
                     IDialogConstants.CANCEL_ID,
                     IDialogConstants.CANCEL_LABEL,
                     false);
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
        base.setLayout(GridLayoutFactory.swtDefaults().numColumns(3).create());

        // Bottom
        Label bottomLabel = new Label(base, SWT.NONE);
        bottomLabel.setText(Resources.getMessage("DialogTopBottomCoding.8")); //$NON-NLS-1$
        bottomLabel.setLayoutData(GridDataFactory.fillDefaults().grab(false, false).span(1, 1).create());

        this.bottomThresholdText = new Text(base, SWT.BORDER);
        this.bottomThresholdText.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
        this.bottomThresholdText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent arg0) {
                checkValidity();
            }
        });
        
        this.bottomInclusiveCheckbox = new Button(base, SWT.CHECK);
        this.bottomInclusiveCheckbox.setText(Resources.getMessage("DialogTopBottomCoding.9")); //$NON-NLS-1$
        this.bottomInclusiveCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                checkValidity();
            }
        });

        // Top
        Label topLabel = new Label(base, SWT.NONE);
        topLabel.setText(Resources.getMessage("DialogTopBottomCoding.10")); //$NON-NLS-1$
        topLabel.setLayoutData(GridDataFactory.fillDefaults().grab(false, false).span(1, 1).create());

        this.topThresholdText = new Text(base, SWT.BORDER);
        this.topThresholdText.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
        this.topThresholdText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent arg0) {
                checkValidity();
            }
        });
        
        this.topInclusiveCheckbox = new Button(base, SWT.CHECK);
        this.topInclusiveCheckbox.setText(Resources.getMessage("DialogTopBottomCoding.11")); //$NON-NLS-1$
        this.topInclusiveCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                checkValidity();
            }
        });

        errorMessageLabel = new Label(base, SWT.NONE);
        errorMessageLabel.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).span(3, 1).create());
        errorMessageLabel.setBackground(errorMessageLabel.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        errorMessageLabel.setForeground(GUIHelper.COLOR_RED);
        applyDialogFont(base);
        checkValidity();
        return composite;
    }

    @Override
    protected ShellListener getShellListener() {
        return new ShellAdapter() {
            @Override
            public void shellClosed(final ShellEvent event) {
                value = null;
                setReturnCode(Window.CANCEL);
            }
        };
    }
}
