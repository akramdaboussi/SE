package main.compression;

/**
 * Interface pour les compresseurs de type Bit-Packing.
 * Définit les méthodes communes requises par le projet.
 */
public interface BitPacker {

    /**
     * Compresse un tableau d'entiers.
     * @param tab Le tableau d'entiers à compresser.
     * @return Le tableau d'entiers compressé.
     */
    int[] compress(int[] tab);

    /**
     * Décompresse le tableau entier.
     * @return Le tableau original décompressé.
     */
    int[] decompress();

    /**
     * Retourne la i-ème valeur du tableau original à partir des données compressées.
     * @param i L'index de la valeur à récupérer.
     * @return La valeur originale.
     */
    int get(int i);

    /**
     * (Pour le débogage) Retourne le tableau compressé brut.
     * @return Le tableau d'entiers compressés.
     */
    public int[] getRawCompressedData();
}