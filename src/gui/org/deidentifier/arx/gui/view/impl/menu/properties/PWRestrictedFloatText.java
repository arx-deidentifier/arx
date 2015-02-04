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

package org.deidentifier.arx.gui.view.impl.menu.properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.mihalis.opal.preferenceWindow.widgets.PWFloatText;

/**
 * Wrapper around PWFloatText
 * 
 * @author Fabian Prasser
 */
public class PWRestrictedFloatText extends PWFloatText {
    
    /** Bound*/
    private final double min;
    /** Bound*/
    private final double max;

    /**
     * Constructor
     * @param arg0
     * @param arg1
     */
    public PWRestrictedFloatText(String arg0, String arg1, double min, double max) {
        super(arg0, arg1);
        this.min = min;
        this.max = max;
    }

    @Override
    public void addVerifyListeners() {
        super.addVerifyListeners();
        this.text.addListener(SWT.Verify, new Listener() {
            @Override
            public void handleEvent(final Event e) {
                try {
                    double value = Double.valueOf(e.text);
                    e.doit = value >= min && value <= max;
                } catch (Exception ex) {
                    e.doit = false;
                }
            }
        });
    }
}
