package org.deidentifier.arx.masking;

/**
 * Class describing a parameter for a variable distribution
 * 
 * @see VariableDistribution
 *
 * @author Karol Babioch
 */
public class VariableParameter<T> {

    private String name;

    private String description;

    private T value;

    public String getName() {

        return name;

    }

    public String getDescription() {

        return description;

    }

    public VariableParameter(String name) {

        this(name, null, "");

    }

    public VariableParameter(String name, T value) {

        this(name, value, "");

    }

    public VariableParameter(String name, T value, String description) {

        this.name = name;
        this.value = value;
        this.description = description;

    }

    public void setValue(T value) {

        this.value = value;

    }

    public T getValue() {

        return value;

    }

}
