package org.deidentifier.arx.framework.lattice;

public class NodeTriggerInverse extends NodeTrigger {

    private final NodeTrigger trigger;

    public NodeTriggerInverse(NodeTrigger trigger) {
        this.trigger = trigger;
    }
    
    @Override
    public boolean appliesTo(Node node) {
        return !trigger.appliesTo(node);
    }
}
