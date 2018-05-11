package org.deidentifier.arx.gui.view.impl.utility;

import java.io.File;

import javax.swing.filechooser.FileSystemView;

import org.apache.commons.lang.StringUtils;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.utility.LayoutUtility.ViewUtilityType;
import org.deidentifier.arx.r.OS;
import org.deidentifier.arx.r.OS.OSType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

public class ViewStatisticsROptions implements ViewStatisticsBasic {

	/** View */
	private Composite root;
	/** Controller */
	private final Controller controller;
	/** Widget */
	private List loadScriptList;
	/** Widget */
	private Label pathText;

	private String pathToR;

	public ViewStatisticsROptions(final Composite parent, final Controller controller) {

		this.controller = controller;
		// root = new Composite(parent, SWT.NONE);
		this.root = parent;
		root.setLayout(SWTUtil.createGridLayout(3, false));

		final Label scriptlabel = new Label(root, SWT.PUSH);
		scriptlabel.setText("Execute a script: ");

		// Select from preexisting scripts
		String[] testpaths = Resources.getScriptNames();

		loadScriptList = new List(root, SWT.SINGLE);
		loadScriptList.setLayoutData(SWTUtil.createFillHorizontallyGridData());
		loadScriptList.setItems(testpaths);
		loadScriptList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				if (loadScriptList.getSelectionIndex() >= 0) {
					String label = loadScriptList.getItem(loadScriptList.getSelectionIndex());
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
				}
			}
		});

		final Label pathlabel = new Label(root, SWT.PUSH);
		pathlabel.setText("Path to your R installation: ");

		pathText = new Label(root, SWT.RIGHT);
		pathText.setLayoutData(SWTUtil.createFillHorizontallyGridData());
		pathToR = OS.getR();
        if ((new File(pathToR)).exists()) {
			setSuccessString();
		} else {
			pathText.setText("Executable of R not found. Please select one manually.");
		}

		Button pathToRButton = new Button(root, SWT.PUSH);
		pathToRButton.setText("Change manually");
		pathToRButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				String filterpath = pathToR; // file has to be stripped away to work properly.
				String[] filterExtensions = OS.getPossibleExecutables();

				// Strip away file ending of path
				if (OS.getOS() == OSType.WINDOWS) {
					// delete the last "\\something" of the String (because this is the file, not
					// the directory).
					for (String s : filterExtensions) {
						String stripped = StringUtils.removeEnd(pathToR, "\\" + s);
						if (!pathToR.equals(stripped)) {
							filterpath = stripped;
							break;
						}
					}
				} else {
					// delet the last "/something" of the String (because this is the file, not the
					// directory).
					for (String s : filterExtensions) {
						String stripped = StringUtils.removeEnd(pathToR, "/" + s);
						if (!pathToR.equals(stripped)) {
							filterpath = stripped;
							break;
						}
					}
				}

				FileDialog fd = new FileDialog(root.getShell(), SWT.OPEN);
				fd.setFilterPath(filterpath);
				// With the following statement it should not be possible to select something
				// that is not an R executable.
				// Nevertheless it might be a good idea to check it a second time in the
				// changePathToR function.
				fd.setFilterExtensions(filterExtensions);

				String filename = fd.open();
				if (filename != null) {
					changePathToR(filename);
					pathToR = filename;
					setSuccessString();
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

	private void setSuccessString() {
		pathText.setText("R executable found."); // TODO: get version. via the R terminal: "version$version.string"
		// You can tell the version the package was compiled for by looking at the
		// ‘Version:’ line in its DESCRIPTION file: R/library/datasets/DESCRIPTION ->
		// Executable in R/bin/R -> only applicable in Unix.
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
