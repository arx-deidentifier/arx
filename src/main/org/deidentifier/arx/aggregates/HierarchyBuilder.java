/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.deidentifier.arx.aggregates;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.deidentifier.arx.AttributeType.Hierarchy;

/**
 * Base class for hierarchy builders. Hierarchies can be built in two ways:<br> 
 * 1. Call prepare(data), which returns some metadata and preserves a state, and then calling build(), or<br>
 * 2. Call build(data)
 * 
 * @author Fabian Prasser
 *
 */
public abstract class HierarchyBuilder<T> implements Serializable {
    
    private static final long serialVersionUID = -4182364711973630816L;
    /** The type*/
    private Type type;
    
    /**
     * Creates a new instance
     * @param type
     */
    protected HierarchyBuilder(Type type){
        this.type = type;
    }
    
    /**
     * Loads a builder from a file
     * @param file
     * @return
     * @throws IOException 
     */
    public static <T> HierarchyBuilder<T> create(String file) throws IOException{
        return create(new File(file));
    }

    /**
     * Loads a builder from a file
     * @param file
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> HierarchyBuilder<T> create(File file) throws IOException{
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(file));
            HierarchyBuilder<T> result = (HierarchyBuilder<T>)ois.readObject();
            return result;
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            if (ois != null) ois.close();
        }
    }
    
    /**
     * The three types of builders
     * @author Fabian Prasser
     */
    public static enum Type {
        INTERVAL_BASED,
        ORDER_BASED,
        REDACTION_BASED
    }
    /**
     * Creates a new hierarchy, based on the predefined specification
     * @param data
     * @return
     */
    public abstract Hierarchy build(String[] data);
    
    /**
     * Creates a new hierarchy, based on the predefined specification
     * @return
     */
    public abstract Hierarchy build();
    
    /**
     * Prepares the builder. Returns a list of the number of equivalence classes per level
     * @return
     */
    public abstract int[] prepare(String[] data);
    

    /**
     * Saves the specification of this builder to the given file
     * @param file
     * @throws IOException
     */
    public void save(String file) throws IOException{
        save(new File(file));
    }
    
    /**
     * Saves the specification of this builder to the given file
     * @param file
     * @throws IOException
     */
    public void save(File file) throws IOException{
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(this);
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            if (oos != null) oos.close();
        }
    }

    /**
     * Returns the type of builder
     * @return
     */
    public Type getType() {
        return type;
    }
}
