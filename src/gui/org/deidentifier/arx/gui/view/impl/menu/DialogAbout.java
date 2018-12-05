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

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IDialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * An about dialog.
 *
 * @author Fabian Prasser
 */
public class DialogAbout extends TitleAreaDialog implements IDialog {

    /**  License */
    private static final String LICENSE      = Resources.getLicenseText();
    
    /**  About */
    private static final String ABOUT        = Resources.getMessage("AboutDialog.16") + "\n" + //$NON-NLS-1$ //$NON-NLS-2$
                                               Resources.getMessage("AboutDialog.18") + "\n\n" + //$NON-NLS-1$ //$NON-NLS-2$
                                               Resources.getMessage("AboutDialog.21") + Resources.getVersion(); //$NON-NLS-1$
    
    /**  Contributors */
    private static final String CONTRIBUTORS = "Karol Babioch (data import wizard)\n" + //$NON-NLS-1$
                                               "Ledian Xhani (hierarchy editor)\n" + //$NON-NLS-1$
                                               "Ljubomir Dshevlekov (hierarchy editor)\n" +  //$NON-NLS-1$
                                               "Michael Schneider (risk analysis)\n" + //$NON-NLS-1$
                                               "Raffael Bild (heuristic search, differential privacy)\n" + //$NON-NLS-1$
                                               "Johanna Eicher (heuristic search, classification)\n" + //$NON-NLS-1$
                                               "Helmut Spengler (heuristic search)\n" + //$NON-NLS-1$
                                               "David Gassmann (HIPAA identifiers)\n" + //$NON-NLS-1$
                                               "Sebastian Stammler (performance improvements, l-diversity)\n" + //$NON-NLS-1$
                                               "Maximilian Zitzmann (distinction and separation)\n" + //$NON-NLS-1$
                                               "James Gaupp (game-theoretic privacy)\n" + //$NON-NLS-1$
                                               "Annika Saken (certificates)\n" + //$NON-NLS-1$
                                               "Martin Waltl (summary statistics, GUI improvements)\n" + //$NON-NLS-1$
                                               "Philip Offtermatt (performance improvements)"; //$NON-NLS-1$
    
    /**  Icon */
    private Image image;

    /**
     * Creates a new instance
     *
     * @param parentShell
     * @param controller
     */
    public DialogAbout(final Shell parentShell, final Controller controller) {
        super(parentShell);
        this.image = controller.getResources().getManagedImage("logo_small.png"); //$NON-NLS-1$
    }

    @Override
    public boolean close() {
        return super.close();
    }

    /**
     * Creates a link.
     *
     * @param parent
     * @param text
     * @param tooltip
     * @param url
     */
    private void createLink(Composite parent, String text, String tooltip, final String url){
        Link link = new Link(parent, SWT.NONE);
        link.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        link.setText(text);
        link.setToolTipText(tooltip);
        link.setBackground(parent.getBackground());
        link.addListener (SWT.Selection, new Listener () {
            public void handleEvent(Event event) {
                try {
                    Program.launch(url);
                } catch (Exception e){
                    /* Ignore*/
                }
            }
        });
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setImages(Resources.getIconSet(newShell.getDisplay()));
    }
    
    @Override
    protected void createButtonsForButtonBar(final Composite parent) {

        // Create OK Button
        parent.setLayoutData(SWTUtil.createFillGridData());
        final Button okButton = createButton(parent,
                                             Window.OK,
                                             Resources.getMessage("AboutDialog.15"), true); //$NON-NLS-1$
        okButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                setReturnCode(Window.OK);
                close();
            }
        });
    }
    
    @Override
    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);
        setTitle(Resources.getMessage("AboutDialog.12")); //$NON-NLS-1$
        setMessage(Resources.getMessage("AboutDialog.13"), IMessageProvider.INFORMATION); //$NON-NLS-1$
        if (image!=null) setTitleImage(image); //$NON-NLS-1$
        return contents;
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        parent.setLayout(new GridLayout());

        // Text
        final Label label = new Label(parent, SWT.CENTER | SWT.NONE);
        label.setText(ABOUT);
        label.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        
        // Folder
        CTabFolder folder = new CTabFolder(parent, SWT.BORDER);
        folder.setSimple(false);
        folder.setLayoutData(SWTUtil.createFillGridData());
        
        // License
        CTabItem item1 = new CTabItem(folder, SWT.NULL);
        item1.setText("License"); //$NON-NLS-1$
        final Text license = new Text(folder, SWT.NONE | SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
        license.setText(LICENSE);
        license.setEditable(false);
        license.setLayoutData(SWTUtil.createFillGridData());
        item1.setControl(license);
        
        // Contributors
        CTabItem item2 = new CTabItem(folder, SWT.NULL);
        item2.setText("Contributors"); //$NON-NLS-1$
        final Text contributors = new Text(folder, SWT.NONE | SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
        contributors.setText(CONTRIBUTORS);
        contributors.setEditable(false);
        contributors.setLayoutData(SWTUtil.createFillGridData());
        item2.setControl(contributors);
        
        // Information
        CTabItem item3 = new CTabItem(folder, SWT.NULL);
        item3.setText("Links"); //$NON-NLS-1$
        Composite composite3 = new Composite(folder, SWT.BORDER);
        composite3.setBackground(license.getBackground());
        item3.setControl(composite3);
        composite3.setLayout(SWTUtil.createGridLayout(1, false));
        createLink(composite3, "Website: <a>arx.deidentifier.org</a>", "Website", "http://arx.deidentifier.org"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        createLink(composite3, "Manual: <a>arx.deidentifier.org/anonymization-tool</a>", "Manual", "http://arx.deidentifier.org/anonymization-tool/"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        createLink(composite3, "API: <a>arx.deidentifier.org/api</a>", "API", "http://arx.deidentifier.org/api"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        createLink(composite3, "Downloads: <a>arx.deidentifier.org/downloads</a>", "Downloads", "http://arx.deidentifier.org/downloads"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        createLink(composite3, "Github: <a>github.com/arx-deidentifier</a>", "Github", "https://github.com/arx-deidentifier"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        return parent;
    }
    
    @Override
    protected boolean isResizable() {
        return false;
    }
}
