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

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.common.ComponentGSSlider;
import org.deidentifier.arx.gui.view.impl.common.DelayedChangeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;

/**
 * This class allows to configure the coding model.
 *
 * @author Fabian Prasser
 */
public class ViewCodingModel implements IView {

    /** Controller */
    private Controller              controller = null;

    /** Model */
    private Model                   model      = null;

    /** Widget */
    private final ComponentGSSlider slider;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     */
    public ViewCodingModel(final Composite parent, final Controller controller) {

        // Register
        this.controller = controller;
        this.controller.addListener(ModelPart.MODEL, this);
        this.controller.addListener(ModelPart.METRIC, this);
        
        this.slider = new ComponentGSSlider(parent);
        this.slider.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent arg0) {
                if (model != null && model.getInputConfig() != null) {
                    model.getInputConfig().setSuppressionWeight(slider.getSelection());
                    model.getMetricConfiguration().setGsFactor(slider.getSelection());
                }
            }
        });
        this.slider.addSelectionListener(new DelayedChangeListener(100) {
            @Override public void delayedEvent() {
                controller.update(new ModelEvent(ViewCodingModel.this, ModelPart.GS_FACTOR, model.getInputConfig().getSuppressionWeight()));
            }
        });
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    @Override
    public void reset() {
        this.slider.setSelection(0.5d);
    }

    @Override
    public void update(ModelEvent event) {
        if (event.part == ModelPart.MODEL) {
            this.model = (Model)event.data;
            if (model.getInputConfig() != null) {
                this.slider.setSelection(this.model.getInputConfig().getSuppressionWeight());
            }
        } 
    }
}
