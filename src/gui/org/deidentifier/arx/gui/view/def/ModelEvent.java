package org.deidentifier.arx.gui.view.def;

public class ModelEvent {
    public static enum EventTarget {
        SELECTED_ATTRIBUTE,
        INPUT,
        OUTPUT,
        ATTRIBUTE_TYPE,
        RESULT,
        DATA_TYPE,
        ALGORITHM,
        METRIC,
        MAX_OUTLIERS,
        FILTER,
        SELECTED_NODE,
        MODEL,
        CLIPBOARD,
        HIERARCHY,
        CRITERION_DEFINITION, 
        RESEARCH_SUBSET, 
        SORT_ORDER
    }

    public final ModelEvent.EventTarget target;
    public final Object      data;
    public final Object      source;

    public ModelEvent(final Object source,
                      final ModelEvent.EventTarget target,
                      final Object data) {
        this.target = target;
        this.data = data;
        this.source = source;
    }
}