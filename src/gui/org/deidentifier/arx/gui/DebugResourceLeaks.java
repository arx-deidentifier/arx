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
    
    class Resource {
        Object resource;
        Error  error;
        int    occurrences;
        
        @Override
        protected Resource clone() {
            Resource resource = new Resource();
            resource.error = error;
            resource.resource = this.resource;
            resource.occurrences = occurrences;
            return resource;
        }
    }
    
    public static void main(String[] args) {
        DebugResourceLeaks sleak = new DebugResourceLeaks();
        Display display = sleak.open();
        Main.main(display, new String[0]);
    }
    
    private Display display;
    
    private Shell shell;
    
    private Label resourceStatistics;
    private Label resourceStackTrace;
    
    private List listResources;
    private List listResourcesSameStackTrace;
    
    private Resource[] resources;
    private Resource[] resourcesSameStackTrace;
    
    private void collectAll() {
        DeviceData info = display.getDeviceData();
        if (!info.tracking) {
            MessageBox dialog = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
            dialog.setText(shell.getText());
            dialog.setMessage("Warning: Device is not tracking resource allocation");
            dialog.open();
        }
        
        Object[] objects = info.objects;
        Error[] errors = info.errors;
        
        resources = new Resource[objects.length];
        for (int i = 0; i < resources.length; i++) {
            
            Resource resource = new Resource();
            resource.error = errors[i];
            resource.resource = objects[i];
            resource.occurrences = 1;
            resources[i] = resource;
        }
        
        Map<String, Integer> objectTypesTimes = new TreeMap<String, Integer>();
        Map<String, Resource> objectSameStackTrace = new HashMap<String, Resource>();
        
        for (int i = 0; i < resources.length; i++) {
            String className = resources[i].resource.getClass().getSimpleName();
            Integer count = objectTypesTimes.get(className);
            if (count == null) {
                objectTypesTimes.put(className, 1);
            } else {
                objectTypesTimes.put(className, count + 1);
            }
            
            String stackTrace = getStackTrace(resources[i].error);
            if (!objectSameStackTrace.containsKey(stackTrace)) {
                Resource resource = resources[i].clone();
                resource.occurrences = 1;
                objectSameStackTrace.put(stackTrace, resource);
            } else {
                Resource resource = objectSameStackTrace.get(stackTrace);
                resource.occurrences++;
            }
        }
        
        resourcesSameStackTrace = new Resource[objectSameStackTrace.size()];
        int idx = 0;
        for (Entry<String, Resource> entry : objectSameStackTrace.entrySet()) {
            resourcesSameStackTrace[idx] = entry.getValue();
            idx++;
        }
        
        Arrays.sort(resourcesSameStackTrace, new Comparator<Resource>() {
            @Override
            public int compare(Resource o1, Resource o2) {
                return o2.occurrences - o1.occurrences;
            }
        });
        
        StringBuilder statistics = new StringBuilder();
        
        for (Entry<String, Integer> entry : objectTypesTimes.entrySet()) {
            statistics.append(entry.getKey());
            statistics.append(": ");
            statistics.append(entry.getValue());
            statistics.append("\n");
        }
        statistics.append("Total: ");
        statistics.append(resources.length);
        statistics.append("\n");
        
        // Display
        listResources.removeAll();
        for (int i = 0; i < resources.length; i++) {
            listResources.add(resources[i].resource.getClass().getSimpleName() + "(" + resources[i].resource.hashCode() + ")");
        }
        
        listResourcesSameStackTrace.removeAll();
        for (int i = 0; i < resourcesSameStackTrace.length; i++) {
            listResourcesSameStackTrace.add(resourcesSameStackTrace[i].resource.getClass().getSimpleName() + "(" + resourcesSameStackTrace[i].resource.hashCode() + ")" + "[" + resourcesSameStackTrace[i].occurrences + "x]");
        }
        
        resourceStatistics.setText(statistics.toString());
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
        
        listResources = new List(shell, SWT.BORDER | SWT.V_SCROLL);
        listResources.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                selectObject();
            }
        });
        listResources.setLayoutData(SWTUtil.createFillGridData());
        
        listResourcesSameStackTrace = new List(shell, SWT.BORDER | SWT.V_SCROLL);
        listResourcesSameStackTrace.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                selectEqualObject();
            }
        });
        listResourcesSameStackTrace.setLayoutData(SWTUtil.createFillGridData());
        
        resourceStackTrace = new Label(shell, SWT.BORDER);
        resourceStackTrace.setText("");
        resourceStackTrace.setLayoutData(SWTUtil.createFillGridData());
        
        resourceStatistics = new Label(shell, SWT.BORDER);
        resourceStatistics.setText("0 object(s)");
        resourceStatistics.setLayoutData(SWTUtil.createFillGridData());
        
        shell.open();
        
        return display;
    }
    
    private void selectEqualObject() {
        int index = listResourcesSameStackTrace.getSelectionIndex();
        if (index == -1) {
            return;
        }
        
        resourceStackTrace.setText(getStackTrace(resourcesSameStackTrace[index].error));
        resourceStackTrace.setVisible(true);
    }
    
    private void selectObject() {
        int index = listResources.getSelectionIndex();
        if (index == -1) {
            return;
        }
        
        resourceStackTrace.setText(getStackTrace(resources[index].error));
        resourceStackTrace.setVisible(true);
    }
    
}
