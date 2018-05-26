package org.deidentifier.arx.gui.view.impl.utility;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import javax.swing.filechooser.FileSystemView;
import org.apache.commons.lang.StringUtils;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledSeparator;
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
import org.eclipse.swt.widgets.TableItem;

import de.linearbits.swt.table.DynamicTable;
import de.linearbits.swt.table.DynamicTableColumn;

public class ViewStatisticsROptions implements ViewStatisticsBasic {

	/** View */
	private Composite root;
	/** Controller */
	private final Controller controller;
	/** Widget */
	private DynamicTable loadScriptTable;
	/** Widget */
	private Label pathText;

	private String pathToR;

	private Map<String, String> rmap;

	public ViewStatisticsROptions(final Composite parent, final Controller controller) {

		this.controller = controller;
		this.root = parent;
		root.setLayout(SWTUtil.createGridLayout(2));

		/**
		 * Execute Script part
		 */
		// Title
		ComponentTitledSeparator scriptLabel = new ComponentTitledSeparator(root, SWT.NONE);
		scriptLabel.setLayoutData(SWTUtil.createFillGridData(2));
		scriptLabel.setText("Execute a script: "); //$NON-NLS-1$

		// Initialize table
		loadScriptTable = SWTUtil.createTableDynamic(root,
				SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
		loadScriptTable.setLayoutData(SWTUtil.createFillGridData(2));
		loadScriptTable.setLinesVisible(true);
		DynamicTableColumn c = new DynamicTableColumn(loadScriptTable, SWT.LEFT);
		c.setWidth("100%", "100px");
		c.pack();
		SWTUtil.createGenericTooltip(loadScriptTable);

		// Select from preexisting scripts
		rmap = Resources.getRMapping(); // Map<Description what script does,rscript.r name>

		String[] paths = rmap.keySet().toArray(new String[0]); // Only description needed for table
		Arrays.sort(paths); // Alphabetically sorted

		for (String s : paths) {
			TableItem item = new TableItem(loadScriptTable, SWT.NONE);
			item.setText(0, s);
		}
		loadScriptTable.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent event) {
				if (loadScriptTable.getSelectionIndex() >= 0) {
					// Get actual script name corresponding to the description which was chosen from
					// the mapping
					String label = rmap.get(loadScriptTable.getItem(loadScriptTable.getSelectionIndex()).getText());
					// Get path to the temporary file where the r script was copied to, to be
					// accessible for R.
					String path = Resources.getRScript(label);
					// Tell R to load it.
					loadRScript(path);
				}
			}
		});

		// File chooser button/File Dialog for choosing of an external R script
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

		/**
		 * Path to R installation part
		 */
		// Title
		ComponentTitledSeparator pathLabel = new ComponentTitledSeparator(root, SWT.NONE);
		pathLabel.setLayoutData(SWTUtil.createFillGridData(2));
		pathLabel.setText("Path to your R installation: "); //$NON-NLS-1$


		// Success message
		pathText = new Label(root, SWT.RIGHT);
		pathText.setLayoutData(SWTUtil.createFillHorizontallyGridData());
		pathToR = OS.getR();
        if (pathToR != null) {
			setSuccessString();
		} else {
			pathText.setText("Executable of R not found. Please select one manually.");
		}

		// Change path manually file chooser dialog
		Button pathToRButton = new Button(root, SWT.PUSH);
		pathToRButton.setText("Change manually");
		pathToRButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				FileDialog fd = new FileDialog(root.getShell(), SWT.OPEN);
				
				String[] filterExtensions = OS.getPossibleExecutables();
				
				if (pathToR != null) {
					String filterpath = pathToR; // file has to be stripped away to work properly.

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
						// delete the last "/something" of the String (because this is the file, not the
						// directory).
						for (String s : filterExtensions) {
							String stripped = StringUtils.removeEnd(pathToR, "/" + s);
							if (!pathToR.equals(stripped)) {
								filterpath = stripped;
								break;
							}
						}
					}
					
					fd.setFilterPath(filterpath);
				}
				
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
			String command = "source(\"" + path.replace("\\", "/") + "\")"; // replace for Windows paths, R does not work otherwise
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
		pathText.setText("R executable found. Version: " + getRVersion()); // TODO: get version. via the R terminal: "version$version.string"
		// You can tell the version the package was compiled for by looking at the
		// "Version:" line in its DESCRIPTION file: R/library/datasets/DESCRIPTION ->
		// Executable in R/bin/R -> only applicable in Unix.
	}
	
	private String getRVersion() {
		String rVersionOutput = getRVersionOutput();
		
		/*
		 * Output should have the following structure:
		 * R version 3.5.0 (2018-04-23) --"Joy in Playing"
		 * Copyright (C) 2018 The R Foundation for Statistical Computing
		 * Platform: i386-w64-mingw32/i386 (32-bit)
		 * 
		 * R is free software [...]
		 */
		if (rVersionOutput != null) {
			String[] lines = rVersionOutput.split("\n");
			
			if (lines.length>= 1) {
				if (lines[0].startsWith("R version")) {

					String[] words = lines[0].split(" ");
					if (words.length >= 3) {
						return words[2];
					}
			    }
			}
		}
		
		return "not found.";
	}
	
	private String getRVersionOutput() {
		
		OSType os = OS.getOS();
		
		Runtime processBuilder = Runtime.getRuntime();//new ProcessBuilder(pathToR, "--version");
		Process process = null;
		try { //TODO: test for OS X
			if (os == OSType.WINDOWS) { //TODO: test on Linux if this if clause is necessary
				String command = "cmd /c \""+pathToR+"\" --version";
				process = processBuilder.exec(command);	
			} else {
				String[] commandarray = {pathToR, "--version"};
				process = processBuilder.exec(commandarray);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		InputStream rOutStream;
		if (os == OSType.WINDOWS) {
			rOutStream = process.getErrorStream();
		} else {
			rOutStream = process.getInputStream(); //TODO: Test for OS X!	
		}
		ByteArrayOutputStream outputstream = new ByteArrayOutputStream();
		byte[] buffer = new byte[4096];
		
        try {
        	int in;
            while ((in = rOutStream.read(buffer)) != -1) {
                outputstream.write(buffer, 0, in);
            }
            process.waitFor();
            return outputstream.toString("UTF-8");
        } catch (IOException e) { //read could throw this
        	e.printStackTrace();
        	process.destroy();
        } catch (InterruptedException e) { //waitFor could throw this
			e.printStackTrace();
			process.destroy();
		}
			
		return null;
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
