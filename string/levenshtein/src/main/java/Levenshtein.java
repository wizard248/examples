import java.util.Arrays;

public class Levenshtein {
    /**
     * Calculates the Levenshtein distance of two strings.
     * Uses an optimized Wagner-Fischer algorithm.
     * @param s first string
     * @param t second string
     * @return Levenshtein distance
     * @see https://en.wikipedia.org/wiki/Wagner%E2%80%93Fischer_algorithm
     */
    public static int distanceWagnerFischerOptimized(final char[] s, final char[] t) {
        // to save memory for the arrays, we prefer having shorter string in columns
        final char[] wordInRows = s.length < t.length ? s : t;
        final char[] wordInColumns = s.length < t.length ? t : s;

        int[] previousRow = new int[wordInRows.length + 1];
        int[] currentRow = new int[wordInRows.length + 1];

        for (int i = 0; i < previousRow.length; i++) {
            // 0, 1, 2, 3, 4, 5...
            previousRow[i] = i;
        }

        for (int i = 0; i < wordInColumns.length; i++) {
            currentRow[0] = i + 1;

            for (int j = 0; j < wordInRows.length; j++) {
                if (wordInColumns[i] == wordInRows[j]) {
                    // no change
                    currentRow[j + 1] = previousRow[j];
                } else {
                    currentRow[j + 1] = min(
                            // insertion
                            currentRow[j] + 1,
                            // removal
                            previousRow[j + 1] + 1,
                            // substitution (if the character is different)
                            previousRow[j] + 1
                    );
                }
            }

            // swap rows

            final int[] tempRow = previousRow;
            previousRow = currentRow;
            currentRow = tempRow;
        }

        return previousRow[wordInRows.length];
    }

    /**
     * Calculates the Levenshtein distance of two strings.
     * Uses a Wagner-Fischer algorithm.
     * @param s first string
     * @param t second string
     * @return Levenshtein distance
     * @see https://en.wikipedia.org/wiki/Wagner%E2%80%93Fischer_algorithm
     */
    public static int distanceWagnerFischer(final char[] s, final char[] t) {
        final int[][] d = new int[s.length + 1][t.length + 1];

        for (int i = 0; i <= s.length; i++) {
            d[i][0] = i;
        }

        for (int i = 0; i <= t.length; i++) {
            d[0][i] = i;
        }

        for (int it = 1; it <= t.length; it++) {
            for (int is = 1; is <= s.length; is++) {
                if (s[is - 1] == t[it - 1]) {
                    // no change
                    d[is][it] = d[is - 1][it - 1];
                } else {
                    d[is][it] = min(
                            // deletion
                            d[is - 1][it] + 1,
                            // insertion
                            d[is][it - 1] + 1,
                            // substitution
                            d[is - 1][it - 1] + 1
                    );
                }
            }
        }

        return d[s.length][t.length];
    }

    private static int min(final int a, final int b, final int c) {
        return Math.min(a, Math.min(b, c));
    }
}