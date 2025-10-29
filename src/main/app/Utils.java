package main.app;

import java.io.*;
import java.util.*;

/**
 * Classe utilitaire contenant des méthodes statiques pour le chargement de fichiers et le formatage.
 */
public class Utils {

    /**
     * Lit un tableau d'entiers à partir d'un fichier texte.
     * Si un élément n'est pas un entier valide, la méthode 
     * lève une exception et arrête le chargement.
     *
     * @param filePath Chemin vers le fichier à lire.
     * @return Tableau d'entiers lus.
     * @throws Exception en cas d'erreur de lecture (IO) ou de format de nombre.
     */
    public static int[] loadDataFromFile(String filePath) throws Exception {
        List<Integer> dataList = new ArrayList<>();
        int lineNumber = 0; // On commence à 0

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++; 
                String[] elements = line.trim().split("\\s+");
                for (String elem : elements) {
                    if (elem.isEmpty()) continue;
                    try {
                        dataList.add(Integer.parseInt(elem));
                    } catch (NumberFormatException e) {
                        throw new Exception(
                            "Erreur de format : L'élément '" + elem + "' à la ligne " + lineNumber + " n'est pas un entier."
                        );
                    }
                }
            }
        }
        
        // Conversion de List<Integer> en int[]
        int[] result = new int[dataList.size()];
        for (int i = 0; i < dataList.size(); i++) {
            result[i] = dataList.get(i);
        }
        return result;
    }

    /**
     * Formate une durée en nanosecondes en une chaîne lisible (ns, µs, ms, s).
     * @param nanos La durée en nanosecondes.
     * @return Une chaîne de caractères formatée.
     */
    public static String formatDuration(long nanos) {
        if (nanos < 1000) {
            return nanos + " ns";
        }
        double micros = nanos / 1000.0;
        if (micros < 1000) {
            return String.format("%.2f µs", micros);
        }
        double millis = micros / 1000.0;
        if (millis < 1000) {
            return String.format("%.2f ms", millis);
        }
        double seconds = millis / 1000.0;
        return String.format("%.2f s", seconds);
    }
}