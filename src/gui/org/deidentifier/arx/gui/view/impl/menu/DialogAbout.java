/*
 * ARX: Powerful Data Anonymization
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
 * An about dialog
 * @author Fabian Prasser
 */
public class DialogAbout extends TitleAreaDialog implements IDialog {

    private static final String LICENSE = Resources.getMessage("AboutDialog.0") + Resources.getMessage("AboutDialog.1") +
                                          Resources.getMessage("AboutDialog.2") + Resources.getMessage("AboutDialog.3") + "\n" +
                                          Resources.getMessage("AboutDialog.5") + Resources.getMessage("AboutDialog.6") +
                                          Resources.getMessage("AboutDialog.7") + Resources.getMessage("AboutDialog.8") + "\n" +
                                          Resources.getMessage("AboutDialog.10") + Resources.getMessage("AboutDialog.11");
    
    private static final String ABOUT =   Resources.getMessage("AboutDialog.16") + "\n" +
                                          Resources.getMessage("AboutDialog.18") + "\n\n" +
                                          Resources.getMessage("AboutDialog.21") + Resources.getVersion();
    
    private static final String CONTRIBUTORS = "Karol Babioch (data import wizard)\n" +
                                               "Ledian Xhani (hierarchy editor)\n" +
                                               "Ljubomir Dshevlekov (hierarchy editor)";
    
    private Image image;

    /**
     * Constructor
     * @param parentShell
     * @param controller
     */
    public DialogAbout(final Shell parentShell, final Controller controller) {
        super(parentShell);
        this.image = controller.getResources().getImage("logo_small.png"); //$NON-NLS-1$
    }

    @Override
    public boolean close() {
        if (image != null)
            image.dispose();
        return super.close();
    }

    /**
     * Creates a link
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
        item1.setText("License");
        final Text license = new Text(folder, SWT.NONE | SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
        license.setText(LICENSE);
        license.setEditable(false);
        license.setLayoutData(SWTUtil.createFillGridData());
        item1.setControl(license);
        
        // Contributors
        CTabItem item2 = new CTabItem(folder, SWT.NULL);
        item2.setText("Contributors");
        Composite composite = new Composite(folder, SWT.BORDER);
        composite.setBackground(license.getBackground());
        item2.setControl(composite);
        composite.setLayout(SWTUtil.createGridLayout(1, false));
        
        final Label contributors = new Label(composite, SWT.NONE);
        contributors.setText(CONTRIBUTORS);
        contributors.setBackground(license.getBackground());
        contributors.setLayoutData(SWTUtil.createFillGridData());
        
        // Information
        CTabItem item3 = new CTabItem(folder, SWT.NULL);
        item3.setText("Links");
        Composite composite3 = new Composite(folder, SWT.BORDER);
        composite3.setBackground(license.getBackground());
        item3.setControl(composite3);
        composite3.setLayout(SWTUtil.createGridLayout(1, false));
        createLink(composite3, "Website: <a>arx.deidentifier.org</a>", "Website", "http://arx.deidentifier.org");
        createLink(composite3, "Manual: <a>arx.deidentifier.org/anonymization-tool</a>", "Manual", "http://arx.deidentifier.org/anonymization-tool/");
        createLink(composite3, "API: <a>arx.deidentifier.org/api</a>", "API", "http://arx.deidentifier.org/api");
        createLink(composite3, "Downloads: <a>arx.deidentifier.org/downloads</a>", "Downloads", "http://arx.deidentifier.org/downloads");
        createLink(composite3, "Github: <a>github.com/arx-deidentifier</a>", "Github", "https://github.com/arx-deidentifier");
        return parent;
    }
    
    @Override
    protected boolean isResizable() {
        return false;
    }
}
