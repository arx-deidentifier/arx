package org.deidentifier.arx.gui.view.impl.wizard.sharingwizard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.impl.wizard.sharingwizard.checklist.Question;
import org.deidentifier.arx.gui.view.impl.wizard.sharingwizard.checklist.Question.*;

/**
 * The radio group component for answering the questions, contains yes, no and n/a as possible answers
 *
 */
public class AnswerRadioGroup extends Composite {
	
	private Button yesButton;
	private Button noButton;
	private Button n_aButton;
	
	/**
	 * the question which will be updated, when the selected button changes
	 */
	private Question item;
	
	/**
	 * create a new answer radio group in the specified composite and targetItem
	 * @param parent
	 * @param targetItem
	 */
	public AnswerRadioGroup(Composite parent, Question targetItem) {
		super(parent,SWT.NONE);
		this.item = targetItem;
		this.setLayout(new FillLayout());
		Group group = new Group(this, SWT.SHADOW_IN);
		group.setLayout(new RowLayout(SWT.HORIZONTAL));

		SelectionListener selectionListener = new SelectionAdapter () {
			public void widgetSelected(SelectionEvent event) {
				Button button = ((Button) event.widget);
				if(button == yesButton) {
					item.answer = Answer.YES;
				} else if(button == noButton) {
					item.answer = Answer.NO;
				} else {
					item.answer = Answer.N_A;
				}
			};
		};

		yesButton = new Button(group, SWT.RADIO);
		yesButton.setText(Resources.getMessage("RiskWizard.3"));
		yesButton.addSelectionListener(selectionListener);

		noButton = new Button(group, SWT.RADIO);
		noButton.setText(Resources.getMessage("RiskWizard.4"));
		noButton.addSelectionListener(selectionListener);

		n_aButton = new Button(group, SWT.RADIO);
		n_aButton.setText(Resources.getMessage("RiskWizard.5"));
		n_aButton.setSelection(true);
		n_aButton.addSelectionListener(selectionListener);
	}

}
