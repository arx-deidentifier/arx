package org.deidentifier.arx.gui.view.impl.define;

import org.deidentifier.arx.criteria.DistinctLDiversity;
import org.deidentifier.arx.criteria.EntropyLDiversity;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.criteria.RecursiveCLDiversity;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;

public class LDiversityView implements ICriterionView, IAttachable{

	private static final String    VARIANTS[] = { Resources.getMessage("CriterionDefinitionView.6"), Resources.getMessage("CriterionDefinitionView.7"), Resources.getMessage("CriterionDefinitionView.8") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    private static final int       SLIDER_MAX      = 1000;
    private static final int       LABEL_WIDTH     = 50;
    
    private final Controller       controller;

    private Scale                  sliderL;
    private Scale                  sliderC;
    private Combo                  comboVariant;
    private Label                  labelC;
    private Label                  labelL;

    private String attribute;
    private int l = 2;
    private double c = 0.001d;
    private int variant = 0;
    
    private Composite root;

    public LDiversityView(final Composite parent,
                          final Controller controller,
                          final String attribute) {

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
        groupInputGridLayout.numColumns = 4;
        group.setLayout(groupInputGridLayout);

        // Create k slider
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
        d4.horizontalSpan = 2;
        sliderL.setLayoutData(d4);
        sliderL.setMaximum(SLIDER_MAX);
        sliderL.setMinimum(0);
        sliderL.setSelection(0);
        sliderL.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                l = sliderToInt(2, 100, sliderL.getSelection());
                labelL.setText(String.valueOf(l));
            }
        });

        // Create criterion combo
        final Label cLabel = new Label(group, SWT.PUSH);
        cLabel.setText(Resources.getMessage("CriterionDefinitionView.33")); //$NON-NLS-1$

        comboVariant = new Combo(group, SWT.READ_ONLY);
        GridData d31 = SWTUtil.createFillHorizontallyGridData();
        d31.verticalAlignment = SWT.CENTER;
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
                c = sliderToDouble(0.001,
                                          100,
                                          sliderC.getSelection());
                labelC.setText(String.valueOf(c));
            }
        });

        comboVariant.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
            	variant = comboVariant.getSelectionIndex();
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
		
		if (variant==0){
			return new DistinctLDiversity(attribute, l);
		} else if (variant==1){
			return new EntropyLDiversity(attribute, l);
		} else if (variant==2){
			return new RecursiveCLDiversity(attribute, c, l);
		} else {
			throw new RuntimeException("Internal error: invalid variant of l-diversity");
		}
	}

	@Override
	public void dispose() {
        controller.removeListener(this);
	}

	@Override
	public void reset() {
		sliderL.setSelection(0);
        sliderC.setSelection(0);
        labelC.setText("0.001"); //$NON-NLS-1$
        labelL.setText("2"); //$NON-NLS-1$
        comboVariant.select(0);
        SWTUtil.disable(root);
        l = 2;
        c = 0.001d;
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
