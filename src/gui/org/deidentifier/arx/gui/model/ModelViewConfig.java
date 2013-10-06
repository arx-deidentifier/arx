package org.deidentifier.arx.gui.model;

import java.io.Serializable;

public class ModelViewConfig implements Serializable {

    public static enum Mode {
        SORTED_INPUT,
        SORTED_OUTPUT,
        GROUPED,
        UNSORTED
    }

    private static final long serialVersionUID = 4770598345842536623L;

    private Mode    mode      = Mode.UNSORTED;
    private String  attribute = null;
    private boolean subset    = false;

    public ModelViewConfig clone() {
        ModelViewConfig result = new ModelViewConfig();
        result.mode = this.mode;
        result.attribute = this.attribute;
        result.subset = this.subset;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ModelViewConfig other = (ModelViewConfig) obj;
        if (attribute == null) {
            if (other.attribute != null) return false;
        } else if (!attribute.equals(other.attribute)) return false;
        if (mode != other.mode) return false;
        if (subset != other.subset) return false;
        return true;
    }

    public String getAttribute() {
        return attribute;
    }

    public Mode getMode() {
        return mode;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((attribute == null) ? 0 : attribute.hashCode());
        result = prime * result + ((mode == null) ? 0 : mode.hashCode());
        result = prime * result + (subset ? 1231 : 1237);
        return result;
    }

    public boolean isSubset() {
        return subset;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public void setSubset(boolean subset) {
        this.subset = subset;
    }
}
