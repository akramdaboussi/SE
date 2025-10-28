package main;

/*
* Classe pour compresser et décompresser un tableau d'entiers 
  en utilisant le bit-packing sans chevauchements.
*/
public class BitPackingNoOverlap implements BitPacker {

    private int[] compressedData; // Le tableau compressé
    private int bitsPerValue; // Le 'k' de l'énoncé qui indique le nombre de bits sur lesquels chaque entier est stocké
    private int size_tab; // Taille du tableau original

    @Override
    public int[] compress(int[] tab) {
        if (tab == null || tab.length == 0) {
            this.size_tab = 0;
            this.compressedData = new int[0];
            return this.compressedData;
        }

        this.size_tab = tab.length;

        // On cherche la valeur maximale sur laquelle on va se baser pour déterminer le nombre de bits 'k'
        int maxVal = 0;
        int i = 0;
        for (i = 0; i < tab.length; i++) {
            int val = tab[i];
            if (val < 0) {
                throw new IllegalArgumentException("Les valeurs négatives ne sont pas supportées : " + val);
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

        int valuesPerInt = 32 / this.bitsPerValue; // renvoie la valeur entière arrondie à l'inférieur du nombre de valeurs par int

        // On calcule la taille du tableau de sortie
        int compressedSize = (int) Math.ceil((double) this.size_tab / valuesPerInt);
        this.compressedData = new int[compressedSize];

        // On remplit le tableau compressé
        for (int j = 0; j < this.size_tab; j++) {
            int index_tab = j / valuesPerInt; // Dans quel int de sortie on écrit
            int index_int = j % valuesPerInt; // À quelle position dans cet int

            int shift = index_int * this.bitsPerValue; // Combien de bits on doit décaler
            
            // On utilise un long pour le décalage pour éviter des problèmes de signe
            long valueToPack = tab[j];

            // On décale la valeur et on l'ajoute avec un OU binaire
            this.compressedData[index_tab] |= (valueToPack << shift);
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

        int valuesPerInt = 32 / this.bitsPerValue;
        int index_tab = i / valuesPerInt;
        int index_int = i % valuesPerInt;
        int shift = index_int * this.bitsPerValue;

        // Création d'un masque pour isoler les 'k' bits qui nous intéressent
        long mask = (1L << this.bitsPerValue) - 1;

        // On récupère le bloc d'entiers packés
        int packedInt = this.compressedData[index_tab];

        // On décale les bits vers la droite pour les aligner puis on applique le masque
        return (int) ((packedInt >> shift) & mask);
    }

    @Override
    public int[] decompress() {
        if (compressedData == null) {
            throw new IllegalStateException("Le tableau n'a pas encore été compressé.");
        }

        int[] decompressedArray = new int[this.size_tab];
        for (int i = 0; i < this.size_tab; i++) {
            decompressedArray[i] = this.get(i);
        }
        return decompressedArray;
    }
}