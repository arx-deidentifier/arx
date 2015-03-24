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

package org.deidentifier.arx.gui.view.impl;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.ARXLattice;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXLattice.Anonymity;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import cern.colt.Arrays;

/**
 * This class implements the global application tool bar.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class MainToolBar extends AbstractMenu {

    /**
     * Helper class including some statistics.
     */
    private static class SearchSpaceStatistics {
        
        /** Count. */
        private final int numTransformationsInSearchSpace;
        
        /** Count. */
        private final int numTransformationsPruned;
        
        /** Count. */
        private final int numTransformationsAnonymous;
        
        /** Count. */
        private final int numTransformationsNotAnonymous;
        
        /** Count. */
        private final int numTransformationsProbablyAnonymous;
        
        /** Count. */
        private final int numTransformationsProbablyNotAnonymous;
        
        /** Count. */
        private final int numTransformationsAnonymityUnknown;
        
        /** Count. */
        private final int numTransformationsInfolossAvailable;
        
        /** Time in seconds. */
        private final double executionTime;
        
        /** Optimal transformation. */
        private final ARXNode optimum;
        
        /**
         * Creates the statistics.
         *
         * @param result
         */
        private SearchSpaceStatistics(ARXResult result){
            
            // Prepare
            int pruned = 0;
            int anonymous = 0;
            int notAnonymous = 0;
            int probablyAnonymous = 0;
            int probablyNotAnonymous = 0;
            int anonymityUnknown = 0;
            int infolossAvailable = 0;
            ARXLattice lattice = result.getLattice();
            
            // Compute statistics
            for (final ARXNode[] level : lattice.getLevels()) {
                for (final ARXNode node : level) {
                    if (!node.isChecked()) {
                        pruned++;
                    }
                    if (node.getAnonymity() == Anonymity.ANONYMOUS) {
                        anonymous++;
                    } else if (node.getAnonymity() == Anonymity.NOT_ANONYMOUS) {
                        notAnonymous++;
                    } else if (node.getAnonymity() == Anonymity.PROBABLY_ANONYMOUS) {
                        probablyAnonymous++;
                    } else if (node.getAnonymity() == Anonymity.PROBABLY_NOT_ANONYMOUS) {
                        probablyNotAnonymous++;
                    } else if (node.getAnonymity() == Anonymity.UNKNOWN) {
                        anonymityUnknown++;
                    }
                    if (node.getMaximumInformationLoss().compareTo(node.getMinimumInformationLoss()) == 0) {
                        infolossAvailable++;
                    }
                }
            }
            
            // Store
            this.executionTime = (double)result.getTime() / 1000d;
            this.numTransformationsInSearchSpace = lattice.getSize();
            this.numTransformationsPruned = pruned;
            this.numTransformationsAnonymous = anonymous;
            this.numTransformationsNotAnonymous = notAnonymous;
            this.numTransformationsAnonymityUnknown = anonymityUnknown;
            this.numTransformationsProbablyAnonymous = probablyAnonymous;
            this.numTransformationsProbablyNotAnonymous = probablyNotAnonymous;
            this.numTransformationsInfolossAvailable = infolossAvailable;
            this.optimum = result.getGlobalOptimum();
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {

            // Prepare
            DecimalFormat format = new DecimalFormat("#########0.000"); //$NON-NLS-1$
            double prunedPercentage = (double) this.numTransformationsPruned /
                                      (double) this.numTransformationsInSearchSpace * 100d;

            // Render statistics about the solution space
            StringBuilder sb = new StringBuilder();
            sb.append(Resources.getMessage("MainToolBar.1")); //$NON-NLS-1$
            sb.append(Resources.getMessage("MainToolBar.2")) //$NON-NLS-1$
              .append(this.numTransformationsInSearchSpace)
              .append("\n"); //$NON-NLS-1$
            sb.append(Resources.getMessage("MainToolBar.12")) //$NON-NLS-1$
              .append(this.numTransformationsPruned);
            sb.append(" [") //$NON-NLS-1$
              .append(format.format(prunedPercentage))
              .append("%]\n"); //$NON-NLS-1$
            sb.append(Resources.getMessage("MainToolBar.18")) //$NON-NLS-1$
              .append(format.format(this.executionTime))
              .append("s"); //$NON-NLS-1$
            
            if (this.numTransformationsAnonymous != 0 ||
                this.numTransformationsNotAnonymous != 0 ||
                this.numTransformationsProbablyAnonymous != 0 ||
                this.numTransformationsProbablyNotAnonymous != 0 ||
                this.numTransformationsAnonymityUnknown != 0 ||
                this.numTransformationsInfolossAvailable != 0) {
                
                // Render the classification result
                sb.append(Resources.getMessage("MainToolBar.22")); //$NON-NLS-1$
                if (this.numTransformationsAnonymous != 0) {
                    sb.append(Resources.getMessage("MainToolBar.24")) //$NON-NLS-1$
                      .append(this.numTransformationsAnonymous);
                }
                if (this.numTransformationsNotAnonymous != 0) {
                    sb.append(Resources.getMessage("MainToolBar.26")) //$NON-NLS-1$
                      .append(this.numTransformationsNotAnonymous);
                }
                if (this.numTransformationsProbablyAnonymous != 0) {
                    sb.append(Resources.getMessage("MainToolBar.28")) //$NON-NLS-1$
                      .append(this.numTransformationsProbablyAnonymous);
                }
                if (this.numTransformationsProbablyNotAnonymous != 0) {
                    sb.append(Resources.getMessage("MainToolBar.30")) //$NON-NLS-1$
                      .append(this.numTransformationsProbablyNotAnonymous);
                }
                if (this.numTransformationsAnonymityUnknown != 0) {
                    sb.append(Resources.getMessage("MainToolBar.34")) //$NON-NLS-1$
                      .append(this.numTransformationsAnonymityUnknown);
                }
                if (this.numTransformationsInfolossAvailable != 0) {
                    sb.append(Resources.getMessage("MainToolBar.35")) //$NON-NLS-1$
                      .append(this.numTransformationsInfolossAvailable);
                }
            }
            // Render information about the optimum
            if (this.optimum != null) {
                sb.append(Resources.getMessage("MainToolBar.36")) //$NON-NLS-1$
                  .append(Resources.getMessage("MainToolBar.37")) //$NON-NLS-1$
                  .append(Arrays.toString(optimum.getTransformation()));
                sb.append(Resources.getMessage("MainToolBar.38")) //$NON-NLS-1$
                  .append(optimum.getMaximumInformationLoss().toString());
            }

            // Return
            return sb.toString();
        }
    }

    /** Static offset. */
    private static final int     OFFSET    = 10;

    /** Text. */
    private String               tooltip;

    /** State. */
    private Model                model;

    /** State. */
    private final List<ToolItem> toolitems = new ArrayList<ToolItem>();

    /** Widget. */
    private ToolBar              toolbar;

    /** Widget. */
    private Label                labelTransformations;

    /** Widget. */
    private Label                labelApplied;

    /** Widget. */
    private Label                labelSelected;

    /** Widget. */
    private Composite            infoComposite;

    /** Widget. */
    private ToolItem             infoItem;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     */
    public MainToolBar(final Shell parent, final Controller controller, List<MainMenuItem> items) {
        super(controller);
        toolbar = new ToolBar(parent, SWT.FLAT);
        toolbar.setLayoutData(SWTUtil.createFillHorizontallyGridData());

        // Create items
        this.createItems(toolbar, items, ""); //$NON-NLS-1$
        this.createLabels();

        // Pack
        toolbar.pack();
        
        // Initialize
        this.update(new ModelEvent(this, ModelPart.MODEL, null));
        
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IView#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IView#reset()
     */
    @Override
    public void reset() {
        if (labelSelected != null) {
            toolbar.setRedraw(false);
            labelSelected.setText(""); //$NON-NLS-1$
            labelSelected.pack();
            layout();
            toolbar.setRedraw(true);
        }
        if (labelApplied != null) {
            toolbar.setRedraw(false);
            labelApplied.setText(""); //$NON-NLS-1$
            labelApplied.pack();
            layout();
            toolbar.setRedraw(true);
        }
        if (labelTransformations != null) {
            toolbar.setRedraw(false);
            labelTransformations.setText(""); //$NON-NLS-1$
            labelTransformations.pack();
            layout();
            toolbar.setRedraw(true);
        }
    }
    
    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IView#update(org.deidentifier.arx.gui.model.ModelEvent)
     */
    @Override
    public void update(final ModelEvent event) {
        
        super.update(event);
        
        if (event.part == ModelPart.SELECTED_NODE) {
            if (model.getSelectedNode() != null) {
                toolbar.setRedraw(false);
                labelSelected.setText(Resources.getMessage("MainToolBar.3") + //$NON-NLS-1$
                                      Arrays.toString(model.getSelectedNode().getTransformation())); 
                labelSelected.pack();
                layout();
                toolbar.setRedraw(true);
            }
        } else if (event.part == ModelPart.OUTPUT) {
            if (model.getOutputNode() != null) {

                // Update tool tip
                SearchSpaceStatistics stats = new SearchSpaceStatistics(model.getResult());
                setToolTip(stats);
                
                // Update labels
                toolbar.setRedraw(false);
                labelTransformations.setText(Resources.getMessage("MainToolBar.6") + //$NON-NLS-1$
                                     String.valueOf(stats.numTransformationsInSearchSpace));
                labelTransformations.pack();
                
                labelApplied.setText(Resources.getMessage("MainToolBar.4") + //$NON-NLS-1$
                                     Arrays.toString(model.getOutputNode().getTransformation()));
                labelApplied.pack();
                
                layout();
                
                toolbar.setRedraw(true);
            } else {
                reset();
            }
        } else if (event.part == ModelPart.RESULT) {
            if (model.getResult() != null) {
                
                // Update tool tip
                SearchSpaceStatistics stats = new SearchSpaceStatistics(model.getResult());
                setToolTip(stats);

                // Update labels
                toolbar.setRedraw(false);
                labelTransformations.setText(Resources.getMessage("MainToolBar.6") + //$NON-NLS-1$
                                     String.valueOf(stats.numTransformationsInSearchSpace)); 
                labelTransformations.pack();

                labelSelected.setText(Resources.getMessage("MainToolBar.7")); //$NON-NLS-1$
                labelSelected.pack();

                labelApplied.setText(Resources.getMessage("MainToolBar.8")); //$NON-NLS-1$
                labelApplied.pack();
                
                layout();
                
                toolbar.setRedraw(true);
            }
        } else if (event.part == ModelPart.MODEL) {
            model = (Model) event.data;
        }
    }

    /**
     * Creates all items
     * @param toolbar
     * @param items
     * @param label
     */
    private void createItems(ToolBar toolbar, List<MainMenuItem> items, String label) {

        // For each item
        for (final MainMenuItem item : items) {

            // Skip items that are not buttons
            if (!item.isButton()) {
                continue;
            }
            
            // Create group
            if (item instanceof MainMenuGroup) {

                MainMenuGroup group = (MainMenuGroup) item;
                if (!this.toolitems.isEmpty()) { 
                    ToolItem menuItem = new ToolItem(toolbar, SWT.SEPARATOR);
                    menuItem.setEnabled(false);
                    menuItem.setData(item);
                    this.toolitems.add(menuItem);
                }
                
                createItems(toolbar, group.getItems(), label.length() != 0 ? label + " -> " + group.getLabel() : group.getLabel()); //$NON-NLS-1$
                
            // Create separator
            } else if (item instanceof MainMenuSeparator) {
                
                if (!this.toolitems.isEmpty()) { 
                    ToolItem menuItem = new ToolItem(toolbar, SWT.SEPARATOR);
                    this.toolitems.add(menuItem);
                }

                // Create item
            } else {

                ToolItem menuItem = new ToolItem(toolbar, SWT.PUSH);
                menuItem.setToolTipText(label.length() != 0 ? label + " -> " + item.getLabel() : item.getLabel()); //$NON-NLS-1$
                if (item.getImage() != null) {
                    menuItem.setImage(item.getImage());
                    SWTUtil.createDisabledImage(menuItem);
                }
                menuItem.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(final SelectionEvent e) {
                        item.action(getController());
                    }
                });
                menuItem.setData(item);
                menuItem.setEnabled(false);
                this.toolitems.add(menuItem);
            }
        }
    }

    private void createLabels() {

        // Add status labels
        infoItem = new ToolItem(toolbar, SWT.SEPARATOR);
        infoComposite = new Composite(toolbar, SWT.NONE);
        infoItem.setControl(infoComposite);
        infoComposite.setLayout(null);

        labelTransformations = new Label(infoComposite, SWT.SINGLE | SWT.READ_ONLY);
        labelTransformations.setText(Resources.getMessage("MainToolBar.33")); //$NON-NLS-1$
        labelTransformations.pack();
        labelSelected = new Label(infoComposite, SWT.SINGLE | SWT.READ_ONLY);
        labelSelected.setText(Resources.getMessage("MainToolBar.31")); //$NON-NLS-1$
        labelSelected.pack();
        labelApplied = new Label(infoComposite, SWT.SINGLE | SWT.READ_ONLY);
        labelApplied.setText(Resources.getMessage("MainToolBar.32")); //$NON-NLS-1$
        labelApplied.pack();
        
        // Copy info to clip board on right-click
        Menu menu = new Menu(toolbar);
        MenuItem itemCopy = new MenuItem(menu, SWT.NONE);
        itemCopy.setText(Resources.getMessage("MainToolBar.42")); //$NON-NLS-1$
        itemCopy.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent arg0) {
                if (tooltip != null) {
                    Clipboard clipboard = new Clipboard(toolbar.getDisplay());
                    TextTransfer textTransfer = TextTransfer.getInstance();
                    clipboard.setContents(new String[]{tooltip}, 
                                          new Transfer[]{textTransfer});
                    clipboard.dispose();
                }
            }
        });
        labelSelected.setMenu(menu);
        labelApplied.setMenu(menu);
        labelTransformations.setMenu(menu);
        
        // Add listener for layout
        toolbar.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(final ControlEvent arg0) {
                layout();
            }
        });
    }

    /**
     * Performs layouting.
     */
    private void layout() {
        
        // Disable redrawing
        toolbar.setRedraw(false);
        
        // Adjust size of items and composite
        Rectangle bounds = toolbar.getBounds();
        int remaining = toolbar.getBounds().width;
        for (final ToolItem item : toolitems) {
            remaining -= item.getBounds().width;
        }
        remaining -= OFFSET;
        infoComposite.setSize(remaining, bounds.height);
        infoItem.setWidth(remaining);
        int locationY = (infoComposite.getBounds().height - labelSelected.getBounds().height)/2;

        // Layout label
        int locationX = remaining - labelApplied.getSize().x;
        labelApplied.setLocation(locationX, locationY);
        if (locationX < 0) labelApplied.setVisible(false);
        else labelApplied.setVisible(true);
                
        // Layout label
        locationX -= labelSelected.getSize().x + OFFSET;
        labelSelected.setLocation(locationX, locationY);
        if (locationX < 0) labelSelected.setVisible(false);
        else labelSelected.setVisible(true);

        // Layout label
        locationX -= labelTransformations.getSize().x + OFFSET;
        labelTransformations.setLocation(locationX, locationY);
        if (locationX < 0) labelTransformations.setVisible(false);
        else labelTransformations.setVisible(true);
                
        // Redraw
        toolbar.setRedraw(true);
    }

    /**
     * Sets the tooltip.
     *
     * @param stats
     */
    private void setToolTip(SearchSpaceStatistics stats) {
        this.tooltip = stats.toString();
        this.labelSelected.setToolTipText(tooltip);
        this.labelApplied.setToolTipText(tooltip);
        this.labelTransformations.setToolTipText(tooltip);
    }
    
    @Override
    protected void update(Model model) {

        // Check
        if (toolbar == null) return;
        
        // For each item
        for (final ToolItem item : toolbar.getItems()) {

            // Check group
            if (!(item.getData() instanceof MainMenuGroup)) {
                MainMenuItem mItem = (MainMenuItem) item.getData();
                item.setEnabled(mItem == null || mItem.isEnabled(model));
            }
        }        
    }
}
