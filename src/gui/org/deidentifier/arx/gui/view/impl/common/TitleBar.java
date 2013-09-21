package org.deidentifier.arx.gui.view.impl.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Image;

public class TitleBar {
    
    private List<String> titles = new ArrayList<String>();
    private Map<String, Image> images = new HashMap<String, Image>();
    private Map<String, Runnable> runnables = new HashMap<String, Runnable>();
    private String id;
    
    public TitleBar(String id) {    
        this.id = id;
    }

    public void add(String title, Image image, Runnable runnable){
        this.titles.add(title);
        this.images.put(title, image);
        this.runnables.put(title, runnable);
    }

    public List<String> getTitles() {
        return titles;
    }

    public Image getImage(String title) {
        return images.get(title);
    }

    public Runnable getRunnable(String title) {
        return runnables.get(title);
    }

    public String getId(){
        return id;
    }
}
