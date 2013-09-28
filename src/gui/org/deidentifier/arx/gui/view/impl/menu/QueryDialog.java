/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
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

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class QueryDialog extends TitleAreaDialog {

    private Button           ok         = null;
    private Button           cancel     = null;
    private StyledText       text       = null;
    private Controller       controller = null;
    private String           initial    = null;

    public QueryDialog(final Controller controller, final Shell parent, String initial) {
        super(parent);
        this.initial = initial;
        this.controller = controller;
    }

    @Override
    public boolean close() {
        return super.close();
    }

    public String getQuery() {
        return text.getText();
    }

    @Override
    protected void createButtonsForButtonBar(final Composite parent) {

        parent.setLayoutData(SWTUtil.createFillGridData());

        // Create OK Button
        ok = createButton(parent, Window.OK, Resources.getMessage("ProjectDialog.3"), true); //$NON-NLS-1$
        ok.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                setReturnCode(Window.OK);
                close();
            }
        });
        ok.setEnabled(false);

        // Create Cancel Button
        cancel = createButton(parent, Window.CANCEL, Resources.getMessage("ProjectDialog.4"), false); //$NON-NLS-1$
        cancel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                setReturnCode(Window.CANCEL);
                close();
            }
        });
    }

    @Override
    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);
        setTitle(Resources.getMessage("QueryDialog.0")); //$NON-NLS-1$
        setMessage(Resources.getMessage("QueryDialog.1"), IMessageProvider.NONE); //$NON-NLS-1$
        return contents;
    }

    @Override
    protected Control createDialogArea(final Composite parent) {

        parent.setLayout(new GridLayout());

        text = new StyledText(parent, SWT.BORDER);
        final GridData d = SWTUtil.createFillGridData();
        d.heightHint = 100;
        text.setLayoutData(d);
        text.setText(this.initial);
        text.addModifyListener(new ModifyListener(){
            @Override
            public void modifyText(ModifyEvent arg0) {
                parse();
            }
        });

        return parent;
    }
    
    private void parse() {
        
        text.setRedraw(false);

        List<StyleRange> styles = new ArrayList<StyleRange>();

        int quote = -1;
        boolean first = true;
        char[] data = text.getText().toCharArray();
        for (int i=0; i<data.length; i++){
            if (data[i]=='\\'){
             // Skip next
                i++; 
            } else if (data[i]=='"'){
                // Start quote
                if (quote == -1){
                    quote = i; 
                // End quote
                } else {
                    StyleRange style = new StyleRange();
                    style.start = quote;
                    style.length = i-quote+1;
                    style.fontStyle = SWT.BOLD;
                    style.foreground = first ? GUIHelper.COLOR_RED : GUIHelper.COLOR_DARK_GRAY;
                    styles.add(style);
                    quote = -1;
                    first = !first;
                }
            // Brackets
            } else if (quote == -1 && (data[i]=='(' || data[i]==')')) {
                StyleRange style = new StyleRange();
                style.start = i;
                style.length = 1;
                style.fontStyle = SWT.BOLD;
                style.foreground = GUIHelper.COLOR_GREEN;
                styles.add(style);
            // And
           }else if (quote == -1 && i<data.length-3 && data[i]=='a' && data[i+1]=='n' && data[i+2]=='d') {
                StyleRange style = new StyleRange();
                style.start = i;
                style.length = 3;
                style.fontStyle = SWT.BOLD;
                style.foreground = GUIHelper.COLOR_GRAY;
                styles.add(style);
           // Or
           }else if (quote == -1 && i<data.length-2 && data[i+1]=='o' && data[i+2]=='r') {
                StyleRange style = new StyleRange();
                style.start = i;
                style.length = 2;
                style.fontStyle = SWT.BOLD;
                style.foreground = GUIHelper.COLOR_GRAY;
                styles.add(style);
            // Equals, Less, Greater
            } else if (quote == -1 && (data[i]=='=' || data[i]=='<' || data[i]=='>')) {
                StyleRange style = new StyleRange();
                style.start = i;
                style.length = 1;
                style.fontStyle = SWT.BOLD;
                style.foreground = GUIHelper.COLOR_BLUE;
                styles.add(style);
            // LEQ or GEQ
            } else if ((quote == -1 && i<data.length-1 && data[i]=='<' && data[i+1]=='=') ||
                       (quote == -1 && i<data.length-1 && data[i]=='>' && data[i+1]=='=')) {
                StyleRange style = new StyleRange();
                style.start = i;
                style.length = 2;
                style.fontStyle = SWT.BOLD;
                style.foreground = GUIHelper.COLOR_BLUE;
                styles.add(style);
                i++;
            }
            if (i>=data.length) break;
        }
        
        text.setStyleRanges(styles.toArray(new StyleRange[styles.size()]));
        
        text.setRedraw(true);
    }

    @Override
    protected boolean isResizable() {
        return false;
    }
}
