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

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.analyze.AnalysisContext.Context;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import de.linearbits.jhc.JHC;
import de.linearbits.jhc.JHCConfiguration;
import de.linearbits.jhc.JHCGradient;
import de.linearbits.jhc.JHCLayout;

/**
 * This class displays a contingency table as a heatmap
 * @author Fabian Prasser
 */
public class ViewDensity implements IView {

    /** Static stuff */
    private static final int  MAX_SIZE = 500;
    /** Internal stuff */
    private AnalysisContext   context  = new AnalysisContext();
    /** Internal stuff */
    private final Controller  controller;
    /** Internal stuff */
    private Model             model;
    /** Internal stuff */
    private final ModelPart   reset;
    /** Internal stuff */
    private final ModelPart   target;
    /** The heat map widget */
    private final JHC         jhc;
    /** The heat map configuration */
    private final JHCGradient gradient;
    /** The heat map configuration */
    private final JHCLayout   layout;

	/**
	 * Creates a new density plot
	 * @param parent
	 * @param controller
	 * @param target
	 * @param reset
	 */
    public ViewDensity(final Composite parent,
                       final Controller controller,
                       final ModelPart target,
                       final ModelPart reset) {

        // Register
        controller.addListener(ModelPart.SELECTED_ATTRIBUTE, this);
        controller.addListener(ModelPart.MODEL, this);
        controller.addListener(ModelPart.VIEW_CONFIG, this);
        controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        controller.addListener(ModelPart.VISUALIZATION, this);
        controller.addListener(target, this);
        this.controller = controller;
        if (reset != null) {
            controller.addListener(reset, this);
        }
        this.reset = reset;
        this.target = target;

        // Create controls
        parent.setLayout(new FillLayout());
        this.jhc = new JHC(parent, SWT.NULL);
        this.gradient = JHCGradient.GRADIENT_HEAT;
        this.layout = new JHCLayout();
        
        // Update font settings
        Font font = jhc.getFont();
        if (font != null) {
            FontData[] fd = font.getFontData();
            if (fd != null && fd.length>0){
                fd[0].setHeight(8);
                jhc.setFont(new Font(jhc.getDisplay(), fd[0]));
            }
        }
    }
    

    @Override
    public void dispose() {
        controller.removeListener(this);
    }

    @Override
    public void reset() {
        jhc.setData(null, new JHCConfiguration("", "", MAX_SIZE, MAX_SIZE, gradient, layout));
    }
    
    @Override
    public void update(final ModelEvent event) {

        if (event.part == ModelPart.OUTPUT) {
            update();
        }

        if (event.part == reset) {
            reset();
            
        } else if (event.part == ModelPart.MODEL) {
            this.model = (Model)event.data;
            this.model.resetAttributePair();
            this.context.setModel(model);
            this.context.setTarget(target);
            reset();

        } else if (event.part == target ||
                   event.part == ModelPart.SELECTED_ATTRIBUTE ||
                   event.part == ModelPart.ATTRIBUTE_TYPE ||
                   event.part == ModelPart.VIEW_CONFIG ||
                   event.part == ModelPart.VISUALIZATION) {
            
            update();
        }
    }

    /**
     * Redraws the plot
     */
    private void update() {

        if (model != null && !model.isVisualizationEnabled()) {
            reset();
            return;
        }

        if (model != null &&
            model.getAttributePair() != null &&
            model.getAttributePair()[0] != null &&
            model.getAttributePair()[1] != null) {

            // Obtain the right handle
            Context context = this.context.getContext();
            if (context==null) {
                reset();
                return;
            }
            DataHandle handle = context.handle;
            if (handle == null) {
                reset();
                return;
            }
            
            String attribute1 = model.getAttributePair()[0];
            String attribute2 = model.getAttributePair()[1];
            int column1 = handle.getColumnIndexOf(attribute1);
            int column2 = handle.getColumnIndexOf(attribute2);
            jhc.setData(new DensityData(handle, column1, column2), new JHCConfiguration(attribute1, attribute2, MAX_SIZE, MAX_SIZE, gradient, layout));
            
        } else {
            reset();
            return; 
        }
    }
}
