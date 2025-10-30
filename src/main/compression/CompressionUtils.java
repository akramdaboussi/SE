package main.compression;

import java.util.*;

/**
 * Classe utilitaire pour les opérations de bit-packing et l'analyse heuristique
 */
public final class CompressionUtils {

    /**
     * Heuristique pour trouver le 'k' (nb de bits) optimal pour séparer les petites valeurs des valeurs d'overflow.
     * * @param tab Le tableau d'entiers à analyser
     * @return Le nb de bits 'k' juste avant le plus grand "saut" de bits
     */
    public static int findBestKByBitJump(int[] tab) {
        if (tab.length == 0) return 1;
        // On collecte les différentes longueurs en bits des valeurs
        Set<Integer> uniqueBitLengths = new HashSet<>();
        for (int val : tab) {
            uniqueBitLengths.add(bitsFor(val));
        }
        // Toutes les valeurs ont la même longueur en bits
        if (uniqueBitLengths.size() < 2) {
            return uniqueBitLengths.iterator().next();
        }
        // On trie les longueurs et on cherche le plus grand saut
        List<Integer> sortedBits = new ArrayList<>(uniqueBitLengths);
        Collections.sort(sortedBits);

        int maxJump = 0;
        int bestKNormal = 31; 
        int prevBits = sortedBits.get(0);

        for (int i = 1; i < sortedBits.size(); i++) {
            int currentBits = sortedBits.get(i);
            int jump = currentBits - prevBits;

            if (jump > maxJump) {
                maxJump = jump;
                bestKNormal = prevBits; 
            }
            prevBits = currentBits;
        }
        // Si le plus grand saut est inférieur à 2, on ne fait pas de séparation
        if (maxJump < 2) return prevBits; 
        // On retourne le k optimal trouvé
        return bestKNormal;
    }

    /**
     * Calcule le nombre de bits minimum requis pour stocker 'n'
     */
    public static int bitsFor(int n) { 
        if (n == 0) return 1; 
        return 32 - Integer.numberOfLeadingZeros(n);
    }

    /**
     * Écrit 'numBits' de 'value' dans 'compressedData' à la position 'startBit'
     */
    public static void writeBits(int[] compressedData, int value, int numBits, int startBit) {
        // Utilisation de long pour éviter les problèmes de dépassement lors du décalage
        long val = value & 0xFFFFFFFFL; 
        int currentBit = startBit;
        int bitsWritten = 0;

        // La boucle gère l'écriture à cheval sur plusieurs entiers (int)
        while (bitsWritten < numBits) {
            int arrayIndex = currentBit / 32;
            int bitOffset = currentBit % 32;
            int bitsToWrite = Math.min(numBits - bitsWritten, 32 - bitOffset);
            
            long mask = (1L << bitsToWrite) - 1;
            long shiftedValue = (val >>> bitsWritten) & mask;
            
            long intMask = ~(mask << bitOffset);
            compressedData[arrayIndex] = (int) ((compressedData[arrayIndex] & intMask) | (shiftedValue << bitOffset));

            bitsWritten += bitsToWrite;
            currentBit += bitsToWrite;
        }
    }

    /**
     * Lit 'numBits' depuis 'compressedData' à la position 'startBit'
     */
    public static int readBits(int[] compressedData, int startBit, int numBits) {
        long value = 0;
        int currentBit = startBit;
        int bitsRead = 0;
        
        // La boucle gère la lecture à cheval sur plusieurs entiers (int)        
        while (bitsRead < numBits) {
            int arrayIndex = currentBit / 32;
            int bitOffset = currentBit % 32;
            int bitsToRead = Math.min(numBits - bitsRead, 32 - bitOffset);

            long mask = (1L << bitsToRead) - 1;
            long readValue = (compressedData[arrayIndex] >>> bitOffset) & mask;

            value |= (readValue << bitsRead);
            bitsRead += bitsToRead;
            currentBit += bitsToRead;
        }
        
        if (numBits == 32) return (int) value;
        return (int) (value & ((1L << numBits) - 1));
    }
}