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

package org.deidentifier.arx.gui.view.impl.define;

import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.ILayout;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolder;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolderButtonBar;
import org.eclipse.swt.widgets.Composite;

/**
 * This view displays settings for all attributes.
 *
 * @author Fabian Prasser
 */
public class LayoutAttributeSettings implements ILayout {

    /** View */
    private final ViewAttributeTransformation attributeTransformationView;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     */
    public LayoutAttributeSettings(final Composite parent,
                                   final Controller controller) {
        
        ComponentTitledFolderButtonBar bar = new ComponentTitledFolderButtonBar("id-1"); //$NON-NLS-1$
        bar.add(Resources.getMessage("AttributeDefinitionView.13"), //$NON-NLS-1$
                controller.getResources().getImage(AttributeType.QUASI_IDENTIFYING_ATTRIBUTE), // $NON-NLS-1$
                new Runnable() {
                    @Override
                    public void run() {
                        attributeTransformationView.actionUpdateAttributeTypes(AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
                    }
                });
        bar.add(Resources.getMessage("AttributeDefinitionView.11"), //$NON-NLS-1$
                controller.getResources().getImage(AttributeType.IDENTIFYING_ATTRIBUTE), // $NON-NLS-1$
                new Runnable() {
                    @Override
                    public void run() {
                        attributeTransformationView.actionUpdateAttributeTypes(AttributeType.IDENTIFYING_ATTRIBUTE);
                    }
                });
        bar.add(Resources.getMessage("AttributeDefinitionView.12"), //$NON-NLS-1$
                controller.getResources().getImage(AttributeType.INSENSITIVE_ATTRIBUTE), // $NON-NLS-1$
                new Runnable() {
                    @Override
                    public void run() {
                        attributeTransformationView.actionUpdateAttributeTypes(AttributeType.INSENSITIVE_ATTRIBUTE);
                    }
                });

        // Create the tab folder
        ComponentTitledFolder folder = new ComponentTitledFolder(parent, controller, bar, "id-1");
        folder.setLayoutData(SWTUtil.createFillGridData());

        // First view
        Composite composite1 = folder.createItem(Resources.getMessage("LayoutAttributeMetadata.0"), null); //$NON-NLS-1$
        composite1.setLayout(SWTUtil.createGridLayout(1));
        this.attributeTransformationView = new ViewAttributeTransformation(composite1, controller);

        // Second view
        composite1 = folder.createItem(Resources.getMessage("LayoutAttributeMetadata.1"), null); //$NON-NLS-1$
        composite1.setLayout(SWTUtil.createGridLayout(1));
        new ViewAttributeList(composite1, controller);
        
        // Select
        folder.setSelection(0);
    }
}