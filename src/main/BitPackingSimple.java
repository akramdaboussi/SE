package main;

import java.util.Arrays;
import java.util.Scanner;

/*
* Classe pour compresser et décompresser un tableau d'entiers 
  en utilisant le bit-packing sans chevauchements.
*/
public class BitPackingSimple {

    private int[] compressedData; // Le tableau compressé
    private int bitsPerValue; // Le 'k' de l'énoncé qui indique le nombre de bits sur lesquels chaque entier est stocké
    private int size_tab; // Taille du tableau original

    /**
     * Compresse un tableau d'entiers.
     * @param tab Le tableau d'entiers à compresser.
     * @return Le tableau d'entiers compressé.
     */
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

    /**
     * Retourne la i-ème valeur du tableau original à partir des données compressées.
     * @param i L'index de la valeur à récupérer.
     * @return La valeur originale.
     */
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

    /**
     * Décompresse le tableau entier.
     * Cette méthode réutilise get() pour plus de simplicité.
     * @return Le tableau original décompressé.
     */
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

    /**
     * Lit un tableau d'entiers à partir d'un fichier texte.
     * Chaque entier doit être séparé par des espaces ou des retours à la ligne.
     * @param filePath Chemin vers le fichier à lire.
     * @return Tableau d'entiers lus.
     * @throws Exception en cas d'erreur de lecture ou de format.
     */
    public static int[] loadDataFromFile(String filePath) throws Exception {
        java.util.List<Integer> dataList = new java.util.ArrayList<>();
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.trim().split("\\s+");
                for (String token : tokens) {
                    if (!token.isEmpty()) {
                        dataList.add(Integer.parseInt(token));
                    }
                }
            }
        }
        int[] result = new int[dataList.size()];
        for (int i = 0; i < dataList.size(); i++) {
            result[i] = dataList.get(i);
        }
        return result;
    }

    /**
     * Le point d'entrée principal du programme.
     * Lance un test à partir d'un fichier, puis passe en mode interactif.
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java main.BitPackingSimple <chemin_vers_le_fichier_de_test>");
            return;
        }
        String filePath = args[0];

        try {
            // --- Compression / Décompression ---
            System.out.println("--- 1. Test de Compression/Décompression ---");
            int[] originalData = loadDataFromFile(filePath);
            System.out.println("Données lues du fichier : " + Arrays.toString(originalData));

            BitPackingSimple compressor = new BitPackingSimple();
            compressor.compress(originalData);
            int[] decompressedData = compressor.decompress();

            if (Arrays.equals(originalData, decompressedData)) {
                System.out.println("SUCCÈS : La décompression est correcte.");
            } else {
                System.out.println("ÉCHEC : Les données sont différentes après décompression !");
                return;
            }

            // --- Récupérer un élément ---
            System.out.println("\n--- 2. Mode Interactif : Récupérer un élément ---");
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.print("Entrez un index pour récupérer sa valeur (ou 'q' pour quitter) : ");
                String input = scanner.nextLine();

                if (input.equalsIgnoreCase("q")) break;

                try {
                    int index = Integer.parseInt(input);
                    int value = compressor.get(index);
                    System.out.println(" L'élément à l'index " + index + " est : " + value);
                } catch (Exception e) {
                    System.out.println(" Erreur : Entrée invalide ou index hors des limites.");
                }
            }
            scanner.close();
            System.out.println("\nProgramme terminé.");

        } catch (Exception e) {
            System.out.println("Une erreur critique est survenue : " + e.getMessage());
        }
    }
}