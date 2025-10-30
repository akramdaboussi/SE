package main.compression;

import java.util.*;
import static main.compression.CompressionUtils.*;


public class BitPackingWithOverflow implements BitPacker {

    private int[] compressedData; // Le tableau compressé
    private int size_tab; // Taille du tableau original

    // --- Métadonnées pour get() ---
    private int payloadBits; // Nb de bits pour la partie payload (index si overflow ou valeur normale)
    private int overflowFlag; // Marqueur binaire d'overflow
    private int payloadMask; // Masque pour extraire la partie payload

    // --- Métadonnées pour get() ---
    private int k_pointers; // Nb de bits par pointeur (1 bit overflow + payloadBits)
    private int k_overflow; // Nb de bits pour les valeurs d'overflow
    private int overflowDataStartBit; // Position de début des données d'overflow dans le tableau compressé
    private int overflowCount; // Nb total de valeurs d'overflow

    // Liste temporaire pour stocker les valeurs d'overflow pendant la compression
    private transient List<Integer> overflowList = new ArrayList<>();


    @Override
    public int[] compress(int[] tab) {
        if (tab == null || tab.length == 0) {
            this.size_tab = 0;
            this.compressedData = new int[0];
            return this.compressedData;
        }
        this.size_tab = tab.length;
        this.overflowList.clear(); // Reset de la liste d'overflow

        // Trouve le 'k' optmimal pour les petites valeurs
        int k_normal = findBestKByBitJump(tab);

        // On sépare les valeurs d'overflow 
        int cutoffValue = (1 << k_normal) - 1;
        int maxOverflowValue = 0;
        for (int val : tab) {
            if (val > cutoffValue) {
                overflowList.add(val);
                if (val > maxOverflowValue) {
                    maxOverflowValue = val;
                }
            }
        }
        this.overflowCount = overflowList.size();

        // Bits nécessaires pour stocker l'index le plus grand d'overflow
        int k_index = bitsFor(Math.max(0, overflowList.size() - 1));
        
        this.payloadBits = Math.max(k_normal, k_index);
        this.k_pointers = 1 + this.payloadBits; // 1 bit pour le flag d'overflow 
        this.k_overflow = bitsFor(maxOverflowValue); 

        this.overflowFlag = 1 << this.payloadBits;
        this.payloadMask = this.overflowFlag - 1;
        int realCutoff = this.payloadMask; 
        
        // On recalcule les overflows si k_index > k_normal
        if (k_index > k_normal) {
            overflowList.clear();
            maxOverflowValue = 0;
            for (int val : tab) {
                if (val > realCutoff) {
                    overflowList.add(val);
                    if (val > maxOverflowValue) {
                        maxOverflowValue = val;
                    }
                }
            }
            // Met à jour k_overflow
            this.k_overflow = bitsFor(maxOverflowValue);
            this.overflowCount = overflowList.size();
        }
        
        // On alloue le tableau de sortie
        int totalPointerBits = this.size_tab * this.k_pointers;
        int totalOverflowBits = this.overflowCount * this.k_overflow;
        int totalBits = totalOverflowBits + totalPointerBits;
        
        this.compressedData = new int[(totalBits + 31) / 32];
        this.overflowDataStartBit = totalPointerBits;
        
        // On remplit le tableau
        int currentWriteBit = 0;
        int overflowCounter = 0;
        // Écrit d'abord les pointeurs et les valeurs normales
        for (int val : tab) {
            if (val <= realCutoff) {
                // Valeur normale : '0' + valeur
                writeBits(this.compressedData, val, this.k_pointers, currentWriteBit);
            } else {
                // Valeur d'overflow : '1' + index dans la liste d'overflow
                int pointer = this.overflowFlag | overflowCounter;
                writeBits(this.compressedData, pointer, this.k_pointers, currentWriteBit);
                overflowCounter++;
            }
            currentWriteBit += this.k_pointers;
        }
        // Écrit ensuite les valeurs d'overflow 
        for (int overflowVal : overflowList) {
            writeBits(this.compressedData, overflowVal, this.k_overflow, currentWriteBit);
            currentWriteBit += this.k_overflow;
        }
        return this.compressedData;
    }

    @Override
    public int get(int i) {
        if (compressedData == null) {
            throw new IllegalStateException("Le tableau n'a pas encore été compressé.");
        }
        if (i < 0 || i >= this.size_tab) {
            throw new IndexOutOfBoundsException("Index " + i + " hors des limites pour la taille " + this.size_tab);
        }
        
        // On lit le pointeur qui est soit une valeur normale, soit un index d'overflow
        int pointerBitPos = i * this.k_pointers;
        int packedValue = readBits(this.compressedData, pointerBitPos, this.k_pointers);

        // On vérifie si c'est une valeur normale ou un overflow
        if ((packedValue & this.overflowFlag) == 0) {
            // Valeur normale
            return packedValue; 
        } else {
            // Valeur d'overflow
            int overflowIndex = packedValue & this.payloadMask;
            // On lit la valeur d'overflow correspondante
            int dataBitPos = this.overflowDataStartBit + (overflowIndex * this.k_overflow);
            return readBits(this.compressedData, dataBitPos, this.k_overflow);
        }
    }

    @Override
    public int[] decompress() {
        if (compressedData == null) {
            throw new IllegalStateException("Le tableau n'a pas encore été compressé.");
        }
        // On décompresse l'ensemble du tableau en utilisant get() 
        int[] decompressedArray = new int[this.size_tab];
        for (int i = 0; i < this.size_tab; i++) {
            decompressedArray[i] = this.get(i); 
        }
        return decompressedArray;
    }

    @Override
    public int[] getRawCompressedData() {
        return this.compressedData;
    }
}