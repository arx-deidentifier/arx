/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.deidentifier.arx.gui.view.impl.define;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolder;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolderButtonBar;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolItem;

/**
 * This view displays basic information about the research subset.
 *
 * @author Fabian Prasser
 */
public class ViewSubsetDefinition implements IView{

    /** Controller */
    private Controller controller;

    /** View */
    private Composite  root;

    /** Model */
    private Model      model;

    /** View */
    private Text       size;

    /** View */
    private Text       origin;

    /** View */
    private Text       total;

    /** View */
    private Text       percent;

    /** View */
    private ToolItem   all;

    /** View */
    private ToolItem   none;

    /** View */
    private ToolItem   file;

    /** View */
    private ToolItem   filter;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     */
    public ViewSubsetDefinition(final Composite parent,
                                final Controller controller) {

        this.controller = controller;
        this.controller.addListener(ModelPart.MODEL, this);
        this.controller.addListener(ModelPart.INPUT, this);
        this.controller.addListener(ModelPart.RESEARCH_SUBSET, this);
        this.root = build(parent);
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    @Override
    public void reset() {
        size.setText("0"); //$NON-NLS-1$
        total.setText("0"); //$NON-NLS-1$
        percent.setText("0%"); //$NON-NLS-1$
        origin.setText(""); //$NON-NLS-1$
        disable();
    }
    
    @Override
    public void update(final ModelEvent event) {
        if (event.part == ModelPart.MODEL) {
            model = (Model) event.data;
            if (model.getInputConfig().getInput()!=null){
                root.setRedraw(false);
                // TODO: Load subset
                enable();
                root.setRedraw(true);
            }
        } else if (event.part == ModelPart.INPUT) {
            if (model.getInputConfig().getInput()!=null){
                enable();
            }
        } else if (event.part == ModelPart.RESEARCH_SUBSET) {
            if (model!=null){
                update();
            }
        } 
    }
    
    /**
     * Builds the view.
     *
     * @param parent
     * @return
     */
    private Composite build(Composite parent) {

        ComponentTitledFolderButtonBar bar = new ComponentTitledFolderButtonBar("id-40"); //$NON-NLS-1$
        bar.add(Resources.getMessage("SubsetDefinitionView.1"),  //$NON-NLS-1$
                controller.getResources().getManagedImage("page_white.png"), //$NON-NLS-1$
                new Runnable() {
                    @Override
                    public void run() {
                        controller.actionSubsetNone();
                    }
                });
        bar.add(Resources.getMessage("SubsetDefinitionView.2"),  //$NON-NLS-1$
                controller.getResources().getManagedImage("page_white_text.png"), //$NON-NLS-1$
                new Runnable() {
                    @Override
                    public void run() {
                        controller.actionSubsetAll();
                    }
                });
        bar.add(Resources.getMessage("SubsetDefinitionView.3"),  //$NON-NLS-1$
                controller.getResources().getManagedImage("disk.png"), //$NON-NLS-1$
                new Runnable() {
                    @Override
                    public void run() {
                        controller.actionSubsetFile();
                    }
                });
        bar.add(Resources.getMessage("SubsetDefinitionView.4"),  //$NON-NLS-1$
                controller.getResources().getManagedImage("find.png"), //$NON-NLS-1$
                new Runnable() {
                    @Override
                    public void run() {
                        controller.actionSubsetQuery();
                    }
                });
        bar.add(Resources.getMessage("SubsetDefinitionView.7"),  //$NON-NLS-1$
                controller.getResources().getManagedImage("shuffle.png"), //$NON-NLS-1$
                new Runnable() {
                    @Override
                    public void run() {
                        controller.actionSubsetRandom();
                    }
                });
        
        ComponentTitledFolder folder = new ComponentTitledFolder(parent, controller, bar, null);
        folder.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        Composite group = folder.createItem(Resources.getMessage("SubsetDefinitionView.0"), null); //$NON-NLS-1$
        folder.setSelection(0);
        GridLayout layout = new GridLayout();
        layout.numColumns = 8;
        layout.makeColumnsEqualWidth = false;
        group.setLayout(layout);
        group.setLayoutData(SWTUtil.createFillGridData());
        
        Label l = new Label(group, SWT.NONE);
        l.setText(Resources.getMessage("SubsetDefinitionView.6")); //$NON-NLS-1$
        size = new Text(group, SWT.BORDER);
        size.setText("0"); //$NON-NLS-1$
        size.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        size.setEditable(false);
        l = new Label(group, SWT.NONE);
        l.setText("/"); //$NON-NLS-1$
        total = new Text(group, SWT.BORDER);
        total.setText("0"); //$NON-NLS-1$
        total.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        total.setEditable(false);
        l = new Label(group, SWT.NONE);
        l.setText("="); //$NON-NLS-1$
        percent = new Text(group, SWT.BORDER);
        percent.setText("0%"); //$NON-NLS-1$
        percent.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        percent.setEditable(false);
        l = new Label(group, SWT.NONE);
        l.setText(Resources.getMessage("SubsetDefinitionView.5")); //$NON-NLS-1$
        origin = new Text(group, SWT.BORDER);
        origin.setText(""); //$NON-NLS-1$
        origin.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        origin.setEditable(false);
        
        all = folder.getButtonItem(Resources.getMessage("SubsetDefinitionView.1")); //$NON-NLS-1$
        none = folder.getButtonItem(Resources.getMessage("SubsetDefinitionView.2")); //$NON-NLS-1$
        file = folder.getButtonItem(Resources.getMessage("SubsetDefinitionView.3")); //$NON-NLS-1$
        filter = folder.getButtonItem(Resources.getMessage("SubsetDefinitionView.4")); //$NON-NLS-1$
        
        return group;
    }
    
    /**
     * Disables the view.
     */
    private void disable(){
        // TODO: Maybe make this a default for all views?
        all.setEnabled(false);
        none.setEnabled(false);
        file.setEnabled(false);
        filter.setEnabled(false);
        SWTUtil.disable(root);
    }

    /**
     * Enables the view.
     */
    private void enable(){
        // TODO: Maybe make this a default for all views?
        all.setEnabled(true);
        none.setEnabled(true);
        file.setEnabled(true);
        filter.setEnabled(true);
        SWTUtil.enable(root);
    }

    /**
     * Updates the view.
     */
    private void update() {
        // TODO: Maybe make this a default for all views?
    	if (model==null || model.getInputConfig()==null || model.getInputConfig().getResearchSubset()==null){
    		reset();
    		return;
    	}
        int size = model.getInputConfig().getResearchSubset().size();
        int total = model.getInputConfig().getInput().getHandle().getNumRows();
        double percent = (double)size / (double)total * 100d;
        this.size.setText(SWTUtil.getPrettyString(size));
        this.total.setText(SWTUtil.getPrettyString(total));
        this.percent.setText(SWTUtil.getPrettyString(percent)+"%");
        this.origin.setText(model.getSubsetOrigin());
    }
}
