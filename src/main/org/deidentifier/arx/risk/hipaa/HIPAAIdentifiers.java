package org.deidentifier.arx.risk.hipaa;

public class HIPAAIdentifiers {
    
    final private Identifier[] identifiers;
    
    public HIPAAIdentifiers(Identifier[] identifiers) {
        this.identifiers = identifiers;
    }
    
    public Identifier[] getIdentifiers() {
        return identifiers;
    }
    
}
