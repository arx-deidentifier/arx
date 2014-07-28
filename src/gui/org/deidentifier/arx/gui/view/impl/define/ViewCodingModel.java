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

package org.deidentifier.arx.gui.view.impl.define;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.metric.Metric;
import org.deidentifier.arx.metric.MetricNDS;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Scale;

/**
 * This class allows to configure the coding model
 * @author Fabian Prasser
 */
public class ViewCodingModel implements IView {
    
    private final Color COLOR_MEDIUM;
    private final Color COLOR_LIGHT;
    private final Color COLOR_DARK;

    private static final int MINIMUM    = 0;
    private static final int MAXIMUM    = 1000;

    private Controller       controller = null;
    private Model            model      = null;

    private final Scale      slider;
    private final Composite  root;

    /**
     * Creates a new instance
     * @param parent
     * @param controller
     */
    public ViewCodingModel(final Composite parent, final Controller controller) {

        // Colors
        COLOR_LIGHT = new Color(parent.getDisplay(), 230, 230, 230);
        COLOR_MEDIUM = new Color(parent.getDisplay(), 200, 200, 200);
        COLOR_DARK = new Color(parent.getDisplay(), 128, 128, 128);
        final Color COLOR_TEXT = parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
        
        // Register
        this.controller = controller;
        this.controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        this.controller.addListener(ModelPart.MODEL, this);
        this.controller.addListener(ModelPart.INPUT, this);
        
        this.root = new Composite(parent, SWT.NONE);
        this.root.setLayout(GridLayoutFactory.swtDefaults().numColumns(1).margins(3, 3).create());
        
        // Triangle view
        final int WIDTH = 3;
        final int OFFSET = 10;
        final Canvas canvas = new Canvas(root, SWT.DOUBLE_BUFFERED);
        canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        canvas.addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent e) {
                
                final Color COLOR_BACKGROUND = root.getBackground();
                Point size = canvas.getSize();
                int width = size.x;
                int height = size.y;
                int x = (int) Math.round(getSuppressionWeight() * (double) (width - OFFSET / 2 - WIDTH * 2 + 2));

                int[] left = new int[] {0, 0, 
                                        width-OFFSET/2, 0,
                                        0, height - OFFSET};
                int[] right = new int[] {width-OFFSET/2, OFFSET/2,
                                         width-OFFSET/2, height - OFFSET/2,
                                         0, height - OFFSET/2};
                int[] center = new int[] {left[2], left[3],
                                          left[4], left[5],
                                          right[4], right[5],
                                          right[0], right[1]};
                
                e.gc.setForeground(COLOR_DARK);
                e.gc.setBackground(COLOR_BACKGROUND);
                e.gc.fillRectangle(0, 0, width, height);

                e.gc.setBackground(COLOR_MEDIUM);
                e.gc.fillPolygon(left);

                e.gc.setForeground(COLOR_TEXT);
                e.gc.drawText("Suppression", OFFSET, OFFSET);

                e.gc.setBackground(COLOR_LIGHT);
                e.gc.fillPolygon(right);

                final String string = "Generalization";
                e.gc.setForeground(COLOR_TEXT);
                Point extent = e.gc.textExtent(string);
                e.gc.drawText(string, width - OFFSET - extent.x, height - OFFSET - extent.y);

                e.gc.setForeground(COLOR_DARK);
                e.gc.setLineWidth(3);
                e.gc.drawLine(WIDTH + x - 1, 0, WIDTH + x - 1, height - OFFSET / 2);

                e.gc.setBackground(COLOR_BACKGROUND);
                e.gc.fillPolygon(center);

                e.gc.setForeground(COLOR_DARK);
                e.gc.setLineWidth(1);
                e.gc.drawPolygon(left);
                e.gc.drawPolygon(right);

            }
        });
        
        // Slider
        Composite sliderBase = new Composite(this.root, SWT.NONE);
        sliderBase.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        sliderBase.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
        
        slider = new Scale(sliderBase, SWT.HORIZONTAL);
        slider.setMinimum(MINIMUM);
        slider.setMaximum(MAXIMUM);
        this.setSuppressionWeight(0.5d);
        slider.setLayoutData(GridDataFactory.fillDefaults()
                                            .grab(true, false)
                                            .create());
        slider.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent arg0) {
                if (model != null && model.getInputConfig() != null) {
                    canvas.redraw();
                    double weight = getSuppressionWeight();
                    model.getInputConfig().setSuppressionWeight(weight);
                    if (model.getInputConfig().getMetric() instanceof MetricNDS) {
                        model.getInputConfig().setMetric(Metric.createNDSMetric(weight));
                    }
                }
            }
        });
        
        Button button = new Button(sliderBase, SWT.PUSH);
        button.setLayoutData(GridDataFactory.fillDefaults().grab(false, false).align(SWT.LEFT, SWT.CENTER).create());
        button.setText("Reset");
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
                setSuppressionWeight(0.5d);
                if (model != null && model.getInputConfig() != null) {
                    model.getInputConfig().setSuppressionWeight(0.5d);
                    if (model.getInputConfig().getMetric() instanceof MetricNDS) {
                        model.getInputConfig().setMetric(Metric.createNDSMetric(0.5d));
                    }
                }
            }
        });
                
        root.pack();
    }

    /**
     * Sets the current suppression weight
     * @param d
     */
    private void setSuppressionWeight(double d) {
        int value = (int)(MINIMUM + d * (double)(MAXIMUM - MINIMUM));
        slider.setSelection(value);
    }

    /**
     * Returns the current suppression weight
     * @return
     */
    private double getSuppressionWeight() {
        return ((double)slider.getSelection() - MINIMUM) / (double)(MAXIMUM - MINIMUM);
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
        root.dispose();
        COLOR_LIGHT.dispose();
        COLOR_MEDIUM.dispose();
        COLOR_DARK.dispose();
    }

    @Override
    public void reset() {
        root.setRedraw(false);
        setSuppressionWeight(0.5d);
        root.setRedraw(true);
    }

    @Override
    public void update(ModelEvent event) {
        if (event.part == ModelPart.MODEL) {
            this.model = (Model)event.data;
            if (model.getInputConfig() != null) {
                this.setSuppressionWeight(this.model.getInputConfig().getSuppressionWeight());
            }
        } 
    }
}
