package org.deidentifier.arx;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import de.linearbits.objectselector.IAccessor;
import de.linearbits.objectselector.Selector;
import de.linearbits.objectselector.SelectorBuilder;
import de.linearbits.objectselector.datatypes.DataType;
import de.linearbits.objectselector.ops.BinaryOperator;
import de.linearbits.objectselector.ops.UnaryOperator;

/**
 * A selector for tuples
 * @author Prasser, Kohlmayer
 *
 */
public class DataSelector {
    
    private class DataAccessor implements IAccessor<Integer>{
        
        protected DataAccessor(Data data){
            
        }
        
        @Override
        public boolean exists(String arg0) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public DataType<?> getType(String arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Object getValue(Integer arg0, String arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isDataTypesSupported() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isExistanceSupported() {
            // TODO Auto-generated method stub
            return false;
        }
        
    }
    
    /** The data handle*/
    private final DataHandle handle;
    /** The builder*/
    private final SelectorBuilder<Integer> builder;
    /** The selector*/
    private Selector<Integer> selector = null;
   
    private DataSelector(Data data){
        this.handle = data.getHandle();
        this.builder = new SelectorBuilder<Integer>(new DataAccessor(data)); 
    }

    private DataSelector(Data data, String query) throws ParseException {
        this.handle = data.getHandle();
        this.builder = new SelectorBuilder<Integer>(new DataAccessor(data), query);
    }

    public static DataSelector create(Data data){
        return new DataSelector(data);
    }

    public static DataSelector create(Data data, String query) throws ParseException{
        return new DataSelector(data, query);
    }
    
    public DataSelector field(String name){
        this.builder.field(name);
        return this;
    }
    
    public DataSelector and(){
        this.builder.and();
        return this;
    }
    
    public DataSelector or(){
        this.builder.or();
        return this;
    }
    
    public DataSelector begin(){
        this.builder.begin();
        return this;
    }
    
    public DataSelector end(){
        this.builder.end();
        return this;
    }
    
    /*
     * NUMERIC
     */
    public DataSelector leq(final double val){
        this.builder.leq(val);
        return this;
    }
    
    public DataSelector geq(final double val){
        this.builder.geq(val);
        return this;
    }
    
    public DataSelector less(final double val){
        this.builder.less(val);
        return this;
    }
    
    public DataSelector greater(final double val){
        this.builder.greater(val);
        return this;
    }
    
    public DataSelector equals(final double val){
        this.builder.equals(val);
        return this;
    }
    
    /* **************************************
     * STRING
     * **************************************/
    public DataSelector leq(final String val){
        this.builder.leq(val);
        return this;
    }
    
    public DataSelector geq(final String val){
        this.builder.geq(val);
        return this;
    }
    
    public DataSelector less(final String val){
        this.builder.less(val);
        return this;
    }
    
    public DataSelector greater(final String val){
        this.builder.greater(val);
        return this;
    }
    
    public DataSelector equals(final String val){
        this.builder.equals(val);
        return this;
    }

    /* **************************************
     * Datetime
     * **************************************/
    public DataSelector leq(final Date val){
        this.builder.leq(val);
        return this;
    }
    
    public DataSelector geq(final Date val){
        this.builder.geq(val);
        return this;
    }
    
    public DataSelector less(final Date val){
        this.builder.less(val);
        return this;
    }
    
    public DataSelector greater(final Date val){
        this.builder.greater(val);
        return this;
    }
    
    public DataSelector equals(final Date val){
        this.builder.equals(val);
        return this;
    }
    
    public void build() throws ParseException{
        this.selector = this.builder.build();
    }

    /**
     * Determines whether the given row is selected by the expression
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
}
