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
import org.eclipse.nebula.visualization.xygraph.linearscale.Range;
import org.eclipse.nebula.visualization.xygraph.util.XYGraphMediaFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
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
    private final CLabel               label;

    /**
     * Creates a new instance
     * @param parent
     * @param shortText 
     */
    public ComponentRiskMonitor(final Composite parent, 
                                final String text, 
                                final String shortText) {
        
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
        CLabel caption = new CLabel(root, SWT.CENTER);
        caption.setText(shortText);
        caption.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        caption.setToolTipText(text);
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
    public void setValue(double value) {
        if (value < 0d) {
            value = 0d;
        }
        if (value > 1d) {
            value = 1d;
        }
        String text = SWTUtil.getPrettyString(value * 100d) + "%";
        meter.setValue(value * 100d);
        label.setText(text);
        label.setToolTipText(text);
    }
}
