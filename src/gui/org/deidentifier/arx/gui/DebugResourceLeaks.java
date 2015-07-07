package org.deidentifier.arx.gui;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.DeviceData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * Code based on: https://www.eclipse.org/articles/swt-design-2/sleak.htm
 *
 */
public class DebugResourceLeaks {
    
    public static void main(String[] args) {
        DebugResourceLeaks sleak = new DebugResourceLeaks();
        Display display = sleak.open();
        Main.main(display, new String[0]);
    }
    
    private Display display;
    
    private Shell shell;
    
    private Label objectStatistics;
    private Label objectStackTrace;
    
    private List listNewObjects;
    private List listEqualObjects;
    
    private Object[] newObjects;
    
    private Error[] newErrors;
    
    private Object[] equalObjects;
    
    private Error[] equalErrors;
    
    private void collectAll() {
        DeviceData info = display.getDeviceData();
        if (!info.tracking) {
            MessageBox dialog = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
            dialog.setText(shell.getText());
            dialog.setMessage("Warning: Device is not tracking resource allocation");
            dialog.open();
        }
        newObjects = info.objects;
        newErrors = info.errors;
        
        Map<String, Integer> objectTypesTimes = new TreeMap<String, Integer>();
        Map<String, Object> objectSameStackTrace = new HashMap<String, Object>();
        final Map<Object, Integer> objectSameStackTraceTimes = new HashMap<Object, Integer>();
        Map<Object, Integer> objectSameStackTraceIDX = new HashMap<Object, Integer>();
        
        for (int i = 0; i < newObjects.length; i++) {
            String className = newObjects[i].getClass().getSimpleName();
            Integer count = objectTypesTimes.get(className);
            if (count == null) {
                objectTypesTimes.put(className, 1);
            } else {
                objectTypesTimes.put(className, count + 1);
            }
            
            String stackTrace = getStackTrace(newErrors[i]);
            if (!objectSameStackTrace.containsKey(stackTrace)) {
                objectSameStackTrace.put(stackTrace, newObjects[i]);
                objectSameStackTraceTimes.put(newObjects[i], 1);
                objectSameStackTraceIDX.put(newObjects[i], i);
            } else {
                Object object = objectSameStackTrace.get(stackTrace);
                objectSameStackTraceTimes.put(object, objectSameStackTraceTimes.get(object) + 1);
            }
        }
        
        equalObjects = new Object[objectSameStackTrace.size()];
        equalErrors = new Error[objectSameStackTrace.size()];
        
        int idx = 0;
        for (Entry<String, Object> entry : objectSameStackTrace.entrySet()) {
            equalObjects[idx++] = entry.getValue();
        }
        
        Arrays.sort(equalObjects, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                return objectSameStackTraceTimes.get(o2) - objectSameStackTraceTimes.get(o1);
            }
        });
        
        for (int i = 0; i < equalErrors.length; i++) {
            equalErrors[i] = newErrors[objectSameStackTraceIDX.get(equalObjects[i])];
        }
        
        StringBuilder statistics = new StringBuilder();
        
        for (Entry<String, Integer> entry : objectTypesTimes.entrySet()) {
            statistics.append(entry.getKey());
            statistics.append(": ");
            statistics.append(entry.getValue());
            statistics.append("\n");
        }
        statistics.append("Total: ");
        statistics.append(newObjects.length);
        statistics.append("\n");
        
        // Display
        listNewObjects.removeAll();
        for (int i = 0; i < newObjects.length; i++) {
            listNewObjects.add(newObjects[i].getClass().getSimpleName() + "(" + newObjects[i].hashCode() + ")");
        }
        
        listEqualObjects.removeAll();
        for (int i = 0; i < equalObjects.length; i++) {
            listEqualObjects.add(equalObjects[i].getClass().getSimpleName() + "(" + equalObjects[i].hashCode() + ")" + "[" + objectSameStackTraceTimes.get(equalObjects[i]) + "x]");
        }
        
        objectStatistics.setText(statistics.toString());
    }
    
    private String getStackTrace(Error error) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PrintStream s = new PrintStream(stream);
        error.printStackTrace(s);
        return stream.toString();
    }
    
    private Display open() {
        DeviceData data = new DeviceData();
        data.tracking = true;
        Display display = new Display(data);
        
        this.display = display;
        shell = new Shell(display);
        
        shell.setText("Resources");
        
        shell.setLayout(SWTUtil.createGridLayout(2));
        
        Button collect = new Button(shell, SWT.PUSH);
        collect.setText("Collect data");
        collect.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                collectAll();
            }
        });
        
        final GridData d = new GridData();
        d.grabExcessHorizontalSpace = true;
        d.horizontalSpan = 2;
        collect.setLayoutData(d);
        
        listNewObjects = new List(shell, SWT.BORDER | SWT.V_SCROLL);
        listNewObjects.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                selectObject();
            }
        });
        listNewObjects.setLayoutData(SWTUtil.createFillGridData());
        
        listEqualObjects = new List(shell, SWT.BORDER | SWT.V_SCROLL);
        listEqualObjects.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                selectEqualObject();
            }
        });
        listEqualObjects.setLayoutData(SWTUtil.createFillGridData());
        
        objectStackTrace = new Label(shell, SWT.BORDER);
        objectStackTrace.setText("");
        objectStackTrace.setLayoutData(SWTUtil.createFillGridData());
        
        objectStatistics = new Label(shell, SWT.BORDER);
        objectStatistics.setText("0 object(s)");
        objectStatistics.setLayoutData(SWTUtil.createFillGridData());
        
        shell.open();
        
        return display;
    }
    
    private void selectEqualObject() {
        int index = listEqualObjects.getSelectionIndex();
        if (index == -1) {
            return;
        }
        
        objectStackTrace.setText(getStackTrace(equalErrors[index]));
        objectStackTrace.setVisible(true);
    }
    
    private void selectObject() {
        int index = listNewObjects.getSelectionIndex();
        if (index == -1) {
            return;
        }
        
        objectStackTrace.setText(getStackTrace(newErrors[index]));
        objectStackTrace.setVisible(true);
    }
    
}
