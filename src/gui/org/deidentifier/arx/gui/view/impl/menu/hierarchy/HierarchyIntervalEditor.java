package org.deidentifier.arx.gui.view.impl.menu.hierarchy;

import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.menu.EditorString;
import org.deidentifier.arx.gui.view.impl.menu.hierarchy.HierarchyModel.HierarchyInterval;
import org.eclipse.swt.widgets.Composite;

public class HierarchyIntervalEditor<T> extends HierarchyFunctionEditor<T> implements IUpdateable{
    
    private HierarchyInterval<T> interval = null;
    private final HierarchyModel<T> model;
    private final EditorString editorMin;
    private final EditorString editorMax;
    
    public HierarchyIntervalEditor(final Composite parent,
                                   final HierarchyModel<T> model) {
        super(parent, model, true);
        this.model = model;
        createLabel(composite, "Min:");
        editorMin = new EditorString(composite) {
            @Override
            public boolean accepts(final String s) {
                if (interval==null) return false;
                return model.type.isValid(s);
            }

            @Override
            public String getValue() {
                if (interval==null) return "";
                else return model.type.format(interval.min);
            }

            @Override
            public void setValue(final String s) {
                if (interval!=null){
                    if (interval.min != model.type.parse(s)){
                        interval.min = model.type.parse(s);
                        model.update(HierarchyIntervalEditor.this);
                    }
                }
            }
        };
        
        createLabel(composite, "Max:");
        editorMax = new EditorString(composite) {
            @Override
            public boolean accepts(final String s) {
                if (interval==null) return false;
                return model.type.isValid(s);
            }

            @Override
            public String getValue() {
                if (interval==null) return "";
                else return model.type.format(interval.max);
            }

            @Override
            public void setValue(final String s) {
                if (interval!=null){
                    if (interval.max != model.type.parse(s)){
                        interval.max = model.type.parse(s);
                        model.update(HierarchyIntervalEditor.this);
                    }
                }
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public void update() {
        if (model.selected instanceof HierarchyInterval){
            this.interval = (HierarchyInterval<T>)model.selected;
        } else {
            this.interval = null;
        }
        super.update();
        this.editorMin.update();
        this.editorMax.update();
        if (interval==null) SWTUtil.disable(composite);
        else SWTUtil.enable(composite);
    }
}
