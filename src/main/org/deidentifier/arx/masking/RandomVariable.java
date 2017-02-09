package org.deidentifier.arx.masking;

import java.io.Serializable;

/**
 * Class describing a random variable
 *
 * @author Karol Babioch
 */
public class RandomVariable implements Serializable {

    private static final long serialVersionUID = 5890088358051823161L;

    private String name;

    // TODO Own type for distribution
    private String distribution;

    public RandomVariable(String name, String distribution) {

        this.name = name;
        this.distribution = distribution;

    }

    public RandomVariable(String name) {

        this.name = name;

    }

    public void setName(String name) {

        this.name = name;

    }

    public String getName() {

        return this.name;

    }

    public void setDistribution(String name) {

        this.distribution = name;

    }

    public String getDistribution() {

        return this.distribution;

    }

}
