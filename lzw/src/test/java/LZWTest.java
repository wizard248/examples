import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class LZWTest {
    @Test
    public void test() {
        testEncoding("MAMA MELE MASO. MASO MELE MAMU.");
        testEncoding("JELENOVI PIVO NELEJ");
        testEncoding("JEDE JEDE POSTOVSKY PANACEK");
    }

    private void testEncoding(final String original) {
        final List<String> alphabet = LZW.getAlphabet(original);
        final List<LZWCodeword> encoded = LZW.compress(alphabet, original);
        final String decoded = LZW.decompress(alphabet, encoded);
        assertEquals(original, decoded);
    }
}
