/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * @param <T>
 */
public abstract class HierarchyBuilder<T> implements Serializable {
    
    /**  TODO */
    private static final long serialVersionUID = -4182364711973630816L;
    
    /** The type. */
    private Type type;
    
    /**
     * Creates a new instance.
     *
     * @param type
     */
    protected HierarchyBuilder(Type type){
        this.type = type;
    }
    
    /**
     * Loads a builder from a file.
     *
     * @param <T>
     * @param file
     * @return
     * @throws IOException
     */
    public static <T> HierarchyBuilder<T> create(String file) throws IOException{
        return create(new File(file));
    }

    /**
     * Loads a builder from a file.
     *
     * @param <T>
     * @param file
     * @return
     * @throws IOException
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
     * The three types of builders.
     *
     * @author Fabian Prasser
     */
    public static enum Type {
        
        /**  TODO */
        INTERVAL_BASED,
        
        /**  TODO */
        ORDER_BASED,
        
        /**  TODO */
        REDACTION_BASED
    }
    
    /**
     * Creates a new hierarchy, based on the predefined specification.
     *
     * @param data
     * @return
     */
    public abstract Hierarchy build(String[] data);
    
    /**
     * Creates a new hierarchy, based on the predefined specification.
     *
     * @return
     */
    public abstract Hierarchy build();
    
    /**
     * Prepares the builder. Returns a list of the number of equivalence classes per level
     *
     * @param data
     * @return
     */
    public abstract int[] prepare(String[] data);
    

    /**
     * Saves the specification of this builder to the given file.
     *
     * @param file
     * @throws IOException
     */
    public void save(String file) throws IOException{
        save(new File(file));
    }
    
    /**
     * Saves the specification of this builder to the given file.
     *
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
     * Returns the type of builder.
     *
     * @return
     */
    public Type getType() {
        return type;
    }
}
