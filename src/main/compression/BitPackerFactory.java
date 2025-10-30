package main.compression;

public class BitPackerFactory {

    /**
     * Crée une instance du compresseur demandé.
     * @param type Le type de compression souhaité.
     * @return Une instance de BitPacker.
     */
    public static BitPacker create(CompressionType type) {
        switch (type) {
            case NO_OVERLAP:
                return new BitPackingNoOverlap();
            case WITH_OVERLAP:
                return new BitPackingWithOverlap();
            case WITH_OVERFLOW:
                return new BitPackingWithOverflow();
            default:
                throw new IllegalArgumentException("Type de compression inconnu: " + type);
        }
    }
}