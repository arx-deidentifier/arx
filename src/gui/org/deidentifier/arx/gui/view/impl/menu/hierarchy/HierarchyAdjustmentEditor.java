package org.deidentifier.arx.gui.view.impl.menu.hierarchy;

import java.text.ParseException;

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
    private final EditorString repeat;
    private final EditorString snap;
    private final EditorString label;

    public HierarchyAdjustmentEditor(final Composite parent,
                                     final HierarchyModel<T> model,
                                     final boolean lower) {

        composite = new Group(parent, SWT.SHADOW_ETCHED_IN);
        composite.setText(lower ? "Lower bound" : "Upper bound");
        composite.setLayout(SWTUtil.createGridLayout(2, false));
        composite.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        model.register(this);
        final HierarchyAdjustment<T> adjustment;
        if (!lower) adjustment = model.upper;
        else adjustment = model.lower;

        createLabel(composite, "Repeat:");
        repeat = new EditorString(composite) {
            @Override
            public boolean accepts(final String s) {
                return model.type.isValid(s);
            }

            @Override
            public String getValue() {
                T value = adjustment.repeat;
                if (value == null) return "";
                else return model.type.format(value);
            }

            @Override
            public void setValue(final String s) {
                T value = model.type.parse(s);
                try {
                    if (model.type.compare(model.type.format(value), 
                                           model.type.format(adjustment.repeat)) != 0){
                        adjustment.repeat = value;
                        model.update(HierarchyAdjustmentEditor.this);
                    }
                } catch (NumberFormatException | ParseException e) {
                    // Ignore
                }
            }
        };

        createLabel(composite, "Snap:");
        snap = new EditorString(composite) {
            @Override
            public boolean accepts(final String s) {
                return model.type.isValid(s);
            }

            @Override
            public String getValue() {
                T value = adjustment.snap;
                if (value == null) return "";
                else return model.type.format(value);
            }

            @Override
            public void setValue(final String s) {
                T value = model.type.parse(s);
                try {
                    if (model.type.compare(model.type.format(value), 
                                           model.type.format(adjustment.snap)) != 0){
                        adjustment.snap = value;
                        model.update(HierarchyAdjustmentEditor.this);
                    }
                } catch (NumberFormatException | ParseException e) {
                    // Ignore
                }
            }
        };

        createLabel(composite, "Label:");
        label = new EditorString(composite) {
            @Override
            public boolean accepts(final String s) {
                return model.type.isValid(s);
            }

            @Override
            public String getValue() {
                T value = adjustment.label;
                if (value == null) return "";
                else return model.type.format(value);
            }

            @Override
            public void setValue(final String s) {
                T value = model.type.parse(s);
                try {
                    if (model.type.compare(model.type.format(value), 
                                           model.type.format(adjustment.label)) != 0){
                        adjustment.label = value;
                        model.update(HierarchyAdjustmentEditor.this);
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
