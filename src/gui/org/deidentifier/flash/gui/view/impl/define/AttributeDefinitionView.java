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

package org.deidentifier.flash.gui.view.impl.define;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deidentifier.flash.AttributeType;
import org.deidentifier.flash.AttributeType.Hierarchy;
import org.deidentifier.flash.DataDefinition;
import org.deidentifier.flash.DataHandle;
import org.deidentifier.flash.DataType;
import org.deidentifier.flash.gui.Controller;
import org.deidentifier.flash.gui.Model;
import org.deidentifier.flash.gui.SWTUtil;
import org.deidentifier.flash.gui.resources.Resources;
import org.deidentifier.flash.gui.view.def.IView;
import org.deidentifier.flash.gui.view.def.IView.ModelEvent.EventTarget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class AttributeDefinitionView implements IView {

    private final Image                  IMAGE_INSENSITIVE;
    private final Image                  IMAGE_SENSITIVE;
    private final Image                  IMAGE_QUASI_IDENTIFYING;
    private final Image                  IMAGE_IDENTIFYING;

    private static final String[]        COMBO1_VALUES = new String[] { Resources.getMessage("AttributeDefinitionView.0"), Resources.getMessage("AttributeDefinitionView.1"), Resources.getMessage("AttributeDefinitionView.2"), Resources.getMessage("AttributeDefinitionView.3") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    private static final AttributeType[] COMBO1_TYPES  = new AttributeType[] { AttributeType.INSENSITIVE_ATTRIBUTE,
            AttributeType.SENSITIVE_ATTRIBUTE,
            null,
            AttributeType.IDENTIFYING_ATTRIBUTE       };

    private static final String[]        COMBO2_VALUES = new String[] { Resources.getMessage("AttributeDefinitionView.4"), Resources.getMessage("AttributeDefinitionView.5"), Resources.getMessage("AttributeDefinitionView.6") };                                                   //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    private static final DataType[]      COMBO2_TYPES  = new DataType[] { DataType.STRING,
            DataType.DECIMAL,
            DataType.DATE                             };

    private final Combo                  typeCombo;
    private final Combo                  dataTypeCombo;
    private String                       attribute     = null;
    private final Composite              group;
    private final CTabItem               tab;
    private Model                        model;
    private final HierarchyView          editor;
    private final Controller             controller;

    public AttributeDefinitionView(final CTabFolder parent,
                                   final String attribute,
                                   final Controller controller) {

        // Load images
        IMAGE_INSENSITIVE = controller.getResources()
                                      .getImage("bullet_green.png"); //$NON-NLS-1$
        IMAGE_SENSITIVE = controller.getResources()
                                    .getImage("bullet_purple.png"); //$NON-NLS-1$
        IMAGE_QUASI_IDENTIFYING = controller.getResources()
                                            .getImage("bullet_yellow.png"); //$NON-NLS-1$
        IMAGE_IDENTIFYING = controller.getResources()
                                      .getImage("bullet_red.png"); //$NON-NLS-1$

        // Register
        this.controller = controller;
        this.attribute = attribute;
        this.controller.addListener(EventTarget.MODEL, this);
        this.controller.addListener(EventTarget.INPUT, this);
        this.controller.addListener(EventTarget.ATTRIBUTE_TYPE, this);

        // Create input group
        tab = new CTabItem(parent, SWT.NULL);
        tab.setText(attribute);
        tab.setShowClose(false);
        tab.setImage(IMAGE_INSENSITIVE);

        group = new Composite(parent, SWT.NULL);
        group.setLayoutData(SWTUtil.createFillGridData());
        final GridLayout groupInputGridLayout = new GridLayout();
        groupInputGridLayout.numColumns = 1;
        group.setLayout(groupInputGridLayout);

        final Composite type = new Composite(group, SWT.NULL);
        type.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        final GridLayout typeInputGridLayout = new GridLayout();
        typeInputGridLayout.numColumns = 2;
        type.setLayout(typeInputGridLayout);

        final IView outer = this;
        final Label kLabel = new Label(type, SWT.PUSH);
        kLabel.setText(Resources.getMessage("AttributeDefinitionView.7")); //$NON-NLS-1$
        typeCombo = new Combo(type, SWT.READ_ONLY);
        typeCombo.setLayoutData(SWTUtil.createFillGridData());
        typeCombo.setItems(COMBO1_VALUES);
        typeCombo.select(0);
        typeCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {

                if ((typeCombo.getSelectionIndex() != -1) &&
                    (attribute != null)) {
                    if ((model != null) &&
                        (model.getInputConfig().getInput() != null)) {
                        final AttributeType type = COMBO1_TYPES[typeCombo.getSelectionIndex()];
                        final DataDefinition definition = model.getInputConfig()
                                                               .getInput()
                                                               .getDefinition();
                        if (type == null) {

                            // Set the attribute type
                            definition.setAttributeType(attribute,
                                                        Hierarchy.create());
                        } else {

                            // Make sure, only one sensitive attribute is
                            // defined
                            if (type == AttributeType.SENSITIVE_ATTRIBUTE) {
                                final Set<String> redefine = new HashSet<String>();
                                redefine.addAll(definition.getSensitiveAttributes());
                                for (final String other : redefine) {
                                    // TODO: We redefine it to be
                                    // quasi-identifiers, needs warning?
                                    definition.setAttributeType(other,
                                                                Hierarchy.create());
                                    controller.update(new ModelEvent(outer,
                                                                     EventTarget.ATTRIBUTE_TYPE,
                                                                     other));
                                }
                            }

                            // Set the attribute type
                            definition.setAttributeType(attribute, type);
                        }

                        // Update icon
                        updateIcon();

                        // Update the views
                        controller.update(new ModelEvent(outer,
                                                         EventTarget.ATTRIBUTE_TYPE,
                                                         attribute));
                    }
                }
            }
        });

        final Label kLabel2 = new Label(type, SWT.PUSH);
        kLabel2.setText(Resources.getMessage("AttributeDefinitionView.8")); //$NON-NLS-1$
        dataTypeCombo = new Combo(type, SWT.READ_ONLY);
        dataTypeCombo.setLayoutData(SWTUtil.createFillGridData());
        dataTypeCombo.setItems(COMBO2_VALUES);
        dataTypeCombo.select(0);
        dataTypeCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                if ((dataTypeCombo.getSelectionIndex() != -1) &&
                    (attribute != null)) {
                    if ((model != null) &&
                        (model.getInputConfig().getInput() != null)) {

                        // Obtain type
                        DataType type = COMBO2_TYPES[dataTypeCombo.getSelectionIndex()];

                        // Open dateformat dialog
                        if (type == DataType.DATE) {
                            final String format = controller.actionShowDateFormatInputDialog(Resources.getMessage("AttributeDefinitionView.9"), Resources.getMessage("AttributeDefinitionView.10"), getValues()); //$NON-NLS-1$ //$NON-NLS-2$
                            if (format == null) {
                                type = DataType.STRING;
                                dataTypeCombo.select(0);
                            } else {
                                type = DataType.DATE(format);
                            }
                        }

                        // Set and update
                        model.getInputConfig()
                             .getInput()
                             .getDefinition()
                             .setDataType(attribute, type);
                        controller.update(new ModelEvent(outer,
                                                         EventTarget.DATA_TYPE,
                                                         attribute));
                    }
                }
            }
        });

        // Editor hierarchy
        editor = new HierarchyView(group, attribute, controller);

        // Attach to tab
        tab.setControl(group);
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
        editor.dispose();
    }

    /**
     * Create an iterator over the values in the column for this attribute
     * 
     * @return
     */
    private Collection<String> getValues() {

        final DataHandle h = model.getInputConfig().getInput().getHandle();
        final List<String> vals = new ArrayList<String>();
        for (final String s : h.getDistinctValues(h.getColumnIndexOf(attribute))) {
            vals.add(s);
        }
        return vals;
    }

    @Override
    public void reset() {
        // Nothing to do
    }

    @Override
    public void update(final ModelEvent event) {
        if (event.target == EventTarget.MODEL) {
            model = (Model) event.data;
            editor.update(event);
        } else if (event.target == EventTarget.ATTRIBUTE_TYPE) {
            final String attr = (String) event.data;
            if (attr.equals(attribute)) {
                updateAttributeType();
                updateIcon();
            }
        } else if (event.target == EventTarget.INPUT) {

            updateAttributeType();
            updateDataType();
            editor.update(event);
        }
    }

    private void updateAttributeType() {
        AttributeType type = model.getInputConfig()
                                  .getInput()
                                  .getDefinition()
                                  .getAttributeType(attribute);
        if (type instanceof Hierarchy) {
            type = null;
        }
        for (int i = 0; i < COMBO1_TYPES.length; i++) {
            if (type == COMBO1_TYPES[i]) {
                typeCombo.select(i);
                break;
            }
        }
    }

    private void updateDataType() {
        // TODO: Handle DATE with user-defined format accordingly
        final DataType dtype = model.getInputConfig()
                                    .getInput()
                                    .getDefinition()
                                    .getDataType(attribute);
        for (int i = 0; i < COMBO2_TYPES.length; i++) {
            if (dtype.equals(COMBO2_TYPES[i])) {
                dataTypeCombo.select(i);
                break;
            }
        }

    }

    private void updateIcon() {
        AttributeType type = model.getInputConfig()
                                  .getInput()
                                  .getDefinition()
                                  .getAttributeType(attribute);
        if (type instanceof Hierarchy) {
            tab.setImage(IMAGE_QUASI_IDENTIFYING);
        } else if (type == AttributeType.INSENSITIVE_ATTRIBUTE) {
            tab.setImage(IMAGE_INSENSITIVE);
        } else if (type == AttributeType.SENSITIVE_ATTRIBUTE) {
            tab.setImage(IMAGE_SENSITIVE);
        } else if (type == AttributeType.IDENTIFYING_ATTRIBUTE) {
            tab.setImage(IMAGE_IDENTIFYING);
        }
    }
}
