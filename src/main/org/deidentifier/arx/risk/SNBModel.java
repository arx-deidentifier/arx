package org.deidentifier.arx.risk;

import java.util.Map;

/**
* This class implements the SNBModel, for details see Chen, 1998
* @author Michael Schneider
* @version 1.09
*/

public class SNBModel extends UniquenessModel {

    /**
     * number of equivalence classes of size one
     */
    protected int    c1;

    /**
     * number of non zero classes equivalence classes in the population
     */
    protected double K;

    /**
     * sampling fraction
     */
    protected double pi;

    /**
     * Shlosser estimator for variable K, giving number of non zero classes in the population
     * @param Pi sampling fraction
     * @param eqClasses Map containing the equivalence class sizes (as keys) of the data set and the corresponding frequency (as values)
     * e.g. if the key 2 has value 3 then there are 3 equivalence classes of size two.
     */
    public SNBModel(final double Pi, final Map<Integer, Integer> eqClasses) {
        super(Pi, eqClasses);
        pi = Pi;
        K = estimateNonZeroEquivalenceClasses();
        c1 = eqClasses.get(1);
    }

    @Override
    public double computeRisk() {
        return (computeUniquenessTotal() / N);
    }

    @Override
    public double computeUniquenessTotal() {
        double result = Double.NaN;
        double alpha = 0, beta = 0;

        final NewtonSNB snbModel = new NewtonSNB(K, pi, eqClasses);

        // start values are initialized randomly
        alpha = Math.random();
        beta = Math.random();
        final double[] initialGuess = { alpha, beta };

        // use Newton Raphson Algorithm to compute solution for the nonlinear multivariate equations
        final double[] output = snbModel.getSolution(initialGuess);

        result = K * Math.pow(output[1], output[0]);
        return result;
    }

    /**
     * @return Shlosser estimator for variable K, giving number of non zero classes in the population 
     * estimated according to Haas, 1998 and Shlosser
     *
     */
    public double estimateNonZeroEquivalenceClasses() {
        double var1 = 0, var2 = 0, var3 = 0, var4 = 0;

        for (final Map.Entry<Integer, Integer> entry : eqClasses.entrySet()) {
            var1 += entry.getKey() * pi * pi * Math.pow((1 - (pi * pi)), entry.getKey() - 1) * entry.getValue();
        }

        for (final Map.Entry<Integer, Integer> entry : eqClasses.entrySet()) {
            var2 += Math.pow((1 - pi), entry.getKey()) * (Math.pow((1 + pi), entry.getKey()) - 1) * entry.getValue();
        }

        for (final Map.Entry<Integer, Integer> entry : eqClasses.entrySet()) {
            var3 += Math.pow((1 - pi), entry.getKey()) * entry.getValue();
        }

        for (final Map.Entry<Integer, Integer> entry : eqClasses.entrySet()) {
            var4 += entry.getKey() * pi * Math.pow((1 - pi), (entry.getKey() - 1)) * entry.getValue();
        }

        final double K = u + (c1 * (var1 / var2) * ((var3 / var4) * (var3 / var4)));
        return K;
    }

}
