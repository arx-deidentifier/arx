/*
 * FLASH: Efficient, Stable and Optimal Data Anonymization
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

package org.deidentifier.flash.gui.view.impl.menu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deidentifier.flash.gui.Controller;
import org.deidentifier.flash.gui.SWTUtil;
import org.deidentifier.flash.gui.resources.Resources;
import org.deidentifier.flash.io.CSVDataInput;
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

public class SeparatorDialog extends TitleAreaDialog {

    private static final int        LINES      = 5;
    private int                     selection;
    private Table                   table;
    private final List<TableColumn> columns    = new ArrayList<TableColumn>();
    private final char[]            separators = { ';', ',', '|', '\t' };
    private final String[]          labels     = { ";", ",", "|", "Tab" };    //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    private final Controller        controller;
    private final String            file;
    private final boolean           data;

    public SeparatorDialog(final Shell parent,
                           final Controller controller,
                           final String file,
                           boolean data) {
        super(parent);
        this.controller = controller;
        this.file = file;
        this.data = data;
    }

    @Override
    public void create() {
        super.create();
        setTitle(Resources.getMessage("SeparatorDialog.4")); //$NON-NLS-1$
        setMessage(Resources.getMessage("SeparatorDialog.5"), IMessageProvider.INFORMATION); //$NON-NLS-1$
        super.getShell().setSize(500, 300);
        super.getShell().layout();
        SWTUtil.center(super.getShell(), super.getParentShell());
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
        table = new Table(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        GridData d = SWTUtil.createFillGridData();
        d.horizontalSpan = 2;
        d.grabExcessHorizontalSpace = true;
        d.grabExcessVerticalSpace = true;
        table.setLayoutData(d);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        try {
            detect(file);
            read(file);
        } catch (final Exception e) {
            controller.actionShowErrorDialog(Resources.getMessage("SeparatorDialog.9"), Resources.getMessage("SeparatorDialog.10") + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
            close();
        }

        final Combo combo = new Combo(parent, SWT.NONE);
        d = SWTUtil.createFillHorizontallyGridData();
        d.horizontalSpan = 2;
        combo.setLayoutData(d);
        for (final String s : labels) {
            combo.add(s);
        }
        combo.select(selection);
        combo.pack();
        combo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {

                try {
                    if (combo.getSelectionIndex() == -1) { return; }
                    selection = combo.getSelectionIndex();
                    read(file);
                } catch (final Exception e) {
                    controller.actionShowErrorDialog(Resources.getMessage("SeparatorDialog.11"), Resources.getMessage("SeparatorDialog.12") + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
                    close();
                }
            }
        });

        return parent;
    }

    /**
     * Detects the most frequent separator in the first few lines
     * 
     * @param file
     * @throws IOException
     */
    private void detect(final String file) throws IOException {

        // Open file
        final BufferedReader r = new BufferedReader(new FileReader(new File(file)));

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
                selection = key;
            }
        }
    }

    public char getSeparator() {
        return separators[selection];
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

    /**
     * Reds the first few files with chosen separator
     * 
     * @param file
     * @return
     * @throws IOException
     */
    private void read(final String file) throws IOException {

        // Read the first few lines
        final CSVDataInput in = new CSVDataInput(file, separators[selection]);
        final Iterator<String[]> it = in.iterator();
        final List<String[]> data = new ArrayList<String[]>();

        int count = 0;
        while (it.hasNext() && (count < LINES)) {
            data.add(it.next());
            count++;
        }
        in.close();

        // In case of hierarchy, add header
        if (!this.data) {
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
        for (final TableColumn c : columns) {
            c.dispose();
        }
        columns.clear();

        if (data.size() == 0) { return; }

        for (final String s : data.get(0)) {
            final TableColumn c = new TableColumn(table, SWT.NONE);
            c.setText(s);
            columns.add(c);
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
}
