package org.deidentifier.arx.gui.view.impl.utility;

import javax.swing.filechooser.FileSystemView;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.utility.LayoutUtility.ViewUtilityType;
import org.deidentifier.arx.r.OS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ViewStatisticsROptions implements ViewStatisticsBasic {

	/** View */
	private Composite root;
	/** Controller */
	private final Controller controller;
	/** Widget */
	private Combo loadScriptCombo;
	/** Widget */
	private Text pathText;

	public ViewStatisticsROptions(final Composite parent, final Controller controller) {

		this.controller = controller;
		// root = new Composite(parent, SWT.NONE);
		this.root = parent;
		root.setLayout(SWTUtil.createGridLayout(3, false));

		final Label scriptlabel = new Label(root, SWT.PUSH);
		scriptlabel.setText("Execute a script: ");

		// Select from preexisting scripts
		String[] testpaths = Resources.getScriptNames();

		loadScriptCombo = new Combo(root, SWT.READ_ONLY);
		loadScriptCombo.setLayoutData(SWTUtil.createFillHorizontallyGridData());
		loadScriptCombo.setItems(testpaths);
		loadScriptCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				if (loadScriptCombo.getSelectionIndex() >= 0) {
					String label = loadScriptCombo.getItem(loadScriptCombo.getSelectionIndex());
					String path = Resources.getRScript(label);
					loadRScript(path);
				}
			}
		});

		// File chooser button/File Dialog
		Button loadScriptButton = new Button(root, SWT.PUSH);
		loadScriptButton.setText("Select from files");
		loadScriptButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				FileDialog fd = new FileDialog(root.getShell(), SWT.OPEN);
				fd.setFilterPath(FileSystemView.getFileSystemView().getHomeDirectory().toString());
				fd.setFilterExtensions(new String[] { "*.r" });

				String filename = fd.open();
				if (filename != null) {
					loadRScript(filename);
					loadScriptCombo.setText(filename);
				}
			}
		});

		final Label pathlabel = new Label(root, SWT.PUSH);
		pathlabel.setText("Path to your R installation: ");

		pathText = new Text(root, SWT.READ_ONLY);
		pathText.setLayoutData(SWTUtil.createFillHorizontallyGridData());
		pathText.setText(OS.getR()); // TODO

		Button pathToRButton = new Button(root, SWT.PUSH);
		pathToRButton.setText("Change manually");
		pathToRButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				FileDialog fd = new FileDialog(root.getShell(), SWT.OPEN);
				fd.setFilterPath(FileSystemView.getFileSystemView().getHomeDirectory().toString());

				// With the following statement it should not be possible to select something
				// that is not an R executable.
				// Nevertheless it might be a good idea to check it a second time in the
				// changePathToR function.
				fd.setFilterExtensions(OS.getPossibleExecutables());

				String filename = fd.open();
				if (filename != null) {
					changePathToR(filename);
					pathText.setText(filename);
				}

			}
		});

	}

	private void loadRScript(String path) {
		// We should check here if the path is a valid one. At least that it ends with
		// .r
		if (path.endsWith(".r")) {
			String command = "source(\"" + path + "\")";
			// communicate with RTerminal to execute command
			controller.update(new ModelEvent(ViewStatisticsROptions.this, ModelPart.R_SCRIPT, command));
		} else {
			System.out.println("Selected File is not ending with '.r'.");
			// Could show message for user too.
		}
	}

	private void changePathToR(String path) {
		controller.update(new ModelEvent(ViewStatisticsROptions.this, ModelPart.R_PATH, path));
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
