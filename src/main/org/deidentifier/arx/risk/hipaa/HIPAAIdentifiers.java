package org.deidentifier.arx.risk.hipaa;

public class HIPAAIdentifiers {
    
    final private Warning[] identifiers;
    
    public HIPAAIdentifiers(Warning[] identifiers) {
        this.identifiers = identifiers;
    }
    
    public Warning[] getIdentifiers() {
        return identifiers;
    }
    
}
