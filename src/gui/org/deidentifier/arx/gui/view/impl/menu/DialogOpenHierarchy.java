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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.resources.Charsets;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IDialog;
import org.deidentifier.arx.io.CSVDataInput;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * 
 */
public class DialogOpenHierarchy extends TitleAreaDialog implements IDialog {

    /** Constant */
    private static final int LINES      = 5;

    /** View */
    private Table            table;

    /** Model */
    private int              selectionSeparator;
    /** Model */
    private int              selectionCharset;
    /** Model */
    private final char[]     separators = { ';', ',', '|', '\t' };               //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    /** Model */
    private final String[]   labels     = { ";", ",", "|", "Tab" };              //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    /** Model */
    private final String[]   charsets   = Charsets.getNamesOfAvailableCharsets();
    /** Model */
    private final String     file;
    /** Model */
    private final boolean    data;

    /**
     * 
     *
     * @param parent
     * @param controller
     * @param file
     * @param data
     */
    public DialogOpenHierarchy(final Shell parent,
                           final Controller controller,
                           final String file,
                           boolean data) {
        super(parent);
        this.file = file;
        this.data = data;
    }
    
    @Override
    public void create() {
        super.create();
        setTitle(Resources.getMessage("SeparatorDialog.4")); //$NON-NLS-1$
        setMessage(Resources.getMessage("SeparatorDialog.5"), IMessageProvider.INFORMATION); //$NON-NLS-1$
        super.getShell().setSize(500, 350);
        super.getShell().layout();
        SWTUtil.center(super.getShell(), super.getParentShell());
    }
    
    /**
     * Returns the selected charset
     */
    public Charset getCharset() {
        return Charsets.getCharsetForName(charsets[selectionCharset]);
    }
    
    /**
     * Returns the selected separator 
     *
     * @return
     */
    public char getSeparator() {
        return separators[selectionSeparator];
    }

    /**
     * Detects the most frequent separator in the first few lines.
     *
     * @param file
     * @throws IOException
     */
    private void detect(final String file) throws IOException {

        // Open file
        final Charset charset = Charsets.getCharsetForName(charsets[selectionCharset]);
        final BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));

        // Count chars
        int count = 0;
        String line = r.readLine();
        final Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        while ((count < LINES) && (line != null)) {
            final char[] a = line.toCharArray();
            for (final char c : a) {
                for (int i = 0; i < separators.length; i++) {
                    if (c == separators[i]) {
                        if (!map.containsKey(i)) {
                            map.put(i, 0);
                        }
                        map.put(i, map.get(i) + 1);
                    }
                }
            }
            line = r.readLine();
            count++;
        }

        // Close
        r.close();

        // Choose max
        if (map.isEmpty()) { return; }
        int max = Integer.MIN_VALUE;
        for (final int key : map.keySet()) {
            if (map.get(key) > max) {
                max = map.get(key);
                selectionSeparator = key;
            }
        }
    }

    /**
     * Returns the index of the default charset
     * @return
     */
    private int getIndexOfDefaultCharset() {
        int index = 0;
        for (String charset : charsets) {
            if (charset.equals(Charsets.getNameOfDefaultCharset())) {
                return index;
            }
            index++;
        }
        return index;
    }

    /**
     * Reds the first few files with chosen separator.
     *
     * @param file
     * @throws IOException
     */
    private void read(final String file) throws IOException {

        // Charset
        final Charset charset = Charsets.getCharsetForName(charsets[selectionCharset]);
        
        // Read the first few lines
        final CSVDataInput in = new CSVDataInput(file, charset, separators[selectionSeparator]);
        final Iterator<String[]> it = in.iterator(false);
        final List<String[]> data = new ArrayList<String[]>();

        int count = 0;
        while (it.hasNext() && (count < LINES)) {
            data.add(it.next());
            count++;
        }
        in.close();

        // In case of hierarchy, add header
        if (!this.data && data.size() > 0) {
            // Duplicate last entry
            data.add(data.get(data.size() - 1));

            // Shift all entries
            for (int i = data.size() - 2; i >= 0; i--) {
                data.set(i + 1, data.get(i));
            }

            // Add header
            int length = data.get(0).length;
            String[] header = new String[length];
            for (int i = 0; i < length; i++) {
                header[i] = Resources.getMessage("SeparatorDialog.6") + (i + 1); //$NON-NLS-1$
            }
            data.set(0, header);
        }

        // Add to table
        table.setRedraw(false);
        table.removeAll();
        for (final TableColumn c : table.getColumns()) {
            c.dispose();
        }

        if (data.size() == 0) { return; }

        for (final String s : data.get(0)) {
            final TableColumn c = new TableColumn(table, SWT.NONE);
            c.setText(s);
            c.pack();
        }
        for (int i = 1; i < data.size(); i++) {
            final TableItem item = new TableItem(table, SWT.NONE);
            for (int j = 0; j < data.get(i).length; j++) {
                item.setText(j, data.get(i)[j]);
            }
        }
        table.setRedraw(true);
        table.redraw();
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
        final Button ok = createButton(parent,
                                       Window.OK,
                                       Resources.getMessage("SeparatorDialog.7"), true); //$NON-NLS-1$
        ok.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                setReturnCode(Window.OK);
                close();
            }
        });

        // Create cancel Button
        parent.setLayoutData(SWTUtil.createFillGridData());
        final Button cancel = createButton(parent,
                                           Window.CANCEL,
                                           Resources.getMessage("SeparatorDialog.8"), false); //$NON-NLS-1$
        cancel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                setReturnCode(Window.CANCEL);
                close();
            }
        });
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        parent.setLayout(new GridLayout());
        final GridLayout l = new GridLayout();
        l.numColumns = 2;
        parent.setLayout(l);

        // Build components
        table = SWTUtil.createTable(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        GridData d = SWTUtil.createFillGridData();
        d.horizontalSpan = 2;
        d.grabExcessHorizontalSpace = true;
        d.grabExcessVerticalSpace = true;
        table.setLayoutData(d);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        try {
            this.selectionCharset = getIndexOfDefaultCharset();
            detect(file);
            read(file);
        } catch (final Exception e) {
            if (e instanceof RuntimeException){
                throw (RuntimeException)e;
            } else {
                throw new RuntimeException(e);
            }
        }

        final Combo combo = new Combo(parent, SWT.NONE);
        d = SWTUtil.createFillHorizontallyGridData();
        d.horizontalSpan = 2;
        combo.setLayoutData(d);
        for (final String s : labels) {
            combo.add(s);
        }
        combo.select(selectionSeparator);
        combo.pack();
        combo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {

                try {
                    if (combo.getSelectionIndex() == -1) { return; }
                    selectionSeparator = combo.getSelectionIndex();
                    read(file);
                } catch (final Exception e) {
                    if (e instanceof RuntimeException){
                        throw (RuntimeException)e;
                    } else {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        final Combo combo2 = new Combo(parent, SWT.NONE);
        d = SWTUtil.createFillHorizontallyGridData();
        d.horizontalSpan = 2;
        combo2.setLayoutData(d);
        for (final String s : charsets) {
            combo2.add(s);
        }
        combo2.select(getIndexOfDefaultCharset());
        combo2.pack();
        combo2.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {

                try {
                    if (combo2.getSelectionIndex() == -1) { return; }
                    selectionCharset = combo2.getSelectionIndex();
                    read(file);
                } catch (final Exception e) {
                    if (e instanceof RuntimeException){
                        throw (RuntimeException)e;
                    } else {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        return parent;
    }

    @Override
    protected ShellListener getShellListener() {
        return new ShellAdapter() {
            @Override
            public void shellClosed(final ShellEvent event) {
                event.doit = false;
            }
        };
    }

    @Override
    protected boolean isResizable() {
        return false;
    }
}
