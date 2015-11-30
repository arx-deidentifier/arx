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

import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.nebula.visualization.widgets.figures.ScaledSliderFigure;
import org.eclipse.nebula.visualization.xygraph.linearscale.Range;
import org.eclipse.nebula.visualization.xygraph.util.XYGraphMediaFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * A risk slider
 * 
 * @author Fabian Prasser
 */
public class ComponentRiskSlider {

    /** View */
    private final Composite         root;
    /** View */
    private final ScaledSliderFigure bar;

    /**
     * Creates a new instance
     * @param parent
     * @param shortText 
     */
    public ComponentRiskSlider(final Composite parent) {
        
        // Layout
        GridLayout layout = SWTUtil.createGridLayout(1);
        layout.marginHeight = 0;
        layout.marginTop = 0;
        layout.marginBottom = 0;
        layout.verticalSpacing = 0;
        
        // Root
        this.root = new Composite(parent, SWT.NONE);
        this.root.setLayout(layout);
        
        // Label 1
        Label label = new Label(root, SWT.CENTER);
        label.setText("Threshold");
        GridData labeldata = SWTUtil.createFillHorizontallyGridData(true);
        labeldata.horizontalAlignment = SWT.CENTER;
        label.setLayoutData(labeldata);
        SWTUtil.createNewFont(label, SWT.BOLD);
        
        // Label 2
        label = new Label(root, SWT.CENTER);
        label.setText("High");
        label.setLayoutData(labeldata);

        // Create canvas
        Canvas canvas = new Canvas(root, SWT.DOUBLE_BUFFERED);
        canvas.setLayoutData(SWTUtil.createFillGridData());
        
        // Slider
        final LightweightSystem lws = new LightweightSystem(canvas);     
        this.bar = new ScaledSliderFigure();
        this.bar.setFillColor(XYGraphMediaFactory.getInstance().getColor(0, 255, 0));
        this.bar.setRange(new Range(0, 100));
        this.bar.setLoLevel(0);
        this.bar.setLoColor(XYGraphMediaFactory.getInstance().getColor(0, 150, 0));
        this.bar.setLoloLevel(25);
        this.bar.setLoloColor(XYGraphMediaFactory.getInstance().getColor(255, 255, 0));
        this.bar.setHiLevel(50);
        this.bar.setHiColor(XYGraphMediaFactory.getInstance().getColor(255, 200, 25));
        this.bar.setHihiLevel(100);
        this.bar.setHihiColor(XYGraphMediaFactory.getInstance().getColor(255, 0, 0));
        this.bar.setMajorTickMarkStepHint(50);
        this.bar.setHorizontal(false);
        this.bar.setShowHi(false);
        this.bar.setShowHihi(false);
        this.bar.setShowLo(false);
        this.bar.setShowLolo(false);
        this.bar.setShowScale(false);
        lws.setContents(bar);
        
        // Label 3
        label = new Label(root, SWT.CENTER);
        label.setText("Low");
        label.setLayoutData(labeldata);
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
        this.bar.setValue(value * 100d);
    }
}
