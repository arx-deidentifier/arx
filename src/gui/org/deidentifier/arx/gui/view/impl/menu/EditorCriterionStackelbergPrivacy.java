package org.deidentifier.arx.gui.view.impl.menu;

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.gui.model.ModelCriterion;
import org.deidentifier.arx.gui.model.ModelStackelbergPrivacyCriterion;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.linearbits.swt.widgets.Knob;

public class EditorCriterionStackelbergPrivacy extends EditorCriterion<ModelStackelbergPrivacyCriterion> {
	
	private Text labelAdvCost;
	private Text labelAdvGain;
	private Text labelPubBenefit;
	private Text labelPubLoss;
	
	Knob<Double> knobAdvCost;
	Knob<Double> knobAdvGain;
	Knob<Double> knobPubBenefit;
	Knob<Double> knobPubLoss;
	
	public EditorCriterionStackelbergPrivacy(final Composite parent, final ModelStackelbergPrivacyCriterion model) {
		super(parent, model);
	}

	@Override
	protected Composite build(Composite parent) {
        // Create input group
        Composite group = new Composite(parent, SWT.NONE);
        group.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        GridLayout groupInputGridLayout = new GridLayout();
        groupInputGridLayout.numColumns = 6;
        group.setLayout(groupInputGridLayout);
        
        Label pubBenefit = new Label(group, SWT.NONE);
        pubBenefit.setText(Resources.getMessage("CriterionDefinitionView.110"));
        labelPubBenefit = createLabel(group);
        knobPubBenefit = createKnobDouble(group, 0, 10000);
        knobPubBenefit.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.setPublisherBenefit(knobPubBenefit.getValue());
                updateLabel(labelPubBenefit, model.getPublisherBenefit());
            }
        });
        
        Label pubLoss = new Label(group, SWT.NONE);
        pubLoss.setText(Resources.getMessage("CriterionDefinitionView.111"));
        labelPubLoss = createLabel(group);
        knobPubLoss = createKnobDouble(group, 0, 10000);
        knobPubLoss.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.setPublisherLoss(knobPubLoss.getValue());
                updateLabel(labelPubLoss, model.getPublisherLoss());
            }
        });
        
        Label advGain = new Label(group, SWT.NONE);
        advGain.setText(Resources.getMessage("CriterionDefinitionView.112"));
        labelAdvGain = createLabel(group);
        knobAdvGain = createKnobDouble(group, 0, 10000);
        knobAdvGain.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.setAdversaryGain(knobAdvGain.getValue());
                updateLabel(labelAdvGain, model.getAdversaryGain());
            }
        });
        
        Label advCost = new Label(group, SWT.NONE);
        advCost.setText(Resources.getMessage("CriterionDefinitionView.113"));
        labelAdvCost = createLabel(group);
        knobAdvCost = createKnobDouble(group, 0, 10000);
        knobAdvCost.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.setAdversaryCost(knobAdvCost.getValue());
                updateLabel(labelAdvCost, model.getAdversaryCost());
            }
        });

        return group;
	}

	@Override
	protected List<ModelCriterion> getTypicalParameters() {
		
		ModelStackelbergPrivacyCriterion mc1 = new ModelStackelbergPrivacyCriterion();
		mc1.setAdversaryCost(4d);
		mc1.setAdversaryGain(300d);
		mc1.setPublisherLoss(300d);
		mc1.setPublisherBenefit(1200d);
		
		List<ModelCriterion> result = new ArrayList<ModelCriterion>();
		result.add(mc1);
        return result;
	}

	@Override
	protected void parse(ModelStackelbergPrivacyCriterion model, boolean defaultParameters) {
		
		updateLabel(labelAdvCost, model.getAdversaryCost());
        updateLabel(labelAdvGain, model.getAdversaryGain());
		updateLabel(labelPubBenefit, model.getPublisherBenefit());
        updateLabel(labelPubLoss, model.getPublisherLoss());
        knobAdvCost.setValue(model.getAdversaryCost());
        knobAdvGain.setValue(model.getAdversaryGain());
        knobPubBenefit.setValue(model.getPublisherBenefit());
        knobPubLoss.setValue(model.getPublisherLoss());
	}
}
