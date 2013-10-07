package org.deidentifier.arx.gui.view.impl.menu;

import java.util.ArrayList;
import java.util.List;

public class DialogHelpConfig {

    public static class Entry {
        
        public final String id;
        public final String title;
        public final String url;
        private Entry(String id, String title, String url) {
            this.id = id;
            this.title = title;
            this.url = url;
        }
    }
    
    private List<Entry> entries = new ArrayList<Entry>();
    
    public DialogHelpConfig(){
        entries.add(new Entry("id-1", "1. Introduction", "http://arx.deidentifier.org/?page_id=1049&content-only=1&css=1"));
        entries.add(new Entry("id-2", "1.1. Viewing data", "http://arx.deidentifier.org/?page_id=1049&content-only=1&css=1"));
        entries.add(new Entry("id-3", "1.2. Defining privacy guarantees", "http://arx.deidentifier.org/?page_id=1049&content-only=1&css=1"));
        entries.add(new Entry("id-4", "2. Introduction", "http://arx.deidentifier.org/?page_id=1049&content-only=1&css=1"));
        entries.add(new Entry("id-5", "2.1. Viewing data", "http://arx.deidentifier.org/?page_id=1049&content-only=1&css=1"));
        entries.add(new Entry("id-6", "2.2. Defining privacy guarantees", "http://arx.deidentifier.org/?page_id=1049&content-only=1&css=1"));
    }
    
    public List<Entry> getEntries(){
        return this.entries;
    }

    public String getUrlOf(int index) {
        return entries.get(index).url;
    }

    public int getIndexOf(String url) {
        for (int i = 0; i < entries.size(); i++){
            if (entries.get(i).url.equals(url)) {
                return i;
            }
        }
        return -1;
    }
}
