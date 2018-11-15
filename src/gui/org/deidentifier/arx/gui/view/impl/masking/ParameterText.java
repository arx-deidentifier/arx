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

package org.deidentifier.arx.gui.view.impl.masking;

import org.deidentifier.arx.masking.variable.DistributionParameter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

// TODO Drop this class and find another solution.
public class ParameterText extends Text {

    // Allow subclassing
    @Override
    protected void checkSubclass() { }

    private DistributionParameter<?> parameter;

    public ParameterText(DistributionParameter<?> parameter, Composite composite, int style) {

        super(composite, style);
        this.parameter = parameter;

        // Set initial value
        setText(String.valueOf(parameter.getInitial()));

    }

    public DistributionParameter<?> getParameter() {

        if (parameter.getType().equals(Integer.class)) {

            ((DistributionParameter.IntegerParameter) parameter).setValue(Integer.valueOf(getText()));

        } else if (parameter.getType().equals(Double.class)) {

            ((DistributionParameter.DoubleParameter) parameter).setValue(Double.valueOf(getText()));

        }

        return parameter;

    }

}
