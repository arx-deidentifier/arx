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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.deidentifier.flash.gui.SWTUtil;
import org.deidentifier.flash.gui.resources.Resources;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class HierarchyWizardPageFanout extends WizardPage{

    private static final int                LABEL_WIDTH = 35;
    private int                             max         = 15;
    private final Combo[]                   combos      = new Combo[max - 1];
    private final Label[]                   labels1     = new Label[max];
    private final Label[]                   labels3     = new Label[max];
    private final HierarchyWizardModel      model;
    private Text                            field;

    public HierarchyWizardPageFanout(final HierarchyWizardModel model) {
        super(""); //$NON-NLS-1$
        this.model = model;
        setTitle(Resources.getMessage("HierarchyWizardPageFanout.1")); //$NON-NLS-1$
        setDescription(Resources.getMessage("HierarchyWizardPageFanout.2")); //$NON-NLS-1$
        for (int i = 0; i < 20; i++) {
            if (Math.pow(2, i) > model.getRows()) {
                max = i;
                break;
            }
        }
        setPageComplete(false);
    }

    @Override
    public boolean canFlipToNextPage() {
        return isPageComplete();
    }

    @Override
    public void createControl(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);

        composite.setLayoutData(SWTUtil.createFillGridData());
        final GridLayout groupInputGridLayout = new GridLayout();
        groupInputGridLayout.numColumns = 4;
        groupInputGridLayout.makeColumnsEqualWidth = false;
        composite.setLayout(groupInputGridLayout);

        final Label kLabel = new Label(composite, SWT.CENTER);
        kLabel.setText(Resources.getMessage("HierarchyWizardPageFanout.3")); //$NON-NLS-1$
        GridData g = new GridData();
        g.widthHint = LABEL_WIDTH * 2;
        kLabel.setLayoutData(g);

        field = new Text(composite, SWT.BORDER);
        field.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        field.setTextLimit(5);

        field.addVerifyListener(new VerifyListener() {
            @Override
            public void verifyText(final VerifyEvent event) {

                event.doit = true;
                final String text = event.text;
                final char[] chars = text.toCharArray();

                for (int i = 0; i < chars.length; i++) {
                    if (!Character.isDigit(chars[i])) {
                        event.doit = false;
                        break;
                    }
                }
            }
        });

        field.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(final ModifyEvent arg0) {
                if ((field.getText() != null)) {

                    String text = field.getText();
                    if (text.equals("")) { //$NON-NLS-1$
                        text = "0"; //$NON-NLS-1$
                    }
                    final int value = Integer.valueOf(text);
                    if ((value > 1) && (value <= model.getRows())) {
                        modified(0);
                    } else {
                        for (final Combo c : combos) {
                            if (c != null) {
                                c.setItems(new String[] {});
                            }
                        }
                        setPageComplete(isPageComplete());
                    }

                    // Update labels
                    for (int i = 0; i < labels1.length; i++) {
                        updateLabel(i);
                    }
                }
            }
        });
        createLabel(0, composite);

        final NumberFormat f = new DecimalFormat("00"); //$NON-NLS-1$
        for (int i = 0; i < (max - 1); i++) {
            final Label label = new Label(composite, SWT.CENTER);
            g = new GridData();
            g.widthHint = LABEL_WIDTH * 2;
            label.setLayoutData(g);
            label.setText(Resources.getMessage("HierarchyWizardPageFanout.7") + f.format(i + 2)); //$NON-NLS-1$
            combos[i] = new Combo(composite, SWT.READ_ONLY);
            combos[i].setLayoutData(SWTUtil.createFillHorizontallyGridData());
            combos[i].setItems(new String[] {});
            combos[i].select(0);
            final int j = i;
            combos[i].addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(final SelectionEvent arg0) {
                    final int k = combos[j].getSelectionIndex();
                    if (k != -1) {
                        modified(j + 1);
                    }
                }
            });
            createLabel(i + 1, composite);
        }

        field.setText("2"); //$NON-NLS-1$

        setControl(composite);
    }

    private void createLabel(final int i, final Composite composite) {

        // Left
        labels1[i] = new Label(composite, SWT.RIGHT);
        final GridData d = new GridData();
        d.widthHint = LABEL_WIDTH;
        labels1[i].setLayoutData(d);
        // Right
        labels3[i] = new Label(composite, SWT.LEFT);
        final GridData d3 = new GridData();
        d3.widthHint = LABEL_WIDTH;
        labels3[i].setLayoutData(d3);
    }

    private boolean isOverflow(final int left, final int right) {
        if (right > 0 ? (left > (Integer.MAX_VALUE / right)) ||
                        (left < (Integer.MIN_VALUE / right))
                : (right < -1 ? (left > (Integer.MIN_VALUE / right)) ||
                                (left < (Integer.MAX_VALUE / right))
                        : (right == -1) && (left == Integer.MIN_VALUE))) { return true; }
        return false;
    }

    @Override
    public boolean isPageComplete() {

        final List<Integer> vals = new ArrayList<Integer>();
        if ((field.getText() != null) && !field.getText().equals("")) { //$NON-NLS-1$
            vals.add(Integer.valueOf(field.getText()));
        } else {
            return false;
        }
        if ((vals.size() > 0) && (vals.get(vals.size() - 1) >= model.getRows())) {
            model.setFanout(vals);
            return true;
        }
        for (int i = 0; i < max; i++) {
            if ((combos[i] != null) && (combos[i].getItemCount() > 0)) {
                final int k = combos[i].getSelectionIndex();
                if (k > -1) {
                    vals.add(Integer.valueOf(combos[i].getItem(k)));
                }
            }
        }
        if ((vals.size() > 0) && (vals.get(vals.size() - 1) >= model.getRows())) {
            model.setFanout(vals);
            return true;
        }
        return false;
    }

    private void modified(final int index) {

        // Have we found an upper boundary as first entry in a combobox?
        boolean upper = false;
        for (int i = index; i < (max - 1); i++) {
            final List<String> items = new ArrayList<String>();
            int base = Integer.valueOf(field.getText());
            if ((i == 0) || (combos[i - 1].getItemCount() > 0)) {
                if (i > 0) {
                    final int k = combos[i - 1].getSelectionIndex();
                    base = Integer.valueOf(combos[i - 1].getItem(k));
                }
                for (int j = 2; j < 12; j++) {
                    if (!upper && ((j == 2) || ((base * j) < model.getRows()))) {
                        if (!isOverflow(base, j)) {
                            items.add(String.valueOf(base * j));
                            if (((base * j) >= model.getRows()) && (j == 2)) {
                                upper = true;
                            }
                        }
                    }
                }
            }
            combos[i].setItems(items.toArray(new String[] {}));
            combos[i].select(0);
        }
        setPageComplete(isPageComplete());

        // Update labels
        for (int i = 0; i < labels1.length; i++) {
            updateLabel(i);
        }
    }

    private void updateLabel(final int index) {

        if (labels1[index] == null) { return; }

        int size = -1;
        if (index == 0) {
            if (!field.getText().equals("") && (Integer.valueOf(field.getText()) > 1)) { //$NON-NLS-1$
                size = Integer.valueOf(field.getText());
            }
        } else {
            int sindex = combos[index - 1].getSelectionIndex();
            if ((sindex == -1) && (combos[index - 1].getItemCount() > 0)) {
                sindex = 0;
            }
            if (sindex != -1) {
                size = Integer.valueOf(combos[index - 1].getItem(sindex));
            }
        }
        if (size == -1) {
            labels1[index].setText(""); //$NON-NLS-1$
            labels3[index].setText(""); //$NON-NLS-1$
            return;
        }

        final int number = model.getRows() / size;
        final int remainder = model.getRows() % size;
        labels1[index].setText(String.valueOf(number + 1));
        labels3[index].setText("(" + String.valueOf(remainder) + ")"); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
