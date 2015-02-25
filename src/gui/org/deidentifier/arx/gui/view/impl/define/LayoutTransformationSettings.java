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

package org.deidentifier.arx.gui.view.impl.define;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelCriterion;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.ILayout;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolder;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolderButton;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;

/**
 * This view displays general settings regarding data transformation.
 *
 * @author Fabian Prasser
 */
public class LayoutTransformationSettings implements ILayout {

    /** Static settings. */
    private static final int      LABEL_WIDTH  = 50;

    /** Static settings. */
    private static final int      LABEL_HEIGHT = 20;

    /** Controller. */
    private final Controller      controller;

    /** Model. */
    private Model                 model;

    /** View. */
    private Composite             root;

    /** View. */
    private ComponentTitledFolder folder;

    /** View. */
    private ViewCriteriaList      clv;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     */
    public LayoutTransformationSettings(final Composite parent,
                               final Controller controller) {

        this.controller = controller;
        this.root = build(parent);
    }

    /**
     * 
     *
     * @param parent
     * @return
     */
    private Composite build(final Composite parent) {

        // Create input group
        Composite group = new Composite(parent, SWT.NONE);
        group.setLayoutData(SWTUtil.createFillGridData());
        group.setLayout(new FillLayout());

        folder = new ComponentTitledFolder(group, controller, null, "id-60");
        
        // Create general tab
        group = folder.createItem(Resources.getMessage("CriterionDefinitionView.61"), null);  //$NON-NLS-1$
        group.setLayout(new FillLayout());
        new ViewTransformationSettings(group, controller);
        
        // Create metrics tab
        Composite composite1 = folder.createItem(Resources.getMessage("CriterionDefinitionView.66"), null);  //$NON-NLS-1$
        composite1.setLayout(new FillLayout());
        new ViewMetric(composite1, controller, folder);
        
        // Create overview tab
        Composite c = folder.createItem(Resources.getMessage("CriterionDefinitionView.62"), null);  //$NON-NLS-1$
        c.setLayout(new FillLayout());
        clv = new ViewCriteriaList(c, controller);
        
        // Select first and finish
        folder.setSelection(0);
        return group;
    }
}
