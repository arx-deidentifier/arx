/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

package org.deidentifier.arx.gui.view.impl.common;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;

import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.draw2d.AbstractLayout;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Polygon;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.nebula.visualization.internal.widgets.introspection.MeterIntrospector;
import org.eclipse.nebula.visualization.widgets.figureparts.RoundScale;
import org.eclipse.nebula.visualization.widgets.figureparts.RoundScaledRamp;
import org.eclipse.nebula.visualization.widgets.figures.AbstractRoundRampedFigure;
import org.eclipse.nebula.visualization.widgets.util.PointsUtil;
import org.eclipse.nebula.visualization.xygraph.util.XYGraphMediaFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

/**
 * The figure of Express Meter. Adapted for ARX by fabian.prasser@gmail.com
 * 
 * @author Xihui Chen
 * @author Fabian Prasser
 */
public class ComponentMeterFigure extends AbstractRoundRampedFigure {

    static class Needle extends Polygon {
        public Needle() {
            setBackgroundColor(XYGraphMediaFactory.getInstance().getColor(XYGraphMediaFactory.COLOR_RED));
        }
        @Override
        protected void fillShape(Graphics g) {
            g.setAntialias(SWT.ON);
            super.fillShape(g);
        }
    }
    
    class XMeterLayout extends AbstractLayout {
        
        private static final int GAP_BTW_NEEDLE_SCALE = -5;
        
        int M = 0;
        
        /** Used as a constraint for the scale. */
        public static final String SCALE = "scale";   //$NON-NLS-1$
        /** Used as a constraint for the Needle. */
        public static final String NEEDLE = "needle"; //$NON-NLS-1$
        /** Used as a constraint for the Ramp */
        public static final String RAMP = "ramp";      //$NON-NLS-1$
        /** Used as a constraint for the value label*/
        public static final String VALUE_LABEL = "valueLabel";      //$NON-NLS-1$
        
        private RoundScale scale;
        private RoundScaledRamp ramp;
        private Polygon needle;
        private Label valueLabel;
        private PointList needlePoints = new PointList(new int[] {0,0,0,0,0,0});
        
        
        public void layout(IFigure container) {
            Rectangle area = container.getClientArea(); 
            // calculate a virtual area
            
            if(scale != null && scale.isDirty()) {
                M = Math.max(FigureUtilities.getTextWidth(
                        scale.format(scale.getRange().getLower()), scale.getFont()),
                        FigureUtilities.getTextWidth(
                        scale.format(scale.getRange().getUpper()), scale.getFont()))/2;
            }
            
            int h = area.height;
            int w = area.width;         
            int offsetY = 0;
            int offsetX = 0;
            if(h > HW_RATIO * (w - 2*M)) {
                h = (int) (HW_RATIO * (w - 2*M));
                offsetY = (area.height - h) / 2;
                if (offsetY > 10) {
                    offsetY -= 10;
                } else {
                    offsetY = 0;
                }
            } else {
                offsetX = ((int) (HW_RATIO * (w - 2*M)) - h);
            }
            
       
            double r = h / (1 - Math.sin(ALPHA) / 2);
            int x = (int) (area.x - r * (1.0 - Math.cos(ALPHA)) + M);
            int y = area.y;
                        
            area = new Rectangle(x + offsetX, y + offsetY, (int)(2*r), (int)(2*r));         
            Point center = area.getCenter();
            
            if(scale != null) {             
                scale.setBounds(area);
            }
            
            if(ramp != null && ramp.isVisible()) {
                Rectangle rampBounds = area.getCopy();
                ramp.setBounds(rampBounds.shrink(area.width/4 -ramp.getRampWidth(), area.height/4 - ramp.getRampWidth()));
            }
            
            if(valueLabel != null) {
                
                Dimension labelSize = valueLabel.getPreferredSize();
                
                int lY1 = area.y + area.height/2 -(scale.getInnerRadius() - area.height/5)/2 - labelSize.height/2;
                int lY2 = container.getClientArea().height - labelSize.height + 3;
                
                valueLabel.setBounds(new Rectangle(area.x + area.width/2 - labelSize.width/2,
                        Math.min(lY1, lY2),
                        labelSize.width, labelSize.height));
            }
            
            if(needle != null && scale != null) {
                needlePoints.setPoint (new Point(center.x + area.width/4, center.y - NEEDLE_WIDTH/2 + 3), 0);
                scale.getScaleTickMarks();
                needlePoints.setPoint(new Point(center.x + scale.getInnerRadius() - GAP_BTW_NEEDLE_SCALE, center.y), 1);
                needlePoints.setPoint(new Point(center.x + area.width/4, center.y + NEEDLE_WIDTH/2 - 3), 2);
    
                double valuePosition = 360 - scale.getValuePosition(getCoercedValue(), false);
                if(maximum > minimum){
                    if(value > maximum)
                        valuePosition += 8;
                    else if(value < minimum)
                        valuePosition -=8;
                }else{
                    if(value > minimum)
                        valuePosition -= 8;
                    else if(value < maximum)
                        valuePosition +=8;
                }
                needlePoints.setPoint(PointsUtil.rotate(needlePoints.getPoint(0), valuePosition, center), 0);
                needlePoints.setPoint(PointsUtil.rotate(needlePoints.getPoint(1), valuePosition, center), 1);
                needlePoints.setPoint(PointsUtil.rotate(needlePoints.getPoint(2), valuePosition, center),2);              
                needle.setPoints(needlePoints);         
            }        
        }


        @Override
        public void setConstraint(IFigure child, Object constraint) {
            if(constraint.equals(SCALE))
                scale = (RoundScale)child;
            else if (constraint.equals(RAMP))
                ramp = (RoundScaledRamp) child;
            else if (constraint.equals(NEEDLE))
                needle = (Polygon) child;
            else if (constraint.equals(VALUE_LABEL))
                valueLabel = (Label)child;
        }


        @Override
        protected Dimension calculatePreferredSize(IFigure container, int w,
                int h) {
            Insets insets = container.getInsets();
            Dimension d = new Dimension(container.getClientArea().width, container.getClientArea().height);
            d.expand(insets.getWidth(), insets.getHeight());
            return d;
        }       
    }
    
    private final static Font DEFAULT_LABEL_FONT = XYGraphMediaFactory.getInstance().getFont(
            new FontData("Arial", 10, SWT.BOLD));
    
    private Needle needle;
    
    private Label valueLabel;
    
    private final static double SPACE_ANGLE = 45;  
    
    public static final int NEEDLE_WIDTH = 16;   
    
    public final static double ALPHA = SPACE_ANGLE * Math.PI/180;
    public final static double HW_RATIO = (1- Math.sin(ALPHA)/2)/(2*Math.cos(ALPHA));
    public ComponentMeterFigure() {
        super();
        setTransparent(false);
        scale.setScaleLineVisible(false);
        
        ((RoundScale)scale).setStartAngle(180-SPACE_ANGLE);
        ((RoundScale)scale).setEndAngle(SPACE_ANGLE);
        ramp.setRampWidth(12);
        setLoColor(XYGraphMediaFactory.getInstance().getColor(XYGraphMediaFactory.COLOR_YELLOW));
        setHiColor(XYGraphMediaFactory.getInstance().getColor(XYGraphMediaFactory.COLOR_YELLOW));
        
        valueLabel = new Label();       
        valueLabel.setFont(DEFAULT_LABEL_FONT);
        needle = new Needle();
        needle.setFill(true);
        needle.setOutline(false);
        
        setLayoutManager(new XMeterLayout());
        add(ramp, XMeterLayout.RAMP);
        add(scale, XMeterLayout.SCALE);         
        add(needle, XMeterLayout.NEEDLE);
        add(valueLabel, XMeterLayout.VALUE_LABEL);
        
        addFigureListener(new FigureListener() {            
            public void figureMoved(IFigure source) {
                ramp.setDirty(true);
                revalidate();   
            }
        }); 
        
    }
    
    @Override
    public BeanInfo getBeanInfo() throws IntrospectionException {
        return new MeterIntrospector().getBeanInfo(this.getClass());
    }
    
    
    /**
     * @return color of the needle.
     */
    public Color getNeedleColor(){
        return needle.getBackgroundColor();
    }
    
    @Override
    public void setBounds(Rectangle rect) {
        
        super.setBounds(rect);
    }

    /**
     * @param needleColor the needleColor to set
     */
    public void setNeedleColor(Color needleColor) {
        needle.setBackgroundColor(needleColor);
    }
    
    @Override
    public void setShowMarkers(boolean showMarkers) {
        super.setShowMarkers(showMarkers);      
        ramp.setVisible(showMarkers);   
    }
    
    @Override
    public void setValue(double value) {
        super.setValue(value);
        valueLabel.setText(SWTUtil.getPrettyString(value) + "%");         
    }

    public void setValueLabelVisibility(boolean visible) {
        valueLabel.setVisible(visible);
    }
}