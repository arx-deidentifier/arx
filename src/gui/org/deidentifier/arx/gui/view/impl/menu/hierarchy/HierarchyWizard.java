package org.deidentifier.arx.gui.view.impl.menu.hierarchy;

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.AggregateFunction;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.menu.hierarchy.HierarchyModel.HierarchyGroup;
import org.deidentifier.arx.gui.view.impl.menu.hierarchy.HierarchyModel.HierarchyInterval;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Just a test class
 * @author Fabian Prasser
 *
 */
public class HierarchyWizard {
    
    HierarchyWizard(final Shell shell){
        HierarchyModel<Long> model = new HierarchyModel<Long>(DataType.INTEGER, true);
        model.getIntervals().clear();
        model.addInterval(new HierarchyInterval<Long>(0l, 1l, model.getDefaultFunction()));
        model.addInterval(new HierarchyInterval<Long>(1l, 3l, model.getDefaultFunction()));
        model.addInterval(new HierarchyInterval<Long>(3l, 5l, model.getDefaultFunction()));
        model.addInterval(new HierarchyInterval<Long>(5l, 9l, model.getDefaultFunction()));
        model.addInterval(new HierarchyInterval<Long>(9l, 13l, model.getDefaultFunction()));
        model.addInterval(new HierarchyInterval<Long>(13l, 15l, model.getDefaultFunction()));
        List<HierarchyGroup<Long>> level1 = new ArrayList<HierarchyGroup<Long>>();
        model.addGroups(level1);
        level1.add(new HierarchyGroup<Long>(2, model.getDefaultFunction()));
        level1.add(new HierarchyGroup<Long>(3, model.getDefaultFunction()));
        level1.add(new HierarchyGroup<Long>(4, AggregateFunction.CONSTANT(DataType.INTEGER, "TESTESTESTEST")));
        List<HierarchyGroup<Long>> level2 = new ArrayList<HierarchyGroup<Long>>();
        model.addGroups(level2);
        level2.add(new HierarchyGroup<Long>(2, model.getDefaultFunction()));
        model.update();
        
        GridLayout layout = SWTUtil.createGridLayout(1);
        layout.marginLeft = 5;
        layout.marginRight = 5;
        layout.marginTop = 5;
        layout.marginBottom = 5;
        shell.setLayout(layout);
        HierarchyEditor<Long> component =  new HierarchyEditor<Long>(shell, model);
        component.setLayoutData(SWTUtil.createFillGridData());
        model.update();
    }

    public static void main(String[] args) {
        Display display = new Display ();
        Shell shell = new Shell(display);
        new HierarchyWizard(shell);
        shell.setSize(800, 600);
        shell.open ();
        while (!shell.isDisposed ()) {
            if (!display.readAndDispatch ()) display.sleep ();
        }
        display.dispose ();
    }
}
