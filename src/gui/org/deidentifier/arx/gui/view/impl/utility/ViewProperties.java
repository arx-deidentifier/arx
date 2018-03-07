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

package org.deidentifier.arx.gui.view.impl.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.deidentifier.arx.ARXLattice;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContext;
import org.deidentifier.arx.metric.InformationLoss;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * This view displays basic properties about input or output data.
 *
 * @author Fabian Prasser
 */
public abstract class ViewProperties implements IView, ViewStatisticsBasic {

    /**
     * A class for properties displayed in the tree view.
     *
     * @author Fabian Prasser
     */
    protected class Property {
        
        /**  Parent */
        public Property       parent;
        
        /**  Children */
        public List<Property> children = new ArrayList<Property>();
        
        /**  Label */
        public String         property;
        
        /**  Values */
        public String[]       values;

        /**
         * Creates a new property
         *
         * @param father
         * @param property
         * @param values
         */
        public Property(final Property father,
                        final String property,
                        final String[] values) {
            father.add(this);
            this.property = property;
            this.values = values;
        }

        /**
         * Creates a new property
         *
         * @param property
         * @param values
         */
        public Property(final String property, final String[] values) {
            roots.add(this);
            this.property = property;
            this.values = values;
        }

        /**
         * Adds a child
         *
         * @param p
         */
        public void add(final Property p) {
            children.add(p);
            p.parent = this;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((children == null) ? 0 : children.hashCode());
            result = prime * result + ((property == null) ? 0 : property.hashCode());
            result = prime * result + Arrays.hashCode(values);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            Property other = (Property) obj;
            if (!getOuterType().equals(other.getOuterType())) return false;
            if (children == null) {
                if (other.children != null) return false;
            } else if (!children.equals(other.children)) return false;
            if (property == null) {
                if (other.property != null) return false;
            } else if (!property.equals(other.property)) return false;
            if (!Arrays.equals(values, other.values)) return false;
            return true;
        }

        /**
         * Helper
         * @return
         */
        private ViewProperties getOuterType() {
            return ViewProperties.this;
        }
    }

    /** Internal stuff. */
    protected final List<Property> roots   = new ArrayList<Property>();

    /** Internal stuff. */
    protected final Composite      root;

    /** Internal stuff. */
    protected Model                model;

    /** Internal stuff. */
    protected final Controller     controller;

    /** Internal stuff. */
    protected TreeViewer           treeViewer;

    /** Internal stuff. */
    private final ModelPart        reset;

    /** Internal stuff. */
    private final AnalysisContext  context = new AnalysisContext();
    
    /**
     * Constructor.
     *
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
        controller.addListener(ModelPart.SELECTED_VIEW_CONFIG, this);
        controller.addListener(ModelPart.SELECTED_ATTRIBUTE, this);
        controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        controller.addListener(ModelPart.METRIC, this);
        controller.addListener(ModelPart.ATTRIBUTE_WEIGHT, this);
        controller.addListener(ModelPart.GS_FACTOR, this);
        controller.addListener(ModelPart.MAX_OUTLIERS, this);
        controller.addListener(ModelPart.DATA_TYPE, this);
        controller.addListener(ModelPart.COST_BENEFIT_MODEL, this);
        controller.addListener(ModelPart.MODEL, this);
        controller.addListener(target, this);
        if (reset != null) {
            controller.addListener(reset, this);
        }
        this.reset = reset;
        this.root = parent;
        this.controller = controller;
        this.context.setTarget(target);
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    @Override
    public Composite getParent() {
        return this.root;
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

        if (event.part == reset) {
            this.reset();
        } else if (event.part == ModelPart.MODEL) {
            this.model = (Model) event.data;
            this.context.setModel(model);
            reset();
        } else {
            SWTUtil.enable(root);
            this.doUpdate(event.part);
        }
    }

    /**
     * Converts an information loss into a relative value in percent
     * TODO: Code duplicate from NodePropertiesView.
     *
     * @param infoLoss
     * @param result
     * @return
     */
    protected double asRelativeValue(final InformationLoss<?> infoLoss, final ARXResult result) {

        ARXLattice lattice = model.getProcessStatistics().isLocalTransformation() ? 
                             model.getProcessStatistics().getLattice() : result.getLattice();
        
        return infoLoss.relativeTo(lattice.getLowestScore(),  lattice.getHighestScore()) * 100d;
    }
    
    /**
     * Returns the context.
     *
     * @return
     */
    protected AnalysisContext getContext(){
        return context;
    }

    /**
     * Refreshes the tree
     */
    protected void refresh() {
        treeViewer.refresh();
        treeViewer.expandAll();
        for (TreeColumn tc : treeViewer.getTree().getColumns()) {
            tc.pack();
        }
    }
    
    /**
     * Implement this to update the view.
     * @param part 
     */
    protected abstract void doUpdate(ModelPart part);
}
