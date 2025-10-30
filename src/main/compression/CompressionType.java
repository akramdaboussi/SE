package main.compression;

public enum CompressionType {
    NO_OVERLAP,     // Sans chevauchement
    WITH_OVERLAP,    // Avec chevauchement
    WITH_OVERFLOW    // Avec gestion d'overflow
}