package org.deidentifier.arx.gui.view.impl.menu.hierarchy;

import org.deidentifier.arx.DataType.DataTypeWithRatioScale;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.menu.EditorString;
import org.deidentifier.arx.gui.view.impl.menu.hierarchy.HierarchyModel.HierarchyInterval;
import org.eclipse.swt.widgets.Composite;

public class HierarchyIntervalEditor<T> extends HierarchyFunctionEditor<T> implements IUpdateable{
    
    private HierarchyInterval<T> interval = null;
    private final HierarchyModel<T> model;
    private final EditorString editorMin;
    private final EditorString editorMax;
    private final DataTypeWithRatioScale<T> type;
    
    @SuppressWarnings("unchecked")
    public HierarchyIntervalEditor(final Composite parent,
                                   final HierarchyModel<T> model) {
        super(parent, model, false);
        this.model = model;
        this.type = (DataTypeWithRatioScale<T>)model.type;
        createLabel(composite, "Min:");
        
        editorMin = new EditorString(composite) {
            @Override
            public boolean accepts(final String s) {
                if (interval==null) return false;
                if (!type.isValid(s)) return false;
                T value = type.parse(s);
                if (type.compare(value, interval.max) > 0) return false;
                else return true;
            }

            @Override
            public String getValue() {
                if (interval==null) return "";
                else return type.format(interval.min);
            }

            @Override
            public void setValue(final String s) {
                if (interval!=null){
                    if (interval.min != type.parse(s)){
                        interval.min = type.parse(s);
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
                if (!type.isValid(s)) return false;
                T value = type.parse(s);
                if (type.compare(value, interval.min) < 0) return false;
                else return true;
            }

            @Override
            public String getValue() {
                if (interval==null) return "";
                else return type.format(interval.max);
            }

            @Override
            public void setValue(final String s) {
                if (interval!=null){
                    if (interval.max != type.parse(s)){
                        interval.max = type.parse(s);
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
            super.setSource(this.interval);
            super.update();
            this.editorMin.update();
            this.editorMax.update();
            
            if (model.isFirst(this.interval)){
                SWTUtil.enable(editorMin.getControl());
            } else {
                SWTUtil.disable(editorMin.getControl());
            }
            if (model.isLast(this.interval)){
                SWTUtil.enable(editorMax.getControl());
            } else {
                SWTUtil.disable(editorMax.getControl());
            }
            SWTUtil.enable(getEditor().getControl());
        } else {
            this.interval = null;
            SWTUtil.disable(editorMin.getControl());
            SWTUtil.disable(editorMax.getControl());
            SWTUtil.disable(getEditor().getControl());
        }
    }
}
