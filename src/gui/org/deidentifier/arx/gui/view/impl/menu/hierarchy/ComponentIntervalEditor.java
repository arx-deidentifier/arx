package org.deidentifier.arx.gui.view.impl.menu.hierarchy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.menu.hierarchy.ModelInterval.ModelIntervalFanout;
import org.deidentifier.arx.gui.view.impl.menu.hierarchy.ModelInterval.ModelIntervalInterval;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class ComponentIntervalEditor<T> {
    
    private static final Font FONT = getFont();
    private static final int OFFSET = 10;
    private static final int INTERVAL_HEIGHT = 20;
    
    private static final Color DISABLED_FOREGROUND = GUIHelper.COLOR_GRAY;
    private static final Color DISABLED_BACKGROUND = GUIHelper.getColor(230, 230, 230);
    private static final Color NORMAL_FOREGROUND = GUIHelper.COLOR_BLACK;
    private static final Color NORMAL_BACKGROUND = GUIHelper.COLOR_WHITE;
    private static final Color SELECTED_BACKGROUND = GUIHelper.COLOR_YELLOW;
    
    private ModelInterval<T> model;
    private Composite composite;
    private ScrolledComposite scrolledcomposite;
    private Map<Rectangle, Object> objectPositions = new HashMap<Rectangle, Object>();

    public ComponentIntervalEditor(Composite parent, ModelInterval<T> model) {
        this.model = model;
        
        scrolledcomposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        scrolledcomposite.setMinSize(600, 600);
        scrolledcomposite.setExpandHorizontal(true);
        scrolledcomposite.setExpandVertical(true);
        
        composite = new Composite(scrolledcomposite, SWT.NONE);
        scrolledcomposite.setContent(composite);
        
        composite.addPaintListener(new PaintListener(){
            public void paintControl(PaintEvent e){
                paint(e.gc);
            }
        }); 
        
        composite.addMouseListener(new MouseAdapter(){
            @Override public void mouseUp(MouseEvent arg0) {
                for (Rectangle r : objectPositions.keySet()) {
                    if (r.contains(arg0.x, arg0.y)) {
                        ComponentIntervalEditor.this.model.selected = objectPositions.get(r);
                        composite.redraw();
                        break;
                    }
                }
            }
        });
    }
    
    public void setLayoutData(Object object){
        this.scrolledcomposite.setLayoutData(object);
    }

    protected void paint(GC gc) {
        
        int intervalBoundWidth = getRequiredWidth(gc, model.intervalBoundLabels) + OFFSET;
        int fanoutBoundWidth = getRequiredWidth(gc, model.fanoutBoundLabels) + OFFSET;
        int intervalLabelWidth = getRequiredWidth(gc, model.intervalLabels) + OFFSET;
        int fanoutLabelWidth = getRequiredWidth(gc, model.fanoutLabels) + OFFSET;
        int intervalTotalWidth = intervalBoundWidth + intervalLabelWidth;
        int fanoutTotalWidth = fanoutBoundWidth + fanoutLabelWidth;
        objectPositions.clear();
        
        for (int i=0; i<model.intervals.size() + model.additionalIntervals; i++){
            
            int index = i % model.intervals.size();
            ModelIntervalInterval<T> interval = model.intervals.get(index);
            String bound = model.intervalBoundLabels.get(i);
            String label = model.intervalLabels.get(i);
            Rectangle rectangle = new Rectangle(OFFSET,
                                                OFFSET + i * (OFFSET + INTERVAL_HEIGHT),
                                                intervalTotalWidth,
                                                INTERVAL_HEIGHT);
            Rectangle rectangle1 = new Rectangle(OFFSET,
                                                OFFSET + i * (OFFSET + INTERVAL_HEIGHT),
                                                intervalBoundWidth,
                                                INTERVAL_HEIGHT);
            Rectangle rectangle2 = new Rectangle(OFFSET + intervalBoundWidth,
                                                OFFSET + i * (OFFSET + INTERVAL_HEIGHT),
                                                intervalLabelWidth,
                                                INTERVAL_HEIGHT);
            if (i<model.intervals.size()){
                gc.setBackground(model.selected == interval ? SELECTED_BACKGROUND : NORMAL_BACKGROUND);
                gc.fillRectangle(rectangle);
                gc.setForeground(NORMAL_FOREGROUND);
                gc.drawRectangle(rectangle);
                gc.drawRectangle(rectangle1);
                drawString(gc, bound, rectangle1);
                
                gc.setBackground(NORMAL_FOREGROUND);
                gc.fillRectangle(rectangle2);
                gc.setForeground(NORMAL_BACKGROUND);
                drawString(gc, label, rectangle2);
                objectPositions.put(rectangle, interval);
            }
            else {
                gc.setBackground(DISABLED_BACKGROUND);
                gc.fillRectangle(rectangle);
                gc.setForeground(DISABLED_FOREGROUND);
                gc.drawRectangle(rectangle);
                gc.drawRectangle(rectangle1);
                drawString(gc, bound, rectangle1);
                
                gc.setBackground(DISABLED_FOREGROUND);
                gc.fillRectangle(rectangle2);
                gc.setForeground(DISABLED_BACKGROUND);
                drawString(gc, label, rectangle2);
            }
            
        }

        int top = OFFSET;
        for (int i=0; i<model.fanouts.size() + model.additionalFanouts; i++){
            
            int index = i % model.fanouts.size();
            ModelIntervalFanout<T> fanout = model.fanouts.get(index);
            String bound = model.fanoutBoundLabels.get(i);
            String label = model.fanoutLabels.get(i);
            int height = !model.pretty ? INTERVAL_HEIGHT : 
                         INTERVAL_HEIGHT * fanout.size + OFFSET * (fanout.size - 1);
            Rectangle rectangle = new Rectangle(OFFSET * 2 + intervalTotalWidth, top, fanoutTotalWidth, 
                                                height);
            Rectangle rectangle1 = new Rectangle(OFFSET * 2 + intervalTotalWidth, top, fanoutBoundWidth, 
                                                height);
            Rectangle rectangle2 = new Rectangle(OFFSET * 2 + intervalTotalWidth + fanoutBoundWidth, top, fanoutLabelWidth, 
                                                height);
            if (i<model.fanouts.size()){
                gc.setBackground(model.selected == fanout ? SELECTED_BACKGROUND : NORMAL_BACKGROUND);
                gc.fillRectangle(rectangle);
                gc.setForeground(NORMAL_FOREGROUND);
                gc.drawRectangle(rectangle);
                gc.drawRectangle(rectangle1);
                objectPositions.put(rectangle, fanout);
                drawString(gc, bound, rectangle1);

                gc.setBackground(NORMAL_FOREGROUND);
                gc.fillRectangle(rectangle2);
                gc.setForeground(NORMAL_BACKGROUND);
                drawString(gc, label, rectangle2);
            }
            else {
                gc.setBackground(DISABLED_BACKGROUND);
                gc.fillRectangle(rectangle);
                gc.setForeground(DISABLED_FOREGROUND);
                gc.drawRectangle(rectangle);
                gc.drawRectangle(rectangle1);
                drawString(gc, bound, rectangle1);
                
                gc.setBackground(DISABLED_FOREGROUND);
                gc.fillRectangle(rectangle2);
                gc.setForeground(DISABLED_BACKGROUND);
                drawString(gc, label, rectangle2);
            }
            
            top += rectangle.height + OFFSET;
        }
        
        int width = OFFSET * 3 + intervalTotalWidth + fanoutTotalWidth;
        int height = top;
        scrolledcomposite.setMinSize(width, height);
    }
    
    private int getRequiredWidth(GC gc, List<String> list){
        gc.setFont(FONT);
        int width = 0;
        for (String s : list) {
            width = Math.max(width, gc.textExtent(s).x);
        }
        return width;
    }

    private void drawString(GC gc, String string, Rectangle r) {
        gc.setFont(FONT);
        Point extent = gc.textExtent(string);
        int xx = r.x + (r.width - extent.x) / 2;
        int yy = r.y + (r.height - extent.y) / 2;
        gc.drawText(string, xx, yy, true);
    }
    
    private static Font getFont(){

        FontData fontdata = GUIHelper.DEFAULT_FONT.getFontData()[0];
        fontdata.setHeight(9);
        return GUIHelper.getFont(fontdata);
    }
}
