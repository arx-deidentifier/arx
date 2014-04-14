/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.arx.gui.view.impl.wizards;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * compact and allows adding additional buttons
 * @author Fabian Prasser
 */
public class ARXWizardDialog extends WizardDialog {

    public static class ARXWizardButton{
        static int ID = Integer.MAX_VALUE - 10;
        private final int id = ID--;
        private final String text;
        private final SelectionListener listener;
        public ARXWizardButton(String text, SelectionListener listener) {
            this.text = text;
            this.listener = listener;
        }
    }
    
    private final List<ARXWizardButton> buttons;
    private final Map<ARXWizardButton, Button> map;
    
    /**
     * Creates a new instance
     * @param parentShell
     * @param newWizard
     */
    public ARXWizardDialog(Shell parentShell, IWizard newWizard) {
        super(parentShell, newWizard);
        this.buttons = null;
        this.map = null;
    }

    public ARXWizardDialog(Shell parentShell, IWizard newWizard,
                           List<ARXWizardButton> buttons) {
        super(parentShell, newWizard);
        this.buttons = buttons;
        this.map = new HashMap<ARXWizardButton, Button>();
    }

    public Button getButton(ARXWizardButton button){
        return map.get(button);
    }
   
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
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


    @Override
    protected Control createDialogArea(Composite parent) {
        Control ctrl = super.createDialogArea(parent);
        getProgressMonitor();
        return ctrl;
    }
    
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
