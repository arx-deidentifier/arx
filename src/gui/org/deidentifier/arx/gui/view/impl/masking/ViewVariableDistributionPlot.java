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

package org.deidentifier.arx.gui.view.impl.masking;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.view.def.IView;
import org.eclipse.swt.widgets.Composite;

/**
 * This view plots the distribution for a particular variable
 *
 * @author Karol Babioch
 */
public class ViewVariableDistributionPlot implements IView {

    private Controller controller;

    private Composite parentComposition;


    public ViewVariableDistributionPlot(final Composite parent, final Controller controller) {

        this.controller = controller;
        this.parentComposition = build(parent);

    }


    private Composite build(Composite parent) {

        return parent;

    }

    @Override
    public void dispose() {

    }

    @Override
    public void reset() {

    }

    @Override
    public void update(ModelEvent event) {

    }

}
