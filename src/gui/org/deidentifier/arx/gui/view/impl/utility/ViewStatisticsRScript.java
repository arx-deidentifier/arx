package org.deidentifier.arx.gui.view.impl.utility;

import javax.swing.filechooser.FileSystemView;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.common.async.Analysis;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContext;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisManager;
import org.deidentifier.arx.gui.view.impl.utility.LayoutUtility.ViewUtilityType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;

public class ViewStatisticsRScript extends ViewStatistics<AnalysisContextR> {

	/** View */
	private Composite root;
	/** Internal stuff. */
	private AnalysisManager manager;
	/** Widget */
	private Combo combo;

	public ViewStatisticsRScript(Composite parent, Controller controller, ModelPart target, ModelPart reset) {
		super(parent, controller, target, reset, true);
		this.manager = new AnalysisManager(parent.getDisplay());
	}

	@Override
	public ViewUtilityType getType() {
		return LayoutUtility.ViewUtilityType.R;
	}

	@Override
	protected Control createControl(Composite parent) {
		root = new Composite(parent, SWT.NONE);
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

		return this.root;
	}

	public void loadRScript(String path) {
		// We should check here if the path is a valid one. At least that it ends with
		// .r
		if (path.endsWith(".r")) {
			String command = "source(\"" + path + "\")";
			// communicate with RTerminal to execute command
			getController().update(new ModelEvent(ViewStatisticsRScript.this, ModelPart.R_SCRIPT, command));
		} else {
			System.out.println("Selected File is not ending with '.r'."); // Could show message for user too.
		}
	}

	@Override
	protected AnalysisContextR createViewConfig(AnalysisContext context) {
		return new AnalysisContextR(context);
	}

	@Override
	protected void doReset() {
		root.setRedraw(false);
		if (this.manager != null) {
			this.manager.stop();
		}
		root.setRedraw(true);
		setStatusEmpty();

	}

	@Override
	protected void doUpdate(AnalysisContextR context) {
		Analysis analysis = new Analysis() {

			private boolean stopped = false;

			@Override
			public int getProgress() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public void onError() {
				// --> TODO: Show according message here
				setStatusEmpty();

			}

			@Override
			public void onFinish() {
				if (stopped || !isEnabled()) {
					return;
				}
				setStatusDone();

			}

			@Override
			public void onInterrupt() {
				if (!isEnabled()) {
					setStatusEmpty();
				} else {
					setStatusWorking();
				}

			}

			@Override
			public void run() throws InterruptedException {
				long time = System.currentTimeMillis();

				while (System.currentTimeMillis() - time < MINIMAL_WORKING_TIME && !stopped) {
					Thread.sleep(10);
				}
			}

			@Override
			public void stop() {
				System.out.println("we got stopped :(");
				this.stopped = true;
			}

		};
		this.manager.start(analysis);
	}

	@Override
	protected boolean isRunning() {
		return manager != null && manager.isRunning();
	}

}
