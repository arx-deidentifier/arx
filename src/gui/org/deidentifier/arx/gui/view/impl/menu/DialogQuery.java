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

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataSelector;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IDialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import de.linearbits.objectselector.ICallback;
import de.linearbits.objectselector.SelectorTokenizer;

/**
 * Query dialog
 * 
 * @author Fabian Prasser
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
                
                DataSelector selector = null;
                synchronized(DialogQuery.this){
                    selector = DialogQuery.this.selector;
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

    /** TODO */
    private static final int INTERVAL               = 500;

    /** TODO */
    private Button           ok                     = null;

    /** TODO */
    private Button           cancel                 = null;

    /** TODO */
    private StyledText       text                   = null;

    /** TODO */
    private Label            status                 = null;

    /** TODO */
    private Data             data                   = null;

    /** TODO */
    private String           queryString            = null;

    /** TODO */
    private DataSelector     selector               = null;

    /** TODO */
    private ICallback        highlighter            = null;

    /** TODO */
    private List<StyleRange> styles                 = new ArrayList<StyleRange>();

    /** TODO */
    private boolean          stop                   = false;

    /** TODO */
    private List<Button>     singleSelectionButtons = new ArrayList<Button>();

    /** TODO */
    private List<Button>     multiSelectionButtons  = new ArrayList<Button>();

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
     * Creates a new button with which elements can be added to the text
     * @param text
     * @param items
     * @param group
     * @param label
     * @param openingSymbol
     * @param closingSymbol
     * @param span
     * @param multi
     */
    private void createButton(final StyledText text,
                              final Combo items,
                              final Group group,
                              final String label,
                              final String tooltip,
                              final String openingSymbol,
                              final String closingSymbol,
                              final int span,
                              final boolean multi) {
        
        final Button button = new Button(group, SWT.PUSH);
        button.setText(label);
        button.setToolTipText(tooltip);
        if (items != null) {
            singleSelectionButtons.add(button);
            button.setEnabled(true);
        } else if (multi) { 
            multiSelectionButtons.add(button);
            button.setEnabled(false);
        } else {
            singleSelectionButtons.add(button);
            button.setEnabled(true);
        }
        
        button.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).span(span, 1).create());
        button.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent arg0) {
                
                // Prepare
                Point selection = text.getSelectionRange();
                StringBuilder builder = new StringBuilder();
                String insert = openingSymbol;
                
                // Handle special case with combo
                if (items != null) {
                    if (selection.y != 0 || items.getSelectionIndex() == -1 ||
                        items.getItem(items.getSelectionIndex()) == null) { 
                        return; 
                    } else {
                        insert = "'" + items.getItem(items.getSelectionIndex()) + "'"; //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
                
                // Create new content
                if (multi && selection.y != 0) {

                    builder.append(text.getTextRange(0, selection.x));
                    builder.append(" "); //$NON-NLS-1$
                    builder.append(openingSymbol);
                    builder.append(" "); //$NON-NLS-1$
                    builder.append(text.getTextRange(selection.x, selection.y));
                    builder.append(" "); //$NON-NLS-1$
                    builder.append(closingSymbol);
                    builder.append(" "); //$NON-NLS-1$
                    builder.append(text.getTextRange(selection.x + selection.y, text.getText().length() - (selection.x + selection.y)));
                    
                } else if (!multi && selection.y == 0){

                    if (selection.x == 0){
                        builder.append(insert);
                        builder.append(" "); //$NON-NLS-1$
                        builder.append(text.getText());
                    } else if (selection.x == text.getText().length()){
                        builder.append(text.getText());
                        builder.append(" "); //$NON-NLS-1$
                        builder.append(insert);
                    } else { 
                        builder.append(text.getText(0, selection.x - 1));
                        builder.append(" "); //$NON-NLS-1$
                        builder.append(insert);
                        builder.append(" "); //$NON-NLS-1$
                        builder.append(text.getText(selection.x, text.getText().length()-1));
                    }
                } else {
                    return;
                }
                    
                // Replace and highlight
                text.setText(builder.toString());
                highlight();
                parse();
                text.setSelection(text.getText().length());
                updateButtons();
            }
        });
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
                public void check() {
                    // ignore
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
        
        synchronized (this) {
            
            // Query
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
            this.status.setText(Resources.getMessage("DialogQuery.10")); //$NON-NLS-1$
            this.queryString = text.getText();
            this.selector = selector;
            this.ok.setEnabled(true);
        }
    }
    
    /**
     * Updates all buttons
     */
    private void updateButtons() {
        int selectionLength = text.getSelectionRange().y;
        for (Button b : singleSelectionButtons) {
            b.setEnabled(selectionLength == 0);
        }
        for (Button b : multiSelectionButtons) {
            b.setEnabled(selectionLength != 0);
        }
    }

    @Override                                                    
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setImages(Resources.getIconSet(newShell.getDisplay()));
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

        parent.setLayout(GridLayoutFactory.swtDefaults().numColumns(2).create());

        Group query = new Group(parent, SWT.SHADOW_ETCHED_IN);
        query.setText(Resources.getMessage("DialogQuery.11")); //$NON-NLS-1$
        query.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
        query.setLayout(GridLayoutFactory.swtDefaults().numColumns(1).spacing(5, 5).create());

        text = new StyledText(query, SWT.BORDER | SWT.MULTI | SWT.WRAP);
        text.setText(this.queryString);
        text.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
        text.addModifyListener(new ModifyListener(){
            @Override
            public void modifyText(ModifyEvent arg0) {
                highlight();
                parse();
            }
        });
        
        // Update buttons
        text.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent arg0) {
                updateButtons();
            }
        });
        
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(GridDataFactory.fillDefaults().grab(false, true).create());
        composite.setLayout(GridLayoutFactory.swtDefaults().numColumns(1).spacing(0, 0).margins(0, 0).create());
        
        Group booleanOperators = new Group(composite, SWT.SHADOW_ETCHED_IN);
        booleanOperators.setText(Resources.getMessage("DialogQuery.12")); //$NON-NLS-1$
        booleanOperators.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
        booleanOperators.setLayout(GridLayoutFactory.swtDefaults().numColumns(2).create());
        
        createButton(text, null, booleanOperators, Resources.getMessage("DialogQuery.13"), Resources.getMessage("DialogQuery.14"), Resources.getMessage("DialogQuery.15"), "", 1, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        createButton(text, null, booleanOperators, Resources.getMessage("DialogQuery.17"), Resources.getMessage("DialogQuery.18"), Resources.getMessage("DialogQuery.19"), "", 1, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        createButton(text, null, booleanOperators, "(", Resources.getMessage("DialogQuery.22"), "(", "", 1, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        createButton(text, null, booleanOperators, ")", Resources.getMessage("DialogQuery.26"), ")", "", 1, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        createButton(text, null, booleanOperators, "( ... )", Resources.getMessage("DialogQuery.30"), "(", ")", 2, true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

        Group relationalOperators = new Group(composite, SWT.SHADOW_ETCHED_IN);
        relationalOperators.setText(Resources.getMessage("DialogQuery.33")); //$NON-NLS-1$
        relationalOperators.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
        relationalOperators.setLayout(GridLayoutFactory.swtDefaults().numColumns(2).create());

        createButton(text, null, relationalOperators, "=", Resources.getMessage("DialogQuery.35"), "=", "", 1, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        createButton(text, null, relationalOperators, "<>", Resources.getMessage("DialogQuery.39"), "<>", "", 1, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        createButton(text, null, relationalOperators, "<", Resources.getMessage("DialogQuery.43"), "<", "", 1, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        createButton(text, null, relationalOperators, "<=", Resources.getMessage("DialogQuery.47"), "<=", "", 1, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        createButton(text, null, relationalOperators, ">", Resources.getMessage("DialogQuery.51"), ">", "", 1, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        createButton(text, null, relationalOperators, ">=", Resources.getMessage("DialogQuery.55"), ">=", "", 1, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        
        Group fields = new Group(composite, SWT.SHADOW_ETCHED_IN);
        fields.setText(Resources.getMessage("DialogQuery.58")); //$NON-NLS-1$
        fields.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
        fields.setLayout(GridLayoutFactory.swtDefaults().numColumns(1).create());
        
        Combo combo = new Combo(fields, SWT.SINGLE | SWT.READ_ONLY | SWT.DROP_DOWN);
        for (int i = 0; i < data.getHandle().getNumColumns(); i++) {
            combo.add(data.getHandle().getAttributeName(i));
        }
        combo.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
        combo.select(0);
        createButton(text, combo, fields, Resources.getMessage("DialogQuery.59"), Resources.getMessage("DialogQuery.60"), "", "", 1, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        createButton(text, null, fields, Resources.getMessage("DialogQuery.63"), Resources.getMessage("DialogQuery.64"), "'value'", "", 1, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

        Group actions = new Group(composite, SWT.SHADOW_ETCHED_IN);
        actions.setText(Resources.getMessage("DialogQuery.67")); //$NON-NLS-1$
        actions.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
        actions.setLayout(GridLayoutFactory.swtDefaults().numColumns(1).create());
        
        Button select = new Button(actions, SWT.PUSH);
        select.setText(Resources.getMessage("DialogQuery.68")); //$NON-NLS-1$
        select.setToolTipText(Resources.getMessage("DialogQuery.69")); //$NON-NLS-1$
        select.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
        select.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
                text.selectAll();
                updateButtons();
            }
        });
        
        Button clear = new Button(actions, SWT.PUSH);
        clear.setText(Resources.getMessage("DialogQuery.70")); //$NON-NLS-1$
        clear.setToolTipText(Resources.getMessage("DialogQuery.71")); //$NON-NLS-1$
        clear.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
        clear.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
                Point selection = text.getSelectionRange();
                StringBuilder builder = new StringBuilder();
                if (selection.y != 0) {
                    builder.append(text.getTextRange(0, selection.x));
                    builder.append(text.getTextRange(selection.x + selection.y, text.getText().length() - (selection.x + selection.y)));
                    text.setText(builder.toString());
                    highlight();
                    parse();
                    updateButtons();
                }
            }   
        });
        
        status = new Label(parent, SWT.NONE);
        status.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).span(2, 1).create());
        status.setText(""); //$NON-NLS-1$
        
        highlight();
        new Thread(updater).start();
        
        return parent;
    }

    @Override
    protected ShellListener getShellListener() {
        return new ShellAdapter() {
            @Override
            public void shellClosed(final ShellEvent event) {
                setReturnCode(Window.CANCEL);
            }
        };
    }

    @Override
    protected boolean isResizable() {
        return false;
    }
}
