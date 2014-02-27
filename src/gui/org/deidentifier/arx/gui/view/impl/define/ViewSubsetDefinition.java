/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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
import org.deidentifier.arx.gui.view.impl.common.ComponentTitleBar;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolItem;

public class ViewSubsetDefinition implements IView{

    private Controller controller;
    private Composite root;
    private Model model;
    
    private Label size;
    private Label origin;
    private Label total;
    private Label percent;

    private ToolItem all;
    private ToolItem none;
    private ToolItem file;
    private ToolItem filter;
    
    private DecimalFormat format = new DecimalFormat("##0.00");
    
    public ViewSubsetDefinition(final Composite parent,
                                final Controller controller) {

        this.controller = controller;
        this.controller.addListener(ModelPart.MODEL, this);
        this.controller.addListener(ModelPart.INPUT, this);
        this.controller.addListener(ModelPart.RESEARCH_SUBSET, this);
        this.root = build(parent);
    }

    private Composite build(Composite parent) {

        ComponentTitleBar bar = new ComponentTitleBar("id-40");
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
        
        all = folder.getBarItem(Resources.getMessage("SubsetDefinitionView.1"));
        none = folder.getBarItem(Resources.getMessage("SubsetDefinitionView.2"));
        file = folder.getBarItem(Resources.getMessage("SubsetDefinitionView.3"));
        filter = folder.getBarItem(Resources.getMessage("SubsetDefinitionView.4"));
        
        return group;
    }


    @Override
    public void dispose() {
        controller.removeListener(this);
    }
    
    @Override
    public void reset() {
        size.setText("0");
        total.setText("0");
        percent.setText("0");
        origin.setText("");
        disable();
    }
    
    private void enable(){
        // TODO: Maybe make this a default for all views?
        all.setEnabled(true);
        none.setEnabled(true);
        file.setEnabled(true);
        filter.setEnabled(true);
        SWTUtil.enable(root);
    }
    
    private void disable(){
        // TODO: Maybe make this a default for all views?
        all.setEnabled(false);
        none.setEnabled(false);
        file.setEnabled(false);
        filter.setEnabled(false);
        SWTUtil.disable(root);
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

    private void update() {
        // TODO: Maybe make this a default for all views?
    	if (model.getInputConfig()==null || model.getInputConfig().getResearchSubset()==null){
    		reset();
    		return;
    	}
        int size = model.getInputConfig().getResearchSubset().size();
        int total = model.getInputConfig().getInput().getHandle().getNumRows();
        double percent = (double)size / (double)total * 100d;
        this.size.setText(String.valueOf(size));
        this.total.setText(String.valueOf(total));
        this.percent.setText(format.format(percent));
        if (this.model != null) {
            this.origin.setText(model.getSubsetOrigin());
        }
    }
}
