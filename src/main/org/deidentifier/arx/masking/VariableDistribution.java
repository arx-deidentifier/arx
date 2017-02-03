package org.deidentifier.arx.masking;

import java.util.List;

/**
 * Class describing a variable distribution
 *
 * @author Karol Babioch
 */
abstract public class VariableDistribution {

    public abstract List<VariableParameter> getParameters();

}
