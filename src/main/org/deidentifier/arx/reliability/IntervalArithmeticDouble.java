/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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

package org.deidentifier.arx.reliability;

/**
 * Interval arithmetic system
 * 
 * @author Fabian Prasser
 * @author Raffael Bild
 */
public class IntervalArithmeticDouble {

    public IntervalDouble ZERO;
    public IntervalDouble ONE;
    public IntervalDouble MINUS_ONE;
    public IntervalDouble PI;

    public IntervalArithmeticDouble() {
        try {
            ZERO      = createInterval(0d);
            ONE       = createInterval(1d);
            MINUS_ONE = createInterval(-1d);
            PI        = createInterval(3.1415926535897932384d, 3.1415926535897932385d);
        } catch (IntervalArithmeticException e) {
            // May never happen
        }
    }
    
    /**
     * Interface for binary operations
     * 
     * @author Fabian Prasser
     */
    private static interface BinaryOperationDouble {
        
        /**
         * Performs the operation
         * @param operand1
         * @param operand2
         * @return
         */
        abstract double apply(double operand1, double operand2);
    }

    /**
     * Interface for unary operations
     * 
     * @author Fabian Prasser
     */
    private static interface UnaryOperationDouble {
        
        /**
         * Performs the operation
         * @param operand
         * @return
         */
        abstract double apply(double operand);
    }
    
    /**
     * Addition
     * @param operand1
     * @param operand2
     * @return
     * @throws IntervalArithmeticException
     */
    public IntervalDouble add(IntervalDouble operand1, IntervalDouble operand2) throws IntervalArithmeticException {
        return apply(operand1, operand2, new BinaryOperationDouble() {
            public double apply(double operand1, double operand2) {
                return operand1 + operand2;
            }
        });
    }

    /**
     * Probability mass function of the binomial distribution
     * based on the saddle point expansion described in the manuscript
     * Catherine Loader. Fast and Accurate Computation of Binomial Probabilities. July 9, 2000.
     * 
     * @param n
     * @param p
     * @param k
     * @return
     * @throws IntervalArithmeticException 
     */
    public IntervalDouble binomialProbability(int n, IntervalDouble p, int k) throws IntervalArithmeticException {
        
        if (lessThan(p, ZERO) || greaterThan(p, ONE)) {
            throw new IntervalArithmeticException("Invalid p: " + p);
        }

        if (n < 0) {
            throw new IntervalArithmeticException("Invalid n: " + n);
        }
        
        if (k < 0 || k > n) {
            return createInterval(0d);
        }
        
        if (k == 0) {
            return pow(sub(ONE, p), n);
        } else if (k == n) {
            return pow(p, n);
        }
        
        IntervalDouble intN = createInterval(n);
        IntervalDouble intK = createInterval(k);
        IntervalDouble subNK = sub(intN, intK);
    
        // dev = k * Math.log (k / (n*p)) + (n-k) * Math.log((n-k) / (n*(1-p)))
        IntervalDouble dev = add(mult(intK, log(div(intK, mult(intN, p)))), mult(subNK, log(div(subNK, mult(intN, sub(ONE, p))))));
        
        // term1 = stirlingError(n) - stirlingError(k) - stirlingError(n-k)
        IntervalDouble term1 = sub(sub(stirlingError(intN), stirlingError(intK)), stirlingError(subNK));
        
        // term2 = Math.sqrt(n / (2d * Math.PI * k * (n-k)))
        IntervalDouble term2 = sqrt(div(intN, mult(mult(mult(createInterval(2), PI), intK), subNK)));
        
        return exp(sub(add(log(term2), term1), dev));
    }

    /**
     * Ceil
     * @param operand
     * @return
     * @throws IntervalArithmeticException
     */
    public IntervalDouble ceil(IntervalDouble operand) throws IntervalArithmeticException {
        return apply(operand, new UnaryOperationDouble() {
            public double apply(double operand) {
                return Math.ceil(operand);
            }
        }, false);
    }
    
    /**
     * Returns ceil of the lower bound to int
     * @param value
     * @return
     */
    public int ceilLowerBoundToInt(IntervalDouble value)  throws IntervalArithmeticException {
        double ceil = Math.ceil(value.getLowerBound());
        if (ceil > (double)Integer.MAX_VALUE || ceil < (double)Integer.MIN_VALUE) {
            throw new IntervalArithmeticException("Value does not fit into an integer: " + ceil);
        }
        return (int)ceil;
    }

    /**
     * Returns ceil() to int, if clearly defined
     * @param value
     * @return
     */
    public int ceilToInt(IntervalDouble value)  throws IntervalArithmeticException {
        IntervalDouble ceil = ceil(value);
        if (ceil.getLowerBound() > (double)Integer.MAX_VALUE || ceil.getLowerBound() < (double)Integer.MIN_VALUE) {
            throw new IntervalArithmeticException("Value does not fit into an integer: " + ceil.getLowerBound());
        }
        if (ceil.getUpperBound() > (double)Integer.MAX_VALUE || ceil.getUpperBound() < (double)Integer.MIN_VALUE) {
            throw new IntervalArithmeticException("Value does not fit into an integer: " + ceil.getUpperBound());
        }
        int lower = (int)ceil.getLowerBound();
        int upper = (int)ceil.getUpperBound();
        if (lower != upper) {
            throw new IntervalArithmeticException("Ceil to integer is undecidable for: " + value);
        }
        return lower;
    }

    /**
     * Creates a new interval
     * @param value
     * @return
     * @throws IntervalArithmeticException
     */
    public IntervalDouble createInterval(double value) throws IntervalArithmeticException {
        checkInterval(value, value);
        return new IntervalDouble(value, value);       
    }
    
    /**
     * Creates a new interval
     * @param lower
     * @param upper
     * @return
     * @throws IntervalArithmeticException
     */
    public IntervalDouble createInterval(double lower, double upper) throws IntervalArithmeticException {
        checkInterval(lower, upper);
        return new IntervalDouble(lower, upper);
    }
    
    /**
     * Creates a new interval
     * @param value
     * @return
     * @throws IntervalArithmeticException
     */
    public IntervalDouble createInterval(int value) {
        return new IntervalDouble(value, value);       
    }

    /**
     * Division
     * @param operand1
     * @param operand2
     * @return
     * @throws IntervalArithmeticException
     */
    public IntervalDouble div(IntervalDouble operand1, IntervalDouble operand2) throws IntervalArithmeticException {
        return apply(operand1, operand2, new BinaryOperationDouble() {
            public double apply(double operand1, double operand2) {
                return operand1 / operand2;
            }
        });
    }

    /**
     * Exp
     * @param operand
     * @return
     * @throws IntervalArithmeticException
     */
    public IntervalDouble exp(IntervalDouble operand) throws IntervalArithmeticException {
        return apply(operand, new UnaryOperationDouble() {
            public double apply(double operand) {
                return Math.exp(operand);
            }
        }, true);
    }
    
    /**
     * Floor
     * @param operand
     * @return
     * @throws IntervalArithmeticException
     */
    public IntervalDouble floor(IntervalDouble operand) throws IntervalArithmeticException {
        return apply(operand, new UnaryOperationDouble() {
            public double apply(double operand) {
                return Math.floor(operand);
            }
        }, false);
    }
    
    /**
     * Returns floor of the lower bound to int
     * @param value
     * @return
     */
    public int floorLowerBoundToInt(IntervalDouble value)  throws IntervalArithmeticException {
        double floor = Math.floor(value.getLowerBound());
        if (floor > (double)Integer.MAX_VALUE || floor < (double)Integer.MIN_VALUE) {
            throw new IntervalArithmeticException("Value does not fit into an integer: " + floor);
        }
        return (int)floor;
    }

    /**
     * Returns floor() to int, if clearly defined
     * @param value
     * @return
     */
    public int floorToInt(IntervalDouble value)  throws IntervalArithmeticException {
        IntervalDouble floor = floor(value);
        if (floor.getLowerBound() > (double)Integer.MAX_VALUE || floor.getLowerBound() < (double)Integer.MIN_VALUE) {
            throw new IntervalArithmeticException("Value does not fit into an integer: " + floor.getLowerBound());
        }
        if (floor.getUpperBound() > (double)Integer.MAX_VALUE || floor.getUpperBound() < (double)Integer.MIN_VALUE) {
            throw new IntervalArithmeticException("Value does not fit into an integer: " + floor.getUpperBound());
        }
        int lower = (int)floor.getLowerBound();
        int upper = (int)floor.getUpperBound();
        if (lower != upper) {
            throw new IntervalArithmeticException("Floor to integer is undecidable for: " + value);
        }
        return lower;
    }

    /**
     * Greater than
     * @param operand1
     * @param operand2
     * @return
     * @throws IntervalArithmeticException
     */
    public boolean greaterThan(IntervalDouble operand1, IntervalDouble operand2) throws IntervalArithmeticException {
        checkComparison(operand1, operand2);
        return operand1.lower > operand2.upper;
    }

    /**
     * 1/x
     * @param operand
     * @return
     * @throws IntervalArithmeticException
     */
    public IntervalDouble inv(IntervalDouble operand) throws IntervalArithmeticException {
        return apply(operand, new UnaryOperationDouble() {
            public double apply(double operand) {
                return 1d / operand;
            }
        }, true);
    }

    /**
     * Less than
     * @param operand1
     * @param operand2
     * @return
     * @throws IntervalArithmeticException
     */
    public boolean lessThan(IntervalDouble operand1, IntervalDouble operand2) throws IntervalArithmeticException {
        checkComparison(operand1, operand2);
        return operand1.upper < operand2.lower;
    }

    /**
     * Less than or overlap
     * @param operand1
     * @param operand2
     * @return
     */
    public boolean lessThanOrOverlap(IntervalDouble operand1, IntervalDouble operand2) {
        return operand1.lower <= operand2.upper;
    }

    /**
     * Less than
     * @param operand1
     * @param operand2
     * @return
     * @throws IntervalArithmeticException
     */
    public boolean lessThanOrEqual(IntervalDouble operand1, IntervalDouble operand2) throws IntervalArithmeticException {
        return (operand1.upper < operand2.lower) || ((operand1.lower == operand2.lower) && (operand1.upper == operand2.upper));
    }

    /**
     * Log
     * @param operand
     * @return
     * @throws IntervalArithmeticException
     */
    public IntervalDouble log(IntervalDouble operand) throws IntervalArithmeticException {
        return apply(operand, new UnaryOperationDouble() {
            public double apply(double operand) {
                return Math.log(operand);
            }
        }, true);
    }

    /**
     * Log of factorial of n. Derived from class BinomialDistribution in SMILE
     * 
     * @throws IntervalArithmeticException 
     */
    public IntervalDouble logFactorial(int n) throws IntervalArithmeticException {
        
        // Check
        if (n < 0) { 
            throw new IntervalArithmeticException("Parameter must be >= 0"); 
        }
        
        // Calculate
        IntervalDouble result = ZERO;
        for (int i = 2; i <= n; i++) {
            result = add(result, log(createInterval(i)));
        }

        // Return
        return result;
    }

    /**
     * Max
     * @param operand1
     * @param operand2
     * @return
     * @throws IntervalArithmeticException
     */
    public IntervalDouble max(IntervalDouble operand1, IntervalDouble operand2) throws IntervalArithmeticException {
        checkComparison(operand1, operand2);
        if (operand1.upper < operand2.lower) {
            return operand2;
        } else {
            return operand1;
        }
    }
    
    /**
     * Min
     * @param operand1
     * @param operand2
     * @return
     * @throws IntervalArithmeticException
     */
    public IntervalDouble min(IntervalDouble operand1, IntervalDouble operand2) throws IntervalArithmeticException {
        checkComparison(operand1, operand2);
        if (operand1.upper < operand2.lower) {
            return operand1;
        } else {
            return operand2;
        }
    }

    /**
     * Multiplication
     * @param operand1
     * @param operand2
     * @return
     * @throws IntervalArithmeticException
     */
    public IntervalDouble mult(IntervalDouble operand1, IntervalDouble operand2) throws IntervalArithmeticException {
        return apply(operand1, operand2, new BinaryOperationDouble() {
            public double apply(double operand1, double operand2) {
                return operand1 * operand2;
            }
        });
    }

    /**
     * Pow
     * @param operand
     * @param exponent
     * @return
     * @throws IntervalArithmeticException
     */
    public IntervalDouble pow(IntervalDouble operand, final int exponent) throws IntervalArithmeticException {
        return apply(operand, new UnaryOperationDouble() {
            public double apply(double operand) {
                return Math.pow(operand, exponent);
            }
        }, true);
    }
    
    /**
     * Square root
     * @param operand
     * @return
     * @throws IntervalArithmeticException
     */
    public IntervalDouble sqrt(IntervalDouble operand) throws IntervalArithmeticException {
        return apply(operand, new UnaryOperationDouble() {
            public double apply(double operand) {
                return Math.sqrt(operand);
            }
        }, true);
    }

    /**
     * Subtraction
     * @param operand1
     * @param operand2
     * @return
     * @throws IntervalArithmeticException
     */
    public IntervalDouble sub(IntervalDouble operand1, IntervalDouble operand2) throws IntervalArithmeticException {
        return apply(operand1, operand2, new BinaryOperationDouble() {
            public double apply(double operand1, double operand2) {
                return operand1 - operand2;
            }
        });
    }

    /**
     * Apply binary operation
     * @param operand1
     * @param operand2
     * @param operation
     * @return
     * @throws IntervalArithmeticException 
     */
    private IntervalDouble apply(IntervalDouble operand1, IntervalDouble operand2, BinaryOperationDouble operation) throws IntervalArithmeticException {

        // Should be safe
        double val0 = operation.apply(operand1.lower, operand2.lower);
        double val1 = operation.apply(operand1.lower, operand2.upper);
        double val2 = operation.apply(operand1.upper, operand2.lower);
        double val3 = operation.apply(operand1.upper, operand2.upper);
        
        // Check
        checkValue(val0);
        checkValue(val1);
        checkValue(val2);
        checkValue(val3);

        // Smallest
        double lower = Math.min(val0, val1);
        lower = Math.min(lower, val2);
        lower = Math.min(lower, val3);

        // Largest
        double upper = Math.max(val0, val1);
        upper = Math.max(upper, val2);
        upper = Math.max(upper, val3);
        
        // Round
        lower = floor(lower);
        upper = ceil(upper);

        // Check and return
        checkInterval(lower, upper);
        return new IntervalDouble(lower, upper);
    }

    
    /**
     * Apply unary operation
     * @param operand
     * @param operation
     * @param expand TODO
     * @return
     */
    private IntervalDouble apply(IntervalDouble operand, UnaryOperationDouble operation, boolean expand) throws IntervalArithmeticException {
        
        // This should be safe
        double val0 = operation.apply(operand.lower);
        double val1 = operation.apply(operand.upper);

        // Check
        checkValue(val0);
        checkValue(val1);

        // Smallest
        double lower = Math.min(val0, val1);

        // Largest
        double upper = Math.max(val0, val1);
        
        // Round
        if (expand) {
            lower = floor(lower);
            upper = ceil(upper);
        }

        // Check and return
        checkInterval(lower, upper);
        return new IntervalDouble(lower, upper);
    }
    
    /**
     * Internal ceil
     * @param value
     * @return
     */
    private double ceil(double value) {
        // According to the java documentation at
        // https://docs.oracle.com/javase/8/docs/api/java/lang/Math.html
        // all methods of the Math class with a 1 ulp error bound guarantee
        // that the floating-point result is one of the two floating-point numbers which bracket the exact result.
        // All methods of the Math class used in this class have an error bound of one ulp.
        // Moreover, all basic arithmetic double operations used in this class use the IEEE 754 round to nearest mode
        // (see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-2.html#jvms-2.8.1)
        // and thus provide the same error bound guarantee.
        // Hence, we can calculate a reliable upper bound by returning the next adjacent floating-point number in direction +infinity.
        return Math.nextAfter(value, Double.POSITIVE_INFINITY);
    }

    /**
     * Checks the decidability of comparisons
     * @param operand1
     * @param operand2
     * @throws IntervalArithmeticException
     */
    private void checkComparison(IntervalDouble operand1, IntervalDouble operand2) throws IntervalArithmeticException {
        // Check for equality
        if (operand1.lower == operand2.lower && operand1.upper == operand2.upper) {
            return;
        }
        // Check for overlap
        if (!(operand1.upper < operand2.lower || operand2.upper < operand1.lower)) {
            throw new IntervalArithmeticException("Undecidable relationship");
        }
    }
    

    /**
     * Checks an interval
     * @param lower
     * @param upper
     * @throws IntervalArithmeticException
     */
    private void checkInterval(double lower, double upper) throws IntervalArithmeticException {
        checkValue(lower);
        checkValue(upper);
        if (lower > upper) {
            throw new IntervalArithmeticException("Invalid range: [" + lower + ", " + upper + "]");
        }
    }
    

    /**
     * Checks a value
     * @param value
     * @throws IntervalArithmeticException
     */
    private void checkValue(double value) throws IntervalArithmeticException {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IntervalArithmeticException("Numeric instability: " + value); 
        }
    }
    

    /**
     * Internal floor
     * @param value
     * @return
     */
    private double floor(double value) {
        // According to the java documentation at
        // https://docs.oracle.com/javase/8/docs/api/java/lang/Math.html
        // all methods of the Math class with a 1 ulp error bound guarantee
        // that the floating-point result is one of the two floating-point numbers which bracket the exact result.
        // All methods of the Math class used in this class have an error bound of one ulp.
        // Moreover, all basic arithmetic double operations used in this class use the IEEE 754 round to nearest mode
        // (see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-2.html#jvms-2.8.1)
        // and thus provide the same error bound guarantee.
        // Hence, we can calculate a reliable lower bound by returning the next adjacent floating-point number in direction -infinity.
        return Math.nextAfter(value, Double.NEGATIVE_INFINITY);
    }
    
    /**
     * Calculate the error of Stirling's series at the given value.
     * For details see
     * https://en.wikipedia.org/wiki/Stirling%27s_approximation#Speed_of_convergence_and_error_estimates
     * @param value
     * @return
     * @throws IntervalArithmeticException
     */
    private IntervalDouble stirlingError(IntervalDouble value) throws IntervalArithmeticException {
        
        IntervalDouble base = sub(inv(mult(createInterval(12d), value)), inv(mult(createInterval(360d), pow(value,3))));
        IntervalDouble bound = inv(mult(createInterval(1260d), pow(value,5)));
        double offset = Math.max(Math.abs(bound.lower), Math.abs(bound.upper));
        
        return createInterval(floor(base.lower - offset), ceil(base.upper + offset));
    }
}