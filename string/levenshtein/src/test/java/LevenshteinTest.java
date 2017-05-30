import org.junit.Test;

import static org.junit.Assert.*;

public class LevenshteinTest {
    @Test
    public void test() throws Exception {
        testUsingAllMethodsBothSides(3, "sitting", "kitten");
        testUsingAllMethodsBothSides(3, "saturday", "sunday");
        testUsingAllMethodsBothSides(8, "raisethysword", "rosettacode");
    }

    private void testUsingAllMethodsBothSides(final int expectedLength, final String either, final String another) {
        testUsingAllMethodsSingleSide(expectedLength, either, another);
        testUsingAllMethodsSingleSide(expectedLength, another, either);
    }

    private void testUsingAllMethodsSingleSide(final int expectedLength, final String first, final String second) {
        assertEquals(expectedLength, Levenshtein.distanceWagnerFischerOptimized(first.toCharArray(), second.toCharArray()));
        assertEquals(expectedLength, Levenshtein.distanceWagnerFischer(first.toCharArray(), second.toCharArray()));
    }
}