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
                throw new IllegalArgumentException("No context specified");
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
    public void compile(){
        if (operators.isEmpty()) {
            throw new RuntimeException("Empty expression");
        }
        this.root = compile(operators, 0, operators.size());
        if (this.root == null) {
            throw new RuntimeException("Cannot parse expression");
        } 
    }

    /**
     * Parses the list of operators within the given range
     * @param ops
     * @param offset
     * @param length
     * @return
     */
    private Operator compile(List<Operator> ops, int offset, int length) {
        
        int lLength = findExpression(ops, offset, length);
        
        if (lLength == length){
            
            // Case 1: EXPR
            if (length == 1){
                
                // Return single operator
                return ops.get(offset);
                
            } else if ((ops.get(offset) instanceof PrecedenceOperator) &&
                       (ops.get(offset + length - 1) instanceof PrecedenceOperator)){
                
                // Remove brackets
                return compile(ops, offset+1, length-2);
                
            } else {
                throw new RuntimeException("Invalid expression");
            }
            
        } else {
            
            // Case 2: EXPR <OP> EXPR
            if (!(ops.get(offset + lLength) instanceof BinaryOperator)){
                
                // Invalid
                throw new RuntimeException("Expecting EXPR <OP> EXPR");
            } else {
                
                // Binary operator
                BinaryOperator bop = (BinaryOperator)ops.get(offset + lLength);
                bop.left = compile(ops, offset, lLength);
                bop.right = compile(ops, offset + lLength + 1, length - lLength - 1);
                return bop;
            }
        }
    }

    /**
     * Finds an expression within the given range
     * @param ops
     * @param offset
     * @param length
     * @return
     */
    private int findExpression(List<Operator> ops, int offset, int length) {
        
        if (offset>=ops.size()) {
            throw new RuntimeException("Missing expression");
        }
        
        Operator op = ops.get(offset);
        if (op instanceof BinaryOperator) {
            
            // Invalid
            throw new RuntimeException("Expression must not start with binary operator");
        } else if (op instanceof UnaryOperator) {
            
            // Just a unary operator
            return 1;
            
        } else if (op instanceof PrecedenceOperator) {
            
            PrecedenceOperator pop = (PrecedenceOperator)op;
            
            if (!pop.begin) {
                
                // Invalid
                throw new RuntimeException("Invalid paranthesis");
                
            } else {
                
                // Find closing bracket
                int open = 1;
                for (int i=offset+1; i<length; i++){
                    if (ops.get(i) instanceof PrecedenceOperator){
                        pop = (PrecedenceOperator)ops.get(i);
                        if (pop.begin) open++;
                        else open--;
                        if (open == 0){
                            return i-offset+1;
                        }
                    }
                }
                // Invalid
                throw new RuntimeException("Missing closing parantheses ("+open+") at "+length);
            }
        } else {
            
            // Invalid
            throw new RuntimeException("Unknown operator");
        }
    }
    
    /**
     * Determines whether the given row is selected by the expression
     * @param row
     * @return
     */
    public boolean selected(int row){
        if (root == null) {
            compile();
        }
        return root.eval(row);
    }
}
