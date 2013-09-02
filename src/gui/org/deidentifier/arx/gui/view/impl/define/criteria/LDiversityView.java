package org.deidentifier.arx.gui.view.impl.define.criteria;

import org.deidentifier.arx.criteria.DistinctLDiversity;
import org.deidentifier.arx.criteria.EntropyLDiversity;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.criteria.RecursiveCLDiversity;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelLDiversityCriterion;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView.ModelEvent.EventTarget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;

public class LDiversityView extends CriterionView{

	private static final String    VARIANTS[] = { Resources.getMessage("CriterionDefinitionView.6"), Resources.getMessage("CriterionDefinitionView.7"), Resources.getMessage("CriterionDefinitionView.8") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    private Scale                  sliderL;
    private Scale                  sliderC;
    private Combo                  comboVariant;
    private Label                  labelC;
    private Label                  labelL;
    private String 				   attribute;

    public LDiversityView(final Composite parent,
                          final Controller controller,
                          final Model model) {
    	
    	super(parent, controller, model);
    	this.controller.addListener(EventTarget.SELECTED_ATTRIBUTE, this);
    	this.controller.addListener(EventTarget.INPUT, this);
    }

    @Override
	protected Composite build(Composite parent) {

        // Create input group
        final Composite group = new Composite(parent, SWT.NONE);
        group.setLayoutData(SWTUtil.createFillGridData());
        final GridLayout groupInputGridLayout = new GridLayout();
        groupInputGridLayout.numColumns = 3;
        group.setLayout(groupInputGridLayout);

        // Create l slider
        final Label lLabel = new Label(group, SWT.NONE);
        lLabel.setText(Resources.getMessage("CriterionDefinitionView.27")); //$NON-NLS-1$

        labelL = new Label(group, SWT.BORDER | SWT.CENTER);
        final GridData d = new GridData();
        d.minimumWidth = LABEL_WIDTH;
        d.widthHint = LABEL_WIDTH;
        labelL.setLayoutData(d);
        labelL.setText("2"); //$NON-NLS-1$

        sliderL = new Scale(group, SWT.HORIZONTAL);
        final GridData d4 = SWTUtil.createFillHorizontallyGridData();
        d4.horizontalSpan = 1;
        sliderL.setLayoutData(d4);
        sliderL.setMaximum(SLIDER_MAX);
        sliderL.setMinimum(0);
        sliderL.setSelection(0);
        sliderL.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
            	ModelLDiversityCriterion m = model.getLDiversityModel().get(attribute);
            	m.setL(sliderToInt(2, 100, sliderL.getSelection()));
                labelL.setText(String.valueOf(m.getL()));
            }
        });

        // Create criterion combo
        final Label cLabel = new Label(group, SWT.PUSH);
        cLabel.setText(Resources.getMessage("CriterionDefinitionView.33")); //$NON-NLS-1$

        comboVariant = new Combo(group, SWT.READ_ONLY);
        GridData d31 = SWTUtil.createFillHorizontallyGridData();
        d31.verticalAlignment = SWT.CENTER;
        d31.horizontalSpan = 2;
        comboVariant.setLayoutData(d31);
        comboVariant.setItems(VARIANTS);
        comboVariant.select(0);

        // Create c slider
        final Label zLabel = new Label(group, SWT.NONE);
        zLabel.setText(Resources.getMessage("CriterionDefinitionView.34")); //$NON-NLS-1$

        labelC = new Label(group, SWT.BORDER | SWT.CENTER);
        final GridData d9 = new GridData();
        d9.minimumWidth = LABEL_WIDTH;
        d9.widthHint = LABEL_WIDTH;
        labelC.setLayoutData(d9);
        labelC.setText("0.001"); //$NON-NLS-1$

        sliderC = new Scale(group, SWT.HORIZONTAL);
        final GridData d6 = SWTUtil.createFillHorizontallyGridData();
        d6.horizontalSpan = 1;
        sliderC.setLayoutData(d6);
        sliderC.setMaximum(SLIDER_MAX);
        sliderC.setMinimum(0);
        sliderC.setSelection(0);
        sliderC.setEnabled(false);
        sliderC.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
            	ModelLDiversityCriterion m = model.getLDiversityModel().get(attribute);
				m.setC(sliderToDouble(0.001d, 100d, sliderC.getSelection()));
                labelC.setText(String.valueOf(m.getC()));
            }
        });

        comboVariant.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
            	ModelLDiversityCriterion m = model.getLDiversityModel().get(attribute);
            	m.setVariant(comboVariant.getSelectionIndex());
            	if (m.getVariant()==2) {
            		sliderC.setEnabled(true);
            	} else {
            		sliderC.setEnabled(false);
            	}
            }
        });

        return group;
	}

	@Override
	public void reset() {
		sliderL.setSelection(0);
        sliderC.setSelection(0);
        labelC.setText("0.001"); //$NON-NLS-1$
        labelL.setText("2"); //$NON-NLS-1$
        comboVariant.select(0);
        super.reset();
	}

	@Override
	public void update(ModelEvent event) {
		if (event.target == EventTarget.SELECTED_ATTRIBUTE) {
			this.attribute = (String)event.data;
			this.parse();
		} 
        super.update(event);
	}

	@Override
	protected boolean parse() {
		ModelLDiversityCriterion m = model.getLDiversityModel().get(attribute);
		if (m==null){
			reset();
			return false;
		}
        labelC.setText(String.valueOf(m.getC()));
        labelL.setText(String.valueOf(m.getL()));
		sliderL.setSelection(intToSlider(2, 100, m.getL()));
		sliderC.setSelection(doubleToSlider(0.001d, 100d, m.getC()));
        comboVariant.select(m.getVariant());
        SWTUtil.enable(root);
        return true;
	}
}
