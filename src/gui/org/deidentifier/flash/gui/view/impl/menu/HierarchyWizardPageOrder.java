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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;

import org.deidentifier.flash.DataType;
import org.deidentifier.flash.gui.Controller;
import org.deidentifier.flash.gui.SWTUtil;
import org.deidentifier.flash.gui.resources.Resources;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;

public class HierarchyWizardPageOrder extends WizardPage {

    private final HierarchyWizardModel model;
    private List                       list;
    private final int                  order = 1;
    private final Controller           controller;
    private String                     format;

    public HierarchyWizardPageOrder(final Controller controller,
                                    final HierarchyWizardModel model) {
        super(""); //$NON-NLS-1$
        this.model = model;
        this.controller = controller;
        setTitle(Resources.getMessage("HierarchyWizardPageOrder.1")); //$NON-NLS-1$
        setDescription(Resources.getMessage("HierarchyWizardPageOrder.2")); //$NON-NLS-1$
        setPageComplete(true);
    }

    @Override
    public boolean canFlipToNextPage() {
        return true;
    }

    @Override
    public void createControl(final Composite parent) {

        final Composite composite = new Composite(parent, SWT.NONE);
        final GridLayout compositeLayout = new GridLayout();
        compositeLayout.numColumns = 1;
        composite.setLayout(compositeLayout);
        list = new List(composite, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);

        // Limit to 10 entries
        final int itemHeight = list.getItemHeight();
        final GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
        data.heightHint = 10 * itemHeight;
        list.setLayoutData(data);

        final Composite bottom2 = new Composite(composite, SWT.NONE);
        bottom2.setLayoutData(SWTUtil.createFillHorizontallyGridData());

        final GridLayout bottomLayout2 = new GridLayout();
        bottomLayout2.numColumns = 2;
        bottom2.setLayout(bottomLayout2);

        final Button up = new Button(bottom2, SWT.NONE);
        up.setText(Resources.getMessage("HierarchyWizardPageOrder.3")); //$NON-NLS-1$
        up.setImage(controller.getResources().getImage("arrow_up.png")); //$NON-NLS-1$
        up.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        up.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                up();
            }
        });

        final Button down = new Button(bottom2, SWT.NONE);
        down.setText(Resources.getMessage("HierarchyWizardPageOrder.5")); //$NON-NLS-1$
        down.setImage(controller.getResources().getImage("arrow_down.png")); //$NON-NLS-1$
        down.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        down.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                down();
            }
        });

        final Composite bottom1 = new Composite(composite, SWT.NONE);
        bottom1.setLayoutData(SWTUtil.createFillHorizontallyGridData());

        final GridLayout bottomLayout = new GridLayout();
        bottomLayout.numColumns = 2;
        bottom1.setLayout(bottomLayout);

        final Label text = new Label(bottom1, SWT.NONE);
        text.setText(Resources.getMessage("HierarchyWizardPageOrder.7")); //$NON-NLS-1$

        final Combo combo = new Combo(bottom1, SWT.NONE);
        combo.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        combo.add(Resources.getMessage("HierarchyWizardPageOrder.8")); //$NON-NLS-1$
        combo.add(Resources.getMessage("HierarchyWizardPageOrder.9")); //$NON-NLS-1$
        combo.add(Resources.getMessage("HierarchyWizardPageOrder.10")); //$NON-NLS-1$
        combo.add(Resources.getMessage("HierarchyWizardPageOrder.11")); //$NON-NLS-1$
        combo.select(1);
        combo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                if (combo.getSelectionIndex() == 0) {
                    sortDefault();
                } else if (combo.getSelectionIndex() == 1) {
                    sortLexicographic();
                } else if (combo.getSelectionIndex() == 2) {
                    sortNumeric();
                } else if (combo.getSelectionIndex() == 3) {

                    // Choose a format
                    format = controller.actionShowDateFormatInputDialog(Resources.getMessage("HierarchyWizardPageOrder.12"), Resources.getMessage("HierarchyWizardPageOrder.13"), model.getItems()); //$NON-NLS-1$ //$NON-NLS-2$

                    // Invalid or valid
                    if (format == null) {
                        sortDefault();
                        combo.select(0);
                    } else {
                        sortDate();
                    }
                }
            }
        });

        sortDefault();

        setControl(composite);
    }

    private void down() {
        final int index = list.getSelectionIndex();
        if ((index != -1) && (index < (list.getItemCount() - 1))) {

            // TODO: Ugly!
            final String t = model.getItems().get(index + 1);
            model.getItems().set(index + 1, model.getItems().get(index));
            model.getItems().set(index, t);
            list.setItems(model.getItems().toArray(new String[] {}));
            list.setSelection(index + 1);
        }
    }

    @Override
    public boolean isPageComplete() {
        return true;
    }

    private void sortDate() {
        list.removeAll();
        final DateFormat f = new SimpleDateFormat(format);
        Collections.sort(model.getItems(), new Comparator<String>() {
            @Override
            public int compare(final String arg0, final String arg1) {
                try {
                    return order * f.parse(arg0).compareTo(f.parse(arg1));
                } catch (final ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        for (final String s : model.getItems()) {
            list.add(s);
        }
    }

    private void sortDefault() {
        list.removeAll();
        final DataType type = model.getDataType();
        Collections.sort(model.getItems(), new Comparator<String>() {
            @Override
            public int compare(final String arg0, final String arg1) {
                try {
                    return order * type.compare(arg0, arg1);
                } catch (final ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        for (final String s : model.getItems()) {
            list.add(s);
        }
    }

    private void sortLexicographic() {
        list.removeAll();
        try {
            Collections.sort(model.getItems(), new Comparator<String>() {
                @Override
                public int compare(final String arg0, final String arg1) {
                    return order * arg0.compareTo(arg1);
                }

            });
        } catch (final Exception e) {
        }
        for (final String s : model.getItems()) {
            list.add(s);
        }
    }

    private void sortNumeric() {
        list.removeAll();
        try {
            Collections.sort(model.getItems(), new Comparator<String>() {
                @Override
                public int compare(final String arg0, final String arg1) {
                    return order *
                           Double.valueOf(arg0).compareTo(Double.valueOf(arg1));
                }
            });
        } catch (final Exception e) {
        }
        for (final String s : model.getItems()) {
            list.add(s);
        }
    }

    private void up() {
        final int index = list.getSelectionIndex();
        if ((index != -1) && (index > 0)) {

            // TODO: Ugly!
            final String t = model.getItems().get(index - 1);
            model.getItems().set(index - 1, model.getItems().get(index));
            model.getItems().set(index, t);
            list.setItems(model.getItems().toArray(new String[] {}));
            list.setSelection(index - 1);
        }
    }
}
