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
package org.deidentifier.arx.gui.view.impl.common;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.nebula.visualization.widgets.figures.GaugeFigure;
import org.eclipse.nebula.visualization.xygraph.linearscale.Range;
import org.eclipse.nebula.visualization.xygraph.util.XYGraphMediaFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

/**
 * A gauge widget
 * 
 * @author Fabian Prasser
 */
public class ComponentGauge {

    /** The root canvas*/
    private final Canvas root;
    /** The figure*/
    private final GaugeFigure gauge;
    
    /**
     * Creates a new instance
     * @param parent
     */
    public ComponentGauge(Composite parent) {

        // Create root
        root = new Canvas(parent, SWT.DOUBLE_BUFFERED);
        
        // Create gauge
        LightweightSystem lws = new LightweightSystem(root);
        gauge = new GaugeFigure();
        
        //Init gauge
        gauge.setBackgroundColor(XYGraphMediaFactory.getInstance().getColor(0, 0, 0));
        gauge.setForegroundColor(XYGraphMediaFactory.getInstance().getColor(255, 255, 255));
        
        // Gradient from green to red
        gauge.setRange(new Range(0, 100));
        gauge.setLoLevel(0);
        gauge.setLoColor(XYGraphMediaFactory.getInstance().getColor(0, 150, 0));
        gauge.setLoloLevel(25);
        gauge.setLoloColor(XYGraphMediaFactory.getInstance().getColor(255, 255, 0));
        gauge.setHiLevel(50);
        gauge.setHiColor(XYGraphMediaFactory.getInstance().getColor(255, 200, 25));
        gauge.setHihiLevel(100);
        gauge.setHihiColor(XYGraphMediaFactory.getInstance().getColor(255, 0, 0));
        gauge.setMajorTickMarkStepHint(50);
        
        // Set
        lws.setContents(gauge);       
    }
    
    /**
     * Sets layout data
     * @param data
     */
    public void setLayoutData(Object data) {
        this.root.setLayoutData(data);
    }
    
    /**
     * Value between 0 and 1
     * @param value
     * @return
     */
    public void setValue(double value) {
        if (value < 0d) {
            value = 0d;
        }
        if (value > 1d) {
            value = 1d;
        }
        gauge.setValue(value * 100d);
    }
}
