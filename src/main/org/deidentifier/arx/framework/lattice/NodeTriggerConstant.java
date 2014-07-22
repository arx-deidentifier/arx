package org.deidentifier.arx.framework.lattice;

public class NodeTriggerConstant extends NodeTrigger {

    private final boolean result;
    
    public NodeTriggerConstant(boolean result) {
        this.result = result;
    }

    @Override
    public boolean appliesTo(Node node) {
        return result;
    }
}
