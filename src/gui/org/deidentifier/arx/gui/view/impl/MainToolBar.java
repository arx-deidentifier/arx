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

package org.deidentifier.arx.gui.view.impl;

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.ARXProcessStatistics;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.metric.v2.QualityMetadata;
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
    private Label                labelAttribute;
    
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

    @Override
    public void dispose() {
        super.dispose();
    }

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
        if (labelAttribute != null) {
            toolbar.setRedraw(false);
            labelAttribute.setText(""); //$NON-NLS-1$
            labelAttribute.pack();
            layout();
            toolbar.setRedraw(true);
        }
    }
    
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
            
            if (model.getOutputTransformation() != null) {

                // Statistics
                ARXProcessStatistics statistics = model.getProcessStatistics();
                
                // Update tool tip
                setToolTip(statistics);
                
                // Update labels
                toolbar.setRedraw(false);
                labelTransformations.setText(Resources.getMessage("MainToolBar.6") + //$NON-NLS-1$
                                             SWTUtil.getPrettyString(statistics.getTransformationsAvailable()));
                labelTransformations.pack();
                
                labelApplied.setText(Resources.getMessage("MainToolBar.4") + //$NON-NLS-1$
                                     Arrays.toString(model.getOutputTransformation().getTransformation()));
                labelApplied.pack();
                
                // Layout
                layout();
                
                toolbar.setRedraw(true);
                
            } else {
                
                reset();
                
            }
        } else if (event.part == ModelPart.RESULT) {
            
            if (model.getResult() != null) {
                
                // Statistics
                ARXProcessStatistics statistics = model.getProcessStatistics();
                
                // Update tool tip
                setToolTip(statistics);

                // Update labels
                toolbar.setRedraw(false);
                labelTransformations.setText(Resources.getMessage("MainToolBar.6") + //$NON-NLS-1$
                                             SWTUtil.getPrettyString(statistics.getTransformationsAvailable())); 
                labelTransformations.pack();

                labelSelected.setText(Resources.getMessage("MainToolBar.7")); //$NON-NLS-1$
                labelSelected.pack();

                labelApplied.setText(Resources.getMessage("MainToolBar.8")); //$NON-NLS-1$
                labelApplied.pack();
                
                layout();
                
                toolbar.setRedraw(true);
            }
            
        } else if (event.part == ModelPart.SELECTED_ATTRIBUTE) {

            // Update label
            String attribute = (String)event.data;
            toolbar.setRedraw(false);
            labelAttribute.setText(Resources.getMessage("MainToolBar.50") + trim(attribute)); //$NON-NLS-1$
            labelAttribute.pack();
            layout();
            toolbar.setRedraw(true);
            
        }  else if (event.part == ModelPart.MODEL) {
            
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

        labelAttribute = new Label(infoComposite, SWT.SINGLE | SWT.READ_ONLY);
        labelAttribute.setText(Resources.getMessage("MainToolBar.33")); //$NON-NLS-1$
        labelAttribute.pack();        
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
        

        // Layout label
        locationX -= labelAttribute.getSize().x + OFFSET;
        labelAttribute.setLocation(locationX, locationY);
        if (locationX < 0) labelAttribute.setVisible(false);
        else labelAttribute.setVisible(true);
                
        // Redraw
        toolbar.setRedraw(true);
    }

    /**
     * Sets the tool tip
     *
     * @param stats
     */
    private void setToolTip(ARXProcessStatistics stats) {
        
        // Prepare
        double prunedPercentage = (double) (stats.getTransformationsAvailable() - stats.getTransformationsChecked()) /
                                  (double) (stats.getTransformationsAvailable()) * 100d;
        
        // Render statistics about the solution space
        StringBuilder sb = new StringBuilder();
        sb.append(Resources.getMessage("MainToolBar.1")); //$NON-NLS-1$
        sb.append(Resources.getMessage("MainToolBar.2")) //$NON-NLS-1$
          .append(stats.getTransformationsAvailable())
          .append("\n"); //$NON-NLS-1$
        
        sb.append(Resources.getMessage("MainToolBar.12")) //$NON-NLS-1$
          .append(stats.getTransformationsAvailable() - stats.getTransformationsChecked());
        sb.append(" [") //$NON-NLS-1$
          .append(SWTUtil.getPrettyString(prunedPercentage))
          .append("%]\n"); //$NON-NLS-1$
        
        sb.append(Resources.getMessage("MainToolBar.18")) //$NON-NLS-1$
          .append(SWTUtil.getPrettyString((double)stats.getDuration() / 1000d))
          .append("s\n"); //$NON-NLS-1$
        
        // Render information about the selected transformation
        if (stats.isSolutationAvailable()) {
            
            // Global transformation scheme
            if (!stats.isLocalTransformation()) {
                sb.append(Resources.getMessage("MainToolBar.36")) //$NON-NLS-1$
                .append(Resources.getMessage("MainToolBar.39")) //$NON-NLS-1$
                .append(stats.getStep(0).isOptimal() ? SWTUtil.getPrettyString(true) : Resources.getMessage("MainToolBar.72"))
                .append(Resources.getMessage("MainToolBar.37")) //$NON-NLS-1$
                .append(Arrays.toString(stats.getStep(0).getTransformation()));
              sb.append(Resources.getMessage("MainToolBar.38")) //$NON-NLS-1$
                .append(stats.getStep(0).getScore().toString());
              for (QualityMetadata<?> metadata : stats.getStep(0).getMetadata()) {
                  sb.append("\n - ") //$NON-NLS-1$
                    .append(metadata.getParameter()).append(": ") //$NON-NLS-1$
                    .append(SWTUtil.getPrettyString(metadata.getValue()));
              }
              
            // Complex transformation
            } else {
                sb.append(Resources.getMessage("MainToolBar.36")) //$NON-NLS-1$
                  .append(Resources.getMessage("MainToolBar.70")) //$NON-NLS-1$
                  .append(SWTUtil.getPrettyString(stats.getNumberOfSteps()))
                  .append(Resources.getMessage("MainToolBar.39")) //$NON-NLS-1$
                  .append(SWTUtil.getPrettyString(false));
            }
            
        // No solution found
        } else {
            sb.append(Resources.getMessage("MainToolBar.71")); //$NON-NLS-1$
        }
        
        this.tooltip = sb.toString();
        this.labelSelected.setToolTipText(tooltip);
        this.labelApplied.setToolTipText(tooltip);
        this.labelTransformations.setToolTipText(tooltip);
    }

    /**
     * Trims the given string to 20 characters
     * @param attribute
     * @return
     */
    private String trim(String attribute) {
        if (attribute.length() > 20) {
            return attribute.substring(0, 20) + "...";
        } else {
            return attribute;
        }
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
