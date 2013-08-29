package org.deidentifier.arx.gui.view.impl.define;

import org.deidentifier.arx.criteria.EqualDistanceTCloseness;
import org.deidentifier.arx.criteria.HierarchicalDistanceTCloseness;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.Model;
import org.deidentifier.arx.gui.SWTUtil;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.def.IAttachable;
import org.deidentifier.arx.gui.view.def.ICriterionView;
import org.deidentifier.arx.gui.view.def.IView.ModelEvent.EventTarget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;

public class TClosenessView implements ICriterionView, IAttachable{


	private static final String    VARIANTS[] = { Resources.getMessage("CriterionDefinitionView.9"), Resources.getMessage("CriterionDefinitionView.10") };                                                  //$NON-NLS-1$ //$NON-NLS-2$
    private static final int       SLIDER_MAX      = 1000;
    private static final int       LABEL_WIDTH     = 50;
    
    private final Controller       controller;
    private final Model			   model;

    private Scale                  sliderT;
    private Combo                  comboVariant;
    private Label                  labelT;


    private String attribute;
    private double t = 0.001d;
    private int variant = 0;
    
    private Composite root;

    public TClosenessView(final Composite parent,
                          final Controller controller,
                          final Model model,
                          final String attribute) {

    	this.model = model;
    	this.attribute = attribute;
        this.controller = controller;
        this.controller.addListener(EventTarget.MODEL, this);
        this.controller.addListener(EventTarget.INPUT, this);
        this.root = build(parent);
    }

	private Composite build(Composite parent) {

        // Create input group
        final Composite group = new Composite(parent, SWT.NONE);
        group.setLayoutData(SWTUtil.createFillGridData());
        final GridLayout groupInputGridLayout = new GridLayout();
        groupInputGridLayout.numColumns = 3;
        group.setLayout(groupInputGridLayout);

        // Create criterion combo
        final Label cLabel = new Label(group, SWT.PUSH);
        cLabel.setText(Resources.getMessage("CriterionDefinitionView.42")); //$NON-NLS-1$

        comboVariant = new Combo(group, SWT.READ_ONLY);
        GridData d32 = SWTUtil.createFillHorizontallyGridData();
        d32.verticalAlignment = SWT.CENTER;
        d32.horizontalSpan = 2;
        comboVariant.setLayoutData(d32);
        comboVariant.setItems(VARIANTS);
        comboVariant.select(0);
        comboVariant.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                variant = comboVariant.getSelectionIndex();
            }
        });

        // Create t slider
        final Label zLabel = new Label(group, SWT.NONE);
        zLabel.setText(Resources.getMessage("CriterionDefinitionView.43")); //$NON-NLS-1$

        labelT = new Label(group, SWT.BORDER | SWT.CENTER);
        final GridData d9 = new GridData();
        d9.minimumWidth = LABEL_WIDTH;
        d9.widthHint = LABEL_WIDTH;
        labelT.setLayoutData(d9);
        labelT.setText("0.001"); //$NON-NLS-1$

        sliderT = new Scale(group, SWT.HORIZONTAL);
        final GridData d6 = SWTUtil.createFillHorizontallyGridData();
        d6.horizontalSpan = 1;
        sliderT.setLayoutData(d6);
        sliderT.setMaximum(SLIDER_MAX);
        sliderT.setMinimum(0);
        sliderT.setSelection(0);
        sliderT.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                t = sliderToDouble(0.001, 1, sliderT.getSelection());
                labelT.setText(String.valueOf(t));
            }
        });

        return group;
	}

    private double sliderToDouble(final double min,
                                  final double max,
                                  final int value) {
        double val = ((double) value / (double) SLIDER_MAX) * max;
        val = Math.round(val * SLIDER_MAX) / (double) SLIDER_MAX;
        if (val < min) {
            val = min;
        }
        if (val > max) {
            val = max;
        }
        return val;
    }

	@Override
	public PrivacyCriterion getCriterion() {
		
		if (variant==0){
			return new EqualDistanceTCloseness(attribute, t);
		} else if (variant==1){
			return new HierarchicalDistanceTCloseness(attribute, t, model.getInputConfig().getHierarchy(attribute));
		} else {
			throw new RuntimeException("Internal error: invalid variant of t-closeness");
		}
	}

	@Override
	public void dispose() {
        controller.removeListener(this);
	}

	@Override
	public void reset() {

        sliderT.setSelection(0);
        labelT.setText("0.001"); //$NON-NLS-1$
        comboVariant.select(0);
        t = 0.001d;
        variant = 0;
        SWTUtil.disable(root);
	}

	@Override
	public void update(ModelEvent event) {
		if (event.target == EventTarget.MODEL) {
			SWTUtil.enable(root);
        } else if (event.target == EventTarget.INPUT) {
            SWTUtil.enable(root);
        }
	}

	@Override
	public Control getControl() {
		return root;
	}
}
