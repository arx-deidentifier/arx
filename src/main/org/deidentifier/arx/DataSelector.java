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

package org.deidentifier.arx;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.deidentifier.arx.DataType.ARXDate;
import org.deidentifier.arx.DataType.ARXDecimal;

import de.linearbits.objectselector.IAccessor;
import de.linearbits.objectselector.Selector;
import de.linearbits.objectselector.SelectorBuilder;
import de.linearbits.objectselector.datatypes.DataType;

/**
 * A selector for tuples.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class DataSelector {
    
    /**
     * An accessor for data elements.
     *
     * @author Fabian Prasser
     */
    private class DataAccessor implements IAccessor<Integer>{

        /** The data handle. */
        private final DataHandle handle;
        
        /** The data definition. */
        private final DataDefinition definition;
        
        /** The data types. */
        private final Map<String, DataType<?>> types;
        
        /** The indices. */
        private final Map<String, Integer> indices;
        
        /**
         * Creates a new instance.
         *
         * @param data
         */
        protected DataAccessor(Data data){
            this.handle = data.getHandle();
            this.definition = data.getDefinition();
            this.types = getTypes();
            this.indices = getIndices();
        }
        
        /* (non-Javadoc)
         * @see de.linearbits.objectselector.IAccessor#exists(java.lang.String)
         */
        @Override
        public boolean exists(String arg0) {
            return indices.containsKey(arg0);
        }

        /* (non-Javadoc)
         * @see de.linearbits.objectselector.IAccessor#getType(java.lang.String)
         */
        @Override
        public DataType<?> getType(String arg0) {
            return types.get(arg0);
        }

        /* (non-Javadoc)
         * @see de.linearbits.objectselector.IAccessor#getValue(java.lang.Object, java.lang.String)
         */
        @Override
        public Object getValue(Integer arg0, String arg1) {
            
            int column = indices.get(arg1);
            DataType<?> type = types.get(arg1);
            String value = handle.getValue(arg0, column);
            return type.fromString(value);
        }

        /* (non-Javadoc)
         * @see de.linearbits.objectselector.IAccessor#isDataTypesSupported()
         */
        @Override
        public boolean isDataTypesSupported() {
            return true;
        }

        /* (non-Javadoc)
         * @see de.linearbits.objectselector.IAccessor#isExistanceSupported()
         */
        @Override
        public boolean isExistanceSupported() {
            return true;
        }

        /**
         * Returns the indices.
         *
         * @return
         */
        private Map<String, Integer> getIndices() {
            Map<String, Integer> result = new HashMap<String, Integer>();
            for (int i=0; i<handle.getNumColumns(); i++){
                result.put(handle.getAttributeName(i), i);
            }
            return result;
        }

        /**
         * Returns the data types.
         *
         * @return
         */
        private Map<String, DataType<?>> getTypes() {
            Map<String, DataType<?>> result = new HashMap<String, DataType<?>>();
            for (int i=0; i<handle.getNumColumns(); i++){
                String attribute = handle.getAttributeName(i);
                org.deidentifier.arx.DataType<?> type = definition.getDataType(attribute);
                if (type instanceof org.deidentifier.arx.DataType.ARXDecimal){
                    String format = ((ARXDecimal)type).getFormat();
                    result.put(attribute, DataType.NUMERIC(format));
                } else if (type instanceof org.deidentifier.arx.DataType.ARXInteger) {
                    result.put(attribute, DataType.NUMERIC);
                } else if (type instanceof org.deidentifier.arx.DataType.ARXString) {
                    result.put(attribute, DataType.STRING);                    
                } else if (type instanceof org.deidentifier.arx.DataType.ARXDate){
                    String format = ((ARXDate)type).getFormat();
                    result.put(attribute, DataType.DATE(format));
                } else {
                    result.put(attribute, DataType.STRING);
                }
            }
            return result;
        }        
    }
    
    /**
     * 
     *
     * @param data
     * @return
     */
    public static DataSelector create(Data data){
        return new DataSelector(data);
    }
    
    /**
     * 
     *
     * @param data
     * @param query
     * @return
     * @throws ParseException
     */
    public static DataSelector create(Data data, String query) throws ParseException{
        return new DataSelector(data, query);
    }
   
    /** The builder. */
    private final SelectorBuilder<Integer> builder;

    /** The selector. */
    private Selector<Integer> selector = null;

    /**
     * 
     *
     * @param data
     */
    private DataSelector(Data data){
        this.builder = new SelectorBuilder<Integer>(new DataAccessor(data)); 
    }

    /**
     * 
     *
     * @param data
     * @param query
     * @throws ParseException
     */
    private DataSelector(Data data, String query) throws ParseException {
        this.builder = new SelectorBuilder<Integer>(new DataAccessor(data), query);
    }
    
    /**
     * 
     *
     * @return
     */
    public DataSelector and(){
        this.builder.and();
        return this;
    }
    
    /**
     * 
     *
     * @return
     */
    public DataSelector begin(){
        this.builder.begin();
        return this;
    }
    
    /**
     * 
     *
     * @throws ParseException
     */
    public void build() throws ParseException{
        this.selector = this.builder.build();
    }
    
    /**
     * 
     *
     * @return
     */
    public DataSelector end(){
        this.builder.end();
        return this;
    }
    
    /**
     * 
     *
     * @param val
     * @return
     */
    public DataSelector equals(final Date val){
        this.builder.equals(val);
        return this;
    }
    
    /**
     * 
     *
     * @param val
     * @return
     */
    public DataSelector equals(final double val){
        this.builder.equals(val);
        return this;
    }
    
    /**
     * 
     *
     * @param val
     * @return
     */
    public DataSelector equals(final String val){
        this.builder.equals(val);
        return this;
    }
    
    /**
     * 
     *
     * @param name
     * @return
     */
    public DataSelector field(String name){
        this.builder.field(name);
        return this;
    }
    
    /**
     * 
     *
     * @param val
     * @return
     */
    public DataSelector geq(final Date val){
        this.builder.geq(val);
        return this;
    }
    
    /**
     * 
     *
     * @param val
     * @return
     */
    public DataSelector geq(final double val){
        this.builder.geq(val);
        return this;
    }
    
    /**
     * 
     *
     * @param val
     * @return
     */
    public DataSelector geq(final String val){
        this.builder.geq(val);
        return this;
    }
    
    /**
     * 
     *
     * @param val
     * @return
     */
    public DataSelector greater(final Date val){
        this.builder.greater(val);
        return this;
    }
    
    /**
     * 
     *
     * @param val
     * @return
     */
    public DataSelector greater(final double val){
        this.builder.greater(val);
        return this;
    }
    
    /**
     * 
     *
     * @param val
     * @return
     */
    public DataSelector greater(final String val){
        this.builder.greater(val);
        return this;
    }
    
    /**
     * Determines whether the given row is selected by the expression.
     *
     * @param row
     * @return
     */
    public boolean isSelected(int row){
        if (selector == null) {
            try {
                build();
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        return selector.isSelected(row);
    }

    /* **************************************
     * Datetime
     * **************************************/
    /**
     * 
     *
     * @param val
     * @return
     */
    public DataSelector leq(final Date val){
        this.builder.leq(val);
        return this;
    }
    
    /*
     * NUMERIC
     */
    /**
     * 
     *
     * @param val
     * @return
     */
    public DataSelector leq(final double val){
        this.builder.leq(val);
        return this;
    }
    
    /* **************************************
     * STRING
     * **************************************/
    /**
     * 
     *
     * @param val
     * @return
     */
    public DataSelector leq(final String val){
        this.builder.leq(val);
        return this;
    }
    
    /**
     * 
     *
     * @param val
     * @return
     */
    public DataSelector less(final Date val){
        this.builder.less(val);
        return this;
    }
    
    /**
     * 
     *
     * @param val
     * @return
     */
    public DataSelector less(final double val){
        this.builder.less(val);
        return this;
    }
    
    /**
     * 
     *
     * @param val
     * @return
     */
    public DataSelector less(final String val){
        this.builder.less(val);
        return this;
    }

    /**
     * 
     *
     * @return
     */
    public DataSelector or(){
        this.builder.or();
        return this;
    }
}
