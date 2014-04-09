package org.deidentifier.arx.gui.view.impl.menu.hierarchy;

import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.menu.EditorString;
import org.deidentifier.arx.gui.view.impl.menu.hierarchy.HierarchyModel.HierarchyGroup;
import org.eclipse.swt.widgets.Composite;

public class HierarchyGroupEditor<T> extends HierarchyFunctionEditor<T> implements IUpdateable{
    
    private HierarchyGroup<T> group = null;
    private final EditorString editor;
    private final HierarchyModel<T> model;

    public HierarchyGroupEditor(final Composite parent,
                                final HierarchyModel<T> model) {
        super(parent, model, false);
        this.model = model;
        createLabel(composite, "Size:");
        this.editor = new EditorString(composite) {
            @Override
            public boolean accepts(final String s) {
                if (group==null) return false;
                try {
                    int i = Integer.parseInt(s);
                    return i>0;
                } catch (NumberFormatException e) {
                    return false;
                }
            }

            @Override
            public String getValue() {
                if (group==null) return "";
                else return String.valueOf(group.size);
            }

            @Override
            public void setValue(final String s) {
                if (group!=null){
                    if (group.size != Integer.valueOf(s)){
                        group.size = Integer.valueOf(s);
                        model.update(HierarchyGroupEditor.this);
                    }
                }
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public void update() {
        if (model.selected instanceof HierarchyGroup){
            super.setSource(this.group);
            super.update();
            this.group = (HierarchyGroup<T>)model.selected;
            this.editor.update();
            SWTUtil.enable(editor.getControl());
            SWTUtil.enable(getEditor().getControl());
        } else {
            this.group = null;
            this.editor.update();
            SWTUtil.disable(editor.getControl());
            SWTUtil.disable(getEditor().getControl());
        }
        
    }
}
