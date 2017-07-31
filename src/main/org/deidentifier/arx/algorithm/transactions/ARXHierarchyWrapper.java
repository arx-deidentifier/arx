package org.deidentifier.arx.algorithm.transactions;

public class ARXHierarchyWrapper {

    public static Hierarchy convert(org.deidentifier.arx.AttributeType.Hierarchy h) {
        Dict d = new Dict(h.getHierarchy());
        return new Hierarchy(h.getHierarchy(), d);
    }
}
