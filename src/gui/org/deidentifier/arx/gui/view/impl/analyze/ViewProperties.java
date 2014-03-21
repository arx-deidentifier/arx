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

package org.deidentifier.arx.gui.view.impl.analyze;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXLattice.Anonymity;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.DataTypeWithFormat;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.criteria.DistinctLDiversity;
import org.deidentifier.arx.criteria.EntropyLDiversity;
import org.deidentifier.arx.criteria.EqualDistanceTCloseness;
import org.deidentifier.arx.criteria.HierarchicalDistanceTCloseness;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.criteria.RecursiveCLDiversity;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelConfiguration;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.metric.InformationLoss;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * This view displays basic properties about input or output data
 * TODO: Split into at least two views
 * 
 * @author Fabian Prasser
 */
public abstract class ViewProperties implements IView {

    /**
     * A class for properties displayed in the tree view
     * @author Fabian Prasser
     */
    protected class Property {
        public Property       parent;
        public List<Property> children = new ArrayList<Property>();
        public String         property;
        public String[]       values;

        public Property(final Property father,
                        final String property,
                        final String[] values) {
            father.add(this);
            this.property = property;
            this.values = values;
        }

        public Property(final String property, final String[] values) {
            roots.add(this);
            this.property = property;
            this.values = values;
        }

        public void add(final Property p) {
            children.add(p);
            p.parent = this;
        }
    }
    protected final List<Property> roots  = new ArrayList<Property>();
    protected final Composite      root;
    protected final NumberFormat   format = new DecimalFormat("##0.000"); //$NON-NLS-1$
    
    protected Model                model;
    protected TreeViewer           treeViewer;
    
    private final Controller     controller;
    private final ModelPart      target;
    private final ModelPart      reset;
    
    /**
     * Constructor
     * @param parent
     * @param controller
     * @param target
     * @param reset
     */
    protected ViewProperties(final Composite parent,
                             final Controller controller,
                             final ModelPart target,
                             final ModelPart reset) {

        // Register
        controller.addListener(ModelPart.SELECTED_ATTRIBUTE, this);
        controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        controller.addListener(ModelPart.METRIC, this);
        controller.addListener(ModelPart.MAX_OUTLIERS, this);
        controller.addListener(ModelPart.DATA_TYPE, this);
        controller.addListener(ModelPart.MODEL, this);
        controller.addListener(target, this);
        this.controller = controller;
        if (reset != null) {
            controller.addListener(reset, this);
        }
        this.reset = reset;
        this.target = target;
        this.root = parent;
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    @Override
    public void reset() {

        root.setRedraw(false);
        roots.clear();
        treeViewer.refresh();
        SWTUtil.disable(root);
        root.setRedraw(true);
    }

    @Override
    public void update(final ModelEvent event) {

        SWTUtil.enable(root);
        update();

        if (event.part == reset) {
            reset();
        } else if (event.part == target) {
            SWTUtil.enable(root);
            update();
        } else if (event.part == ModelPart.MODEL) {
            model = (Model) event.data;
            reset();
        }
    }

    /**
     * Implement this to update the view
     */
    protected abstract void update();

    /**
     * Converts an information loss into a relative value in percent 
     * TODO: Code duplicate from NodePropertiesView
     * 
     * @param infoLoss
     * @return
     */
    protected double asRelativeValue(final InformationLoss infoLoss,
                                   final ARXResult result) {
        return ((infoLoss.getValue() - result.getLattice()
                                             .getBottom()
                                             .getMinimumInformationLoss()
                                             .getValue()) / result.getLattice()
                                                                  .getTop()
                                                                  .getMaximumInformationLoss()
                                                                  .getValue()) * 100d;
    }
}
