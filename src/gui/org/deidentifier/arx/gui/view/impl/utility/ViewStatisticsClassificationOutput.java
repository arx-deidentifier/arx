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
package org.deidentifier.arx.gui.view.impl.utility;

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.aggregates.StatisticsClassification;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.eclipse.swt.widgets.Composite;

/**
 * This view displays a statistics about the performance of logistic regression classifiers
 *
 * @author Fabian Prasser
 */
public class ViewStatisticsClassificationOutput  extends ViewStatisticsClassification {

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     */
    public ViewStatisticsClassificationOutput(final Composite parent, final Controller controller) {
        super(parent, controller, ModelPart.OUTPUT);
    }

    @Override
    protected String[] getColumnHeaders() {
        return new String[] {
                Resources.getMessage("ViewStatisticsClassificationInput.3"), //$NON-NLS-1$
                Resources.getMessage("ViewStatisticsClassificationInput.9"), //$NON-NLS-1$
                Resources.getMessage("ViewStatisticsClassificationInput.1"), //$NON-NLS-1$
                Resources.getMessage("ViewStatisticsClassificationInput.13"), //$NON-NLS-1$
                Resources.getMessage("ViewStatisticsClassificationInput.7"), //$NON-NLS-1$
                Resources.getMessage("ViewStatisticsClassificationInput.12") //$NON-NLS-1$
        };
    }
    
    @Override
    protected List<Double> getColumnValues(StatisticsClassification result) {
        List<Double> list = new ArrayList<Double>();
        list.add(result.getZeroRAccuracy());
        list.add(result.getOriginalAccuracy());
        list.add(result.getAccuracy());
        if (result.getOriginalAccuracy() - result.getZeroRAccuracy() == 0d) {
            list.add(result.getAccuracy() / result.getZeroRAccuracy());
        } else {
            list.add((result.getAccuracy() - result.getZeroRAccuracy()) / (result.getOriginalAccuracy() - result.getZeroRAccuracy()));
        }
        list.add(result.getAverageError());
        list.add(result.getAverageError()-result.getOriginalAverageError());
        return list;
    }

}
