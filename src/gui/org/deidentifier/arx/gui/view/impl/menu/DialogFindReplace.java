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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.util.Pair;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.gui.model.Model;
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
 * A dialog for finding and replacing data items
 * 
 * @author Fabian Prasser
 */
public class DialogFindReplace extends TitleAreaDialog {

    /** Model */
    private String               title;
    /** Model */
    private String               message;
    /** Model */
    private Pair<String, String> value    = null;
    /** Model */
    private Set<String>          elements = new HashSet<String>();
    /** Model */
    private String               attribute;
    /** Model */
    private Model                model;

    /** View */
    private Button               okButton;
    /** View */
    private Text                 find;
    /** View */
    private Text                 replace;
    /** View */
    private Label                errorMessage;

    /**
     * Creates a new instance
     * 
     * @param parentShell
     * @param handle
     * @param column
     */
    public DialogFindReplace(Shell parentShell,
                             Model model,
                             DataHandle handle,
                             int column) {
        super(parentShell);
        this.title = Resources.getMessage("DialogFindReplace.0"); //$NON-NLS-1$
        this.model = model;
        this.attribute = handle.getAttributeName(column);
        this.message = Resources.getMessage("DialogFindReplace.1") + //$NON-NLS-1$
                       handle.getAttributeName(column) + Resources.getMessage("DialogFindReplace.2"); //$NON-NLS-1$
        this.elements.addAll(Arrays.asList(handle.getDistinctValues(column)));
    }

    /**
     * Returns a pair containing the string to be found and the string with
     * which it is to be replaced
     * 
     * @return the value
     */
    public Pair<String, String> getValue() {
        return value;
    }

    @Override
    public void setErrorMessage(String message) {
        
        // Check
        if (this.errorMessage.isDisposed()) return;
        
        // Set
        if (message != null) {
            this.errorMessage.setText(message);
        } else {
            this.errorMessage.setText(""); //$NON-NLS-1$
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
        if (!elements.contains(find.getText())) {
            setErrorMessage(Resources.getMessage("DialogFindReplace.4")); //$NON-NLS-1$
        } else {
            DataType<?> type = model.getInputDefinition().getDataType(attribute);
            if (!type.isValid(replace.getText())) {
                setErrorMessage(Resources.getMessage("DialogFindReplace.5") + //$NON-NLS-1$
                                type.getDescription()
                                .getLabel()+Resources.getMessage("DialogFindReplace.6")); //$NON-NLS-1$
            } else {
                if (elements.contains(replace.getText())) {
                    setErrorMessage(Resources.getMessage("DialogFindReplace.9")); //$NON-NLS-1$
                } else {
                    setErrorMessage(null);
                }
            }
        }
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            value = new Pair<String, String>(find.getText(), replace.getText());
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
        okButton.setEnabled(elements.contains("")); //$NON-NLS-1$
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
        base.setLayout(GridLayoutFactory.swtDefaults().numColumns(2).create());

        Label messageLabel = new Label(base, SWT.NONE);
        messageLabel.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).span(2, 1).create());
        messageLabel.setText(Resources.getMessage("DialogFindReplace.3")); //$NON-NLS-1$

        Label findLabel = new Label(base, SWT.NONE);
        findLabel.setText(Resources.getMessage("DialogFindReplace.7")); //$NON-NLS-1$

        this.find = new Text(base, SWT.BORDER);
        this.find.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
        this.find.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent arg0) {
                checkValidity();
            }
        });

        Label replaceLabel = new Label(base, SWT.NONE);
        replaceLabel.setText(Resources.getMessage("DialogFindReplace.8")); //$NON-NLS-1$

        this.replace = new Text(base, SWT.BORDER);
        this.replace.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
        this.replace.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent arg0) {
                checkValidity();
            }
        });

        errorMessage = new Label(base, SWT.NONE);
        errorMessage.setLayoutData(GridDataFactory.fillDefaults()
                                                  .grab(true, true)
                                                  .span(2, 1)
                                                  .create());
        errorMessage.setBackground(errorMessage.getDisplay()
                                               .getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        errorMessage.setForeground(GUIHelper.COLOR_RED);
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
