package org.deidentifier.arx.gui.view.impl.menu.hierarchy;

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.aggregates.HierarchyBuilder;

public abstract class HierarchyWizardModelAbstract<T> {

    protected final String[] data;
    protected int[] groupsizes;
    protected Hierarchy hierarchy;
    protected String error;
    protected HierarchyWizardView view;

    public HierarchyWizardModelAbstract(String[] data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public Hierarchy getHierarchy() {
        return hierarchy;
    }

    public int[] getGroups() {
        return groupsizes;
    }
    
    public void update(){
        internalUpdate();
        if (view != null) view.update();
    }

    protected abstract void internalUpdate();
    
    public void setView(HierarchyWizardView view){
        this.view = view;
    }
    
    public String[] getData() {
        return data;
    }
    
    public abstract HierarchyBuilder<T> getBuilder();
    public abstract void parse(HierarchyBuilder<T> builder);
}
