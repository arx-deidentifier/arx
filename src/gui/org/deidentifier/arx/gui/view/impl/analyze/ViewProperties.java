/*
 * ARX: Powerful Data Anonymization
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
import java.util.List;

import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.metric.InformationLoss;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;

/**
 * This view displays basic properties about input or output data.
 *
 * @author Fabian Prasser
 */
public abstract class ViewProperties implements IView {

    /**
     * A class for properties displayed in the tree view.
     *
     * @author Fabian Prasser
     */
    protected class Property {
        
        /**  TODO */
        public Property       parent;
        
        /**  TODO */
        public List<Property> children = new ArrayList<Property>();
        
        /**  TODO */
        public String         property;
        
        /**  TODO */
        public String[]       values;

        /**
         * 
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
         * 
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
         * 
         *
         * @param p
         */
        public void add(final Property p) {
            children.add(p);
            p.parent = this;
        }
    }

    /** Internal stuff. */
    protected final List<Property> roots  = new ArrayList<Property>();
    
    /** Internal stuff. */
    protected final Composite      root;
    
    /** Internal stuff. */
    protected final NumberFormat   format = new DecimalFormat("##0.000"); //$NON-NLS-1$
    
    /** Internal stuff. */
    protected Model                model;
    
    /** Internal stuff. */
    protected TreeViewer           treeViewer;
    
    /** Internal stuff. */
    private final Controller       controller;
    
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
        controller.addListener(ModelPart.VIEW_CONFIG, this);
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
        this.root = parent;
        this.context.setTarget(target);
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

        root.setRedraw(false);
        roots.clear();
        treeViewer.refresh();
        SWTUtil.disable(root);
        root.setRedraw(true);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.gui.view.def.IView#update(org.deidentifier.arx.gui.model.ModelEvent)
     */
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
            this.update();
        }
    }

    /**
     * Implement this to update the view.
     */
    protected abstract void update();

    /**
     * Converts an information loss into a relative value in percent
     * TODO: Code duplicate from NodePropertiesView.
     *
     * @param infoLoss
     * @param result
     * @return
     */
    protected double asRelativeValue(final InformationLoss<?> infoLoss, final ARXResult result) {
        return infoLoss.relativeTo(model.getResult().getLattice().getMinimumInformationLoss(), 
                                   model.getResult().getLattice().getMaximumInformationLoss()) * 100d;
    }
    
    /**
     * Returns the context.
     *
     * @return
     */
    protected AnalysisContext getContext(){
        return context;
    }
}
