package org.deidentifier.arx.criteria;

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.framework.data.DataManager;

/**
 * The t-closeness criterion with hierarchical-distance EMD
 * @author Fabian Prasser
 */
public class HierarchicalDistanceTCloseness extends TCloseness {

    private static final long serialVersionUID = -2142590190479670706L;
    
    /** The hierarchy used for the EMD*/
    public final Hierarchy hierarchy;
    /** Internal tree*/
    protected int[]        tree;

    /**
     * Creates a new instance
     * @param t
     * @param h
     */
    public HierarchicalDistanceTCloseness(double t, Hierarchy h) {
        super(t);
        this.hierarchy = h;
    }

    @Override
    public void initialize(DataManager manager) {
        tree = manager.getTree();
    }
}
