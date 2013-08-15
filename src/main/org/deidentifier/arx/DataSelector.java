package org.deidentifier.arx;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A selector for tuples
 * @author Prasser, Kohlmayer
 *
 */
public class DataSelector {
    
    /* **************************************
     * Operators
     * **************************************/
    private abstract class Operator {
        protected final int params;
        private Operator(int params){
            this.params = params;
        }
        
        abstract boolean eval(int row);
    }
    
    private class PrecedenceOperator extends Operator{
        protected final boolean begin;
        private PrecedenceOperator(boolean begin) {
            super(0);
            this.begin = begin;
        }
        @Override
        boolean eval(int row) {
            throw new RuntimeException("Cannot be evaluated");
        }
    }
    
    private abstract class UnaryOperator extends Operator{
        private int column;
        private DataType<?> type;
        private UnaryOperator(String context) {
            super(1);
            if (context == null){
                throw new IllegalArgumentException("No context specified!");
            }
            column = handle.getColumnIndexOf(context);
            type = handle.getDataType(context);
        }
        
        protected double getDouble(int row){
            return (Double)type.fromString(handle.getValue(row, column));
        }
        
        protected String getString(int row){
            return handle.getValue(row, column);
        }

        protected Date getDate(int row){
            return (Date)type.fromString(handle.getValue(row, column));
        }
    }
    
    private abstract class BinaryOperator extends Operator{
        protected Operator left;
        protected Operator right;
        private BinaryOperator() {
            super(2);
        }
    }
    
    private class CompiledOperator {
        public final Operator operator;
        public final int lOffset;
        public final int lLength;
        public final int rOffset;
        public final int rLength;
        private CompiledOperator(Operator operator, int lOffset, int lLength, int rOffset, int rLength) {
            this.operator = operator;
            this.lOffset = lOffset;
            this.lLength = lLength;
            this.rOffset = rOffset;
            this.rLength = rLength;
        }
    }
    
    /* **************************************
     * Class
     * **************************************/
    
    /** The data handle*/
    private final DataHandle handle;
    /** The list of operators defined via the builder pattern*/
    private final List<Operator> operators;
    /** The current context (field)*/
    private String context = null;
    /** The root operator after compilation*/
    private Operator root = null;
    
    private DataSelector(Data data){
        this.handle = data.getHandle();
        this.operators = new ArrayList<Operator>();
    }
    
    public static DataSelector create(Data data){
        return new DataSelector(data);
    }
    
    public DataSelector field(String name){
        this.context = name;
        return this;
    }
    
    public DataSelector and(){
        operators.add(new BinaryOperator(){
            @Override
            boolean eval(int row) {
                return left.eval(row) && right.eval(row);
            }
        });
        return this;
    }
    
    public DataSelector or(){
        operators.add(new BinaryOperator(){
            @Override
            boolean eval(int row) {
                return left.eval(row) || right.eval(row);
            }
        });
        return this;
    }
    
    public DataSelector begin(){
        operators.add(new PrecedenceOperator(true));
        return this;
    }
    
    public DataSelector end(){
        operators.add(new PrecedenceOperator(false));
        return this;
    }
    
    /*
     * NUMERIC
     */
    public DataSelector leq(final double val){
        operators.add(new UnaryOperator(context){
            @Override
            boolean eval(int row) {
                return getDouble(row) <= val;
            }
        });
        return this;
    }
    
    public DataSelector geq(final double val){
        operators.add(new UnaryOperator(context){
            @Override
            boolean eval(int row) {
                return getDouble(row) >= val;
            }
        });
        return this;
    }
    
    public DataSelector less(final double val){
        operators.add(new UnaryOperator(context){
            @Override
            boolean eval(int row) {
                return getDouble(row) < val;
            }
        });
        return this;
    }
    
    public DataSelector greater(final double val){
        operators.add(new UnaryOperator(context){
            @Override
            boolean eval(int row) {
                return getDouble(row) > val;
            }
        });
        return this;
    }
    
    public DataSelector equals(final double val){
        operators.add(new UnaryOperator(context){
            @Override
            boolean eval(int row) {
                return getDouble(row) == val;
            }
        });
        return this;
    }
    
    /* **************************************
     * STRING
     * **************************************/
    public DataSelector leq(final String val){
        operators.add(new UnaryOperator(context){
            @Override
            boolean eval(int row) {
                return getString(row).compareTo(val) <= 0;
            }
        });
        return this;
    }
    
    public DataSelector geq(final String val){
        operators.add(new UnaryOperator(context){
            @Override
            boolean eval(int row) {
                return getString(row).compareTo(val) >= 0;
            }
        });
        return this;
    }
    
    public DataSelector less(final String val){
        operators.add(new UnaryOperator(context){
            @Override
            boolean eval(int row) {
                return getString(row).compareTo(val) < 0;
            }
        });
        return this;
    }
    
    public DataSelector greater(final String val){
        operators.add(new UnaryOperator(context){
            @Override
            boolean eval(int row) {
                return getString(row).compareTo(val) > 0;
            }
        });
        return this;
    }
    
    public DataSelector equals(final String val){
        operators.add(new UnaryOperator(context){
            @Override
            boolean eval(int row) {
                return getString(row).compareTo(val) == 0;
            }
        });
        return this;
    }

    /* **************************************
     * Datetime
     * **************************************/
    public DataSelector leq(final Date val){
        operators.add(new UnaryOperator(context){
            @Override
            boolean eval(int row) {
                return getDate(row).compareTo(val) <= 0;
            }
        });
        return this;
    }
    
    public DataSelector geq(final Date val){
        operators.add(new UnaryOperator(context){
            @Override
            boolean eval(int row) {
                return getDate(row).compareTo(val) >= 0;
            }
        });
        return this;
    }
    
    public DataSelector less(final Date val){
        operators.add(new UnaryOperator(context){
            @Override
            boolean eval(int row) {
                return getDate(row).compareTo(val) < 0;
            }
        });
        return this;
    }
    
    public DataSelector greater(final Date val){
        operators.add(new UnaryOperator(context){
            @Override
            boolean eval(int row) {
                return getDate(row).compareTo(val) > 0;
            }
        });
        return this;
    }
    
    public DataSelector equals(final Date val){
        operators.add(new UnaryOperator(context){
            @Override
            boolean eval(int row) {
                return getDate(row).compareTo(val) == 0;
            }
        });
        return this;
    }
    
    /* **************************************
     * Compile
     * **************************************/
    protected void compile(){
        CompiledOperator op = compile(0, operators.size());
        if (op.operator == null || op.lLength != 0 || op.rLength != 0) {
            throw new RuntimeException("Cannot parse expression!");
        } else {
            this.root = op.operator;
        }
    }

    private CompiledOperator compile(int offset, int length) {
        // TODO Auto-generated method stub
        return null;
    }
}
