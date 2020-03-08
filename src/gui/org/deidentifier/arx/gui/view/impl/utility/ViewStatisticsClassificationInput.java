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
public class ViewStatisticsClassificationInput extends ViewStatisticsClassification {

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     */
    public ViewStatisticsClassificationInput(final Composite parent,
                                             final Controller controller) {
        super(parent, controller, ModelPart.INPUT);
    }

    @Override
    protected String[] getColumnHeadersForAUCTable() {
        return new String[] {
                // Baseline AUC
                Resources.getMessage("ViewStatisticsClassificationInput.14"), //$NON-NLS-1$
                // AUC
                Resources.getMessage("ViewStatisticsClassificationInput.24"), //$NON-NLS-1$
        };
    }
    
    @Override
    protected String[] getColumnHeadersForPerformanceForOverallPerformanceTable() {
        return new String[] {
                // Baseline accuracy
                Resources.getMessage("ViewStatisticsClassificationInput.3"), //$NON-NLS-1$
                // Accuracy
                Resources.getMessage("ViewStatisticsClassificationInput.1"), //$NON-NLS-1$
        };
    }
    
    @Override
    protected boolean[] getColumnTypesForPerformanceForOverallPerformanceTable() {
        return new boolean[] {true, true};
    }

    @Override
    protected List<Double> getColumnValuesForOverallPerformanceTable(StatisticsClassification result) {
        List<Double> list = new ArrayList<Double>();
        list.add(result.getZeroRAccuracy());
        list.add(result.getOriginalAccuracy());
        return list;
    }
}
