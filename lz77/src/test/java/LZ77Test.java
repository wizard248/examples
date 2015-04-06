import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class LZ77Test {
    private final List<Integer> sizesBack = Arrays.asList(6, 5, 4, 10, 100);
    private final List<Integer> sizesAhead = Arrays.asList(4, 3, 2, 20, 90);

    @Test
    public void test() {
        testEncoding("MAMA MELE MASO. MASO MELE MAMU.");
        testEncoding("JELENOVI PIVO NELEJ");
        testEncoding("JEDE JEDE POSTOVSKY PANACEK");
    }

    private void testEncoding(final String original) {
        for (final int sizeBack : sizesBack) {
            for (final int sizeAhead : sizesAhead) {
                final List<LZ77Codeword> encoded = LZ77.compress(original, sizeBack, sizeAhead);
                final String decoded = LZ77.decompress(encoded);
                assertEquals(original, decoded);
            }
        }
    }
}
