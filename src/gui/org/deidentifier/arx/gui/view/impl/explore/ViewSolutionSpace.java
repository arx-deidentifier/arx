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

package org.deidentifier.arx.gui.view.impl.explore;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.deidentifier.arx.ARXLattice;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXLattice.Anonymity;
import org.deidentifier.arx.ARXProcessStatistics;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.model.ModelNodeFilter;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.metric.InformationLoss;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import de.linearbits.swt.tiles.DecoratorString;

/**
 * This class provides an abstract base for views that display parts of the solution space
 *  
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 *
 */
public abstract class ViewSolutionSpace implements IView {

    /** The controller. */
    private final Controller         controller;

    /** Context menu. */
    private Menu                     menu              = null;

    /** The selected node. */
    private ARXNode                  selectedNode      = null;

    /** The model. */
    private Model                    model             = null;

    /** The parent */
    private Composite                parent;

    /** View component */
    private Composite                primary;

    /** View component */
    private Composite                secondary;

    /** View component */
    private StackLayout              layout;

    /** View component */
    private Composite                base;

    /** View component */
    private CLabel                   label;

    /** Tooltip decorator */
    private DecoratorString<ARXNode> tooltipDecorator  = null;

    /** The optimum. */
    private ARXNode                  optimum           = null;

    /** Color. */
    private static final Color       COLOR_GREEN       = GUIHelper.getColor(50, 205, 50);

    /** Color. */
    private static final Color       COLOR_LIGHT_GREEN = GUIHelper.getColor(150, 255, 150);

    /** Color. */
    private static final Color       COLOR_RED         = GUIHelper.getColor(255, 99, 71);

    /** Color. */
    private static final Color       COLOR_LIGHT_RED   = GUIHelper.getColor(255, 150, 150);

    /** Color. */
    private static final Color       COLOR_BLUE        = GUIHelper.getColor(0, 0, 255);

    /** Color. */
    private static final Color       COLOR_YELLOW      = GUIHelper.getColor(255, 215, 0);

    /** Color. */
    private static final Color       COLOR_DARK_GRAY   = GUIHelper.getColor(180, 180, 180);

    /** Color. */
    private static final Color       COLOR_GRAY        = GUIHelper.getColor(160, 160, 160);

    /** Color. */
    private static final Color       COLOR_BLACK       = GUIHelper.getColor(0, 0, 0);

    /** Decorator */
    private Gradient                 gradient;

    /** Maximal length of a label in characters */
    private static final int         MAX_LABEL_LENGTH  = 20;

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
        controller.addListener(ModelPart.EXPAND, this);

        // Store
        this.parent = parent;
        this.controller = controller;
        
        // Initialize
        initializeMenu();
        initializeTooltip();
        this.gradient = new Gradient(parent.getDisplay());
        
        // Create stack
        this.base = new Composite(parent, SWT.NONE); 
        this.base.setLayoutData(SWTUtil.createFillGridData());
        
        this.layout = new StackLayout();
        this.base.setLayout(layout);
        
        // Create the primary composite
        this.primary = new Composite(this.base, SWT.NONE);
        this.primary.setLayout(SWTUtil.createGridLayout(1));
        
        // Create the secondary composite
        this.secondary = new Composite(this.base, SWT.NONE);
        this.secondary.setLayout(SWTUtil.createGridLayout(1));
        
        this.label = new CLabel(this.secondary, SWT.NONE);
        this.label.setLayoutData(GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, true).minSize(400, 200).create());
        this.label.setImage(controller.getResources().getManagedImage("warning.png")); //$NON-NLS-1$
        this.label.setText(""); //$NON-NLS-1$
        this.label.setAlignment(SWT.LEFT);
        
        // Show primary
        this.showPrimaryComposite();
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
        gradient.dispose();
    }
    
    /**
     * Resets the view.
     */
    @Override
    public void reset() {
        this.optimum = null;
        this.selectedNode = null;
    }
    
    @Override
    public void update(final ModelEvent event) {

        if (event.part == ModelPart.SELECTED_NODE) {
            selectedNode = (ARXNode) event.data;
            eventNodeSelected();
        } else if (event.part == ModelPart.RESULT) {
            ARXResult result = (ARXResult)event.data;
            if (model != null && result != null && result.getGlobalOptimum() != null && !model.getProcessStatistics().isLocalTransformation()) {
                optimum = result.getGlobalOptimum();
            } else {
                optimum = null;
            }
            if (model!=null && !isTooLarge(result, model.getProcessStatistics(), model.getNodeFilter(), model.getMaxNodesInViewer())) {
                eventResultChanged(result);
            }
        } else if (event.part == ModelPart.MODEL) {
            model = (Model) event.data;
            if (model != null && model.getResult() != null &&
                model.getResult().getGlobalOptimum() != null && !model.getProcessStatistics().isLocalTransformation()) {
                optimum = model.getResult().getGlobalOptimum();
            } else {
                optimum = null;
            }
            if (model!=null && !isTooLarge(model.getResult(), model.getProcessStatistics(), model.getNodeFilter(), model.getMaxNodesInViewer())) {
                eventModelChanged();
            }
        } else if (event.part == ModelPart.FILTER) {
            if (model!=null && !isTooLarge(model.getResult(), model.getProcessStatistics(), (ModelNodeFilter) event.data, model.getMaxNodesInViewer())) {
                eventFilterChanged(model.getResult(), (ModelNodeFilter) event.data);
            }
        } else if (event.part == ModelPart.EXPAND) {
            if (model!=null && !isTooLarge(model.getResult(), model.getProcessStatistics(), model.getNodeFilter(), model.getMaxNodesInViewer())) {
                eventFilterChanged(model.getResult(), model.getNodeFilter());
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

        MenuItem item3 = new MenuItem(menu, SWT.NONE);
        item3.setText(Resources.getMessage("LatticeView.11")); //$NON-NLS-1$
        item3.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.setSelectedNode(selectedNode);
                controller.update(new ModelEvent(ViewSolutionSpace.this, ModelPart.SELECTED_NODE, selectedNode));
                controller.actionExpand(selectedNode);
                controller.update(new ModelEvent(ViewSolutionSpace.this, ModelPart.EXPAND, selectedNode));
                eventFilterChanged(model.getResult(), model.getNodeFilter());
            }
        });
    }
    
    private void initializeTooltip() {
        this.tooltipDecorator = new DecoratorString<ARXNode>() {
            @Override
            public String decorate(ARXNode node) {
                final StringBuffer b = new StringBuffer();
                b.append(Resources.getMessage("LatticeView.1")); //$NON-NLS-1$
                b.append(SWTUtil.getPrettyString(asRelativeValue(node.getLowestScore())));
                b.append(" - "); //$NON-NLS-1$
                b.append(SWTUtil.getPrettyString(asRelativeValue(node.getHighestScore())));
                b.append(" [%]\n"); //$NON-NLS-1$
                if (model.getOutputDefinition() != null) {
                    for (final String qi : node.getQuasiIdentifyingAttributes()) {

                        // Determine height of hierarchy
                        int height = model.getOutputDefinition().isHierarchyAvailable(qi) ?
                                model.getOutputDefinition().getHierarchy(qi)[0].length : 0;
                        b.append(" * "); //$NON-NLS-1$
                        b.append(qi);
                        b.append(": "); //$NON-NLS-1$
                        b.append(SWTUtil.getPrettyString(asRelativeValue(node.getGeneralization(qi), height - 1)));
                        b.append(" [%]\n"); //$NON-NLS-1$
                    }
                }
                b.setLength(b.length() - 1);
                return b.toString();
            }
        };
    }
    
    /**
     * Check whether the filtered part of the solution space is too large
     * @param result
     * @param statistics
     * @param filter
     * @return
     */
    private boolean isTooLarge(ARXResult result, ARXProcessStatistics statistics, ModelNodeFilter filter, int max) {

        if(result == null) {
            showPrimaryComposite();
            return false;
        }
        
        if (statistics.isLocalTransformation()) {
            showPrimaryComposite();
            return false;
        }

        int count = 0;
        final ARXLattice l = result.getLattice();
        for (final ARXNode[] level : l.getLevels()) {
            for (final ARXNode node : level) {
                if (filter.isAllowed(result.getLattice(), node)) {
                    count++;
                }
            }
        }
        if (count > max) {
            showSecondaryComposite(count, max);
            return true;
        } else {
            showPrimaryComposite();
            return false;
        }
    }

    /**gray.dispose();
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
        // Only show for global transformation schemes
        if (model != null && model.getProcessStatistics() != null && !model.getProcessStatistics().isLocalTransformation()) {
            menu.setLocation(x, y);
            menu.setVisible(true);
        }
    }

    /**
     * Converts an score into a relative value in percent.
     *
     * @param infoLoss
     * @return
     */
    protected double asRelativeValue(final InformationLoss<?> infoLoss) {
        if (model != null && model.getResult() != null && model.getResult().getLattice() != null &&
            model.getResult().getLattice().getBottom() != null && model.getResult().getLattice().getTop() != null) {
            
            final ARXLattice lattice = getModel().getProcessStatistics().isLocalTransformation() ? 
                                       getModel().getProcessStatistics().getLattice() : model.getResult().getLattice();
            
            return infoLoss.relativeTo(lattice.getLowestScore(),
                                       lattice.getHighestScore()) * 100d;
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
     * Returns the inner color.
     *
     * @param node
     * @return
     */
    protected Color getInnerColor(final ARXNode node) {
        if (node.getAnonymity() == Anonymity.ANONYMOUS) {
            return node.equals(optimum) ? COLOR_YELLOW : COLOR_GREEN;
        } else if (node.getAnonymity() == Anonymity.PROBABLY_ANONYMOUS) {
            return COLOR_LIGHT_GREEN;
        } else if (node.getAnonymity() == Anonymity.PROBABLY_NOT_ANONYMOUS) {
            return COLOR_LIGHT_RED;
        } else if (node.getAnonymity() == Anonymity.UNKNOWN) {
            return COLOR_DARK_GRAY;
        } else {
            return COLOR_RED;
        }
    }
    
    /**
     * Returns the model
     * @return
     */
    protected Model getModel() {
        return this.model;
    }
    
    /**
     * Returns the outer color.
     *
     * @param node
     * @return
     */
    protected Color getOuterColor(final ARXNode node) {
        return node.isChecked() ? COLOR_BLUE : COLOR_BLACK;
    }
    
    /**
     * Returns the outer stroke width.
     *
     * @param node
     * @param width
     * @return
     */
    protected int getOuterStrokeWidth(final ARXNode node, final int width) {
        int result = node.isChecked() ? width / 100 : 1;
        result = node.isChecked() ? result + 1 : result;
        return result >=1 ? result < 1 ? 1 : result : 1;
    }

    /**
     * Returns the primary composite
     * @return
     */
    protected Composite getPrimaryComposite() {
        return this.primary;
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
     * Returns the color according to a nodes utility
     * 
     * @param node The node
     * @return
     */
    protected Color getUtilityColor(ARXNode node) {
        if (node.getLowestScore().compareTo(node.getHighestScore()) != 0 &&
            asRelativeValue(node.getLowestScore()) == 0d) {
            return COLOR_GRAY;
        } else {
            return gradient.getColor(asRelativeValue(node.getLowestScore()) / 100d);
        }
    }
    
    /**
     * Shows the primary composite
     */
    protected void showPrimaryComposite() {
        this.layout.topControl = this.primary;
        this.base.layout();
    }
    
    /**
     * Shows the secondary composite
     */
    protected void showSecondaryComposite(int num, int max) {
        
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ps.format(Resources.getMessage("LatticeView.7"), num, max); //$NON-NLS-1$
        label.setText(os.toString());
        
        this.layout.topControl = this.secondary;
        this.base.layout();
    }

    /**
     * Trims the label to a predefined length
     * @param label
     * @return
     */
    protected String trimLabel(String label) {
        if (label.length() > MAX_LABEL_LENGTH) {
            label = label.replace(" ", "");
        }
        if (label.length() > MAX_LABEL_LENGTH) {
            label = label.replace(",", "");
        }
        if (label.length() > MAX_LABEL_LENGTH) {
            label = label.substring(0, MAX_LABEL_LENGTH -3) + "...";
        }
        return label;
    }
}
