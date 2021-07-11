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
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.risk.RiskQuestionnaireQuestion;
import org.deidentifier.arx.risk.RiskQuestionnaireQuestion.*;

/**
 * The radio group component for answering the questions, contains yes, no and
 * n/a as possible answers
 * 
 * @author Thomas Guenzel
 * @author Fabian Prasser
 */
public class RiskWizardComponentAnswer extends Composite {

    /** Widget */
    private Button   yesButton;
    /** Widget */
    private Button   noButton;
    /** Widget */
    private Button   n_aButton;
    /** The question which will be updated, when the selected button changes */
    private RiskQuestionnaireQuestion item;

    /**
     * create a new answer radio group in the specified composite and targetItem
     * 
     * @param parent
     * @param targetItem
     */
    public RiskWizardComponentAnswer(Composite parent, RiskQuestionnaireQuestion targetItem) {
        super(parent, SWT.NONE);
        this.item = targetItem;
        this.setLayout(new FillLayout());
        Group group = new Group(this, SWT.SHADOW_IN);
        group.setLayout(new RowLayout(SWT.HORIZONTAL));

        SelectionListener selectionListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                Button button = ((Button) event.widget);
                if (button == yesButton) {
                    item.answer = Answer.YES;
                } else if (button == noButton) {
                    item.answer = Answer.NO;
                } else {
                    item.answer = Answer.N_A;
                }
            };
        };

        yesButton = new Button(group, SWT.RADIO);
        yesButton.setText(Resources.getMessage("RiskWizard.3"));
        yesButton.addSelectionListener(selectionListener);

        noButton = new Button(group, SWT.RADIO);
        noButton.setText(Resources.getMessage("RiskWizard.4"));
        noButton.addSelectionListener(selectionListener);

        n_aButton = new Button(group, SWT.RADIO);
        n_aButton.setText(Resources.getMessage("RiskWizard.5"));
        n_aButton.setSelection(true);
        n_aButton.addSelectionListener(selectionListener);
    }

}
