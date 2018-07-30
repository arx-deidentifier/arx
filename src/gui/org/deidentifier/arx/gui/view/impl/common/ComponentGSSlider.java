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

import org.deidentifier.arx.gui.resources.Resources;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Scale;

/**
 * This component allows to configure the coding model.
 *
 * @author Fabian Prasser
 */
public class ComponentGSSlider {

    /** Color */
    private final Color      COLOR_MEDIUM;

    /** Color */
    private final Color      COLOR_LIGHT;

    /** Color */
    private final Color      COLOR_DARK;

    /** Constant */
    private static final int MINIMUM = 0;

    /** Constant */
    private static final int MAXIMUM = 1000;

    /** Widget */
    private final Scale      slider;

    /** Widget */
    private final Composite  root;

    /** Widget */
    private final Canvas     canvas;

    /** Button */
    private final Button     button;

    /**
     * Creates a new instance.
     *
     * @param parent
     */
    public ComponentGSSlider(final Composite parent) {

        // Colors
        COLOR_LIGHT = new Color(parent.getDisplay(), 230, 230, 230);
        COLOR_MEDIUM = new Color(parent.getDisplay(), 200, 200, 200);
        COLOR_DARK = new Color(parent.getDisplay(), 128, 128, 128);
        final Color COLOR_TEXT = parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
        
        this.root = new Composite(parent, SWT.NONE);
        this.root.setLayout(GridLayoutFactory.swtDefaults().numColumns(1).margins(3, 3).create());
        
        this.root.addDisposeListener(new DisposeListener(){
            @Override
            public void widgetDisposed(DisposeEvent arg0) {
                if (COLOR_LIGHT != null && !COLOR_LIGHT.isDisposed()) COLOR_LIGHT.dispose();
                if (COLOR_MEDIUM != null && !COLOR_MEDIUM.isDisposed()) COLOR_MEDIUM.dispose();
                if (COLOR_DARK != null && !COLOR_DARK.isDisposed()) COLOR_DARK.dispose();
            }
        });
        
        // Triangle view
        final int WIDTH = 3;
        final int OFFSET = 10;
        this.canvas = new Canvas(root, SWT.DOUBLE_BUFFERED);
        this.canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        this.canvas.addPaintListener(new PaintListener() {
            
            @Override
            public void paintControl(PaintEvent e) {
                
                e.gc.setAdvanced(true);
                e.gc.setAntialias(SWT.ON);
                
                final Color COLOR_BACKGROUND = root.getBackground();
                final Point size = canvas.getSize();
                final int width = size.x;
                final int height = size.y;
                final int x = (int) Math.round(getSelection() * (double) (width - OFFSET / 2 - WIDTH * 2 + 2));

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
                e.gc.drawText(Resources.getMessage("ViewCodingModel.0"), OFFSET, OFFSET); //$NON-NLS-1$

                e.gc.setBackground(COLOR_LIGHT);
                e.gc.fillPolygon(right);

                final String string = Resources.getMessage("ViewCodingModel.1"); //$NON-NLS-1$
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
        slider.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
        slider.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent arg0) {
                canvas.redraw();
            }
        });
        
        // Button
        button = new Button(sliderBase, SWT.FLAT);
        button.setLayoutData(GridDataFactory.fillDefaults().grab(false, false).align(SWT.LEFT, SWT.CENTER).create());
        button.setText(Resources.getMessage("ViewCodingModel.2")); //$NON-NLS-1$
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
                setSelection(0.5d);
                canvas.redraw();
            }
        });
                
        root.pack();
        this.setSelection(0.5d);
    }
    
    /**
     * Adds a selection listener
     * @param listener
     */
    public void addSelectionListener(SelectionListener listener) {
        this.slider.addSelectionListener(listener);
        this.button.addSelectionListener(listener);
    }
    
    /**
     * Gets the selection
     * @return
     */
    public double getSelection() {
        return ((double)slider.getSelection() - MINIMUM) / (double)(MAXIMUM - MINIMUM);
    }
    
    /**
     * Sets layout data
     * @param data
     */
    public void setLayoutData(Object data) {
        this.root.setLayoutData(data);
    }
    
    /**
     * Sets the selection
     * @param selection
     */
    public void setSelection(double selection) {
        if (selection > 1d) {
            selection = 1d;
        }
        if (selection < 0d) {
            selection = 0d;
        }
        int value = (int)(MINIMUM + selection * (double)(MAXIMUM - MINIMUM));
        if (!this.root.isDisposed()) this.root.setRedraw(false);
        if (!this.slider.isDisposed()) this.slider.setSelection(value);
        if (!this.canvas.isDisposed()) this.canvas.redraw();
        if (!this.root.isDisposed()) this.root.setRedraw(true);
    }
}
