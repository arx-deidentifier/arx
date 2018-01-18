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
package org.deidentifier.arx.gui.view.impl.common;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.nebula.visualization.xygraph.linearscale.Range;
import org.eclipse.nebula.visualization.xygraph.util.XYGraphMediaFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

/**
 * A risk monitor
 * 
 * @author Fabian Prasser
 */
public class ComponentRiskMonitor {

    /** View */
    private final Composite            root;
    /** View */
    private final ComponentMeterFigure meter;
    /** View */
    private final CLabel               caption;
    /** View */
    private final CLabel               label;
    /** View */
    private double                     threshold = 0d;
    /** View */
    private double                     risk = 0d;
    /** View */
    private final Image                imageLow;
    /** View */
    private final Image                imageHigh;

    /**
     * Creates a new instance
     * @param parent
     * @param controller
     * @param text
     * @param shortText
     */
    public ComponentRiskMonitor(final Composite parent, 
                                final Controller controller,
                                final String text, 
                                final String shortText) {
        
        // Images
        imageLow = controller.getResources().getManagedImage("bullet_green.png"); //$NON-NLS-1$
        imageHigh = controller.getResources().getManagedImage("bullet_red.png"); //$NON-NLS-1$
        
        // Layout
        GridLayout layout = SWTUtil.createGridLayout(1);
        layout.marginHeight = 0;
        layout.marginTop = 0;
        layout.marginBottom = 0;
        layout.verticalSpacing = 0;
        
        // Root
        this.root = new Composite(parent, SWT.NONE);
        this.root.setLayout(layout);
        this.root.setToolTipText(text);
        
        // Caption
        this.caption = new CLabel(root, SWT.CENTER);
        this.caption.setText(shortText);
        this.caption.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        this.caption.setToolTipText(text);
        this.caption.setImage(imageHigh);
        SWTUtil.changeFont(caption, SWT.BOLD);
        
        // Content
        Composite content = new Composite(root, SWT.NONE);
        content.setLayoutData(SWTUtil.createFillGridData());
        content.setToolTipText(text);

        // Create meter
        Canvas canvas = new Canvas(content, SWT.DOUBLE_BUFFERED);
        canvas.setToolTipText(text);
        this.meter = new ComponentMeterFigure();
        this.meter.setNeedleColor(XYGraphMediaFactory.getInstance().getColor(0, 0, 0));
        this.meter.setValueLabelVisibility(true);
        this.meter.setRange(new Range(0, 100));
        this.meter.setLoLevel(0);
        this.meter.setLoColor(XYGraphMediaFactory.getInstance().getColor(0, 150, 0));
        this.meter.setLoloLevel(25);
        this.meter.setLoloColor(XYGraphMediaFactory.getInstance().getColor(255, 255, 0));
        this.meter.setHiLevel(50);
        this.meter.setHiColor(XYGraphMediaFactory.getInstance().getColor(255, 200, 25));
        this.meter.setHihiLevel(100);
        this.meter.setHihiColor(XYGraphMediaFactory.getInstance().getColor(255, 0, 0));
        this.meter.setMajorTickMarkStepHint(50);
        LightweightSystem lws = new LightweightSystem(canvas);
        lws.setContents(this.meter);
        
        // Create label
        label = new CLabel(content, SWT.CENTER);
        label.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        label.setToolTipText(text);
        
        // Create responsive layout
        new ComponentResponsiveLayout(content, 100, 50, canvas, label);
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
    public void setRisk(double value) {
        if (value < 0d) {
            value = 0d;
        }
        if (value > 1d) {
            value = 1d;
        }
        this.risk = value;
        String text = SWTUtil.getPrettyString(value * 100d) + "%";
        meter.setValue(value * 100d);
        label.setText(text);
        label.setToolTipText(text);
        if (this.risk > this.threshold) {
            caption.setImage(imageHigh);
        } else {
            caption.setImage(imageLow);
        }
    }
    
    /**
     * Sets the risk threshold
     * @param value
     */
    public void setThreshold(double value) {
        if (value < 0d) {
            value = 0d;
        }
        if (value > 1d) {
            value = 1d;
        }
        this.threshold = value;
        if (this.risk > this.threshold) {
            caption.setImage(imageHigh);
        } else {
            caption.setImage(imageLow);
        }
    }
}
