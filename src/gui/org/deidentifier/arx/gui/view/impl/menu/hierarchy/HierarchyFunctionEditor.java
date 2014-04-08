package org.deidentifier.arx.gui.view.impl.menu.hierarchy;

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.aggregates.AggregateFunction;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.menu.EditorSelection;
import org.deidentifier.arx.gui.view.impl.menu.hierarchy.HierarchyModel.HierarchyGroup;
import org.deidentifier.arx.gui.view.impl.menu.hierarchy.HierarchyModel.HierarchyInterval;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class HierarchyFunctionEditor<T> implements IUpdateable {
 
    protected final Composite composite;
    private final List<AggregateFunction<T>> functions;
    private final List<String> labels;
    private Object source;
    private final EditorSelection editor;

    public HierarchyFunctionEditor(final Composite parent,
                                   final HierarchyModel<T> model,
                                   final boolean isGeneral) {

        this.composite = new Composite(parent, SWT.NONE);
        this.composite.setLayout(SWTUtil.createGridLayout(2, false));
        this.composite.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        model.register(this);
        this.functions = new ArrayList<AggregateFunction<T>>();
        this.labels = new ArrayList<String>();
        
        if (isGeneral) {
            functions.add(null);
            labels.add("Default");
        }
        
        addFunction(AggregateFunction.BOUNDS(model.type));
        addFunction(AggregateFunction.COMMON_PREFIX(model.type));
        addFunction(AggregateFunction.COMMON_PREFIX(model.type, '-'));
        addFunction(AggregateFunction.COMMON_PREFIX(model.type, 'x'));
        addFunction(AggregateFunction.CONSTANT(model.type, "Parameter"));
        addFunction(AggregateFunction.INTERVAL(model.type, true, false));
        addFunction(AggregateFunction.SET(model.type));
        addFunction(AggregateFunction.SET_OF_PREFIXES(model.type, 1));
        addFunction(AggregateFunction.SET_OF_PREFIXES(model.type, 2));
        addFunction(AggregateFunction.SET_OF_PREFIXES(model.type, 3));
        addFunction(AggregateFunction.SET_OF_PREFIXES(model.type, 4));
        addFunction(AggregateFunction.SET_OF_PREFIXES(model.type, 5));
        

        createLabel(composite, "Aggregate function:");
        editor = new EditorSelection(composite, labels.toArray(new String[labels.size()])) {
            @Override
            public boolean accepts(final String s) {
                return labels.contains(s);
            }

            @Override
            @SuppressWarnings("unchecked")
            public String getValue() {
                if (isGeneral){
                    return model.function.toString();
                }
                if (source != null){
                    if (source instanceof HierarchyInterval){
                        AggregateFunction<T> function = ((HierarchyInterval<T>)source).function;
                        if (function == model.function) return "Default";
                        else return function.toString();
                    } else if (source instanceof HierarchyGroup){
                        AggregateFunction<T> function = ((HierarchyGroup<T>)source).function;
                        if (function == model.function) return "Default";
                        else return function.toString();
                    } else {
                        return null;
                    }
                }
                return null;
            }

            @Override
            @SuppressWarnings("unchecked")
            public void setValue(final String s) {
                AggregateFunction<T> function = functions.get(labels.indexOf(s));
                if (isGeneral){
                    if (function != null && function != model.function){
                        model.function = function;
                        model.update(HierarchyFunctionEditor.this);
                    }
                }
                if (source != null){
                    if (source instanceof HierarchyInterval){
                        if (function != null) {
                            if (((HierarchyInterval<T>)source).function != function) {
                                ((HierarchyInterval<T>)source).function = function;
                                model.update(HierarchyFunctionEditor.this);
                            }
                        }
                        else {
                            if (((HierarchyInterval<T>)source).function != model.function) {
                                ((HierarchyInterval<T>)source).function = model.function;
                                model.update(HierarchyFunctionEditor.this);
                            }
                        }
                        model.update(HierarchyFunctionEditor.this);
                    }else if (source instanceof HierarchyGroup){
                        if (function != null){
                            if (((HierarchyGroup<T>)source).function != function){
                                ((HierarchyGroup<T>)source).function = function;
                                model.update(HierarchyFunctionEditor.this);
                            }
                        }
                        else{
                            if (((HierarchyGroup<T>)source).function != model.function){
                                ((HierarchyGroup<T>)source).function = model.function;
                                model.update(HierarchyFunctionEditor.this);
                            }
                        }
                    }
                }
            }
        };
    }

    private void addFunction(AggregateFunction<T> function) {
        this.functions.add(function);
        this.labels.add(function.toString());
    }

    protected Label createLabel(Composite composite, String string) {
        Label label = new Label(composite, SWT.NONE);
        label.setText(string);
        GridData data = SWTUtil.createFillVerticallyGridData();
        data.verticalAlignment = SWT.CENTER;
        label.setLayoutData(data);
        return label;
    }
    
    public void setSource(Object object){
        this.source = object;
    }

    @Override
    public void update() {
        editor.update();
    }
}
