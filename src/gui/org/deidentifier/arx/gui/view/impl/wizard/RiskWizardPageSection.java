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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.risk.RiskQuestionnaireQuestion;
import org.deidentifier.arx.risk.RiskQuestionnaireSection;

/**
 * Each SectionPage shows all the questions from a checklist section
 * 
 * @author Thomas Guenzel
 * @author Fabian Prasser
 */
public class RiskWizardPageSection extends WizardPage {

    /** Widget */
    private RiskQuestionnaireSection                         section;
    /** Widget */
    private Composite                       container;
    /** Field */
    private boolean                         weightEditable;
    /** Field */
    private List<RiskWizardComponentWeight> weightFields;

    /**
     * create a new page for a section
     * 
     * @param section
     *            the section of this page
     */
    public RiskWizardPageSection(RiskQuestionnaireSection section) {
        super(section.getTitle());

        this.weightEditable = false;
        this.section = section;
        this.setTitle(section.getTitle());
        this.setDescription(Resources.getMessage("RiskWizard.1"));
    }

    /**
     * creates the control, by adding the questions inside a scroll view
     */
    @Override
    public void createControl(Composite parent) {
        final Composite rootComposite = new Composite(parent, SWT.NONE);

        GridLayout rootGrid = new GridLayout();
        rootComposite.setLayout(rootGrid);

        final ScrolledComposite sc = new ScrolledComposite(rootComposite, SWT.BORDER | SWT.V_SCROLL);
        GridData sgd = new GridData(GridData.FILL_BOTH);
        sgd.grabExcessHorizontalSpace = true;
        sgd.grabExcessVerticalSpace = true;
        sgd.widthHint = 400;// SWT.DEFAULT;
        sgd.heightHint = 300;
        sc.setLayoutData(sgd);

        sc.setExpandHorizontal(true);
        sc.setExpandVertical(true);

        container = new Composite(sc, SWT.NULL);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        layout.numColumns = 3;
        layout.verticalSpacing = 12;

        createItems();

        rootComposite.addListener(SWT.Resize, new Listener() {
            int width = -1;

            @Override
            public void handleEvent(org.eclipse.swt.widgets.Event e) {
                int newWidth = rootComposite.getSize().x;
                if (newWidth != width) {
                    sc.setMinHeight(container.computeSize(newWidth, SWT.DEFAULT).y + 40);
                    width = newWidth;
                }
            }
        });

        sc.setContent(container);
        sc.setMinSize(container.computeSize(400, SWT.DEFAULT));
        sc.layout();

        setControl(rootComposite);
    }

    /**
     * enable or disable the weight edit mode
     */
    public void setWeightEditable(boolean editable) {
        if (editable == this.weightEditable) { return; }
        this.weightEditable = editable;

        // TODO iterate and set
        for (RiskWizardComponentWeight w : this.weightFields) {
            w.setEnabled(editable);
        }
    }

    /**
     * creates the interface for the individual items (questions)
     */
    private void createItems() {
        // add weight fields
        this.weightFields = new ArrayList<RiskWizardComponentWeight>();
        this.weightFields.add(new RiskWizardComponentWeight(container, this.section, false));

        Label label = new Label(container, SWT.NONE | SWT.WRAP);
        label.setText(this.section.getTitle());
        // from
        // http://eclipsesource.com/blogs/2014/02/10/swt-best-practices-changing-fonts/
        FontDescriptor boldDescriptor = FontDescriptor.createFrom(label.getFont())
                                                      .setStyle(SWT.BOLD);
        Font boldFont = boldDescriptor.createFont(label.getDisplay());
        label.setFont(boldFont);

        GridData headerGridData = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
        headerGridData.verticalAlignment = GridData.CENTER;
        headerGridData.grabExcessVerticalSpace = false;
        headerGridData.horizontalSpan = 2;
        label.setLayoutData(headerGridData);

        RiskQuestionnaireQuestion[] items = section.getItems();
        for (int i = 0; i < items.length; i++) {
            RiskQuestionnaireQuestion itm = items[i];

            this.weightFields.add(new RiskWizardComponentWeight(container, itm, false));

            Label label1 = new Label(container, SWT.NONE | SWT.WRAP);
            label1.setText(itm.getTitle());
            GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
            gd.verticalAlignment = GridData.CENTER;
            gd.grabExcessVerticalSpace = false;
            label1.setLayoutData(gd);

            new RiskWizardComponentAnswer(container, itm);
        }
    }

    /**
     * update the weights (called when a new weight configuration is loaded)
     */
    protected void updateWeights() {
        if (this.weightFields == null) { return; }
        for (RiskWizardComponentWeight field : weightFields) {
            field.updateText();
        }
    }
}
