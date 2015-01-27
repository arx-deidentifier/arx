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
import java.util.Locale;

import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IDialog;
import org.deidentifier.arx.gui.view.def.IEditor;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * This class implements a dialog for editing project properties.
 *
 * @author Fabian Prasser
 */
public class DialogProperties extends TitleAreaDialog implements IDialog {

    /**
     * Validates double input.
     *
     * @author Fabian Prasser
     */
    private static class DoubleValidator {
        
        /**  TODO */
        private final double min;
        
        /**  TODO */
        private final double max;

        /**
         * Creates a new instance.
         *
         * @param min
         * @param max
         */
        public DoubleValidator(final double min, final double max) {
            this.min = min;
            this.max = max;
        }

        /**
         * Validates the string.
         *
         * @param s
         * @return
         */
        public boolean validate(final String s) {
            
            // TODO: Ugly
            try {
                final double i = Double.valueOf(s);
                return (i > min) && (i < max);
            } catch (final Exception e) {
                return false;
            }
        }
    }

    /**
     * Validates integer input.
     *
     * @author Fabian Prasser
     */
    private static class IntegerValidator {
        
        /**  TODO */
        private final int min;
        
        /**  TODO */
        private final int max;

        /**
         * Creates a new instance.
         *
         * @param min
         * @param max
         */
        public IntegerValidator(final int min, final int max) {
            this.min = min;
            this.max = max;
        }

        /**
         * 
         *
         * @param s
         * @return
         */
        public boolean validate(final String s) {
            // TODO: Ugly
            try {
                final int i = Integer.valueOf(s);
                return (i >= min) && (i <= max);
            } catch (final Exception e) {
                return false;
            }
        }
    }

    /**  TODO */
    private final Model model;

    /**  TODO */
    private Button      ok;
    
    /**  TODO */
    private TabFolder   folder;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param model
     */
    public DialogProperties(final Shell parent, final Model model) {
        super(parent);
        this.model = model;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#create()
     */
    @Override
    public void create() {
        super.create();
        setTitle(Resources.getMessage("PropertyDialog.0")); //$NON-NLS-1$
        setMessage(Resources.getMessage("PropertyDialog.1"), IMessageProvider.NONE); //$NON-NLS-1$

        // Obtain editors and categories
        final List<IEditor<?>> editors = getEditors(model);
        final List<String> categories = new ArrayList<String>();
        for (final IEditor<?> e : editors) {
            if (!categories.contains(e.getCategory())) {
                categories.add(e.getCategory());
            }
        }

        // Build tabs
        for (final String category : categories) {

            // Create the tab folder
            final TabItem tab = new TabItem(folder, SWT.NONE);
            tab.setText(category);
            final Composite tabC = buildCategory(folder, category, editors);
            tab.setControl(tabC);
        }

        // Repack the dialog
        super.getShell().pack();
    }

    /**
     * Builds the content for a specific category.
     *
     * @param folder
     * @param category
     * @param editors
     * @return
     */
    private Composite buildCategory(final TabFolder folder,
                                    final String category,
                                    final List<IEditor<?>> editors) {
        final Composite c = new Composite(folder, SWT.NONE);
        c.setLayout(new GridLayout(2, false));
        for (final IEditor<?> e : editors) {
            if (e.getCategory().equals(category)) {
                final Label l = new Label(c, SWT.NONE);
                l.setText(e.getLabel() + ":"); //$NON-NLS-1$
                e.createControl(c);
            }
        }
        return c;
    }

    /**
     * Builds all editors for the model.
     *
     * @param model
     * @return
     */
    private List<IEditor<?>> getEditors(final Model model) {

        // Init
        final List<IEditor<?>> result = new ArrayList<IEditor<?>>();

        // Project category
        result.add(new EditorString(Resources.getMessage("PropertyDialog.3"), Resources.getMessage("PropertyDialog.4"), ok, false) { //$NON-NLS-1$ //$NON-NLS-2$
            @Override
            public boolean accepts(final String s) {
                if (s.equals("")) { //$NON-NLS-1$
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public String getValue() {
                return model.getName();
            }

            @Override
            public void setValue(final String s) {
                model.setName(s);
            }
        });

        result.add(new EditorString(Resources.getMessage("PropertyDialog.6"), Resources.getMessage("PropertyDialog.7"), ok, true) { //$NON-NLS-1$ //$NON-NLS-2$
            @Override
            public boolean accepts(final String s) {
                return true;
            }

            @Override
            public String getValue() {
                return model.getDescription();
            }

            @Override
            public void setValue(final String s) {
                model.setDescription(s);
            }
        });

        result.add(new EditorString(Resources.getMessage("PropertyDialog.8"), Resources.getMessage("PropertyDialog.9"), ok, false) { //$NON-NLS-1$ //$NON-NLS-2$
            @Override
            public boolean accepts(final String s) {
                if (s.length() == 1) {
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public String getValue() {
                return String.valueOf(model.getSeparator());
            }

            @Override
            public void setValue(final String s) {
                model.setSeparator(s.toCharArray()[0]);
            }
        });

        // Create list of locales
        List<String> languages = new ArrayList<String>();
        languages.add("Default");
        for (String lang : Locale.getISOLanguages()) {
            languages.add(lang.toUpperCase());
        }
        
        // Create editor
        result.add(new EditorSelection(Resources.getMessage("PropertyDialog.8"), Resources.getMessage("PropertyDialog.33"), languages.toArray(new String[]{})) { //$NON-NLS-1$ //$NON-NLS-2$

            @Override
            public String getValue() {
                return String.valueOf(model.getLocale().getLanguage().toUpperCase());
            }

            @Override
            public void setValue(final String s) {
                if (s.equals("Default")) {
                    model.setLocale(Locale.getDefault());
                } else {
                    model.setLocale(new Locale(s.toLowerCase()));
                }
            }
        });

        // Transformation category
        result.add(new EditorBoolean(Resources.getMessage("PropertyDialog.10"), Resources.getMessage("PropertyDialog.11")) { //$NON-NLS-1$ //$NON-NLS-2$
            @Override
            public Boolean getValue() {
                return model.getInputConfig().isSuppressionAlwaysEnabled();
            }

            @Override
            public void setValue(final Boolean t) {
                model.getInputConfig().setSuppressionAlwaysEnabled(t);
            }
        });
        result.add(new EditorBoolean(Resources.getMessage("PropertyDialog.10"), Resources.getMessage("PropertyDialog.31")) { //$NON-NLS-1$ //$NON-NLS-2$
            @Override
            public Boolean getValue() {
                return model.getInputConfig().isAttributeTypeSuppressed(AttributeType.SENSITIVE_ATTRIBUTE);
            }

            @Override
            public void setValue(final Boolean t) {
                model.getInputConfig().setAttributeTypeSuppressed(AttributeType.SENSITIVE_ATTRIBUTE, t);
            }
        });
        result.add(new EditorBoolean(Resources.getMessage("PropertyDialog.10"), Resources.getMessage("PropertyDialog.32")) { //$NON-NLS-1$ //$NON-NLS-2$
            @Override
            public Boolean getValue() {
                return model.getInputConfig().isAttributeTypeSuppressed(AttributeType.INSENSITIVE_ATTRIBUTE);
            }

            @Override
            public void setValue(final Boolean t) {
                model.getInputConfig().setAttributeTypeSuppressed(AttributeType.INSENSITIVE_ATTRIBUTE, t);
            }
        });
        
        result.add(new EditorString(Resources.getMessage("PropertyDialog.12"), Resources.getMessage("PropertyDialog.13"), ok, false) { //$NON-NLS-1$ //$NON-NLS-2$
            @Override
            public boolean accepts(final String s) {
                return true;
            }

            @Override
            public String getValue() {
                return model.getInputConfig().getSuppressionString();
            }

            @Override
            public void setValue(final String s) {
                model.getInputConfig().setSuppressionString(s);
            }
        });
        final IntegerValidator v = new IntegerValidator(0, 1000001);
        result.add(new EditorString(Resources.getMessage("PropertyDialog.14"), Resources.getMessage("PropertyDialog.15"), ok, false) { //$NON-NLS-1$ //$NON-NLS-2$
            @Override
            public boolean accepts(final String s) {
                return v.validate(s);
            }

            @Override
            public String getValue() {
                return String.valueOf(model.getMaxNodesInLattice());
            }

            @Override
            public void setValue(final String s) {
                model.setMaxNodesInLattice(Integer.valueOf(s));
            }
        });

        // Internals category
        final IntegerValidator v2 = new IntegerValidator(0, 1000001);
        result.add(new EditorString(Resources.getMessage("PropertyDialog.16"), Resources.getMessage("PropertyDialog.17"), ok, false) { //$NON-NLS-1$ //$NON-NLS-2$
            @Override
            public boolean accepts(final String s) {
                return v2.validate(s);
            }

            @Override
            public String getValue() {
                return String.valueOf(model.getHistorySize());
            }

            @Override
            public void setValue(final String s) {
                model.setHistorySize(Integer.valueOf(s));
            }
        });
        final DoubleValidator v3 = new DoubleValidator(0d, 1d);
        result.add(new EditorString(Resources.getMessage("PropertyDialog.18"), Resources.getMessage("PropertyDialog.19"), ok, false) { //$NON-NLS-1$ //$NON-NLS-2$
            @Override
            public boolean accepts(final String s) {
                return v3.validate(s);
            }

            @Override
            public String getValue() {
                return String.valueOf(model.getSnapshotSizeDataset());
            }

            @Override
            public void setValue(final String s) {
                model.setSnapshotSizeDataset(Double.valueOf(s));
            }
        });
        result.add(new EditorString(Resources.getMessage("PropertyDialog.20"), Resources.getMessage("PropertyDialog.21"), ok, false) { //$NON-NLS-1$ //$NON-NLS-2$
            @Override
            public boolean accepts(final String s) {
                return v3.validate(s);
            }

            @Override
            public String getValue() {
                return String.valueOf(model.getSnapshotSizeSnapshot());
            }

            @Override
            public void setValue(final String s) {
                model.setSnapshotSizeSnapshot(Double.valueOf(s));
            }
        });
        final IntegerValidator v5 = new IntegerValidator(0, Integer.MAX_VALUE);
        result.add(new EditorString(Resources.getMessage("PropertyDialog.20"), Resources.getMessage("PropertyDialog.28"), ok, false) { //$NON-NLS-1$ //$NON-NLS-2$
            @Override
            public boolean accepts(final String s) {
                return v5.validate(s);
            }

            @Override
            public String getValue() {
                return String.valueOf(model.getMaximalSizeForComplexOperations());
            }

            @Override
            public void setValue(final String s) {
                model.setMaximalSizeForComplexOperations(Integer.valueOf(s));
            }
        });
        result.add(new EditorBoolean(Resources.getMessage("PropertyDialog.20"), Resources.getMessage("PropertyDialog.29")) { //$NON-NLS-1$ //$NON-NLS-2$
            @Override
            public Boolean getValue() {
                return model.isDebugEnabled();
            }

            @Override
            public void setValue(final Boolean s) {
                model.setDebugEnabled(s);
            }
        });

        // Viewer category
        final IntegerValidator v4 = new IntegerValidator(0, 10000);
        result.add(new EditorString(Resources.getMessage("PropertyDialog.22"), Resources.getMessage("PropertyDialog.23"), ok, false) { //$NON-NLS-1$ //$NON-NLS-2$
            @Override
            public boolean accepts(final String s) {
                return v4.validate(s);
            }

            @Override
            public String getValue() {
                return String.valueOf(model.getInitialNodesInViewer());
            }

            @Override
            public void setValue(final String s) {
                model.setInitialNodesInViewer(Integer.valueOf(s));
            }
        });
        result.add(new EditorString(Resources.getMessage("PropertyDialog.24"), Resources.getMessage("PropertyDialog.25"), ok, false) { //$NON-NLS-1$ //$NON-NLS-2$
            @Override
            public boolean accepts(final String s) {
                return v4.validate(s);
            }

            @Override
            public String getValue() {
                return String.valueOf(model.getMaxNodesInViewer());
            }

            @Override
            public void setValue(final String s) {
                model.setMaxNodesInViewer(Integer.valueOf(s));
            }
        });
        // Return
        return result;
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

        // Create OK Button
        parent.setLayoutData(SWTUtil.createFillGridData());
        ok = createButton(parent,
                          Window.OK,
                          Resources.getMessage("PropertyDialog.26"), true); //$NON-NLS-1$
        ok.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                setReturnCode(Window.OK);
                close();
            }
        });
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(final Composite parent) {
        parent.setLayout(new GridLayout(1, false));

        folder = new TabFolder(parent, SWT.NONE);
        folder.setLayoutData(SWTUtil.createFillGridData());

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
