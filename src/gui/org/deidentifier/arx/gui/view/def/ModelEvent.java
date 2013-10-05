package org.deidentifier.arx.gui.view.def;

public class ModelEvent {
    
    public static enum ModelPart {
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

    public final ModelPart   part;
    public final Object      data;
    public final Object      source;

    public ModelEvent(final Object source,
                      final ModelPart target,
                      final Object data) {
        this.part = target;
        this.data = data;
        this.source = source;
    }
}