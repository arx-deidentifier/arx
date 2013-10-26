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

import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataSelector;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IDialog;
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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import de.linearbits.objectselector.ICallback;
import de.linearbits.objectselector.SelectorTokenizer;

public class DialogQuery extends TitleAreaDialog implements IDialog {

    private static enum Operator{
        EQUALS,
        GEQ,
        LEQ,
        LESS,
        GREATER
    }
    
    private Runnable updater = new Runnable(){
        
        private DataSelector previous = null; 
        
        @Override
        public void run() {
            while (!stop){
                try {
                    Thread.sleep(INTERVAL);
                } catch (InterruptedException e) {
                    // Ignore
                }
                if (selector != null && selector != previous){
                    previous = selector;
                    int count = 0;
                    for (int i=0; i<data.getHandle().getNumRows(); i++){
                        count += selector.isSelected(i) ? 1 : 0;
                    }
                    final int fcount = count;
                    if (status!=null && !status.isDisposed()){
                        Display.getDefault().asyncExec(new Runnable() {
                            public void run() {
                                status.setText(Resources.getMessage("QueryDialog.8")+fcount); //$NON-NLS-1$
                            }
                        });
                    }
                }
            }
        }
    };
    
    private static final int INTERVAL = 500;
    
    private Button           ok          = null;
    private Button           cancel      = null;
    private StyledText       text        = null;
    private Label            status       = null;
    private Data             data        = null;
    private String           queryString = null;
    private DataSelector     selector    = null;
    private ICallback        highlighter = null;
    private List<StyleRange> styles      = new ArrayList<StyleRange>();
    private boolean          stop        = false;

    public DialogQuery(final Data data, final Shell parent, String initial) {
        super(parent);
        this.queryString = initial;
        this.data = data;
    }

    @Override
    public boolean close() {
        return super.close();
    }

    public DialogQueryResult getResult() {
        return new DialogQueryResult(queryString, selector);
    }
    
    private void highlight() {
        
        if (highlighter==null){
            
            highlighter = new ICallback(){

                @Override
                public void and(int start, int length) {
                    StyleRange style = new StyleRange();
                    style.start = start;
                    style.length = length;
                    style.fontStyle = SWT.BOLD;
                    style.foreground = GUIHelper.COLOR_GRAY;
                    styles.add(style);
                }

                @Override
                public void begin(int start) {
                    StyleRange style = new StyleRange();
                    style.start = start;
                    style.length = 1;
                    style.fontStyle = SWT.BOLD;
                    style.foreground = GUIHelper.COLOR_GREEN;
                    styles.add(style);
                }

                @Override
                public void end(int start) {
                    StyleRange style = new StyleRange();
                    style.start = start;
                    style.length = 1;
                    style.fontStyle = SWT.BOLD;
                    style.foreground = GUIHelper.COLOR_GREEN;
                    styles.add(style);
                }

                @Override
                public void equals(int start) {
                    StyleRange style = new StyleRange();
                    style.start = start;
                    style.length = 1;
                    style.fontStyle = SWT.BOLD;
                    style.foreground = GUIHelper.COLOR_BLUE;
                    styles.add(style);
                }

                @Override
                public void field(int start, int length) {
                    StyleRange style = new StyleRange();
                    style.start = start;
                    style.length = length;
                    style.fontStyle = SWT.BOLD;
                    style.foreground = GUIHelper.COLOR_RED;
                    styles.add(style);
                }

                @Override
                public void geq(int start, int length) {
                    StyleRange style = new StyleRange();
                    style.start = start;
                    style.length = length;
                    style.fontStyle = SWT.BOLD;
                    style.foreground = GUIHelper.COLOR_BLUE;
                    styles.add(style);
                }

                @Override
                public void greater(int start) {
                    StyleRange style = new StyleRange();
                    style.start = start;
                    style.length = 1;
                    style.fontStyle = SWT.BOLD;
                    style.foreground = GUIHelper.COLOR_BLUE;
                    styles.add(style);
                }

                @Override
                public void invalid(int start) {
                    // ignore
                }

                @Override
                public void leq(int start, int length) {
                    StyleRange style = new StyleRange();
                    style.start = start;
                    style.length = length;
                    style.fontStyle = SWT.BOLD;
                    style.foreground = GUIHelper.COLOR_BLUE;
                    styles.add(style);
                }

                @Override
                public void less(int start) {
                    StyleRange style = new StyleRange();
                    style.start = start;
                    style.length = 1;
                    style.fontStyle = SWT.BOLD;
                    style.foreground = GUIHelper.COLOR_BLUE;
                    styles.add(style);
                }

                @Override
                public void or(int start, int length) {
                    StyleRange style = new StyleRange();
                    style.start = start;
                    style.length = length;
                    style.fontStyle = SWT.BOLD;
                    style.foreground = GUIHelper.COLOR_GRAY;
                    styles.add(style);
                }
                
                @Override
                public void value(int start, int length) {
                    StyleRange style = new StyleRange();
                    style.start = start;
                    style.length = length;
                    style.fontStyle = SWT.BOLD;
                    style.foreground = GUIHelper.COLOR_DARK_GRAY;
                    styles.add(style);
                }
            };
        }
        
        styles.clear();
        SelectorTokenizer<Integer> tokenizer = new SelectorTokenizer<Integer>(highlighter);
        tokenizer.tokenize(text.getText());

        text.setRedraw(false);
        text.setStyleRanges(styles.toArray(new StyleRange[styles.size()]));        
        text.setRedraw(true);
    }

    private void parse() {
        final String query = text.getText();
        final DataSelector selector;
        try {
            selector = DataSelector.create(data, query);
            selector.build();
        } catch (Exception e){
            this.status.setText(e.getMessage());
            this.ok.setEnabled(false);
            this.selector = null;
            return;
        }
        this.status.setText("OK");
        this.queryString = text.getText();
        this.selector = selector;
        this.ok.setEnabled(true);
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
                stop = true;
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
                stop = true;
                close();
            }
        });
        
        parse();
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

        text = new StyledText(parent, SWT.BORDER | SWT.MULTI | SWT.WRAP);
        text.setLayoutData(SWTUtil.createFillGridData());
        text.setText(this.queryString);
        text.addModifyListener(new ModifyListener(){
            @Override
            public void modifyText(ModifyEvent arg0) {
                highlight();
                parse();
            }
        });

        status = new Label(parent, SWT.NONE);
        status.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        status.setText("");
        
        highlight();
        new Thread(updater).start();
        
        return parent;
    }

    @Override
    protected boolean isResizable() {
        return false;
    }
}
