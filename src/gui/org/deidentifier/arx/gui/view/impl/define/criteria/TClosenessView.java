package org.deidentifier.arx.gui.view.impl.define.criteria;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelTClosenessCriterion;
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

public class TClosenessView extends CriterionView {

	private static final String VARIANTS[] = {
			Resources.getMessage("CriterionDefinitionView.9"), Resources.getMessage("CriterionDefinitionView.10") }; //$NON-NLS-1$ //$NON-NLS-2$

	private Scale sliderT;
	private Combo comboVariant;
	private Label labelT;

	private String attribute;

	public TClosenessView(final Composite parent, final Controller controller,
			final Model model) {

		super(parent, controller, model);
		this.controller.addListener(EventTarget.SELECTED_ATTRIBUTE, this);
    	this.controller.addListener(EventTarget.INPUT, this);
    	this.controller.addListener(EventTarget.ATTRIBUTE_TYPE, this);
	}

	@Override
	public void reset() {
		sliderT.setSelection(0);
		labelT.setText("0.001"); //$NON-NLS-1$
		comboVariant.select(0);
		super.reset();
	}

	@Override
	public void update(ModelEvent event) {
		if (event.target == EventTarget.SELECTED_ATTRIBUTE) {
			this.attribute = (String)event.data;
			this.parse();
		} else if (event.target == EventTarget.ATTRIBUTE_TYPE) {
			if (event.data.equals(this.attribute)) {
				this.parse();
			}
		}
		super.update(event);
	}

	@Override
	protected Composite build(Composite parent) {

		// Create input group
		final Composite group = new Composite(parent, SWT.NONE);
		group.setLayoutData(SWTUtil.createFillGridData());
		final GridLayout groupInputGridLayout = new GridLayout();
		groupInputGridLayout.numColumns = 5;
		group.setLayout(groupInputGridLayout);

		// Create criterion combo
		final Label cLabel = new Label(group, SWT.PUSH);
		cLabel.setText(Resources.getMessage("CriterionDefinitionView.42")); //$NON-NLS-1$

		comboVariant = new Combo(group, SWT.READ_ONLY);
		GridData d32 = SWTUtil.createFillHorizontallyGridData();
		d32.verticalAlignment = SWT.CENTER;
		d32.horizontalSpan = 1;
		comboVariant.setLayoutData(d32);
		comboVariant.setItems(VARIANTS);
		comboVariant.select(0);
		comboVariant.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent arg0) {
				ModelTClosenessCriterion m = model.getTClosenessModel().get(
						attribute);
				m.setVariant(comboVariant.getSelectionIndex());
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
				ModelTClosenessCriterion m = model.getTClosenessModel().get(
						attribute);
				m.setT(sliderToDouble(0.001d, 1d, sliderT.getSelection()));
				labelT.setText(String.valueOf(m.getT()));
			}
		});

		return group;
	}

	@Override
	protected void parse() {
		ModelTClosenessCriterion m = model.getTClosenessModel().get(attribute);
		if (m==null){
			reset();
			return;
		}
		root.setRedraw(false);
		sliderT.setSelection(doubleToSlider(0.001d, 1d, m.getT()));
		labelT.setText(String.valueOf(m.getT()));
		comboVariant.select(m.getVariant());
		if (m.isActive() && m.isEnabled()) {
			SWTUtil.enable(root);
		} else {
			SWTUtil.disable(root);
		}
		root.setRedraw(true);
	}
}
