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

package org.deidentifier.arx.gui.view.impl.explore;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.model.ModelNodeFilter;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.metric.InformationLoss;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import de.linearbits.tiles.DecoratorString;

/**
 * This class provides an abstract base for views that display parts of the solution space
 *  
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 *
 */
public abstract class ViewSolutionSpace implements IView {

    /** Number format. */
    private final NumberFormat       format;

    /** The controller. */
    private final Controller         controller;

    /** Context menu. */
    private Menu                     menu             = null;

    /** The selected node. */
    private ARXNode                  selectedNode     = null;

    /** The model. */
    private Model                    model            = null;

    /** The parent */
    private Composite                parent;

    /** Tooltip decorator */
    private DecoratorString<ARXNode> tooltipDecorator = null;

    /**
     * Constructor
     * @param parent
     * @param controller
     */
    public ViewSolutionSpace(final Composite parent, final Controller controller) {

        // Listen
        controller.addListener(ModelPart.SELECTED_NODE, this);
        controller.addListener(ModelPart.FILTER, this);
        controller.addListener(ModelPart.MODEL, this);
        controller.addListener(ModelPart.RESULT, this);

        this.parent = parent;
        this.controller = controller;
        this.format = new DecimalFormat("##0.000"); //$NON-NLS-1$
        
        initializeMenu();
        initializeTooltip();
    }

    /**
     * Resets the view.
     */
    @Override
    public void reset() {
        this.selectedNode = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.arx.gui.view.def.IView#dispose()
     */
    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IView#update(org.deidentifier.arx.gui.model.ModelEvent)
     */
    @Override
    public void update(final ModelEvent event) {

        if (event.part == ModelPart.SELECTED_NODE) {
            selectedNode = (ARXNode) event.data;
            eventNodeSelected();
        } else if (event.part == ModelPart.RESULT) {
            eventResultChanged(model.getResult());
        } else if (event.part == ModelPart.MODEL) {
            model = (Model) event.data;
            eventModelChanged();
        } else if (event.part == ModelPart.FILTER) {
            if (model != null) {
                eventFilterChanged(model.getResult(), (ModelNodeFilter) event.data);
            }
        }
    }

    /**
     * Creates the context menu.
     */
    private void initializeMenu() {
        menu = new Menu(parent.getShell());
        MenuItem item1 = new MenuItem(menu, SWT.NONE);
        item1.setText(Resources.getMessage("LatticeView.9")); //$NON-NLS-1$
        item1.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.getClipboard().addToClipboard(selectedNode);
                controller.update(new ModelEvent(ViewSolutionSpace.this, ModelPart.CLIPBOARD, selectedNode));
                model.setSelectedNode(selectedNode);
                controller.update(new ModelEvent(ViewSolutionSpace.this, ModelPart.SELECTED_NODE, selectedNode));
                actionRedraw();
            }
        });
        
        MenuItem item2 = new MenuItem(menu, SWT.NONE);
        item2.setText(Resources.getMessage("LatticeView.10")); //$NON-NLS-1$
        item2.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                controller.actionApplySelectedTransformation();
                model.setSelectedNode(selectedNode);
                controller.update(new ModelEvent(ViewSolutionSpace.this, ModelPart.SELECTED_NODE, selectedNode));
                actionRedraw();
            }
        });
    }

    private void initializeTooltip() {
        this.tooltipDecorator = new DecoratorString<ARXNode>() {
            @Override
            public String decorate(ARXNode node) {
                final StringBuffer b = new StringBuffer();
                b.append(Resources.getMessage("LatticeView.1")); //$NON-NLS-1$
                b.append(format.format(asRelativeValue(node.getMinimumInformationLoss())));
                b.append(" - "); //$NON-NLS-1$
                b.append(format.format(asRelativeValue(node.getMaximumInformationLoss())));
                b.append(" [%]\n"); //$NON-NLS-1$
                if (model.getOutputDefinition() != null) {
                    for (final String qi : node.getQuasiIdentifyingAttributes()) {

                        // Determine height of hierarchy
                        int height = model.getOutputDefinition().isHierarchyAvailable(qi) ?
                                model.getOutputDefinition().getHierarchy(qi)[0].length : 0;
                        b.append(" * "); //$NON-NLS-1$
                        b.append(qi);
                        b.append(": "); //$NON-NLS-1$
                        b.append(format.format(asRelativeValue(node.getGeneralization(qi), height - 1)));
                        b.append(" [%]\n"); //$NON-NLS-1$
                    }
                }
                b.setLength(b.length() - 1);
                return b.toString();
            }
        };
    }

    /**
     * Action to redraw
     */
    protected abstract void actionRedraw();

    /**
     * Action: select node
     * @param node
     */
    protected void actionSelectNode(ARXNode node) {
        this.selectedNode = node;
        getModel().setSelectedNode(node);
        controller.update(new ModelEvent(this, ModelPart.SELECTED_NODE, node));
    }
    
    /**
     * Action show menu
     * @param x
     * @param y
     */
    protected void actionShowMenu(int x, int y){
        menu.setLocation(x, y);
        menu.setVisible(true);
    }
    /**
     * Converts an information loss into a relative value in percent.
     *
     * @param infoLoss
     * @return
     */
    protected double asRelativeValue(final InformationLoss<?> infoLoss) {
        if (model != null && model.getResult() != null && model.getResult().getLattice() != null &&
            model.getResult().getLattice().getBottom() != null && model.getResult().getLattice().getTop() != null) {
            return infoLoss.relativeTo(model.getResult().getLattice().getMinimumInformationLoss(),
                                       model.getResult().getLattice().getMaximumInformationLoss()) * 100d;
        } else {
            return 0;
        }
    }
    
    /**
     * Converts a generalization to a relative value.
     *
     * @param generalization
     * @param max
     * @return
     */
    protected double asRelativeValue(final int generalization, final int max) {
        if (model != null && model.getResult() != null && model.getResult().getLattice() != null &&
            model.getResult().getLattice().getBottom() != null && model.getResult().getLattice().getTop() != null) {
            return ((double) generalization / (double) max) * 100d;
        } else {
            return 0;
        }
    }
    
    /**
     * Event: filter changed
     * @param result
     * @param filter
     */
    protected abstract void eventFilterChanged(ARXResult result, ModelNodeFilter filter);

    /**
     * Event: model changed
     */
    protected abstract void eventModelChanged();

    /**
     * Event: node selected
     */
    protected abstract void eventNodeSelected();
    
    /**
     * Event: result changed
     * @param result
     */
    protected abstract void eventResultChanged(ARXResult result);

    /**
     * Returns the controller
     * @return
     */
    protected Controller getController() {
        return this.controller;
    }

    /**
     * Returns the model
     * @return
     */
    protected Model getModel() {
        return this.model;
    }

    /**
     * Returns the selected node
     * @return
     */
    protected ARXNode getSelectedNode() {
        return this.selectedNode;
    }

    /**
     * Returns the tool tip decorator
     * @return
     */
    protected DecoratorString<ARXNode> getTooltipDecorator() {
        return this.tooltipDecorator;
    }

    /**
     * Returns the number formatter
     * @return
     */
    protected NumberFormat getFormat() {
        return this.format;
    }
}
