package main.app;

import main.compression.CompressionType;

/**
 * Point d'entrée principal du programme.
 * Son unique rôle est de valider les arguments et de 
 * lancer le processus de test de performance.
 */
public class Main {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java main.Main <chemin_vers_le_fichier> <type_compression>");
            System.out.println("Types valides : 'overlap', 'no_overlap', 'with_overflow'");
            return;
        }
        
        String filePath = args[0];
        String typeString = args[1];
        
        CompressionType compressionType;
        if ("overlap".equalsIgnoreCase(typeString)) {
            compressionType = CompressionType.WITH_OVERLAP;
        } else if ("no_overlap".equalsIgnoreCase(typeString)) {
            compressionType = CompressionType.NO_OVERLAP;
        } else if ("with_overflow".equalsIgnoreCase(typeString)) {
            compressionType = CompressionType.WITH_OVERFLOW;
        } else {
            System.out.println("Erreur : Type de compression '" + typeString + "' non valide.");
            System.out.println("Types valides : 'overlap', 'no_overlap', 'with_overflow'");
            return;
        }
        BenchmarkRunner runner = new BenchmarkRunner(filePath, compressionType);

        try {
            runner.run();
        } catch (Exception e) {
            System.out.println("Une erreur critique est survenue : " + e.getMessage());
        }
    }
}