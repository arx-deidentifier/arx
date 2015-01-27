/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
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
package org.deidentifier.arx.gui.view.impl.wizard;

import java.util.Arrays;

import org.deidentifier.arx.gui.view.impl.wizard.ARXWizardDialog.ARXWizardButton;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;

/**
 * This class implements an abstract base class for wizards.
 *
 * @author Fabian Prasser
 * @param <T>
 */
public abstract class ARXWizard<T> extends Wizard implements IWizard {

    /** Var. */
    private ARXWizardDialog   dialog;
    
    /** Var. */
    private ARXWizardButton[] buttons;
    
    /** Var. */
    private final Point       pageSize;

    /**
     * Creates a new instance.
     */
    protected ARXWizard() {
        this.pageSize = null;
        this.buttons = null;
    }
    
    /**
     * Creates a new instance with given page size.
     *
     * @param pageSize
     */
    protected ARXWizard(Point pageSize) {
        this.pageSize = pageSize;
        this.buttons = null;
    }

    /**
     * Returns the result.
     *
     * @return
     */
    public abstract T getResult();

    /**
     * Opens the dialog.
     *
     * @param shell
     * @return OK pressed
     */
    public boolean open(final Shell shell) {
        
        if (buttons != null){
            this.dialog = new ARXWizardDialog(shell, this, Arrays.asList(buttons));   
        } else {
            this.dialog = new ARXWizardDialog(shell, this);   
        }
        if (pageSize != null) this.dialog.setPageSize(pageSize);
        return dialog.open() == Window.OK;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        return true;
    }
    
    /**
     * Returns the button created for the given specification.
     *
     * @param button
     * @return
     */
    protected Button getButton(ARXWizardButton button){
        if (dialog == null) return null;
        else return dialog.getButton(button);
    }
    
    /**
     * Returns the associated dialog.
     *
     * @return
     */
    protected ARXWizardDialog getDialog(){
        return dialog;
    }
    
    /**
     * Sets the buttons.
     *
     * @param buttons
     */
    protected void setButtons(ARXWizardButton... buttons){
        this.buttons = buttons;
    }
}
