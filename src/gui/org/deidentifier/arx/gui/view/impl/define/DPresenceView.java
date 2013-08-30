package org.deidentifier.arx.gui.view.impl.define;

import org.deidentifier.arx.criteria.DPresence;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;

public class DPresenceView implements ICriterionView, IAttachable{


    private static final int       SLIDER_MAX      = 1000;
    private static final int       LABEL_WIDTH     = 50;
    
    private final Controller       controller;
    private final Model			   model;

    private Scale                  sliderDMin;
    private Scale                  sliderDMax;
    private Label                  labelDMin;
    private Label                  labelDMax;

    private double dmin = 0.001d;
    private double dmax = 0.001d;
    
    private Composite root;

    public DPresenceView(final Composite parent,
                         final Controller controller,
                         final Model model) {

    	this.model = model;
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

        // Create dmin slider
        final Label zLabel = new Label(group, SWT.NONE);
        zLabel.setText(Resources.getMessage("CriterionDefinitionView.50")); //$NON-NLS-1$

        labelDMin = new Label(group, SWT.BORDER | SWT.CENTER);
        final GridData d9 = new GridData();
        d9.minimumWidth = LABEL_WIDTH;
        d9.widthHint = LABEL_WIDTH;
        labelDMin.setLayoutData(d9);
        labelDMin.setText("0.001"); //$NON-NLS-1$

        sliderDMin = new Scale(group, SWT.HORIZONTAL);
        final GridData d6 = SWTUtil.createFillHorizontallyGridData();
        d6.horizontalSpan = 1;
        sliderDMin.setLayoutData(d6);
        sliderDMin.setMaximum(SLIDER_MAX);
        sliderDMin.setMinimum(0);
        sliderDMin.setSelection(0);
        sliderDMin.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                dmin = sliderToDouble(0.001, 1, sliderDMin.getSelection());
                labelDMin.setText(String.valueOf(dmin));
            }
        });


        // Create dax slider
        final Label z2Label = new Label(group, SWT.NONE);
        z2Label.setText(Resources.getMessage("CriterionDefinitionView.51")); //$NON-NLS-1$

        labelDMax = new Label(group, SWT.BORDER | SWT.CENTER);
        final GridData d91 = new GridData();
        d91.minimumWidth = LABEL_WIDTH;
        d91.widthHint = LABEL_WIDTH;
        labelDMax.setLayoutData(d91);
        labelDMax.setText("0.001"); //$NON-NLS-1$

        sliderDMax = new Scale(group, SWT.HORIZONTAL);
        final GridData d62 = SWTUtil.createFillHorizontallyGridData();
        d62.horizontalSpan = 1;
        sliderDMax.setLayoutData(d62);
        sliderDMax.setMaximum(SLIDER_MAX);
        sliderDMax.setMinimum(0);
        sliderDMax.setSelection(0);
        sliderDMax.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                dmax = sliderToDouble(0.001, 1, sliderDMax.getSelection());
                labelDMax.setText(String.valueOf(dmax));
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
		return new DPresence(dmin, dmax, model.getInputConfig().getResearchSubset());
	}

	@Override
	public void dispose() {
        controller.removeListener(this);
	}

	@Override
	public void reset() {

        sliderDMin.setSelection(0);
        sliderDMax.setSelection(0);
        labelDMin.setText("0.001"); //$NON-NLS-1$
        labelDMax.setText("0.001"); //$NON-NLS-1$
        dmin = 0.001d;
        dmax = 0.001d;
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
