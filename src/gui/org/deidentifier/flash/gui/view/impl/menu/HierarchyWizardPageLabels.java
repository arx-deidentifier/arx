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

import java.util.HashSet;
import java.util.Set;

import org.deidentifier.flash.AttributeType.Hierarchy;
import org.deidentifier.flash.gui.SWTUtil;
import org.deidentifier.flash.gui.resources.Resources;
import org.deidentifier.flash.gui.view.def.IHierarchyEditorView;
import org.deidentifier.flash.gui.view.impl.define.HierarchyView;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

public class HierarchyWizardPageLabels extends WizardPage {

    private static enum LabelType {
        INTERVAL,
        SET,
        PREFIXES1,
        PREFIXES2,
        PREFIXES3,
        PREFIXES4
    }

    private final HierarchyWizardModel model;
    private IHierarchyEditorView       editor;
    private static final String[]      COMBO_LABELS = { Resources.getMessage("HierarchyWizardPageLabels.0"), Resources.getMessage("HierarchyWizardPageLabels.1"), Resources.getMessage("HierarchyWizardPageLabels.2"), Resources.getMessage("HierarchyWizardPageLabels.3"), Resources.getMessage("HierarchyWizardPageLabels.4"), Resources.getMessage("HierarchyWizardPageLabels.5") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
    private static final LabelType[]   COMBO_VALUES = { LabelType.INTERVAL,
            LabelType.SET,
            LabelType.PREFIXES1,
            LabelType.PREFIXES2,
            LabelType.PREFIXES3,
            LabelType.PREFIXES4                    };
    private LabelType                  labelType    = LabelType.INTERVAL;

    public HierarchyWizardPageLabels(final HierarchyWizardModel model) {
        super(""); //$NON-NLS-1$
        this.model = model;
        setTitle(Resources.getMessage("HierarchyWizardPageLabels.7")); //$NON-NLS-1$
        setDescription(Resources.getMessage("HierarchyWizardPageLabels.8")); //$NON-NLS-1$
    }

    @Override
    public boolean canFlipToNextPage() {
        return false;
    }

    @Override
    public void createControl(final Composite parent) {

        final Composite composite = new Composite(parent, SWT.NONE);
        final GridLayout compositeL = new GridLayout();
        compositeL.numColumns = 2;
        composite.setLayout(compositeL);

        editor = new HierarchyView(composite, model.getAttribute());
        final GridData d = SWTUtil.createFillGridData();
        d.horizontalSpan = 2;
        editor.setLayoutData(d);

        final Label label = new Label(composite, SWT.NONE);
        label.setText(Resources.getMessage("HierarchyWizardPageLabels.9")); //$NON-NLS-1$

        final Combo combo = new Combo(composite, SWT.READ_ONLY);
        combo.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        combo.setItems(COMBO_LABELS);
        combo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                if (combo.getSelectionIndex() != -1) {
                    labelType = COMBO_VALUES[combo.getSelectionIndex()];
                    update();
                }
            }
        });
        combo.select(0);
        setControl(composite);
        
        // Redraw table when shown
        composite.addListener(SWT.Show, new Listener() {
            public void handleEvent(Event event) {
                update();
            }
        });

    }

    private String getLabel(final String[] vals,
                            final int from,
                            final int to,
                            final int fanout,
                            final Set<String> others) {
        switch (labelType) {
        case INTERVAL:
            return getLabelInterval(vals, from, to, fanout, others);
        case SET:
            return getLabelSet(vals, from, to, fanout, others);
        case PREFIXES1:
            return getLabelPrefix(vals, from, to, fanout, 1, others);
        case PREFIXES2:
            return getLabelPrefix(vals, from, to, fanout, 2, others);
        case PREFIXES3:
            return getLabelPrefix(vals, from, to, fanout, 3, others);
        case PREFIXES4:
            return getLabelPrefix(vals, from, to, fanout, 4, others);
        default:
            throw new RuntimeException(Resources.getMessage("HierarchyWizardPageLabels.10")); //$NON-NLS-1$
        }
    }

    private String getLabelInterval(final String[] vals,
                                    final int from,
                                    final int to,
                                    final int fanout,
                                    final Set<String> others) {
        int diff = 1;
        String label = vals[from] + "-" + vals[to]; //$NON-NLS-1$
        while (others.contains(label)) {
            diff++;
            label = ""; //$NON-NLS-1$
            int max = 0;
            for (int a = 0; a < diff; a++) {
                if ((from + a) == vals.length) {
                    break;
                }
                label += vals[from + a] + "-"; //$NON-NLS-1$
                max = from + a;
            }
            for (int a = 0; a < (diff - 1); a++) {
                final int idx = to - 1 - a;
                if (idx == max) {
                    break;
                }
                label += vals[idx] + "-"; //$NON-NLS-1$
            }
            final int idx = Math.min(fanout - 1 - diff, vals.length - 1);
            if (idx < max) {
                label += vals[idx];
            }
        }
        others.add(label);
        return label;
    }

    private String getLabelPrefix(final String[] vals,
                                  final int from,
                                  final int to,
                                  final int fanout,
                                  final int size,
                                  final Set<String> others) {

        final Set<String> probe = new HashSet<String>();
        final StringBuffer b = new StringBuffer();
        for (int i = from; i <= to; i++) {
            if (!probe.contains(vals[i])) {
                probe.add(vals[i]);
                if (b.length() > 0) {
                    b.append("-"); //$NON-NLS-1$
                }
                int length = size;
                if (length > vals[i].length()) {
                    length = vals[i].length();
                }
                b.append(vals[i].substring(0, length));
            }
        }
        return b.toString();
    }

    private String getLabelSet(final String[] vals,
                               final int from,
                               final int to,
                               final int fanout,
                               final Set<String> others) {

        final Set<String> probe = new HashSet<String>();
        final StringBuffer b = new StringBuffer();
        for (int i = from; i <= to; i++) {
            if (!probe.contains(vals[i])) {
                probe.add(vals[i]);
                if (b.length() == 0) {
                    b.append("{"); //$NON-NLS-1$
                } else {
                    b.append(", "); //$NON-NLS-1$
                }
                b.append(vals[i]);
            }
        }
        b.append("}"); //$NON-NLS-1$
        return b.toString();
    }

    @Override
    public boolean isPageComplete() {
        return true;
    }

    private void update() {

        final String[] vals = model.getItems().toArray(new String[] {});
        final String[][] hierarchy = new String[vals.length][model.getFanout()
                                                                  .size() + 1];

        for (int i = 0; i < vals.length; i++) {
            hierarchy[i] = new String[model.getFanout().size() + 1];
            hierarchy[i][0] = vals[i];
        }

        for (int i = 1; i < model.getFanout().size(); i++) {
            final Set<String> elems = new HashSet<String>();
            final int fanout = model.getFanout().get(i - 1);
            for (int j = 0; j < vals.length; j += fanout) {
                final String label = getLabel(vals,
                                              j,
                                              Math.min((j + fanout) - 1,
                                                       vals.length - 1),
                                              fanout,
                                              elems);
                for (int a = 0; a < fanout; a++) {
                    final int idx = j + a;
                    if (idx < vals.length) {
                        hierarchy[idx][i] = label;
                    }
                }
            }
        }
        for (int i = 0; i < vals.length; i++) {
            hierarchy[i][model.getFanout().size()] = model.getSuppressionString();
        }
        model.setHierarchy(Hierarchy.create(hierarchy));
        if (editor != null) {
            editor.setHierarchy(model.getHierarchy());
        }
    }
}
