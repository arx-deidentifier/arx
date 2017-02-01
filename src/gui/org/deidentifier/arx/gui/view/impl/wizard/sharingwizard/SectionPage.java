package org.deidentifier.arx.gui.view.impl.wizard.sharingwizard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import org.deidentifier.arx.gui.view.impl.wizard.sharingwizard.checklist.Question;
import org.deidentifier.arx.gui.view.impl.wizard.sharingwizard.checklist.Section;
import org.deidentifier.arx.gui.view.impl.wizard.sharingwizard.checklist.Question.Answer;

/**
 * each SectionPage shows all the questions from a checklist section
 *
 */
public class SectionPage extends WizardPage {
	private Section section;
	private Composite container;
	private boolean weightEditable;
	private List<WeightField> weightFields;


	public SectionPage(Section section) {
		super(section.getTitle());

		this.weightEditable = false;
		this.section = section;
		this.setTitle(section.getTitle());
		this.setDescription("Answer the following questions");
	}

	@Override
	public void createControl(Composite parent) {
		final Composite rootComposite = new Composite(parent, SWT.NONE);
		
		GridLayout rootGrid = new GridLayout();
		rootComposite.setLayout(rootGrid);

		final ScrolledComposite sc = new ScrolledComposite(rootComposite, SWT.BORDER | SWT.V_SCROLL);
		GridData sgd = new GridData(GridData.FILL_BOTH);
		sgd.grabExcessHorizontalSpace = true;
		sgd.grabExcessVerticalSpace = true;
		sgd.widthHint = 400;//SWT.DEFAULT;
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
					sc.setMinHeight(container.computeSize(newWidth, SWT.DEFAULT).y+40);
					width = newWidth;
				}
			}
		});

		sc.setContent(container);
		sc.setMinSize(container.computeSize(400, SWT.DEFAULT));
		sc.layout();

		setControl(rootComposite);
	}

	private void createItems() {
		// add weight fields
		this.weightFields = new ArrayList<WeightField>();
		this.weightFields.add(new WeightField(container, this.section, false));
			
		Label label = new Label(container, SWT.NONE | SWT.WRAP);
		label.setText(this.section.getTitle());
			// from http://eclipsesource.com/blogs/2014/02/10/swt-best-practices-changing-fonts/
		FontDescriptor boldDescriptor = FontDescriptor.createFrom(label.getFont()).setStyle(SWT.BOLD);
		Font boldFont = boldDescriptor.createFont(label.getDisplay());
		label.setFont( boldFont );
			
		GridData headerGridData = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
		headerGridData.verticalAlignment = GridData.CENTER;
		headerGridData.grabExcessVerticalSpace = false;
		headerGridData.horizontalSpan = 2;
		label.setLayoutData(headerGridData);
			
		Question[] items = section.getItems();
		for(int i = 0; i < items.length; i++) {
			Question itm = items[i];

			this.weightFields.add(new WeightField(container, itm, false));

			Label label1 = new Label(container, SWT.NONE | SWT.WRAP);
			label1.setText(itm.getTitle());
			GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
			gd.verticalAlignment = GridData.CENTER;
			gd.grabExcessVerticalSpace = false;
			label1.setLayoutData(gd);

			new AnswerRadioGroup(container, itm);
		}
	}
	
	public void setWeightEditable(boolean editable) {
		if(editable == this.weightEditable) {
			return;
		}
		this.weightEditable = editable;
		
		// TODO iterate and set
		for(WeightField w : this.weightFields) {
			w.setEnabled(editable);
		}
	}
	
	protected void updateWeights() {
		if(this.weightFields == null) {
			return;
		}
		for(WeightField field : weightFields) {
			field.updateText();
		}
	}
}
