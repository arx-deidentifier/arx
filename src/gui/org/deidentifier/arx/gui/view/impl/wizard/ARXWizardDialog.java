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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deidentifier.arx.gui.resources.Resources;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * A custom implementation of the default SWT WizardDialog that is more
 * compact and allows adding additional buttons.
 *
 * @author Fabian Prasser
 */
public class ARXWizardDialog extends WizardDialog {

    /**
     * A specification for a button to add to the wizard.
     *
     * @author Fabian Prasser
     */
    protected static class ARXWizardButton{
        
        /** Var. */
        private static int ID = Integer.MAX_VALUE - 10;
        
        /** Var. */
        private final int id = ID--;
        
        /** Var. */
        private final String text;
        
        /** Var. */
        private final SelectionListener listener;
        
        /**
         * Creates a new instance.
         *
         * @param text
         * @param listener
         */
        public ARXWizardButton(String text, SelectionListener listener) {
            this.text = text;
            this.listener = listener;
        }
    }
    
    /** Var. */
    private final List<ARXWizardButton> buttons;
    
    /** Var. */
    private final Map<ARXWizardButton, Button> map;
    
    /**
     * Creates a new instance.
     *
     * @param parentShell
     * @param newWizard
     */
    protected ARXWizardDialog(Shell parentShell, IWizard newWizard) {
        super(parentShell, newWizard);
        this.buttons = null;
        this.map = null;
    }

    /**
     * Creates a new instance.
     *
     * @param parentShell
     * @param newWizard
     * @param buttons
     */
    protected ARXWizardDialog(Shell parentShell, IWizard newWizard,
                           List<ARXWizardButton> buttons) {
        super(parentShell, newWizard);
        this.buttons = buttons;
        this.map = new HashMap<ARXWizardButton, Button>();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.WizardDialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        this.getShell().setImages(Resources.getIconSet(getShell().getDisplay()));
        if (buttons != null){
            for (ARXWizardButton button : buttons){
                Button bt = super.createButton(parent, button.id, button.text, false);
                setButtonLayoutData(bt);
                bt.addSelectionListener(button.listener);
                map.put(button, bt);
            }
        }
        super.createButtonsForButtonBar(parent);
    }
   
    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.WizardDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Control ctrl = super.createDialogArea(parent);
        getProgressMonitor();
        return ctrl;
    }


    /**
     * Returns the button created for the given specification.
     *
     * @param button
     * @return
     */
    protected Button getButton(ARXWizardButton button){
        return map.get(button);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.WizardDialog#getProgressMonitor()
     */
    @Override
    protected IProgressMonitor getProgressMonitor() {
        ProgressMonitorPart monitor = (ProgressMonitorPart) super.getProgressMonitor();
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.heightHint = 0;
        monitor.setLayoutData(gridData);
        monitor.setVisible(false);
        return monitor;
    }
}
