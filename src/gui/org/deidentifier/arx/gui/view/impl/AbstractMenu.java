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

package org.deidentifier.arx.gui.view.impl;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.view.def.IView;

/**
 * Abstract base class for menus and toolbars
 * @author Fabian Prasser
 */
public abstract class AbstractMenu implements IView {

    /** The controller */
    private Controller controller;
    /** The model */
    private Model model;
    
    public AbstractMenu(Controller controller){
        this.controller = controller;
        controller.addListener(ModelPart.MODEL, this);
        controller.addListener(ModelPart.SELECTED_NODE, this);
        controller.addListener(ModelPart.SELECTED_ATTRIBUTE, this);
        controller.addListener(ModelPart.SELECTED_PERSPECTIVE, this);
        controller.addListener(ModelPart.OUTPUT, this);
        controller.addListener(ModelPart.RESULT, this);
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
    }



    @Override
    public void reset() {
        this.model = null;
    }



    @Override
    public void update(ModelEvent event) {
        if (event.part == ModelPart.MODEL) {
            if (event.data != null && (event.data instanceof Model)) {
                this.model = (Model)event.data;
            }
        }
        this.update(this.model);
    }

    /**
     * @return the controller
     */
    protected Controller getController() {
        return controller;
    }

    protected abstract void update(Model model);
}
