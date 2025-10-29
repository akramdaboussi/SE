package main.app;

import java.util.*;

import main.compression.BitPacker;
import main.compression.BitPackerFactory;
import main.compression.CompressionType;

/**
 * Gère l'exécution du test de compression, les mesures de performance et le mode interactif.
 */
public class BenchmarkRunner {

    private String filePath;
    private CompressionType compressionType;

    public BenchmarkRunner(String filePath, CompressionType compressionType) {
        this.filePath = filePath;
        this.compressionType = compressionType;
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
        BitPacker compressor = BitPackerFactory.create(this.compressionType);

        long startTimeCompress = System.nanoTime();
        compressor.compress(originalData);
        long durationCompress = System.nanoTime() - startTimeCompress;
        System.out.println(" *** Temps de compression : " + Utils.formatDuration(durationCompress) + " ***\n");

        // --- DÉBOGAGE ---
        System.out.println("--- Données brutes compressées (" + compressor.getClass().getSimpleName() + ") ---");
        int[] rawData = compressor.getRawCompressedData();
        if (rawData != null) {
            System.out.println("Taille du tableau de sortie : " + rawData.length + " entiers.");
            for (int i = 0; i < rawData.length; i++) {
                // Affiche l'entier en Binaire (formaté sur 32 bits avec des zéros)
                System.out.println(" tab[" + i + "] = " + 
                    String.format("%32s", Integer.toBinaryString(rawData[i])).replace(' ', '0')
                );
            }
        }
        System.out.println("\n");
        
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
            int maxIndex = compressor.decompress().length - 1;
            while (true) {
                System.out.print("Entrez un index pour récupérer sa valeur (<= " + maxIndex + ") (ou 'q' pour quitter) : ");
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("q")) break;

                try {
                    int index = Integer.parseInt(input);
                    
                    long startTimeGet = System.nanoTime();
                    int value = compressor.get(index);
                    long durationGet = System.nanoTime() - startTimeGet;

                    System.out.println(" \n *** L'élément à l'index " + index + " est : " + value + " ***");
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