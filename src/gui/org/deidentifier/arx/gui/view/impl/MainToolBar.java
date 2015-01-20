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
import org.deidentifier.arx.gui.view.def.IView;
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
public class MainToolBar implements IView {

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
            DecimalFormat format = new DecimalFormat("#########0.000");
            double prunedPercentage = (double) this.numTransformationsPruned /
                                      (double) this.numTransformationsInSearchSpace * 100d;

            // Render statistics about the solution space
            StringBuilder sb = new StringBuilder();
            sb.append("Solution space\n");
            sb.append(" - Total transformations: ")
              .append(this.numTransformationsInSearchSpace)
              .append("\n");
            sb.append(" - Pruned transformations: ")
              .append(this.numTransformationsPruned);
            sb.append(" [")
              .append(format.format(prunedPercentage))
              .append("%]\n");
            sb.append(" - Execution time: ")
              .append(format.format(this.executionTime))
              .append("s");
            
            if (this.numTransformationsAnonymous != 0 ||
                this.numTransformationsNotAnonymous != 0 ||
                this.numTransformationsProbablyAnonymous != 0 ||
                this.numTransformationsProbablyNotAnonymous != 0 ||
                this.numTransformationsAnonymityUnknown != 0 ||
                this.numTransformationsInfolossAvailable != 0) {
                
                // Render the classification result
                sb.append("\nClassification result");
                if (this.numTransformationsAnonymous != 0) {
                    sb.append("\n - Anonymous transformations: ")
                      .append(this.numTransformationsAnonymous);
                }
                if (this.numTransformationsNotAnonymous != 0) {
                    sb.append("\n - Non-anonymous transformations: ")
                      .append(this.numTransformationsNotAnonymous);
                }
                if (this.numTransformationsProbablyAnonymous != 0) {
                    sb.append("\n - Probably anonymous transformations: ")
                      .append(this.numTransformationsProbablyAnonymous);
                }
                if (this.numTransformationsProbablyNotAnonymous != 0) {
                    sb.append("\n - Probably non-anonymous transformations: ")
                      .append(this.numTransformationsProbablyNotAnonymous);
                }
                if (this.numTransformationsAnonymityUnknown != 0) {
                    sb.append("\n - Transformations with unknown properties: ")
                      .append(this.numTransformationsAnonymityUnknown);
                }
                if (this.numTransformationsInfolossAvailable != 0) {
                    sb.append("\n - Transformations with information loss available: ")
                      .append(this.numTransformationsInfolossAvailable);
                }
            }
            // Render information about the optimum
            if (this.optimum != null) {
                sb.append("\nGlobal optimum")
                  .append("\n - Transformation: ")
                  .append(Arrays.toString(optimum.getTransformation()));
                sb.append("\n - Information loss: ")
                  .append(optimum.getMaximumInformationLoss().toString());
            }

            // Return
            return sb.toString();
        }
    }

    /** Static offset. */
    private static final int OFFSET = 10;

    /** Text. */
    private String           tooltip;

    /** State. */
    private Controller       controller;
    
    /** State. */
    private Model            model;
    
    /** State. */
    private List<ToolItem>   toolitems;

    /** Widget. */
    private ToolBar          toolbar;
    
    /** Widget. */
    private Label            labelTransformations;
    
    /** Widget. */
    private Label            labelApplied;
    
    /** Widget. */
    private Label            labelSelected;
    
    /** Widget. */
    private Composite        infoComposite;
    
    /** Widget. */
    private ToolItem         infoItem;

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     */
    public MainToolBar(final Shell parent, final Controller controller) {
        toolbar = new ToolBar(parent, SWT.FLAT);
        toolbar.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        this.controller = controller;
        controller.addListener(ModelPart.MODEL, this);
        controller.addListener(ModelPart.SELECTED_NODE, this);
        controller.addListener(ModelPart.OUTPUT, this);
        controller.addListener(ModelPart.RESULT, this);
        build();
        toolbar.pack();
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
     * Builds the component.
     */
    private void build() {
        toolitems = new ArrayList<ToolItem>();

        ToolItem item;

        item = new ToolItem(toolbar, SWT.PUSH);
        item.setToolTipText(Resources.getMessage("MainToolBar.9")); //$NON-NLS-1$
        item.setImage(controller.getResources().getImage("file_new.png")); //$NON-NLS-1$
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.actionMenuFileNew();
            }
        });
        toolitems.add(item);

        toolitems.add(new ToolItem(toolbar, SWT.SEPARATOR));

        item = new ToolItem(toolbar, SWT.PUSH);
        item.setToolTipText(Resources.getMessage("MainToolBar.11")); //$NON-NLS-1$
        item.setImage(controller.getResources().getImage("file_load.png")); //$NON-NLS-1$
        item.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.actionMenuFileOpen();
            }
        });
        toolitems.add(item);

        item = new ToolItem(toolbar, SWT.PUSH);
        item.setToolTipText(Resources.getMessage("MainToolBar.13")); //$NON-NLS-1$
        item.setImage(controller.getResources().getImage("file_save.png")); //$NON-NLS-1$
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.actionMenuFileSave();
            }
        });
        toolitems.add(item);

        item = new ToolItem(toolbar, SWT.PUSH);
        item.setToolTipText(Resources.getMessage("MainToolBar.15")); //$NON-NLS-1$
        item.setImage(controller.getResources().getImage("file_save_as.png")); //$NON-NLS-1$
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.actionMenuFileSaveAs();
            }
        });

        toolitems.add(item);

        toolitems.add(new ToolItem(toolbar, SWT.SEPARATOR));

        item = new ToolItem(toolbar, SWT.PUSH);
        item.setToolTipText(Resources.getMessage("MainToolBar.17")); //$NON-NLS-1$
        item.setImage(controller.getResources()
                                .getImage("file_import_data.png")); //$NON-NLS-1$
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.actionMenuFileImportData();
            }
        });
        toolitems.add(item);

        item = new ToolItem(toolbar, SWT.PUSH);
        item.setToolTipText(Resources.getMessage("MainToolBar.19")); //$NON-NLS-1$
        item.setImage(controller.getResources()
                                .getImage("file_export_data.png")); //$NON-NLS-1$
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.actionMenuFileExportData();
            }
        });

        toolitems.add(item);

        toolitems.add(new ToolItem(toolbar, SWT.SEPARATOR));

        item = new ToolItem(toolbar, SWT.PUSH);
        item.setToolTipText(Resources.getMessage("MainToolBar.21")); //$NON-NLS-1$
        item.setImage(controller.getResources()
                                .getImage("file_import_hierarchy.png")); //$NON-NLS-1$
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.actionMenuFileImportHierarchy();
            }
        });
        toolitems.add(item);

        item = new ToolItem(toolbar, SWT.PUSH);
        item.setToolTipText(Resources.getMessage("MainToolBar.23")); //$NON-NLS-1$
        item.setImage(controller.getResources()
                                .getImage("file_export_hierarchy.png")); //$NON-NLS-1$
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.actionMenuFileExportHierarchy();
            }
        });

        toolitems.add(item);

        toolitems.add(new ToolItem(toolbar, SWT.SEPARATOR));

        item = new ToolItem(toolbar, SWT.PUSH);
        item.setToolTipText(Resources.getMessage("MainToolBar.25")); //$NON-NLS-1$
        item.setImage(controller.getResources().getImage("edit_anonymize.png")); //$NON-NLS-1$
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.actionMenuEditAnonymize();
            }
        });

        toolitems.add(item);

        item = new ToolItem(toolbar, SWT.PUSH);
        item.setToolTipText(Resources.getMessage("MainToolBar.27")); //$NON-NLS-1$
        item.setImage(controller.getResources()
                                .getImage("edit_create_hierarchy.png")); //$NON-NLS-1$
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.actionMenuEditCreateHierarchy();
            }
        });

        toolitems.add(item);

        toolitems.add(new ToolItem(toolbar, SWT.SEPARATOR));

        item = new ToolItem(toolbar, SWT.PUSH);
        item.setToolTipText(Resources.getMessage("MainToolBar.29")); //$NON-NLS-1$
        item.setImage(controller.getResources().getImage("edit_settings.png")); //$NON-NLS-1$
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.actionMenuEditSettings();
            }
        });
        toolitems.add(item);
        toolitems.add(new ToolItem(toolbar, SWT.SEPARATOR));

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
        
        // Copy info to clipboard on right-click
        Menu menu = new Menu(toolbar);
        MenuItem itemCopy = new MenuItem(menu, SWT.NONE);
        itemCopy.setText("Copy");
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
}
