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

import java.text.DecimalFormat;
import java.text.ParseException;

import org.deidentifier.arx.risk.RiskQuestionnaireItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

/**
 * A drop down field for changing a question's weight
 * 
 * @author Thomas Guenzel
 * @author Fabian Prasser
 */
public class RiskWizardComponentWeight {

    /** The targeted item */
    private RiskQuestionnaireItem               item;
    /** Widget */
    private Combo              dropdown;
    /** Flag */
    private boolean            disableUpdateQuestion;
    /** Flag */
    private boolean            enabled;
    /** Format */
    static final DecimalFormat df = new DecimalFormat("#.00");

    /**
     * create a new weight field for an item (question or section)
     * 
     * @param composite
     *            the composite
     * @param item
     *            the item this field targets
     * @param enabled
     *            whether this control is currently enabled
     */
    public RiskWizardComponentWeight(Composite composite, RiskQuestionnaireItem item, boolean enabled) {
        dropdown = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
        dropdown.add("0.25");
        dropdown.add("0.50");
        dropdown.add("1.00");
        dropdown.add("1.50");
        dropdown.add("2.00");
        dropdown.add("3.00");
        dropdown.add("5.00");

        ModifyListener modifyListener = new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                RiskWizardComponentWeight.this.updateItem();
            }
        };

        dropdown.addModifyListener(modifyListener);

        this.item = item;
        this.setEnabled(enabled);

        GridData gridData = new GridData(GridData.VERTICAL_ALIGN_CENTER);
        gridData.widthHint = 72;
        dropdown.setLayoutData(gridData);

        updateText();
    }

    /**
     * returns, whether the field is editable
     * 
     * @return if field can be edited
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * change whether field can be edited
     * 
     * @param enabled
     *            whether field can be edited
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.dropdown.setEnabled(enabled);
    }

    /**
     * updates the text of the dropdown
     */
    public void updateText() {
        disableUpdateQuestion = true;
        double newWeight = item.getWeight();
        String weightString = df.format(newWeight);
        // System.out.println("Weight: "+weightString+" item="+item.getIdentifier());

        boolean containsText = false;
        for (String t : dropdown.getItems()) {
            if (t.equals(weightString)) {
                containsText = true;
            }
        }
        if (containsText == false) {
            dropdown.add(weightString);
        }

        dropdown.setText(df.format(newWeight));
        disableUpdateQuestion = false;
    }

    /**
     * updates the target item
     */
    protected void updateItem() {
        if (disableUpdateQuestion) { return; }
        try {
            double value = df.parse(dropdown.getText()).doubleValue();
            item.setWeight(value);
        } catch (ParseException e) {
            // e.printStackTrace();
        }
    }

}
