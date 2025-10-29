package main.compression;

/**
 * Classe pour compresser et décompresser un tableau d'entiers 
 * en utilisant le bit-packing avec chevauchements.
 */
public class BitPackingWithOverlap implements BitPacker {

    private int[] compressedData; // Le tableau compressé
    private int bitsPerValue; // Le 'k' de l'énoncé qui indique le nombre de bits sur lesquels chaque entier est stocké
    private int originalSize; // Taille du tableau original
    
    // Le masque est calculé 1 seule fois et stocké ici
    private long mask; 

    @Override
    public int[] compress(int[] tab) {
        if (tab == null || tab.length == 0) {
            this.originalSize = 0;
            this.compressedData = new int[0];
            return this.compressedData;
        }

        this.originalSize = tab.length;

        // On cherche la valeur maximale sur laquelle on va se baser pour déterminer le nombre de bits 'k'
        int maxVal = 0;
        int i = 0;
        for (i = 0; i < tab.length; i++) {
            int val = tab[i];
            if (val < 0) {
                throw new IllegalArgumentException("Les nombres négatifs ne sont pas supportés : " + val);
            }
            if (val > maxVal) {
                maxVal = val;
            }
        }

         // On calcule le nombre de bits 'k' nécessaires pour écrire maxVal 
        if (maxVal == 0){
            this.bitsPerValue = 1; // Même si toutes les valeurs sont 0, on a besoin d'au moins 1 bit pour les représenter
        } else {
            this.bitsPerValue = 32 - Integer.numberOfLeadingZeros(maxVal); 
        }

        // On calcule la taille de sortie
        long totalBitsNeeded = (long) this.originalSize * this.bitsPerValue;
        int compressedSize = (int) ((totalBitsNeeded + 31) / 32); 
        this.compressedData = new int[compressedSize];
        
        // On pré-calcule le masque pour 'k' bits
        this.mask = (1L << this.bitsPerValue) - 1;

        // On remplit le tableau
        long currentBitOffset = 0;
        for (int j = 0; j < this.originalSize; j++) {
            long valueToPack = tab[j];
            int arrayIndex = (int) (currentBitOffset / 32);
            int bitInInt = (int) (currentBitOffset % 32);

            // Écrit la partie de la valeur qui tient dans l'entier courant
            this.compressedData[arrayIndex] |= (valueToPack << bitInInt);

            // Écrit la partie de la valeur qui dépasse dans l'entier suivant (chevauchement)
            int bitsWritten = 32 - bitInInt;
            if (bitsWritten < this.bitsPerValue) {
                this.compressedData[arrayIndex + 1] |= (valueToPack >> bitsWritten);
            }

            currentBitOffset += this.bitsPerValue;
        }

        return this.compressedData;
    }

    @Override
    public int get(int i) {
        if (compressedData == null) {
            throw new IllegalStateException("Le tableau n'a pas encore été compressé.");
        }
        if (i < 0 || i >= this.originalSize) {
            throw new IndexOutOfBoundsException("Index " + i + " hors des limites pour la taille " + this.originalSize);
        }

        long startBit = (long) i * this.bitsPerValue;
        int arrayIndex = (int) (startBit / 32);
        int bitInInt = (int) (startBit % 32);

        // Lit la partie de la valeur qui se trouve dans l'entier actuel
        // On utilise (& 0xFFFFFFFFL) pour une conversionn non-signée en long
        long value = (this.compressedData[arrayIndex] & 0xFFFFFFFFL) >> bitInInt;

        // Lit le reste de la valeur (le débordement) depuis l'entier suivant
        int bitsRead = 32 - bitInInt;
        if (bitsRead < this.bitsPerValue) {
            long nextValue = (this.compressedData[arrayIndex + 1] & 0xFFFFFFFFL);
            
            // On ne lit que les bits manquants
            int bitsToRead = this.bitsPerValue - bitsRead;
            long highBits = nextValue & ((1L << bitsToRead) - 1);
            
            // On assemble la valeur
            value |= (highBits << bitsRead);
        }

        // On applique le masque (pré-calculé)
        return (int) (value & this.mask);
    }

    /**
     * Cette méthode lit les données séquentiellement au lieu d'appeler get() N fois, ce qui est beaucoup plus rapide.
     */
    @Override
    public int[] decompress() {
        if (compressedData == null) {
            throw new IllegalStateException("Le tableau n'a pas encore été compressé.");
        }

        int[] decompressedArray = new int[this.originalSize];
        long currentBitOffset = 0;

        for (int i = 0; i < this.originalSize; i++) {
            
            int arrayIndex = (int) (currentBitOffset / 32);
            int bitInInt = (int) (currentBitOffset % 32);

            // Lit la partie de la valeur qui se trouve dans l'entier actuel
            long value = (this.compressedData[arrayIndex] & 0xFFFFFFFFL) >> bitInInt;

            // Lit le reste de la valeur (le débordement) depuis l'entier suivant
            int bitsRead = 32 - bitInInt;
            if (bitsRead < this.bitsPerValue) {
                long nextValue = (this.compressedData[arrayIndex + 1] & 0xFFFFFFFFL);
                
                int bitsToRead = this.bitsPerValue - bitsRead;
                long highBits = nextValue & ((1L << bitsToRead) - 1);
                
                value |= (highBits << bitsRead);
            }

            // Applique le masque et stocke la valeur
            decompressedArray[i] = (int) (value & this.mask);
            
            // Avance le curseur
            currentBitOffset += this.bitsPerValue;
        }
        return decompressedArray;
    }

    @Override
    public int[] getRawCompressedData() {
        return this.compressedData;
    }
}