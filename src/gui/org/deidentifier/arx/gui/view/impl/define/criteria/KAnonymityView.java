package org.deidentifier.arx.gui.view.impl.define.criteria;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelKAnonymityCriterion;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;

public class KAnonymityView extends CriterionView {

	private Label labelK;
	private Scale sliderK;

	public KAnonymityView(final Composite parent, final Controller controller,
			final Model model) {

		super(parent, controller, model);
	}

	@Override
	public void reset() {
		sliderK.setSelection(0);
		labelK.setText("2");
		super.reset();
	}

	@Override
	protected Composite build(Composite parent) {

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
				model.getKAnonymityModel().setK(
						sliderToInt(2, 100, sliderK.getSelection()));
				labelK.setText(String
						.valueOf(model.getKAnonymityModel().getK()));
			}
		});
		return group;
	}

	@Override
	protected boolean parse() {
		ModelKAnonymityCriterion m = model.getKAnonymityModel();
		if (m==null){
			reset();
			return false;
		}
		labelK.setText(String.valueOf(m.getK()));
		sliderK.setSelection(intToSlider(2, 100, m.getK()));
		if (m.isActive()) {
			SWTUtil.enable(root);
		} else {
			SWTUtil.disable(root);
		}
		return true;
	}
}
