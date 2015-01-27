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
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import de.linearbits.objectselector.ICallback;
import de.linearbits.objectselector.SelectorTokenizer;

/**
 * 
 */
public class DialogQuery extends TitleAreaDialog implements IDialog {
    
    /**  TODO */
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
    
    /**  TODO */
    private static final int INTERVAL = 500;
    
    /**  TODO */
    private Button           ok          = null;
    
    /**  TODO */
    private Button           cancel      = null;
    
    /**  TODO */
    private StyledText       text        = null;
    
    /**  TODO */
    private Label            status       = null;
    
    /**  TODO */
    private Data             data        = null;
    
    /**  TODO */
    private String           queryString = null;
    
    /**  TODO */
    private DataSelector     selector    = null;
    
    /**  TODO */
    private ICallback        highlighter = null;
    
    /**  TODO */
    private List<StyleRange> styles      = new ArrayList<StyleRange>();
    
    /**  TODO */
    private boolean          stop        = false;

    /**
     * 
     *
     * @param data
     * @param parent
     * @param initial
     */
    public DialogQuery(final Data data, final Shell parent, String initial) {
        super(parent);
        this.queryString = initial;
        this.data = data;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#close()
     */
    @Override
    public boolean close() {
        return super.close();
    }
    
    /**
     * 
     *
     * @return
     */
    public DialogQueryResult getResult() {
        return new DialogQueryResult(queryString, selector);
    }
    
    /**
     * 
     */
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
                public void neq(int start, int length) {
                    StyleRange style = new StyleRange();
                    style.start = start;
                    style.length = length;
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
    
    /**
     * 
     */
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

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.TitleAreaDialog#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);
        setTitle(Resources.getMessage("QueryDialog.0")); //$NON-NLS-1$
        setMessage(Resources.getMessage("QueryDialog.1"), IMessageProvider.NONE); //$NON-NLS-1$
        return contents;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
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

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#isResizable()
     */
    @Override
    protected boolean isResizable() {
        return false;
    }
}
