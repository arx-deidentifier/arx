package org.deidentifier.arx.gui.view.impl.menu.hierarchy;

import java.util.Arrays;
import java.util.List;

import org.deidentifier.arx.gui.view.impl.menu.hierarchy.HierarchyModel.HierarchyGroup;

public class HierarchyLayout<T> {

    public static final int                    PRETTY_THRESHOLD    = 100;
    public static final int                    LAYOUT_COMPLEXITY   = 1000000;

    private HierarchyModel<T> model;
    private boolean pretty = true;
    
    public HierarchyLayout(HierarchyModel<T> model){
        this.model = model;
    }

    /**
     * This is so ugly
     * @return
     */
    public int[] layout() {
        
        // Find max factor to test for
        int size = model.showIntervals ? 1 + model.groups.size() : model.groups.size();
        int[] result = new int[size];
        int factor = (int)Math.floor(Math.pow(LAYOUT_COMPLEXITY, 1d / (double)size));
        
        long time = System.currentTimeMillis();
        
        // Compute base sizes and return if factor is 0
        int[] base = new int[size];
        if (model.showIntervals) base[0] = model.intervals.size();
        for (int i=0; i<model.groups.size(); i++){
            if (model.showIntervals) base[1+i] = model.groups.get(i).size();
            else base[i] = model.groups.get(i).size();
        }
        if (factor==0) return base;
        
        // Provide arrays with heights
        int[][] heights = new int[result.length][factor];
        for (int i=0; i<result.length; i++){
            heights[i] = new int[factor];
            if (model.showIntervals && i==0) Arrays.fill(heights[i], 1);
        }
    
        // Perform testing
        int[] array = layout(result, base, new int[result.length], heights, factor, 0);
        System.out.println("Layouting: "+(System.currentTimeMillis() - time));
        System.out.println("Solution found: "+(array != null));
        if ((array != null)) System.out.println("Solution: "+Arrays.toString(array));
        
        if (array == null){
            pretty = false;
            return base;
        }
        else {
            for (int i : array) {
                if (i>PRETTY_THRESHOLD) {
                    pretty = false;
                    return base;
                }
            }
            return array;
        }
    }
    
    /**
     * Is the layout pretty
     * @return
     */
    public boolean isPretty(){
        return pretty;
    }

    /**
     * This is so ugly
     * @param result
     * @param base
     * @param check
     * @param heights
     * @param factor
     * @param index
     * @return
     */
    private int[] layout(int[] result, int[] base, int[] check, int[][] heights, int factor, int index) {
        
        if (index==result.length) {
            
            if (model.showIntervals) {
                // Check whether it is a solution
                check[0] = result[0];
                for (int j=0; j<check.length-1; j++){
                    
                    check[j+1] = 0;
                    int leftIndex = 0;
                    int rightIndex = 0;
                    
                    // For each column, compute the row height by iterating over all fanouts
                    for (int i=0; i<result[j+1]; i++){
                        List<HierarchyGroup<T>> list = model.groups.get(j);
                        HierarchyGroup<T> fanout = list.get(rightIndex % list.size());
                        
                        // Add heights based on heights of left neighbors
                        int height = 0;
                        for (int k=0; k<fanout.size; k++){
                            height += heights[j][leftIndex % result[j]]; 
                            leftIndex++;
                        }
                        
                        if (rightIndex == heights[j+1].length) return null;
                        
                        // Store height and count
                        check[j+1]+=height;
                        heights[j+1][rightIndex]=height;
                        rightIndex++;
                    }
                }
            } else {
                // Check whether it is a solution
                for (int j=0; j<check.length; j++){
                    
                    check[j] = 0;
                    int leftIndex = 0;
                    int rightIndex = 0;
                    
                    // For each column, compute the row height by iterating over all fanouts
                    for (int i=0; i<result[j]; i++){
                        List<HierarchyGroup<T>> list = model.groups.get(j);
                        HierarchyGroup<T> fanout = list.get(rightIndex % list.size());
                        
                        // Add heights based on heights of left neighbors
                        int height = 0;
                        for (int k=0; k<fanout.size; k++){
                            if (j==0){
                                height += 1;
                            } else {
                                height += heights[j-1][leftIndex % result[j-1]]; 
                                leftIndex++;
                            }
                        }
                        
                        if (rightIndex == heights[j].length) return null;
                        
                        // Store height and count
                        check[j]+=height;
                        heights[j][rightIndex]=height;
                        rightIndex++;
                    }
                }
            }
            
            // Check whether all values are equal
            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;
            for (int i : check) {
                min = Math.min(min, i);
                max = Math.max(max, i);
            }
            if (min==max) return result;
            else return null;
            
        } else {
            
            // Enumerate
            for (int j=0; j<=factor; j++){
                
                // Call
                result[index] = base[index] + j;
                int[] array = layout(result, base, check, heights, factor, index+1);
                
                // Check
                if (array != null) return array;
            }
            
            // Nothing found
            return null;
        }
    }

}
