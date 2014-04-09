package org.deidentifier.arx.gui.view.impl.menu.hierarchy;

import java.text.ParseException;

import org.deidentifier.arx.DataType.DataTypeWithRatioScale;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.menu.EditorString;
import org.deidentifier.arx.gui.view.impl.menu.hierarchy.HierarchyModel.HierarchyAdjustment;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

public class HierarchyAdjustmentEditor<T> implements IUpdateable {

    private final Group composite;
    private final boolean lower;
    private final DataTypeWithRatioScale<T> type;
    private final HierarchyModel<T> model;
    private final HierarchyAdjustment<T> adjustment;
    
    private EditorString repeat;
    private EditorString snap;
    private EditorString label;

    @SuppressWarnings("unchecked")
    public HierarchyAdjustmentEditor(final Composite parent,
                                     final HierarchyModel<T> model,
                                     final boolean lower) {

        this.composite = new Group(parent, SWT.SHADOW_ETCHED_IN);
        this.composite.setText(lower ? "Lower bound" : "Upper bound");
        this.composite.setLayout(SWTUtil.createGridLayout(2, false));
        this.composite.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        this.lower = lower;
        this.type = (DataTypeWithRatioScale<T>)model.type;
        this.model = model;
        this.model.register(this);
        if (!lower) {
            this.adjustment = model.upper;
            createRepeat(model, lower, adjustment);
            createSnap(model, lower, adjustment);
            createLabel(model, lower, adjustment);
        }
        else {
            this.adjustment = model.lower;
            createLabel(model, lower, adjustment);
            createSnap(model, lower, adjustment);
            createRepeat(model, lower, adjustment);
        }
    }

    private void createLabel(final HierarchyModel<T> model,
                             final boolean lower,
                             final HierarchyAdjustment<T> adjustment) {
        createLabel(composite, "Label:");
        label = new EditorString(composite) {
            
            @Override
            public boolean accepts(final String s) {
                return type.isValid(s);
            }

            @Override
            public String getValue() {
                T value = adjustment.label;
                if (value == null) return "";
                else return type.format(value);
            }

            @Override
            public void setValue(final String s) {
                T value = type.parse(s);
                try {
                    if (type.compare(type.format(value), 
                                     type.format(adjustment.label)) != 0){
                        adjustment.label = value;
                        if (lower){
                            if (type.compare(adjustment.snap, adjustment.label) < 0) {
                                adjustment.snap = adjustment.label;
                            }
                            if (type.compare(adjustment.repeat, adjustment.snap) < 0) {
                                adjustment.repeat = adjustment.snap;
                            }
                        } else {
                            if (type.compare(adjustment.snap, adjustment.label) > 0) {
                                adjustment.snap = adjustment.label;
                            }
                            if (type.compare(adjustment.repeat, adjustment.snap) > 0) {
                                adjustment.repeat = adjustment.snap;
                            }
                        }
                        
                        model.update();
                    }
                } catch (NumberFormatException | ParseException e) {
                    // Ignore
                }
            }
        };
    }

    private void createSnap(final HierarchyModel<T> model,
                            final boolean lower,
                            final HierarchyAdjustment<T> adjustment) {
        createLabel(composite, "Snap:");
        snap = new EditorString(composite) {
            
            @Override
            public boolean accepts(final String s) {
                return type.isValid(s);
            }

            @Override
            public String getValue() {
                T value = adjustment.snap;
                if (value == null) return "";
                else return type.format(value);
            }

            @Override
            public void setValue(final String s) {
                T value = type.parse(s);
                try {
                    if (type.compare(type.format(value), 
                                     type.format(adjustment.snap)) != 0){
                        
                        adjustment.snap = value;
                        if (lower){
                            if (type.compare(adjustment.repeat, adjustment.snap) < 0) {
                                adjustment.repeat = adjustment.snap;
                            }
                            if (type.compare(adjustment.snap, adjustment.label) < 0) {
                                adjustment.label = adjustment.snap;
                            }
                        } else {
                            if (type.compare(adjustment.repeat, adjustment.snap) > 0) {
                                adjustment.repeat = adjustment.snap;
                            }
                            if (type.compare(adjustment.snap, adjustment.label) > 0) {
                                adjustment.label = adjustment.snap;
                            }
                        }
                        
                        model.update();
                    }
                } catch (NumberFormatException | ParseException e) {
                    // Ignore
                }
            }
        };
    }

    private void createRepeat(final HierarchyModel<T> model,
                              final boolean lower,
                              final HierarchyAdjustment<T> adjustment) {
        createLabel(composite, "Repeat:");
        repeat = new EditorString(composite) {
            
            @Override
            public boolean accepts(final String s) {
                return type.isValid(s);
            }

            @Override
            public String getValue() {
                T value = adjustment.repeat;
                if (value == null) return "";
                else return type.format(value);
            }

            @Override
            public void setValue(final String s) {
                T value = type.parse(s);
                try {
                    if (type.compare(type.format(value), 
                                     type.format(adjustment.repeat)) != 0){
                         
                        adjustment.repeat = value;
                        if (lower){
                            if (type.compare(adjustment.repeat, adjustment.snap) < 0) {
                                adjustment.snap = adjustment.repeat;
                            }
                            if (type.compare(adjustment.snap, adjustment.label) < 0) {
                                adjustment.label = adjustment.snap;
                            }
                        } else {
                            if (type.compare(adjustment.repeat, adjustment.snap) > 0) {
                                adjustment.snap = adjustment.repeat;
                            }
                            if (type.compare(adjustment.snap, adjustment.label) > 0) {
                                adjustment.label = adjustment.snap;
                            }
                        }
                        
                        model.update();
                    }
                } catch (NumberFormatException | ParseException e) {
                    // Ignore
                }
            }
        };
    }

    private Label createLabel(Composite composite, String string) {
        Label label = new Label(composite, SWT.NONE);
        label.setText(string);
        GridData data = SWTUtil.createFillVerticallyGridData();
        data.verticalAlignment = SWT.CENTER;
        label.setLayoutData(data);
        return label;
    }

    @Override
    public void update() {
        repeat.update();
        snap.update();
        label.update();
    }
}
