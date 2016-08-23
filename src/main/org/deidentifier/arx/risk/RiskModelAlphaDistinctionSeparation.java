package org.deidentifier.arx.risk;


class RiskModelAlphaDistinctionSeparation extends RiskModelSample {

    /**
     * Creates a new instance
     *
     * @param histogram
     */
    RiskModelAlphaDistinctionSeparation(RiskModelHistogram histogram) {
        super(histogram);
    }

    double getAlphaDistinction() {
        // alpha-distinct = countUniqueTuples / countAllTuples
        return getHistogram().getNumClasses() / getHistogram().getNumRecords();

    }
    double getAlphaSeparation() {
        return 0;
        //return 1.0d / getHistogram().getNumClasses();
    }

}