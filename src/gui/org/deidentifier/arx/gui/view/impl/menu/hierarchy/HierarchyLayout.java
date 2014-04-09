package org.deidentifier.arx.gui.view.impl.menu.hierarchy;

import java.util.Arrays;
import java.util.List;

import org.deidentifier.arx.gui.view.impl.menu.hierarchy.HierarchyModel.HierarchyGroup;

public class HierarchyLayout<T> {

    public static final int   PRETTY_THRESHOLD = 100;

    private HierarchyModel<T> model;
    private boolean           pretty           = true;
    
    public HierarchyLayout(HierarchyModel<T> model){
        this.model = model;
    }

    /**
     * This is so ugly
     * @return
     */
    public int[] layout() {

        long time = System.currentTimeMillis();
        
        // Size of the solution
        int size = model.showIntervals ? 1 + model.groups.size() : model.groups.size();
        
        // Init elements, sum, cardinality
        int[] sum = new int[size];
        int[] pointer = new int[size];
        int[] cardinality = new int[size];
        int[] base = new int[size];
        int[][] elements = new int[size][];
        
        // Init from intervals
        if (model.showIntervals) {
            elements[0] = new int[model.intervals.size()];
            Arrays.fill(elements[0], 1);
            cardinality[0] = model.intervals.size();
            sum[0] = model.intervals.size();
        } 
        
        // Init from groups
        for (int i=0; i < model.groups.size(); i++){
            
            // Prepare
            int index = model.showIntervals ? i +1 : i;
            List<HierarchyGroup<T>> groups = model.groups.get(i);
            
            // Prepare cardinality
            cardinality[index] = groups.size();
            
            // Prepare elements and sum
            int[] array = new int[groups.size()];
            elements[index] = array;
            for (int j=0; j<array.length; j++){
                array[j] = groups.get(j).size;
                sum[index]+=groups.get(j).size;
            }
        }
        
        // Prepare base cardinality
        System.arraycopy(cardinality, 0, base, 0, base.length);
        
        // Prepare flags
        boolean repeat = true;
        pretty = true;
        
        // Repeat
        while (repeat) {
            
            // Do not repeat
            repeat = false;
            
            // Sweep right to left
            for (int i=cardinality.length-1; i>0; i--){
                while (cardinality[i-1] < sum[i]) {
                    repeat = true;
                    sum[i-1]+=elements[i-1][pointer[i-1]++];
                    if (pointer[i-1]==base[i-1]) pointer[i-1]=0;
                    cardinality[i-1]++;
                }
            }
            
            // Sweep left to right
            for (int i=0; i<cardinality.length-1; i++){
                while (cardinality[i] > sum[i+1]) {
                    repeat = true;
                    sum[i+1]+=elements[i+1][pointer[i+1]++];
                    if (pointer[i+1]==base[i+1]) pointer[i+1]=0;
                    cardinality[i+1]++;
                }
            }
            
            // Check if still pretty
            for (int i=0; i<cardinality.length; i++){
                if (cardinality[i] > PRETTY_THRESHOLD) {
                    pretty = false;
                    repeat = false;
                }
            }
        }
        
        System.out.println("Layouting: "+(System.currentTimeMillis() - time));
        
        // Return
        if (!pretty) {
            return base;
        } else {
            return cardinality;
        }
    }
    
    /**
     * Is the layout pretty
     * @return
     */
    public boolean isPretty(){
        return pretty;
    }
}
