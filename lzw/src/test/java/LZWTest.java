import java.util.List;

import org.junit.Test;

public class LZWTest {
    @Test
    public void test() {
        // provést kompresi a zpětnou dekompresi

        final String original = "MAMA MELE MASO. MASO MELE MAMU.";
        final List<String> alphabet = LZW.getAlphabet(original);
        final List<LZWCodeword> encoded = LZW.compress(alphabet, original);
        final String decoded = LZW.decompress(alphabet, encoded);

        // vypsat vstup a výstup pro ověření funkce

        System.out.println("Original:\n\n" + original + "\n");
        System.out.println("Encoded:\n\n" + encoded + "\n");
        System.out.println("Decoded:\n\n" + decoded + "\n");

        // ověřit shodu

        if (original.equals(decoded)) {
            System.out.println("SUCCESS");
        } else {
            System.out.println("ERROR");
        }
    }
}
