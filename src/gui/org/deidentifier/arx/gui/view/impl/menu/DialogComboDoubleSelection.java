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

import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.def.IValidator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * A selection dialog for elements from a combo box. 
 * 
 * @author Fabian Prasser
 */
public class DialogComboDoubleSelection extends Dialog {
    
    /** The title of the dialog. */
    private String               title;

    /** The message to display, or <code>null</code> if none. */
    private String               message;

    /** The input value; the empty string by default. */
    private String               value1 = "";     //$NON-NLS-1$

    /** The input value; the empty string by default. */
    private String               value2 = "";     //$NON-NLS-1$

    /** The input validator, or <code>null</code> if none. */
    private IValidator<String[]> validator;

    /** Choices for combo widget. */
    private String[]             choices1;

    /** Choices for combo widget. */
    private String[]             choices2;

    /** Input combo widget. */
    private Combo                combo1;

    /** Input combo widget. */
    private Combo                combo2;

    /** Error message label widget. */
    private Text                 errorMessageText;

    /** Error message string. */
    private String               errorMessage;

    /**
     * Creates a new instance
     * @param parentShell
     * @param dialogTitle
     * @param dialogMessage
     * @param choices1
     * @param initialValue1
     * @param choices2
     * @param initialValue2
     * @param validator
     */
    public DialogComboDoubleSelection(Shell parentShell,
                                String dialogTitle,
                                String dialogMessage,
                                String[] choices1,
                                String initialValue1,
                                String[] choices2,
                                String initialValue2,
                                IValidator<String[]> validator) {
        super(parentShell);
        this.title = dialogTitle;
        message = dialogMessage;
        this.choices1 = choices1;
        this.choices2 = choices2;
        if (initialValue1 == null) {
            value1 = "";//$NON-NLS-1$
        } else {
            value1 = initialValue1;
        }
        if (initialValue2 == null) {
            value2 = "";//$NON-NLS-1$
        } else {
            value2 = initialValue2;
        }
        this.validator = validator;
    }

    /**
     * Returns the string typed into this input dialog.
     * 
     * @return the input string
     */
    public String getValue1() {
        return value1;
    }

    /**
     * Returns the string typed into this input dialog.
     * 
     * @return the input string
     */
    public String getValue2() {
        return value2;
    }
    /**
     * Sets or clears the error message. If not <code>null</code>, the OK button
     * is disabled.
     * 
     * @param errorMessage
     *            the error message, or <code>null</code> to clear
     * @since 3.0
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        if (errorMessageText != null && !errorMessageText.isDisposed()) {
            errorMessageText.setText(errorMessage == null ? " \n " : errorMessage); //$NON-NLS-1$
            // Disable the error message text control if there is no error, or
            // no error text (empty or whitespace only). Hide it also to avoid
            // color change. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=130281
            boolean hasError = errorMessage != null && (StringConverter.removeWhiteSpaces(errorMessage)).length() > 0;
            errorMessageText.setEnabled(hasError);
            errorMessageText.setVisible(hasError);
            errorMessageText.getParent().update();
            // Access the ok button by id, in case clients have overridden
            // button creation. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=113643
            Control button = getButton(IDialogConstants.OK_ID);
            if (button != null) {
                button.setEnabled(errorMessage == null);
            }
        }
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            value1 = combo1.getText();
            value2 = combo2.getText();
        } else {
            value1 = null;
            value2 = null;
        }
        super.buttonPressed(buttonId);
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        if (title != null) {
            shell.setText(title);
        }
        shell.setImages(Resources.getIconSet(shell.getDisplay()));
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        
        // Create OK and Cancel buttons by default
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
        
        // Do this here because setting the text will set enablement on the ok button
        combo1.setFocus();
        if (value1 != null) {
            combo1.setText(value1);
        }
        combo2.setFocus();
        if (value2 != null) {
            combo2.setText(value2);
        }
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        // create composite
        Composite composite = (Composite) super.createDialogArea(parent);
        // create message
        if (message != null) {
            Label label = new Label(composite, SWT.WRAP);
            label.setText(message);
            GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL |
                                         GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
            data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
            label.setLayoutData(data);
            label.setFont(parent.getFont());
        }
        combo1 = new Combo(composite, SWT.NONE);
        combo1.setItems(choices1);
        combo1.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
        combo1.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });
        combo2 = new Combo(composite, SWT.NONE);
        combo2.setItems(choices2);
        combo2.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
        combo2.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });
        errorMessageText = new Text(composite, SWT.READ_ONLY | SWT.WRAP);
        errorMessageText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
        errorMessageText.setBackground(errorMessageText.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        // Set the error message text
        // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=66292
        setErrorMessage(errorMessage);

        applyDialogFont(composite);
        return composite;
    }

    @Override
    protected ShellListener getShellListener() {
        return new ShellAdapter() {
            @Override
            public void shellClosed(final ShellEvent event) {
                setReturnCode(Window.CANCEL);
            }
        };
    }

    /**
     * Validates the input.
     * <p>
     * The default implementation of this framework method delegates the request
     * to the supplied input validator object; if it finds the input invalid,
     * the error message is displayed in the dialog's message line. This hook
     * method is called whenever the text changes in the input field.
     * </p>
     */
    protected void validateInput() {
        String errorMsg = null;
        if (validator != null) {
            errorMsg = validator.isValid(new String[]{combo1.getText(), combo2.getText()});
        }
        // Bug 16256: important not to treat "" (blank error) the same as null
        // (no error)
        setErrorMessage(errorMsg);
    }
}
