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

package org.deidentifier.arx.gui.view.impl.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IDialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * This class implements a dialog for creating a project.
 *
 * @author Fabian Prasser
 */
public class DialogProject extends TitleAreaDialog implements IDialog {

    /** Widget. */
    private Text   name        = null;
    
    /** Widget. */
    private Text   description = null;
    
    /** Widget. */
    private Button ok          = null;
    
    /** Model. */
    private Model  model       = null;
    
    /** Widget. */
    private Combo  locale      = null;
    
    /** Locale. */
    private Locale selectedLocale    = Locale.getDefault();
    
    /**
     * Creates a new instance.
     *
     * @param parent
     */
    public DialogProject(final Shell parent) {
        super(parent);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#create()
     */
    @Override
    public void create() {
        super.create();
        setTitle(Resources.getMessage("ProjectDialog.0")); //$NON-NLS-1$
        setMessage(Resources.getMessage("ProjectDialog.1"), IMessageProvider.NONE); //$NON-NLS-1$

        name.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(final ModifyEvent arg0) {
                model = new Model(name.getText(), description.getText(), selectedLocale);
                if (name.getText().equals("")) { //$NON-NLS-1$
                    ok.setEnabled(false);
                } else {
                    ok.setEnabled(true);
                }
            }
        });

        description.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(final ModifyEvent arg0) {
                model = new Model(name.getText(), description.getText(), selectedLocale);
            }
        });
        
        locale.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                if (locale.getSelectionIndex() >= 0) {
                    selectedLocale = new Locale(locale.getItem(locale.getSelectionIndex()).toLowerCase());
                } else {
                    selectedLocale = Locale.getDefault();
                }
                model = new Model(name.getText(), description.getText(), selectedLocale);
            }
        });
    }

    /**
     * Returns the resulting project.
     *
     * @return
     */
    public Model getProject() {
        return model;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setImages(Resources.getIconSet(newShell.getDisplay()));
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(final Composite parent) {

        final GridData gridData = new GridData();
        gridData.verticalAlignment = GridData.FILL;
        gridData.horizontalSpan = 2;
        gridData.verticalSpan = 2;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = false;
        gridData.horizontalAlignment = SWT.LEFT;
        parent.setLayoutData(gridData);
        parent.getParent().setLayoutData(gridData);

        // Create OK Button
        ok = createButton(parent,
                          Window.OK,
                          Resources.getMessage("ProjectDialog.3"), true); //$NON-NLS-1$
        ok.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                setReturnCode(Window.OK);
                close();
            }
        });
        ok.setEnabled(false);

        // Create Cancel Button
        parent.setLayoutData(SWTUtil.createFillGridData());
        final Button cancel = createButton(parent,
                                           Window.CANCEL,
                                           Resources.getMessage("ProjectDialog.4"), false); //$NON-NLS-1$
        cancel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                setReturnCode(Window.CANCEL);
                close();
            }
        });
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(final Composite parent) {
        final GridLayout l = new GridLayout();
        l.numColumns = 2;
        l.makeColumnsEqualWidth = false;
        parent.setLayout(l);

        // Build components
        final Label label = new Label(parent, SWT.NONE);
        label.setText(Resources.getMessage("ProjectDialog.5")); //$NON-NLS-1$
        label.setLayoutData(SWTUtil.createNoFillGridData());
        name = new Text(parent, SWT.BORDER);
        name.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        name.setText(""); //$NON-NLS-1$

        final Label label2 = new Label(parent, SWT.LEFT | SWT.WRAP);
        label2.setText(Resources.getMessage("ProjectDialog.7")); //$NON-NLS-1$
        label2.setLayoutData(SWTUtil.createNoFillGridData());
        description = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.WRAP |
                                       SWT.V_SCROLL);
        description.setLayoutData(SWTUtil.createFillGridData());
        description.setText(""); //$NON-NLS-1$
        
        final Label label3 = new Label(parent, SWT.LEFT | SWT.WRAP);
        label3.setText(Resources.getMessage("ProjectDialog.8")); //$NON-NLS-1$
        label3.setLayoutData(SWTUtil.createNoFillGridData());

        // Create list of locales
        List<String> languages = new ArrayList<String>();
        for (String lang : Locale.getISOLanguages()) {
            languages.add(lang.toUpperCase());
        }
        
        locale = new Combo(parent, SWT.READ_ONLY);
        locale.setItems(languages.toArray(new String[]{}));
        locale.select(languages.indexOf(Locale.getDefault().getLanguage().toUpperCase()));
        locale.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        
        return parent;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#getShellListener()
     */
    @Override
    protected ShellListener getShellListener() {
        return new ShellAdapter() {
            @Override
            public void shellClosed(final ShellEvent event) {
                setReturnCode(Window.CANCEL);
            }
        };
    }
}
