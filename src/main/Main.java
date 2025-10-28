package main;

/**
 * Point d'entrée principal du programme.
 * Son unique rôle est de valider les arguments et de 
 * lancer le processus de test de performance.
 */
public class Main {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java main.Main <chemin_vers_le_fichier_de_test>");
            return;
        }
        
        String filePath = args[0];
        BenchmarkRunner runner = new BenchmarkRunner(filePath);
        
        try {
            runner.run();
        } catch (Exception e) {
            System.out.println("Une erreur critique est survenue : " + e.getMessage());
        }
    }
}