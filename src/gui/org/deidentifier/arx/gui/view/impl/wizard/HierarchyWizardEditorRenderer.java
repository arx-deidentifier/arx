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

package org.deidentifier.arx.gui.view.impl.wizard;

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.DataType.DataTypeWithRatioScale;
import org.deidentifier.arx.gui.view.impl.wizard.HierarchyWizardModelGrouping.HierarchyWizardGroupingGroup;
import org.deidentifier.arx.gui.view.impl.wizard.HierarchyWizardModelGrouping.HierarchyWizardGroupingInterval;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Renders the content.
 *
 * @author Fabian Prasser
 * @param <T>
 */
public class HierarchyWizardEditorRenderer<T> {

    /**
     * Base class for rendering contexts.
     *
     * @author Fabian Prasser
     * @param <T>
     */
    public abstract static class RenderedComponent<T> {
        
        /** Var. */
        public Rectangle rectangle1;
        
        /** Var. */
        public Rectangle rectangle2;
        
        /** Var. */
        public int       depth;
        
        /** Var. */
        public boolean   enabled;
        
        /** Var. */
        public String    label;
        
        /** Var. */
        public String    bounds;
        
        /** Var. */
        public T         min;
        
        /** Var. */
        public T         max;
    }
    
    /**
     * A rendering context for a group.
     *
     * @author Fabian Prasser
     * @param <T>
     */
    public static class RenderedGroup<T> extends RenderedComponent<T> {
        
        /** Var. */
        public HierarchyWizardGroupingGroup<T> group;

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            @SuppressWarnings("rawtypes")
            RenderedGroup other = (RenderedGroup) obj;
            if (group == null) {
                if (other.group != null) return false;
            } else if (!group.equals(other.group)) return false;
            return true;
        }
 
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((group == null) ? 0 : group.hashCode());
            return result;
        }
    }
    
    /**
     * A rendering context for an interval.
     *
     * @author Fabian Prasser
     * @param <T>
     */
    public static class RenderedInterval<T> extends RenderedComponent<T> {
        
        /** Var. */
        public HierarchyWizardGroupingInterval<T> interval;
        
        /** Var. */
        public T                                  offset;

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            @SuppressWarnings("rawtypes")
            RenderedInterval other = (RenderedInterval) obj;
            if (interval == null) {
                if (other.interval != null) return false;
            } else if (!interval.equals(other.interval)) return false;
            return true;
        }
 
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((interval == null) ? 0 : interval.hashCode());
            return result;
        }
    }

    /**
     * Returns the font.
     *
     * @return
     */
    private static Font getFont(){

        FontData fontdata = GUIHelper.DEFAULT_FONT.getFontData()[0];
        fontdata.setHeight(9);
        return GUIHelper.getFont(fontdata);
    }
    
    /** Constants. */
    public static final Font  FONT                   = getFont();
    
    /** Constants. */
    public static final int   OFFSET                 = 10;
    
    /** Constants. */
    public static final int   INTERVAL_HEIGHT        = 20;
    
    /** Constants. */
    public static final Color WIDGET_BACKGROUND      = GUIHelper.COLOR_WHITE;
    
    /** Constants. */
    public static final Color DISABLED_FOREGROUND    = GUIHelper.COLOR_GRAY;
    
    /** Constants. */
    public static final Color DISABLED_BACKGROUND    = GUIHelper.getColor(230, 230, 230);
    
    /** Constants. */
    public static final Color NORMAL_FOREGROUND      = GUIHelper.COLOR_BLACK;
    
    /** Constants. */
    public static final Color ALTERNATIVE_FOREGROUND = GUIHelper.COLOR_WHITE;
    
    /** Constants. */
    public static final Color NORMAL_BACKGROUND      = GUIHelper.getColor(230, 230, 230);

    /** Constants. */
    public static final Color SELECTED_BACKGROUND    = GUIHelper.COLOR_YELLOW;

    /** Var. */
    private final List<RenderedInterval<T>>        intervals         = new ArrayList<RenderedInterval<T>>();
    
    /** Var. */
    private final List<List<RenderedGroup<T>>>     groups            = new ArrayList<List<RenderedGroup<T>>>();
    
    /** Var. */
    private final List<RenderedInterval<T>>        renderedIntervals = new ArrayList<RenderedInterval<T>>();
    
    /** Var. */
    private final List<List<RenderedGroup<T>>>     renderedGroups    = new ArrayList<List<RenderedGroup<T>>>();
    
    /** Var. */
    private final HierarchyWizardEditorLayout<T> layout;
    
    /** Var. */
    private final HierarchyWizardModelGrouping<T>  model;

    /**
     * Creates a new instance.
     *
     * @param model
     */
    public HierarchyWizardEditorRenderer(HierarchyWizardModelGrouping<T> model) {
        this.model = model;
        this.layout = new HierarchyWizardEditorLayout<T>(model);
    }

    /**
     * Returns all components.
     *
     * @return
     */
    public List<RenderedComponent<T>> getComponents(){
        List<RenderedComponent<T>> result = new ArrayList<RenderedComponent<T>>();
        if (model.isShowIntervals()) result.addAll(intervals);
        for (List<RenderedGroup<T>> list : groups){
            result.addAll(list);
        }
        return result;
    }
    

    /**
     * Returns the required minimal size.
     *
     * @return
     */
    public Point getMinSize() {
        int minWidth = 0;
        int minHeight = 0;
        for (RenderedComponent<T> component : getComponents()) {
            minWidth = Math.max(minWidth, component.rectangle2.x + component.rectangle2.width);
            minHeight = Math.max(minHeight, component.rectangle2.y + component.rectangle2.height);
        }
        return new Point(minWidth + OFFSET, minHeight + OFFSET);
    }

    /**
     * Mouse click.
     *
     * @param x
     * @param y
     * @return
     */
    public boolean select(int x, int y) {
        Object result = null;
        for (RenderedComponent<T> component : getComponents()) {
            if (component.enabled) {
                if ((component.rectangle1 != null && component.rectangle1.contains(x, y)) || 
                    (component.rectangle2 != null && component.rectangle2.contains(x, y))) {
                    if (component instanceof RenderedInterval) {
                        result = ((RenderedInterval<T>)component).interval;
                    } else {
                        result = ((RenderedGroup<T>)component).group;
                    }
                    break;
                }
            }
        }
        if (result != model.getSelectedElement()) {
            model.setSelectedElement(result);
            return true;
        } else {
            model.setSelectedElement(result);
            return false;
        }
    }
    
    /**
     * Updates the drawing context.
     */
    public void update(){
        
        // Init
        int[] factors = layout.layout();
        List<HierarchyWizardGroupingInterval<T>> modelIntervals = updateIntervals(factors);
        updateGroups(factors, modelIntervals);
    }

    /**
     * Update graphics layout.
     *
     * @param gc
     */
    public void update(GC gc){
        
        int intervalLabelWidth = 0;
        int intervalBoundWidth = 0;
        int intervalTotalWidth = 0;
        if (model.isShowIntervals()) {
            intervalLabelWidth = getRequiredLabelWidth(gc, intervals) + OFFSET;
            intervalBoundWidth = getRequiredBoundWidth(gc, intervals) + OFFSET;
            intervalTotalWidth = intervalLabelWidth + intervalBoundWidth;
        }
        
        List<Integer> fanoutLabelWidth = new ArrayList<Integer>();
        List<Integer> fanoutBoundWidth = new ArrayList<Integer>();
        List<Integer> fanoutTotalWidth = new ArrayList<Integer>();
        
        for (List<RenderedGroup<T>> list : groups){
            int label = getRequiredLabelWidth(gc, list) + OFFSET;
            int bound = getRequiredBoundWidth(gc, list) + OFFSET;
            fanoutLabelWidth.add(label);
            fanoutBoundWidth.add(bound);
            fanoutTotalWidth.add(label + bound);
        }
        
        int top = OFFSET;
        if (model.isShowIntervals()) {
            for (RenderedInterval<T> context : intervals){
                context.rectangle1 = new Rectangle(OFFSET, top, intervalBoundWidth, INTERVAL_HEIGHT);
                context.rectangle2 = new Rectangle(OFFSET + intervalBoundWidth, top, intervalLabelWidth, INTERVAL_HEIGHT);
                top += INTERVAL_HEIGHT + OFFSET;
            }
        }
        
        int left = OFFSET * 2 + intervalTotalWidth;
        for (int i=0; i<groups.size(); i++){
            top = OFFSET;
            int offset = 0;
            for (RenderedGroup<T> context : groups.get(i)) {
                int height = INTERVAL_HEIGHT;
                if (layout.isPretty()){
                    if (i==0){
                        height = INTERVAL_HEIGHT * context.group.size + OFFSET * (context.group.size - 1);
                    } else {
                        RenderedGroup<T> reference1 = groups.get(i-1).get(offset);
                        offset += context.group.size;
                        RenderedGroup<T> reference2 = groups.get(i-1).get(offset - 1);
                        height = reference2.rectangle1.y + reference2.rectangle1.height - reference1.rectangle1.y;
                    }
                }
                context.rectangle1 = new Rectangle(left, top, fanoutBoundWidth.get(i), height);
                context.rectangle2 = new Rectangle(left + fanoutBoundWidth.get(i), top, fanoutLabelWidth.get(i), height);
                top += height + OFFSET;
            }
            left += fanoutTotalWidth.get(i) + OFFSET;
        }
        
        renderedIntervals.clear();
        renderedIntervals.addAll(intervals);
        renderedGroups.clear();
        renderedGroups.addAll(groups);
    }

    /**
     * 
     *
     * @param gc
     * @param list
     * @return
     */
    @SuppressWarnings("unchecked")
    private int getRequiredBoundWidth(GC gc, List<?> list){
        gc.setFont(FONT);
        int width = 0;
        for (Object elem : list){
            width = Math.max(width, gc.textExtent(((RenderedComponent<T>)elem).bounds).x);
        }
        return width;
    }

    /**
     * 
     *
     * @param gc
     * @param list
     * @return
     */
    @SuppressWarnings("unchecked")
    private int getRequiredLabelWidth(GC gc, List<?> list){
        gc.setFont(FONT);
        int width = 0;
        for (Object elem : list){
            
            width = Math.max(width, gc.textExtent(((RenderedComponent<T>)elem).label).x);
        }
        return width;
    }
    
    /**
     * 
     *
     * @param factors
     * @param modelIntervals
     */
    @SuppressWarnings("unchecked")
    private void updateGroups(int[] factors, List<HierarchyWizardGroupingInterval<T>> modelIntervals) {
        
        // Init
        List<List<HierarchyWizardGroupingGroup<T>>> modelGroups = model.getModelGroups();
        boolean showIntervals = model.isShowIntervals();
        T width = null;
        DataTypeWithRatioScale<T> dtype = null;
        if (showIntervals) {
            dtype = (DataTypeWithRatioScale<T>)model.getDataType();
            width = dtype.subtract(modelIntervals.get(modelIntervals.size()-1).max, modelIntervals.get(0).min);
        }
        
        // Create groups
        groups.clear();
        int shift = showIntervals ? 1 : 0;
        for (int i=0; i<modelGroups.size(); i++){
            groups.add(new ArrayList<RenderedGroup<T>>());
            int offset = 0;
            
            if (layout.isPretty() && showIntervals && i>0) {
                width = dtype.subtract(groups.get(i-1).get(groups.get(i-1).size()-1).max, groups.get(i-1).get(0).min);
            }
            
            for (int j=0; j < factors[i+shift]; j++) {
                List<HierarchyWizardGroupingGroup<T>> list = modelGroups.get(i);
                HierarchyWizardGroupingGroup<T> group = list.get(j % list.size());
                RenderedGroup<T> element = new RenderedGroup<T>();
                element.depth = i + 1;
                element.enabled = j < list.size();
                
                if (layout.isPretty() && showIntervals){
                    
                    T min = null;
                    T max = null;
                    T scale1 = null;
                    T scale2 = null;
                    
                    if (i==0) {
                        min = modelIntervals.get(offset % modelIntervals.size()).min;
                        if (offset >= modelIntervals.size()) {
                            int factor = offset / modelIntervals.size();
                            scale1 = dtype.multiply(width, factor);
                        }
                        offset += group.size;
                        max = modelIntervals.get((offset-1)% modelIntervals.size()).max;
                        if (offset >= modelIntervals.size()) {
                            int factor = (offset -1) / modelIntervals.size();
                            scale2 = dtype.multiply(width, factor);
                        }
                    } else {
                        min = groups.get(i-1).get(offset % groups.get(i-1).size()).min;
                        if (offset >= groups.get(i-1).size()) {
                            int factor = offset / groups.get(i-1).size();
                            scale1 = dtype.multiply(width, factor);
                        }
                        offset += group.size;
                        max = groups.get(i-1).get((offset-1) % groups.get(i-1).size()).max;
                        if (offset >= groups.get(i-1).size()) {
                            int factor = (offset -1) / groups.get(i-1).size();
                            scale2 = dtype.multiply(width, factor);
                        }
                    }
                    
                    if (scale1 != null){
                        min = dtype.add(scale1, min);
                    } 
                    if (scale2 != null){
                        max = dtype.add(scale2, max); 
                    }
                    
                    element.bounds = "["+dtype.format(min)+", "+dtype.format(max)+"[";
                    String[] values = {dtype.format(min), dtype.format(max)};
                    element.label = group.function.aggregate(values);
                    element.min = min;
                    element.max = max;
                } else {
                    element.bounds = String.valueOf(group.size); 
                    element.label = group.function.toString();
                }
                element.group = group;
                groups.get(i).add(element);
                
            }
        }
    }

    /**
     * 
     *
     * @param factors
     * @return
     */
    @SuppressWarnings("unchecked")
    private List<HierarchyWizardGroupingInterval<T>> updateIntervals(int[] factors) {

        // Init
        List<HierarchyWizardGroupingInterval<T>> modelIntervals = model.getIntervals();
        boolean showIntervals = model.isShowIntervals();
        if (showIntervals) intervals.clear();
        DataTypeWithRatioScale<T> dtype = null;
        T width = null;
        if (showIntervals) {
            dtype = (DataTypeWithRatioScale<T>)model.getDataType();
            width = dtype.subtract(modelIntervals.get(modelIntervals.size()-1).max, modelIntervals.get(0).min);
        }
       
        // Create intervals
        if (showIntervals) {
            for (int i=0; i < factors[0]; i++) {
                HierarchyWizardGroupingInterval<T> interval = modelIntervals.get(i % modelIntervals.size());
                RenderedInterval<T> element = new RenderedInterval<T>();
                if (i<modelIntervals.size()) {
                    element.offset = null;
                } else {
                    int factor = i / modelIntervals.size();
                    element.offset = dtype.multiply(width, factor);
                }
                element.depth = 0;
                element.enabled = i < modelIntervals.size();
                T min = interval.min;
                T max = interval.max;
                if (element.offset != null){
                    min = dtype.add(element.offset, min);
                    max = dtype.add(element.offset, max);
                } 
                element.bounds = "["+dtype.format(min)+", "+dtype.format(max)+"[";
                String[] values = {dtype.format(min), dtype.format(max)};
                element.label = interval.function.aggregate(values);
                element.interval = interval;
                intervals.add(element);
            }
        }
     
        return modelIntervals;
    }
}
