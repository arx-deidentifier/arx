package org.deidentifier.arx.gui.view.impl.menu.hierarchy;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased;
import org.deidentifier.arx.aggregates.HierarchyBuilderOrderBased;

public class HierarchyWizardModelOrder<T> extends HierarchyWizardModelGrouping<T>{
    
    private final String[] data;

    /**
     * Constructor to create an initial definition
     * @param dataType
     * @param data
     */
    public HierarchyWizardModelOrder(final DataType<T> dataType, String[] data) {
        super(data, dataType, false);
        this.data = data;
        this.internalSort(super.getDataType());
        this.update();
    }

    /**
     * Sort
     * @param type
     * @return successful, or not
     */
    private boolean internalSort(final DataType<?> type) {
        try {
            Arrays.sort(data, new Comparator<String>(){
                @Override public int compare(String o1, String o2) {
                    try {
                        return type.compare(o1, o2);
                    } catch (NumberFormatException | ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            return true;
        } catch (Exception e){
            return false;
        }
    }
    
    /**
     * Sorts the data
     * @param type
     */
    public boolean sort(DataType<?> type){
        boolean result = internalSort(type);
        update();
        return result;
    }
    
    /**
     * Moves an item up
     * @param index
     */
    public void moveUp(int index) {
        if (index<=0) return;
        String temp = data[index-1];
        data[index-1] = data[index];
        data[index] = temp;
        update();
    }
    
    /**
     * Moves an item down
     * @param index
     */
    public void moveDown(int index) {
        if (index>=data.length-1 || index<0) return;
        String temp = data[index+1];
        data[index+1] = data[index];
        data[index] = temp;
        update();
    }

    @Override
    protected void internalUpdate() {
        super.hierarchy = null;
        super.error = null;
        super.groupsizes = null;
        
        HierarchyBuilderOrderBased<T> builder = getBuilder();
        
        String error = builder.isValid();
        if (error != null) {
            super.error = error;
            return;
        }
        
        try {
            super.groupsizes = builder.prepare(data);
        } catch(Exception e){
            super.error = e.getMessage();
            return;
        }
        
        try {
            super.hierarchy = builder.build();
        } catch(Exception e){
            super.error = e.getMessage();
            return;
        }
    }

    @Override
    public HierarchyBuilderOrderBased<T> getBuilder() {
        HierarchyBuilderOrderBased<T> builder = HierarchyBuilderOrderBased.create(super.getDataType(), false);

        int level = 0;
        for (List<HierarchyWizardGroupingGroup<T>> list : super.getModelGroups()) {
            for (HierarchyWizardGroupingGroup<T> group : list){
                builder.getLevel(level).addGroup(group.size, group.function);
            }
            level++;
        }
        
        return builder;
    }
    

    @Override
    public void parse(HierarchyBuilder<T> builder) throws IllegalArgumentException {
        
        if (!(builder instanceof HierarchyBuilderOrderBased)) {
            return;
        }
        super.parse((HierarchyBuilderOrderBased<T>)builder);
    }
}
