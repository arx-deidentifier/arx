package org.deidentifier.arx.risk;

import java.util.Map;

/**
* This class implements the Newton Raphson Algorithm for the SNB Model
* @author Michael Schneider
* @version 1.0
*/

public class NewtonSNB extends NewtonRaphsonAlgorithm {

    /**
     * number of equivalence classes of size one in the sample
     */
    private final int c1;

    /**
     * number of equivalence classes of size two in the sample
     */
    private final int c2;

    /**
     * number of non zero classes equivalence classes in the population (estimated)
     */
    protected double  K;

    /**
     * sampling fraction
     */
    protected double  Pi;

    /**
     * Implements procedures of Newton Raphson algorithm for SNB Model
     * @param Pi sampling fraction,  
     * @param eqClasses Map containing the equivalence class sizes (as keys) of the data set and the corresponding frequency (as values)
     * e.g. if the key 2 has value 3 then there are 3 equivalence classes of size two.	  
     * @param K number of non zero classes equivalence classes in the population (estimated)
     */
    public NewtonSNB(final double K, final double Pi, final Map<Integer, Integer> eqClasses) {
        this.K = K;
        this.Pi = Pi;
        c1 = eqClasses.get(1);
        c2 = eqClasses.get(2);
    }

    /**
     * The method for computing the first derivatives of
     * the object functions evaluated at the iterated solutions.
     * @param iteratedSolution the iterated vector of solutions.
     * @return the first derivatives of the object functions evaluated at the
     *         iterated solutions.
     */
    @Override
    public double[][] firstDerivativeMatrix(final double[] iteratedSolution) {
        final double[][] result = new double[iteratedSolution.length][iteratedSolution.length];
        final double iBeta = iteratedSolution[1] - 1;
        // The derivation of the following formulas has been obtained using Mathematica

        // Formula 1, d alpha
        result[0][0] = (-(Pi * K * mathHelper((iteratedSolution[1] / ((iteratedSolution[1] * (-Pi)) + iteratedSolution[1] + Pi)), iteratedSolution[0]) * ((((iteratedSolution[0] * iBeta * (Pi - 1)) +
                                                                                                                                                            (iteratedSolution[1] * (-Pi)) +
                                                                                                                                                            iteratedSolution[1] + Pi) * Math.log(((iteratedSolution[1] * (-Pi)) +
                                                                                                                                                                                                  iteratedSolution[1] + Pi))) + (iBeta * (Pi - 1)))) / ((iteratedSolution[1] * (Pi - 1)) - Pi));

        // Formula 1, d beta
        result[0][1] = ((iteratedSolution[0] * Pi * Math.pow((iteratedSolution[1] / ((iteratedSolution[1] + Pi) - (iteratedSolution[1] * Pi))), iteratedSolution[0] - 1) *
                         ((iteratedSolution[1] * (Pi - 1) * (1 + ((iteratedSolution[0] - 1) * Pi))) + (Pi * ((iteratedSolution[0] + Pi) - (iteratedSolution[0] * Pi)))) * K) / (mathHelper(((iteratedSolution[1] + Pi) - (iteratedSolution[1] * Pi)),
                                                                                                                                                                                           3)));

        // Formula 2, d alpha
        result[1][0] = (mathHelper(2, -iteratedSolution[0] - 2) * (1 - iteratedSolution[1]) * mathHelper(iteratedSolution[1], iteratedSolution[0]) * (Pi * Pi) *
                        mathHelper(((iteratedSolution[1] + Pi) - (iteratedSolution[1] * Pi)), -iteratedSolution[0] - 2) * K * (((1 + iteratedSolution[1] +
                                                                                                                                 (2 * iteratedSolution[0] * (iteratedSolution[1] - 1) * (Pi - 1)) + Pi) - (Pi * iteratedSolution[1])) + ((iteratedSolution[0] * ((1 +
                                                                                                                                                                                                                                                                  iteratedSolution[1] +
                                                                                                                                                                                                                                                                  (iteratedSolution[0] *
                                                                                                                                                                                                                                                                   (iteratedSolution[1] - 1) * (Pi - 1)) + Pi) - (iteratedSolution[1] * Pi))) * (Math.log(iteratedSolution[1]) - Math.log(2 * ((iteratedSolution[1] + Pi) - (iteratedSolution[1] * Pi)))))));

        // Formula 2, d beta
        result[1][1] = (mathHelper(-2, -iteratedSolution[0] - 2) *
                        iteratedSolution[0] *
                        mathHelper(iteratedSolution[1], iteratedSolution[0] - 1) *
                        (Pi * Pi) *
                        mathHelper((iteratedSolution[1] + Pi) - (iteratedSolution[1] * Pi), -iteratedSolution[0] - 3) *
                        (((2 * iteratedSolution[1] * ((1 + iteratedSolution[0]) - (iteratedSolution[0] * iteratedSolution[1]))) - ((iteratedSolution[0] * ((-1 + (iteratedSolution[0] * (iteratedSolution[1] - 1))) - (3 * iteratedSolution[1]))) * ((iteratedSolution[1] - 1) * Pi))) + ((iteratedSolution[0] - 1) *
                                                                                                                                                                                                                                                                                          iteratedSolution[0] *
                                                                                                                                                                                                                                                                                          mathHelper((iteratedSolution[1] - 1),
                                                                                                                                                                                                                                                                                                     2) * (Pi * Pi))) * K);
        return result;
    }

    /** 
     * Helper function to compute the value of the first argument raised to the power of the second argument.
     * @param base (first argument)
     * @param power (second argument)
     * @return value of the first argument raised to the power of the second argument
     */
    public double mathHelper(final double base, final double power) {
        double result;
        if (base < 0) {
            if (power < 0) {
                result = -1.0 / Math.pow(Math.abs(base), Math.abs(power));
            } else {
                result = -Math.pow(Math.abs(base), power);
            }
        } else {
            if (power < 0) {
                result = 1.0 / Math.pow(Math.abs(base), Math.abs(power));
            } else {
                result = Math.pow(Math.abs(base), power);
            }
        }

        return result;
    }

    /**
     * The method for computing
     * the object functions evaluated at the iterated solutions.
     * @param iteratedSolution the iterated vector of solutions.
     * @return the object functions evaluated at the iterated solutions.
     */
    @Override
    public double[] objectFunctionVector(final double[] iteratedSolution) {
        final double[] result = new double[iteratedSolution.length];
        final double iNenner = ((1 - Pi) * (1 - iteratedSolution[1]));

        // original equations to determine the value of the parameters alpha and beta in the SNB Model:
        result[0] = (K * Pi * mathHelper((iteratedSolution[1] / (1 - iNenner)), iteratedSolution[0]) * (((iteratedSolution[0] * iNenner) / (1 - iNenner)) + 1)) - c1;

        result[1] = (K *
                     ((iteratedSolution[0] * mathHelper(iteratedSolution[1], iteratedSolution[0]) * (Pi * Pi) * (1 - iteratedSolution[1])) / (2 * mathHelper((1 - iNenner), iteratedSolution[0] + 2))) * (2 - ((1 - iteratedSolution[0]) * iNenner))) -
                    c2;

        return result;
    }

}
