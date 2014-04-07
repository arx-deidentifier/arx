package org.deidentifier.arx.gui.view.impl.menu.hierarchy;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.AggregateFunction;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.menu.hierarchy.ModelInterval.ModelIntervalFanout;
import org.deidentifier.arx.gui.view.impl.menu.hierarchy.ModelInterval.ModelIntervalInterval;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class WizardHierarchy {
    
    WizardHierarchy(final Shell shell){
        ModelInterval<Long> model = new ModelInterval<Long>();
        model.type = DataType.INTEGER;
        model.function = AggregateFunction.INTERVAL(model.type, true, false);
        model.intervals.add(new ModelIntervalInterval<Long>(0l, 1l, model.function));
        model.intervals.add(new ModelIntervalInterval<Long>(1l, 3l, model.function));
        model.intervals.add(new ModelIntervalInterval<Long>(3l, 5l, model.function));
        model.intervals.add(new ModelIntervalInterval<Long>(5l, 9l, model.function));
        model.intervals.add(new ModelIntervalInterval<Long>(9l, 13l, model.function));
        model.intervals.add(new ModelIntervalInterval<Long>(13l, 15l, model.function));
        model.fanouts.add(new ModelIntervalFanout<Long>(2, model.function));
        model.fanouts.add(new ModelIntervalFanout<Long>(3, model.function));
        model.fanouts.add(new ModelIntervalFanout<Long>(4, AggregateFunction.CONSTANT(DataType.INTEGER, "TESTESTESTEST")));
        model.update();
        
        GridLayout layout = SWTUtil.createGridLayout(1);
        layout.marginLeft = 5;
        layout.marginRight = 5;
        layout.marginTop = 5;
        layout.marginBottom = 5;
        shell.setLayout(layout);
        ComponentIntervalSpecificationEditor<Long> component = 
                new ComponentIntervalSpecificationEditor<Long>(shell, model);
        component.setLayoutData(SWTUtil.createFillGridData());
    }

    public static void main(String[] args) {
        Display display = new Display ();
        Shell shell = new Shell(display);
        new WizardHierarchy(shell);
        shell.setSize(800, 600);
        shell.open ();
        while (!shell.isDisposed ()) {
            if (!display.readAndDispatch ()) display.sleep ();
        }
        display.dispose ();
    }
}
