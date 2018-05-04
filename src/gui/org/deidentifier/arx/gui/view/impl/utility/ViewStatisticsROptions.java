package org.deidentifier.arx.gui.view.impl.utility;

import javax.swing.filechooser.FileSystemView;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.utility.LayoutUtility.ViewUtilityType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;

public class ViewStatisticsROptions implements ViewStatisticsBasic {

	/** View */
	private Composite root;
	/** Widget */
	private Combo combo;
	private final Controller controller;

	public ViewStatisticsROptions(final Composite parent, final Controller controller) {

		this.controller = controller;
		// root = new Composite(parent, SWT.NONE);
		this.root = parent;
		root.setLayout(SWTUtil.createGridLayout(3, false));

		final Label label = new Label(root, SWT.PUSH);
		label.setText("Please select a script: ");

		// Select from preexisting scripts
		combo = new Combo(root, SWT.READ_ONLY);
		combo.setLayoutData(SWTUtil.createFillHorizontallyGridData());
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				if (combo.getSelectionIndex() >= 0) {
					String label = combo.getItem(combo.getSelectionIndex());
					loadRScript(label);
				}
			}
		});
		// Some Tests scripts for presentation purposes, TODO: change
		String[] testpaths = { "~/Documents/CAP/test.r" };
		combo.setItems(testpaths);

		// File chooser button/File Dialog
		Button button = new Button(root, SWT.PUSH);
		button.setText("Select from files");
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				FileDialog fd = new FileDialog(root.getShell(), SWT.OPEN);
				fd.setFilterPath(FileSystemView.getFileSystemView().getHomeDirectory().toString());
				fd.setFilterExtensions(new String[] { "*.r" });

				String filename = fd.open();
				if (filename != null) {
					loadRScript(filename);
					combo.setText(filename);
				}
			}
		});

	}

	public void loadRScript(String path) {
		// We should check here if the path is a valid one. At least that it ends with
		// .r
		if (path.endsWith(".r")) {
			String command = "source(\"" + path + "\")";
			// communicate with RTerminal to execute command
			controller.update(new ModelEvent(ViewStatisticsROptions.this, ModelPart.R_SCRIPT, command));
		} else {
			System.out.println("Selected File is not ending with '.r'."); // Could show message for user too.
		}
	}


	@Override
	public void dispose() {
		// We don't need this at the moment. (If we had a Listener this would be
		// useful).
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
	}

	@Override
	public void update(ModelEvent event) {
		// We don't need this at the moment. (If we had a Listener this would be
		// useful).
	}

	@Override
	public Composite getParent() {
		return this.root;
	}

	@Override
	public ViewUtilityType getType() {
		return LayoutUtility.ViewUtilityType.R;
	}

}
