import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class LZ78Test {
    @Test
    public void test() {
        testEncoding("MAMA MELE MASO. MASO MELE MAMU.");
        testEncoding("JELENOVI PIVO NELEJ");
        testEncoding("JEDE JEDE POSTOVSKY PANACEK");
    }

    private void testEncoding(final String original) {
        final List<LZ78Codeword> encoded = LZ78.compress(original);
        final String decoded = LZ78.decompress(encoded);
        assertEquals(original, decoded);
    }
}
