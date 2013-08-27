package org.deidentifier.arx.gui.view.impl.define;

import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.gui.Controller;
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

public class KAnonymityView implements ICriterionView, IAttachable{
	
    private static final int       SLIDER_MAX      = 1000;
    private static final int       LABEL_WIDTH     = 50;
    
    private final Controller       controller;
    private Label                  labelK;
    private Scale                  sliderK;
    
    private int k = 2;
    private Composite root;

    public KAnonymityView(final Composite parent,
                           final Controller controller) {

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

        // Create k slider
        final Label kLabel = new Label(group, SWT.NONE);
        kLabel.setText(Resources.getMessage("CriterionDefinitionView.22")); //$NON-NLS-1$

        labelK = new Label(group, SWT.BORDER | SWT.CENTER);
        final GridData d = new GridData();
        d.minimumWidth = LABEL_WIDTH;
        d.widthHint = LABEL_WIDTH;
        labelK.setLayoutData(d);
        labelK.setText("2"); //$NON-NLS-1$

        sliderK = new Scale(group, SWT.HORIZONTAL);
        sliderK.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        sliderK.setMaximum(SLIDER_MAX);
        sliderK.setMinimum(0);
        sliderK.setSelection(0);
        sliderK.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                k = sliderToInt(2, 100, sliderK.getSelection());
                labelK.setText(String.valueOf(k));
            }
        });
        return group;
	}

    private int sliderToInt(final int min, final int max, final int value) {
        int val = (int) Math.round(((double) value / (double) SLIDER_MAX) * max);
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
		return new KAnonymity(k);
	}

	@Override
	public void dispose() {
        controller.removeListener(this);
	}

	@Override
	public void reset() {
        sliderK.setSelection(0);
        labelK.setText("2");
        k = 2;
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
