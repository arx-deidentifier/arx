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

package org.deidentifier.arx.gui.view.impl.explore;

import org.deidentifier.arx.ARXLattice;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXLattice.Anonymity;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelNodeFilter;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledBorder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * This class displays a filter for the lattice
 * @author Fabian Prasser
 */
public class ViewFilter implements IView {

    private static final int SCALE_MAX_VALUE = 1000;
    
    private Combo            anonymous;
    private Combo            attribute;
    private final Controller controller;

    private ModelNodeFilter  filter = null;
    private Table            generalization;
    private final Image      IMG_GREEN;
    private final Image      IMG_ORANGE;
    private final Image      IMG_RED;
    private Scale            max;
    private int[]            maxlevels;
    private Scale            min;
    private Model            model;
    private Combo            notanonymous;
    private ARXResult        result;
    private final Composite  root;
    private int              selectedDimension;
    private Combo            unknown;

    /**
     * Creates a new instance
     * @param parent
     * @param controller
     */
    public ViewFilter(final Composite parent, final Controller controller) {

        this.controller = controller;
        this.controller.addListener(ModelPart.RESULT, this);
        this.controller.addListener(ModelPart.INPUT, this);
        this.controller.addListener(ModelPart.MODEL, this);
        this.controller.addListener(ModelPart.FILTER, this);

        IMG_RED = controller.getResources().getImage("red.gif"); //$NON-NLS-1$
        IMG_GREEN = controller.getResources().getImage("green.gif"); //$NON-NLS-1$
        IMG_ORANGE = controller.getResources().getImage("orange.gif"); //$NON-NLS-1$

        // Create group
        ComponentTitledBorder border = new ComponentTitledBorder(parent, controller, Resources.getMessage("NodeFilterView.3"), "id-21"); //$NON-NLS-1$
        root = new Composite(border.getControl(), SWT.NONE);
        border.setChild(root);
        border.setLayoutData(SWTUtil.createFillGridData());
        final GridLayout groupLayout = new GridLayout();
        groupLayout.numColumns = 3;
        root.setLayout(groupLayout);

        create(root);
        reset();
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    @Override
    public void reset() {
        filter = null;
        result = null;
        maxlevels = null;
        attribute.removeAll();
        anonymous.select(-1);
        notanonymous.select(-1);
        unknown.select(-1);
        min.setSelection(min.getMinimum());
        min.setMinimum(0);
        min.setMaximum(SCALE_MAX_VALUE);
        max.setSelection(max.getMaximum());
        max.setMinimum(0);
        max.setMaximum(SCALE_MAX_VALUE);
        attribute.setEnabled(false);
        anonymous.setEnabled(false);
        notanonymous.setEnabled(false);
        unknown.setEnabled(false);
        min.setEnabled(false);
        max.setEnabled(false);
        generalization.removeAll();
        generalization.setEnabled(false);
        SWTUtil.disable(root);
    }

    @Override
    public void update(final ModelEvent event) {
        if (event.part == ModelPart.INPUT) {
            reset();
        } else if (event.part == ModelPart.RESULT) {
            if (model.getResult() == null || model.getResult().getLattice() == null){
                reset();
                SWTUtil.disable(root);
            } else {
                initialize(model.getResult(), null);
                SWTUtil.enable(root);
            }
        } else if (event.part == ModelPart.MODEL) {
            model = (Model) event.data;
            reset();
        } else if (event.part == ModelPart.FILTER) {
            // Only update if we receive a new filter
            if ((filter == null) || (model.getNodeFilter() != filter)) {
                if (model.getResult() == null || model.getResult().getLattice() == null){
                    reset();
                    SWTUtil.disable(root);
                } else {
                    initialize(model.getResult(), model.getNodeFilter());
                    SWTUtil.enable(root);
                }
            }
        }
    }

    /**
     * Creates the view
     * @param parent
     */
    private void create(final Composite parent) {

        final IView outer = this;
        final Label tableItem1 = new Label(parent, SWT.NONE);
        tableItem1.setText(Resources.getMessage("NodeFilterView.4")); //$NON-NLS-1$
        attribute = new Combo(parent, SWT.BORDER);
        attribute.pack();
        attribute.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        attribute.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                if (attribute.getSelectionIndex() != -1) {
                    if (result != null) {
                        createGeneralization(result.getLattice()
                                                   .getBottom()
                                                   .getQuasiIdentifyingAttributes()[attribute.getSelectionIndex()]);
                    }
                }
            }
        });

        // Add table
        generalization = new Table(parent, SWT.CHECK | SWT.BORDER |
                                           SWT.V_SCROLL | SWT.SINGLE);
        final GridData d = SWTUtil.createFillVerticallyGridData();
        d.verticalSpan = 6;
        generalization.setLayoutData(d);
        generalization.setHeaderVisible(true);
        generalization.setLinesVisible(true);
        final TableColumn col = new TableColumn(generalization, SWT.NONE);
        col.setText(Resources.getMessage("NodeFilterView.5")); //$NON-NLS-1$
        col.pack();
        generalization.pack();

        generalization.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {

                if (event.detail == SWT.CHECK &&
                    event.item instanceof TableItem) {

                    if (filter != null) {
                        final int level = generalization.indexOf((TableItem) event.item);
                        if (generalization.getItems()[level].getChecked()) {
                            filter.allowGeneralization(selectedDimension, level);
                        } else {
                            filter.disallowGeneralization(selectedDimension,
                                                          level);
                        }
                    }
                    controller.update(new ModelEvent(outer,
                                                     ModelPart.FILTER,
                                                     filter));
                }
            }
        });

        final Label tableItem2 = new Label(parent, SWT.NONE);
        tableItem2.setText(Resources.getMessage("NodeFilterView.6")); //$NON-NLS-1$
        anonymous = new Combo(parent, SWT.BORDER);
        anonymous.add(Resources.getMessage("NodeFilterView.7")); //$NON-NLS-1$
        anonymous.add(Resources.getMessage("NodeFilterView.8")); //$NON-NLS-1$
        anonymous.pack();
        anonymous.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        anonymous.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                if (filter != null) {
                    if (anonymous.getSelectionIndex() == 0) {
                        filter.allowAnonymous();
                    } else {
                        filter.disallowAnonymous();
                    }
                    controller.update(new ModelEvent(outer,
                                                     ModelPart.FILTER,
                                                     filter));
                }
            }
        });

        final Label tableItem3 = new Label(parent, SWT.NONE);
        tableItem3.setText(Resources.getMessage("NodeFilterView.9")); //$NON-NLS-1$
        notanonymous = new Combo(parent, SWT.BORDER);
        notanonymous.add(Resources.getMessage("NodeFilterView.10")); //$NON-NLS-1$
        notanonymous.add(Resources.getMessage("NodeFilterView.11")); //$NON-NLS-1$
        notanonymous.pack();
        notanonymous.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        notanonymous.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                if (filter != null) {
                    if (notanonymous.getSelectionIndex() == 0) {
                        filter.allowNonAnonymous();
                    } else {
                        filter.disallowNonAnonymous();
                    }
                    controller.update(new ModelEvent(outer,
                                                     ModelPart.FILTER,
                                                     filter));
                }
            }
        });

        final Label tableItem4 = new Label(parent, SWT.NONE);
        tableItem4.setText(Resources.getMessage("NodeFilterView.12")); //$NON-NLS-1$
        unknown = new Combo(parent, SWT.BORDER);
        unknown.add(Resources.getMessage("NodeFilterView.13")); //$NON-NLS-1$
        unknown.add(Resources.getMessage("NodeFilterView.14")); //$NON-NLS-1$
        unknown.pack();
        unknown.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        unknown.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                if (filter != null) {
                    if (unknown.getSelectionIndex() == 0) {
                        filter.allowUnknown();
                    } else {
                        filter.disallowUnknown();
                    }
                    controller.update(new ModelEvent(outer,
                                                     ModelPart.FILTER,
                                                     filter));
                }
            }
        });

        final Label tableItem5 = new Label(parent, SWT.NONE);
        tableItem5.setText(Resources.getMessage("NodeFilterView.15")); //$NON-NLS-1$
        min = new Scale(parent, SWT.HORIZONTAL);
        min.setMaximum(SCALE_MAX_VALUE);
        min.setMinimum(0);
        min.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        min.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                if (filter != null) {
                    
                    double minLoss = intToInformationLoss(min.getSelection());
                    double maxLoss = filter.getAllowedMaxInformationLoss();
                    filter.allowInformationLoss(minLoss, maxLoss);
                    controller.update(new ModelEvent(outer,
                                                     ModelPart.FILTER,
                                                     filter));
                }
            }
        });

        final Label tableItem6 = new Label(parent, SWT.NONE);
        tableItem6.setText(Resources.getMessage("NodeFilterView.16")); //$NON-NLS-1$
        max = new Scale(parent, SWT.HORIZONTAL);
        max.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        max.setMaximum(SCALE_MAX_VALUE);
        max.setMinimum(0);
        max.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                if (filter != null) {
                    filter.allowInformationLoss(filter.getAllowedMinInformationLoss(),
                                                intToInformationLoss(max.getSelection()));
                    controller.update(new ModelEvent(outer,
                                                     ModelPart.FILTER,
                                                     filter));
                }
            }
        });
    }

    /**
     * Initializes the generalization table
     * 
     * @param string
     */
    private void createGeneralization(final String attribute) {

        generalization.removeAll();

        final int max = result.getLattice()
                              .getTop()
                              .getGeneralization(attribute);
        selectedDimension = result.getLattice()
                                  .getTop()
                                  .getDimension(attribute);

        for (int i = 0; i <= max; i++) {
            final TableItem item = new TableItem(generalization, SWT.BORDER);
            item.setText(0, String.valueOf(i));
            item.setImage(0,
                          getImage(result.getLattice(), selectedDimension, i));
            if (filter.isAllowedGeneralization(selectedDimension, i)) {
                item.setChecked(true);
            } else {
                item.setChecked(false);
            }
        }
    }

    /**
     * Returns an image representing the distribution of anonymous and non-anonymous nodes
     * on the given level of the lattice
     * @param lattice
     * @param dimension
     * @param level
     * @return
     */
    private Image getImage(final ARXLattice lattice,
                           final int dimension,
                           final int level) {

        boolean anonymous = false;
        boolean nonanonymous = false;

        for (final ARXNode[] lvl : lattice.getLevels()) {
            for (final ARXNode node : lvl) {
                if (node.getTransformation()[dimension] == level) {
                    if (node.isAnonymous() == Anonymity.ANONYMOUS) {
                        anonymous = true;
                    } else {
                        nonanonymous = true;
                    }
                }
                if (anonymous && nonanonymous) {
                    break;
                }
            }
            if (anonymous && nonanonymous) {
                break;
            }
        }

        if (anonymous && !nonanonymous) {
            return IMG_GREEN;
        } else if (anonymous && nonanonymous) {
            return IMG_ORANGE;
        } else {
            return IMG_RED;
        }
    }

    /**
     * Converter TODO: Seems to not work
     * 
     * @param value
     * @return
     */
    private int informationLossToInt(final double value) {

        // Relative
        double min = result.getLattice().getBottom().getMinimumInformationLoss().getValue();
        double max = result.getLattice().getTop().getMaximumInformationLoss().getValue();
        double val = (value - min) / (max - min);
        
        // Scaled with integer.max
        val = val * SCALE_MAX_VALUE;
        
        // Return
        return (int) val;
    }

    /**
     * Initializes the view
     * @param result
     * @param nodeFilter
     */
    private void initialize(final ARXResult result,
                            final ModelNodeFilter nodeFilter) {

        // Reset
        reset();
        
        // Return if there is no lattice
        if (result==null || result.getLattice() == null) return;

        // Reset filter
        maxlevels = result.getLattice().getTop().getTransformation();
        if (nodeFilter == null) {
            filter = new ModelNodeFilter(maxlevels, model.getInitialNodesInViewer());
            filter.initialize(result);
        } else {
            filter = nodeFilter;
        }
        this.result = result;

        // Attr
        for (final String attr : result.getLattice()
                                       .getBottom()
                                       .getQuasiIdentifyingAttributes()) {
            attribute.add(attr);
        }
        attribute.select(0);
        attribute.setEnabled(true);

        // Anonymous
        if (filter.isAllowedAnonymous()) {
            anonymous.select(0);
        } else {
            anonymous.select(1);
        }
        anonymous.setEnabled(true);

        // NotAnonymous
        if (filter.isAllowedNonAnonymous()) {
            notanonymous.select(0);
        } else {
            notanonymous.select(1);
        }
        notanonymous.setEnabled(true);

        // Unknown
        if (filter.isAllowedUnknown()) {
            unknown.select(0);
        } else {
            unknown.select(1);
        }
        unknown.setEnabled(true);

        // Min and max
        min.setMinimum(informationLossToInt(result.getLattice()
                                                  .getBottom()
                                                  .getMinimumInformationLoss()
                                                  .getValue()));
        max.setMaximum(informationLossToInt(result.getLattice()
                                                  .getTop()
                                                  .getMaximumInformationLoss()
                                                  .getValue()));
        min.setSelection(informationLossToInt(filter.getAllowedMinInformationLoss()));
        max.setSelection(informationLossToInt(filter.getAllowedMaxInformationLoss()));
        min.setEnabled(true);
        max.setEnabled(true);

        generalization.removeAll();
        createGeneralization(result.getLattice()
                                   .getBottom()
                                   .getQuasiIdentifyingAttributes()[attribute.getSelectionIndex()]);
        generalization.setEnabled(true);

        if (model != null) {
            model.setNodeFilter(filter);
            controller.update(new ModelEvent(this, ModelPart.FILTER, filter));
        }
    }

    /**
     * Converter 
     * TODO: Seems to not work
     * 
     * @param value
     * @return
     */
    private double intToInformationLoss(final int value) {

        double min = result.getLattice().getBottom().getMinimumInformationLoss().getValue();
        double max = result.getLattice().getTop().getMaximumInformationLoss().getValue();
        
        // Corner cases
        if (value == 0) {
            return min;
        } else if (value >= SCALE_MAX_VALUE - 1) { 
            return max; 
        }

        // In relation to integer.max
        double val = min + (((double) value / (double) SCALE_MAX_VALUE) * (max - min));

        // Return
        return val;
    }
}
