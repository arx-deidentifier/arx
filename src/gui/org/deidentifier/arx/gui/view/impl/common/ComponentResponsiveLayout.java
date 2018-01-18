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
package org.deidentifier.arx.gui.view.impl.common;

import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A layout that shows different contents, depending on available size
 * 
 * @author Fabian Prasser
 *
 */
public class ComponentResponsiveLayout {
    
    /**
     * Primary will be shown as long as width and height are within the given bounds,
     * otherwise the composite will switch to the secondary control
     * 
     * @param parent
     * @param minWidth
     * @param minHeight
     */
    public ComponentResponsiveLayout(final Composite parent, 
                                     final int minWidth, 
                                     final int minHeight,
                                     final Control primary,
                                     final Control secondary) {
        final StackLayout layout = new StackLayout();
        parent.setLayout (layout);
        layout.topControl = primary;
        parent.layout();
        parent.addControlListener(new ControlAdapter(){

            @Override
            public void controlResized(ControlEvent arg0) {

                if (parent.getSize().x < minWidth || parent.getSize().y < minHeight) {
                    if (layout.topControl != secondary) {
                        layout.topControl = secondary;
                        parent.layout();
                    }
                } else {
                    if (layout.topControl != primary) {
                        layout.topControl = primary;
                        parent.layout();
                    }
                }
            }
        });
    }
}
