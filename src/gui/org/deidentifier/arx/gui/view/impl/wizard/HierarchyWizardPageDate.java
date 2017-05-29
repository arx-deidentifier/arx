/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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

package org.deidentifier.arx.gui.view.impl.wizard;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import org.deidentifier.arx.aggregates.HierarchyBuilderDate.Granularity;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import de.linearbits.swt.table.DynamicTable;
import de.linearbits.swt.table.DynamicTableColumn;

/**
 * A page for configuring the date-based builder.
 *
 * @author Fabian Prasser
 */
public class HierarchyWizardPageDate extends HierarchyWizardPageBuilder<Date> {

    /** Model */
    private final HierarchyWizardModelDate model;

    /** View */
    private DynamicTable                   table;

    /** View */
    private Text                           text;

    /** View */
    private Combo                          combo;

    /**
     * Creates a new instance.
     *
     * @param controller
     * @param wizard
     * @param model
     * @param finalPage
     */
    public HierarchyWizardPageDate(Controller controller,
                                   final HierarchyWizard<Date> wizard,
                                   final HierarchyWizardModel<Date> model,
                                   final HierarchyWizardPageFinal<Date> finalPage) {
        super(wizard, model.getDateModel(), finalPage);
        this.model = model.getDateModel();
        setTitle(Resources.getMessage("HierarchyWizardPageDate.0")); //$NON-NLS-1$
        setDescription(Resources.getMessage("HierarchyWizardPageDate.1")); //$NON-NLS-1$
        setPageComplete(true);
    }
    
    @Override
    public void createControl(final Composite parent) {
        
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(SWTUtil.createGridLayout(1, false));
        
        // List of granularities
        Group group1 = new Group(composite, SWT.SHADOW_ETCHED_IN);
        group1.setText(Resources.getMessage("HierarchyWizardPageDate.2")); //$NON-NLS-1$
        group1.setLayout(SWTUtil.createGridLayout(1, false));
        group1.setLayoutData(SWTUtil.createFillGridData());
        table = SWTUtil.createTableDynamic(group1, SWT.CHECK | SWT.SINGLE | SWT.BORDER);
        table.setLayoutData(SWTUtil.createFillGridData());
        
        DynamicTableColumn column1 = new DynamicTableColumn(table, SWT.NONE);
        column1.setWidth("100%", "40px");
    
        createItems(table);
        
        table.addSelectionListener(new SelectionAdapter(){
            @Override public void widgetSelected(SelectionEvent arg0) {
                
                // Check
                if (table.getSelection() == null || table.getSelection().length == 0) {
                    text.setText(""); //$NON-NLS-1$
                    text.setEnabled(false);
                    setPageComplete(isPageComplete());
                    return;
                }
                
                // Update format text field
                Granularity g = (Granularity)table.getSelection()[0].getData();
                if (g.isFormatSupported()) {
                    String _format = model.getFormat().get(g);
                    _format = _format != null ? _format : g.getDefaultFormat();
                    text.setText(_format);
                    text.setEnabled(true);
                } else {
                    text.setText(""); //$NON-NLS-1$
                    text.setEnabled(false);
                }
                
                // Update granularities
                model.getGranularities().clear();
                for (TableItem item : table.getItems()) {
                    if (item.getChecked()) {
                        model.getGranularities().add((Granularity)item.getData());
                    }
                }
            }
        });
    
        // Format string
        Group group2 = new Group(composite, SWT.SHADOW_ETCHED_IN);
        group2.setText(Resources.getMessage("HierarchyWizardPageDate.3")); //$NON-NLS-1$
        group2.setLayout(SWTUtil.createGridLayout(1, false));
        group2.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        
        text = new Text(group2, SWT.BORDER);
        text.setLayoutData(SWTUtil.createFillGridData());

        decorateAndStore(text);
        
        // Time zones
        Group group3 = new Group(composite, SWT.SHADOW_ETCHED_IN);
        group3.setText(Resources.getMessage("HierarchyWizardPageDate.4")); //$NON-NLS-1$
        group3.setLayout(SWTUtil.createGridLayout(2, false));
        group3.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        
        combo = new Combo(group3, SWT.SINGLE);
        combo.setLayoutData(SWTUtil.createFillGridData());

        createItems(combo);
    
        combo.addSelectionListener(new SelectionAdapter(){
            @Override public void widgetSelected(SelectionEvent arg0) {
                
                // Check
                if (combo.getSelectionIndex() < 0) {
                    return;
                } 
                
                // Update timezone
                String label = combo.getItem(combo.getSelectionIndex());
                String[] ids = TimeZone.getAvailableIDs();
                for (String id : ids) {
                    TimeZone timezone = TimeZone.getTimeZone(id);
                    if (label.equals(timezone.getDisplayName())) {
                        model.setTimeZone(timezone);
                    }
                }
            }
        });
       
        updatePage();
        setControl(composite);
    }

    @Override
    public void setVisible(boolean value){
        super.setVisible(value);
        model.setVisible(value);
    }

    @Override
    public void updatePage() {
        
        // Granularities
        for (TableItem item : table.getItems()) {
            item.dispose();
        }
        createItems(table);
        table.select(0);
        
        // Format string
        text.setText(model.getFormat().get((Granularity)table.getItem(0).getData()));

        // Timezone
        combo.removeAll();
        createItems(combo);
        combo.select(combo.indexOf(model.getTimeZone().getDisplayName()));
    }
    
    /**
     * Creates a list of all timezones
     * @param combo
     */
    private void createItems(Combo combo) {
        Set<String> items = new HashSet<String>();
        String[] ids = TimeZone.getAvailableIDs();
        for (String id : ids) {
            String name = TimeZone.getTimeZone(id).getDisplayName();
            if (!items.contains(name)) {
                combo.add(name);
                items.add(name);
            }
        }
    }
    
    /**
     * Creates a list of all available granularities
     * @param table
     */
    private void createItems(DynamicTable table) {
        // Initialize
        for (Granularity g : Granularity.values()) {
            TableItem item = new TableItem(table, SWT.CHECK);
            item.setText(Resources.getMessage("HierarchyWizardPageDate." + g.toString())); //$NON-NLS-1$
            item.setData(g);
        }
    }
    
    /**
     * Decorates a text field for domain properties.
     *
     * @param text
     */
    private void decorateAndStore(final Text text) {
        final ControlDecoration decoration = new ControlDecoration(text, SWT.RIGHT);
        text.addModifyListener(new ModifyListener(){
            @Override
            public void modifyText(ModifyEvent arg0) {
                
                // Extract granularity
                Granularity g = null;
                if (table.getSelection() != null && table.getSelection().length != 0) {
                    g = (Granularity)table.getSelection()[0].getData();
                }
                
                // Check
                if (g.isFormatSupported()) {
                    String formatString = text.getText();
                    if (g != null && model.getFormat().isValid(formatString, g.getDefaultFormat())) {
                        decoration.hide();
                        model.getFormat().set((Granularity)table.getSelection()[0].getData(), formatString);
                    } else {
                        decoration.setDescriptionText(Resources.getMessage("HierarchyWizardPageDate.5")); //$NON-NLS-1$
                        Image image = FieldDecorationRegistry.getDefault()
                              .getFieldDecoration(FieldDecorationRegistry.DEC_ERROR)
                              .getImage();
                        decoration.setImage(image);
                        decoration.show();
                    }
                } else {
                    decoration.hide();
                }
            }
        });
    }
}