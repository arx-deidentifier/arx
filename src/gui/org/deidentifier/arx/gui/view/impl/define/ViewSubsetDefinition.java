/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
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

import java.text.DecimalFormat;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolderButton;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolItem;

/**
 * This view displays basic information about the research subset.
 *
 * @author Fabian Prasser
 */
public class ViewSubsetDefinition implements IView{

    /**  TODO */
    private Controller controller;
    
    /**  TODO */
    private Composite root;
    
    /**  TODO */
    private Model model;
    
    /**  TODO */
    private Label size;
    
    /**  TODO */
    private Label origin;
    
    /**  TODO */
    private Label total;
    
    /**  TODO */
    private Label percent;

    /**  TODO */
    private ToolItem all;
    
    /**  TODO */
    private ToolItem none;
    
    /**  TODO */
    private ToolItem file;
    
    /**  TODO */
    private ToolItem filter;
    
    /**  TODO */
    private DecimalFormat format = new DecimalFormat("##0.00");
    
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

    /**
     * Builds the view.
     *
     * @param parent
     * @return
     */
    private Composite build(Composite parent) {

        ComponentTitledFolderButton bar = new ComponentTitledFolderButton("id-40");
        bar.add(Resources.getMessage("SubsetDefinitionView.1"), 
                controller.getResources().getImage("page_white.png"),
                new Runnable() {
                    @Override
                    public void run() {
                        controller.actionSubsetNone();
                    }
                });
        bar.add(Resources.getMessage("SubsetDefinitionView.2"), 
                controller.getResources().getImage("page_white_text.png"),
                new Runnable() {
                    @Override
                    public void run() {
                        controller.actionSubsetAll();
                    }
                });
        bar.add(Resources.getMessage("SubsetDefinitionView.3"), 
                controller.getResources().getImage("disk.png"),
                new Runnable() {
                    @Override
                    public void run() {
                        controller.actionSubsetFile();
                    }
                });
        bar.add(Resources.getMessage("SubsetDefinitionView.4"), 
                controller.getResources().getImage("find.png"),
                new Runnable() {
                    @Override
                    public void run() {
                        controller.actionSubsetQuery();
                    }
                });
        
        ComponentTitledFolder folder = new ComponentTitledFolder(parent, controller, bar, null);
        folder.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        Composite group = folder.createItem(Resources.getMessage("SubsetDefinitionView.0"), null);
        folder.setSelection(0);
        GridLayout layout = new GridLayout();
        layout.numColumns = 9;
        layout.makeColumnsEqualWidth = false;
        group.setLayout(layout);
        group.setLayoutData(SWTUtil.createFillGridData());
        
        Label l = new Label(group, SWT.NONE);
        l.setText(Resources.getMessage("SubsetDefinitionView.6"));
        size = new Label(group, SWT.BORDER);
        size.setText("0");
        size.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        l = new Label(group, SWT.NONE);
        l.setText("/");
        total = new Label(group, SWT.BORDER);
        total.setText("0");
        total.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        l = new Label(group, SWT.NONE);
        l.setText("=");
        percent = new Label(group, SWT.BORDER);
        percent.setText("0");
        percent.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        l = new Label(group, SWT.NONE);
        l.setText("%");
        l = new Label(group, SWT.NONE);
        l.setText(Resources.getMessage("SubsetDefinitionView.5"));
        origin = new Label(group, SWT.BORDER);
        origin.setText("");
        origin.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        
        all = folder.getButtonItem(Resources.getMessage("SubsetDefinitionView.1"));
        none = folder.getButtonItem(Resources.getMessage("SubsetDefinitionView.2"));
        file = folder.getButtonItem(Resources.getMessage("SubsetDefinitionView.3"));
        filter = folder.getButtonItem(Resources.getMessage("SubsetDefinitionView.4"));
        
        return group;
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IView#dispose()
     */
    @Override
    public void dispose() {
        controller.removeListener(this);
    }
    
    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IView#reset()
     */
    @Override
    public void reset() {
        size.setText("0");
        total.setText("0");
        percent.setText("0");
        origin.setText("");
        disable();
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

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IView#update(org.deidentifier.arx.gui.model.ModelEvent)
     */
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
        this.size.setText(String.valueOf(size));
        this.total.setText(String.valueOf(total));
        this.percent.setText(format.format(percent));
        this.origin.setText(model.getSubsetOrigin());
    }
}
