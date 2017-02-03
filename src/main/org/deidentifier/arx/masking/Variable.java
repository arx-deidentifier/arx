package org.deidentifier.arx.masking;

/**
 * Class describing a masking variable
 *
 * @author Karol Babioch
 */
public class Variable {

    private String name;

    private VariableDistribution distribution;

    public Variable(String name) {

        this.name = name;

    }

    public Variable(String name, VariableDistribution distribution) {

        this.name = name;
        this.distribution = distribution;

    }

    public String getName() {

        return name;

    }

    public void setName(String name) {

        this.name = name;

    }

    public VariableDistribution getDistribution() {

        return distribution;

    }

    public void setDistribution(VariableDistribution distribution) {

        this.distribution = distribution;

    }

}
