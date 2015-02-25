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
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * This class displays a list of all defined privacy criteria.
 *
 * @author Fabian Prasser
 */
public class ViewPrivacyCriteria implements IView {

    /**  TODO */
    private Controller  controller;
    
    /**  TODO */
    private Model       model = null;
    
    /**  TODO */
    private Composite   root;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     */
    public ViewPrivacyCriteria(final Composite parent, final Controller controller) {

        // Register
        this.controller = controller;
        this.controller.addListener(ModelPart.CRITERION_DEFINITION, this);
        this.controller.addListener(ModelPart.MODEL, this);

        root = new Composite(parent, SWT.NONE);
        root.setLayout(SWTUtil.createGridLayout(1));
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    @Override
    public void reset() {
        root.setRedraw(false);
        
        root.setRedraw(true);
    }

    @Override
    public void update(ModelEvent event) {
        
    }
}
