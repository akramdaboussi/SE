package main;

import java.util.Arrays;
import java.util.Scanner;

/**
 * Gère l'exécution du test de compression, les mesures de performance
 * et le mode interactif.
 */
public class BenchmarkRunner {

    private String filePath;

    public BenchmarkRunner(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Lance l'ensemble du processus de test.
     * @throws Exception Si le chargement du fichier échoue.
     */
    public void run() throws Exception {
        // --- Chargement des données ---
        int[] originalData = Utils.loadDataFromFile(filePath);
        if (originalData.length == 0) {
            System.out.println("Aucune donnée valide n'a été lue du fichier. Arrêt.");
            return;
        }

        // --- Compression ---
        System.out.println("\n--- Test de Compression/Décompression ---");
        
        BitPacker compressor = new BitPackingNoOverlap();
        
        long startTimeCompress = System.nanoTime();
        compressor.compress(originalData);
        long durationCompress = System.nanoTime() - startTimeCompress;
        System.out.println(" *** Temps de compression : " + Utils.formatDuration(durationCompress) + " ***");

        // --- Décompression ---
        long startTimeDecompress = System.nanoTime();
        int[] decompressedData = compressor.decompress();
        long durationDecompress = System.nanoTime() - startTimeDecompress;
        System.out.println(" *** Temps de décompression : " + Utils.formatDuration(durationDecompress) + " ***");

        // --- Validation ---
        if (Arrays.equals(originalData, decompressedData)) {
            System.out.println("SUCCÈS : La décompression est correcte.");
        } else {
            System.out.println("ÉCHEC : Les données sont différentes après décompression !");
            return; 
        }

        // --- Mode interactif (pour le get()) ---
        runInteractiveMode(compressor);
    }

    /**
     * Boucle pour tester la fonction get().
     * @param compressor Le compresseur initialisé.
     */
    private void runInteractiveMode(BitPacker compressor) {
        System.out.println("\n--- Récupérer un élément ---");
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("Entrez un index pour récupérer sa valeur (ou 'q' pour quitter) : ");
                String input = scanner.nextLine();

                if (input.equalsIgnoreCase("q")) break;

                try {
                    int index = Integer.parseInt(input);
                    
                    long startTimeGet = System.nanoTime();
                    int value = compressor.get(index);
                    long durationGet = System.nanoTime() - startTimeGet;

                    System.out.println(" *** L'élément à l'index " + index + " est : " + value + " ***");
                    System.out.println(" *** Temps d'accès 'get' : " + Utils.formatDuration(durationGet) + " ***\n");

                } catch (NumberFormatException e) {
                    System.out.println(" Erreur : Entrée invalide. Veuillez entrer un nombre.");
                } catch (Exception e) {
                    System.out.println(" Erreur : " + e.getMessage());
                }
            }
        }
    }
}