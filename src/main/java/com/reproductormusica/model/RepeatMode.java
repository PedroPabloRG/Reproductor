package com.reproductormusica.model;

/**
 * Enumeración para los modos de repetición
 */
public enum RepeatMode {
    OFF("No Repeat"),
    ALL("Repeat All"),
    ONE("Repeat One");
    
    private final String displayName;
    
    RepeatMode(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
