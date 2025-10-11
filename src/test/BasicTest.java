package test;
import java.util.Arrays;
import main.BitPackingSimple;

public class BasicTest {

    public static void main(String[] args) {
        System.out.println("--- DÉBUT DES TESTS ---");

        testSimpleCase();
        testDirectAccessGet();
        testEmptyArray();

        System.out.println("--- FIN DES TESTS ---");
    }

    public static void testSimpleCase() {
        System.out.print("Test: Cas simple : ");
        BitPackingSimple compressor = new BitPackingSimple();
        int[] original = {1, 2, 3, 4, 5, 6, 7, 8};
        compressor.compress(original);
        int[] decompressed = compressor.decompress();

        if (Arrays.equals(original, decompressed)) {
            System.out.println("SUCCÈS");
        } else {
            System.out.println("ÉCHEC");
        }
    }

    public static void testDirectAccessGet() {
        System.out.print("Test: Accès direct get(3)... ");
        BitPackingSimple compressor = new BitPackingSimple();
        int[] original = {10, 20, 30, 40, 50};
        compressor.compress(original);
        int result = compressor.get(3);

        if (result == 40) {
            System.out.println("SUCCÈS");
        } else {
            System.out.println("ÉCHEC (attendu: 40, obtenu: " + result + ")");
        }
    }
    
    public static void testEmptyArray() {
        System.out.print("Test: Tableau vide... ");
        BitPackingSimple compressor = new BitPackingSimple();
        int[] original = {};
        
        int[] compressed = compressor.compress(original);
        if (compressed.length == 0) {
            System.out.println("SUCCÈS");
        } else {
            System.out.println("ÉCHEC");
        }
    }
}