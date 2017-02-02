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

package org.deidentifier.arx.gui.view.impl.wizard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.swtchart.*;
import org.swtchart.ISeries.*;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.risk.RiskQuestionnaire;
import org.deidentifier.arx.risk.RiskQuestionnaireQuestion;
import org.deidentifier.arx.risk.RiskQuestionnaireSection;

/**
 * The stack visualization
 * 
 * @author Thomas Guenzel
 * @author Fabian Prasser
 */
public class RiskWizardVisualizationStack extends RiskWizardVisualization {

    /** Displayed chart */
    private Chart      chart;

    /** Bar series for the positive values */
    private IBarSeries positive;

    /** Bar series for the neutral values */
    private IBarSeries neutral;

    /** Bar series for the negative values */
    private IBarSeries negative;

    /**
     * Create a new stack visualization
     * 
     * @param parent
     *            the parent
     * @param controller
     *            the controller
     * @param checklist
     *            the checklist
     */
    public RiskWizardVisualizationStack(Composite parent, Controller controller, RiskQuestionnaire checklist) {
        super(parent, controller, checklist);
    }

    /**
     * Updates the UI when the weights/score changes
     */
    @Override
    public void updateWeights() {
        RiskQuestionnaireSection sections[] = this.checklist.getSections();
        double[] posY = new double[sections.length];
        double[] neuY = new double[sections.length];
        double[] negY = new double[sections.length];

        int idx = 0;
        for (RiskQuestionnaireSection sec : sections) {
            double pos = 0.0;
            double neu = 0.0;
            double neg = 0.0;

            for (RiskQuestionnaireQuestion q : sec.getItems()) {
                double w = q.getWeight();
                double s = q.getScore();
                if (s == 0.0) {
                    neu += w;
                } else if (s > 0.0) {
                    pos += w;
                } else {
                    neg += w;
                }
            }

            posY[idx] = pos / sec.getMaximumWeight();
            neuY[idx] = neu / sec.getMaximumWeight();
            negY[idx] = neg / sec.getMaximumWeight();

            idx++;
        }

        positive.setYSeries(posY);
        positive.enableStack(true);
        neutral.setYSeries(neuY);
        neutral.enableStack(true);
        negative.setYSeries(negY);
        negative.enableStack(true);

        chart.update();
        chart.redraw();
    }

    /**
     * Creates the view containing the stack bar graph visualization
     */
    @Override
    protected void createVisualization() {
        RiskQuestionnaireSection sections[] = this.checklist.getSections();
        String sectionNames[] = new String[sections.length];
        int idx = 0;
        for (RiskQuestionnaireSection s : sections) {
            sectionNames[idx] = s.getTitle();
            idx++;
        }

        FillLayout fillLayout = new FillLayout();
        fillLayout.type = SWT.VERTICAL;
        this.setLayout(fillLayout);
        chart = new Chart(this, SWT.NONE);
        chart.getTitle().setText(Resources.getMessage("RiskWizard.18"));

        double[] positiveSeries = { 0.1, 0, 0 };
        double[] neutralSeries = { 0.1, 0, 0 };
        double[] negativeSeries = { 0.1, 0, 0 };

        ISeriesSet seriesSet = chart.getSeriesSet();

        positive = (IBarSeries) seriesSet.createSeries(SeriesType.BAR,
                                                       Resources.getMessage("RiskWizard.15"));
        positive.setBarColor(this.getDisplay().getSystemColor(SWT.COLOR_GREEN));
        positive.enableStack(true);
        positive.setYSeries(positiveSeries);

        neutral = (IBarSeries) seriesSet.createSeries(SeriesType.BAR,
                                                      Resources.getMessage("RiskWizard.16"));
        neutral.setBarColor(this.getDisplay().getSystemColor(SWT.COLOR_GRAY));
        neutral.enableStack(true);
        neutral.setYSeries(neutralSeries);

        negative = (IBarSeries) seriesSet.createSeries(SeriesType.BAR,
                                                       Resources.getMessage("RiskWizard.17"));
        negative.setBarColor(this.getDisplay().getSystemColor(SWT.COLOR_RED));
        negative.enableStack(true);
        negative.setYSeries(negativeSeries);

        IAxisSet axisSet = chart.getAxisSet();
        axisSet.adjustRange();

        IAxis yAxis = axisSet.getYAxis(0);
        yAxis.setRange(new Range(0.0, 1.05));
        yAxis.getTitle().setVisible(false);

        IAxis xAxis = axisSet.getXAxis(0);
        xAxis.setCategorySeries(sectionNames);
        xAxis.enableCategory(true);
        xAxis.getTitle().setVisible(false);
    }
}
