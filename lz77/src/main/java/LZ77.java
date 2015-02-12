import java.util.LinkedList;
import java.util.List;

/**
 * Kompresní algoritmus LZ77.
 *
 * @author Vojtěch Hordějčuk
 */
public class LZ77 {
    /**
     * Posuvné okénko.
     *
     * @author Vojtěch Hordějčuk
     */
    private static class Window {
        /**
         * levý index
         */
        private int left;
        /**
         * poslední index search bufferu
         */
        private int middle;
        /**
         * pravý index
         */
        private int right;

        /**
         * Vytvoří nové posuvné okénko.
         *
         * @param sizeBack velikost search bufferu
         * @param sizeAhead velikost look-ahead bufferu
         */
        public Window(final int sizeBack, final int sizeAhead) {
            assert (sizeBack > 0);
            assert (sizeAhead > 0);
            assert (sizeAhead <= sizeBack);

            this.left = -sizeBack;
            this.middle = -1;
            this.right = sizeAhead - 1;
        }

        /**
         * Ověří, zda lze posuvné okénko ještě posunout.
         * (ekvivalentní s tím, zda je look-ahead buffer prázdný)
         *
         * @param input vstupní řetězec
         * @return TRUE právě když lze posuvné okénko ještě posunout
         */
        public boolean canMove(final String input) {
            return (this.middle + 1 < input.length());
        }

        /**
         * Posune posuvné okénko o zadaný počet znaků vpravo.
         *
         * @param delta počet znaků, o který se má okénko posunout
         */
        public void move(final int delta) {
            assert (delta > 0);

            this.left += delta;
            this.middle += delta;
            this.right += delta;
        }

        /**
         * Vyhledá nejdelší prefix look-ahead bufferu v search bufferu.
         * Pro ten vrátí "nejkratší" kódové slovo.
         *
         * @param input vstupní řetězec
         * @return kódové slovo
         */
        public LZ77Codeword getLongestPrefix(final String input) {
            final List<LZ77Codeword> prefixes = new LinkedList<LZ77Codeword>();

            for (int i = this.middle; i >= this.left; i--) {
                // pozice v řetězci - search buffer

                int j = i;

                // pozice v řetězci - look ahead buffer

                int k = this.middle + 1;

                // délka nalezeného řetězce

                int l = 0;

                // dokud se prefixy shodují nebo nebylo dosaženo hranic řetězců, posun

                while ((j >= 0) && (k >= 0) && (j < input.length()) && (k < input.length()) && (j <= this.right) && (k <= this.right) && (input.charAt(j) == input.charAt(k))) {
                    j++;
                    k++;
                    l++;
                }

                if (l > 0) {
                    // společný prefix byl nalezen

                    prefixes.add(new LZ77Codeword(this.middle - i, l, LZ77.getSafeChar(input, k)));
                }
            }

            if (prefixes.isEmpty()) {
                // nebyl nalezen žádný společný prefix

                return new LZ77Codeword(0, 0, LZ77.getSafeChar(input, this.middle + 1));
            } else {
                // alespoň jeden společný prefix byl nalezen, vybrat ten nejdelší

                LZ77Codeword best = null;

                for (final LZ77Codeword temp : prefixes) {
                    if ((best == null) || (temp.getLength() > best.getLength())) {
                        best = temp;
                    }
                }

                return best;
            }
        }
    }

    ;

    /**
     * Zakomprimuje vstupní řetězec algoritmem LZ77.
     *
     * @param input vstupní řetězec
     * @param sizeBack velikost search bufferu
     * @param sizeAhead velikost look-ahead bufferu
     * @return posloupnost kódových slov
     */
    public static List<LZ77Codeword> compress(final String input, final int sizeBack, final int sizeAhead) {
        // posuvné okénko

        final Window window = new Window(sizeBack, sizeAhead);

        // výstupní posloupnost kódových slov

        final List<LZ77Codeword> output = new LinkedList<LZ77Codeword>();

        while (window.canMove(input)) {
            // najít následující kódové slovo

            final LZ77Codeword codeword = window.getLongestPrefix(input);

            // přidat jej na výstup

            output.add(codeword);

            // posunout posuvné okénko

            window.move(codeword.getLength() + 1);
        }

        return output;
    }

    /**
     * Dekomprimuje posloupnost kódových slov algoritmu LZ77.
     *
     * @param codewords vstupní posloupnost kódových slov
     * @return dekomprimovaný řetězec
     */
    public static String decompress(final List<LZ77Codeword> codewords) {
        // výstupní dekódovaný řetězec

        final StringBuffer output = new StringBuffer();

        for (final LZ77Codeword codeword : codewords) {
            // do bufferu přidat prefix

            if (codeword.getPosition() != 0) {
                for (int i = 0; i < codeword.getLength(); i++) {
                    output.append(output.charAt(output.length() - codeword.getPosition()));
                }
            }

            // do bufferu přidat znak po prefixu

            output.append(codeword.getTerminal());
        }

        // na výstup přidat celou dekomprimovanou posloupnost

        return output.toString();
    }

    /**
     * Vrátí znak na zadané pozici.
     * Pokud je pozice mimo rozsah, vrátí prázdný řetězec ("").
     *
     * @param input vstupní řetězec
     * @param index pozice (index)
     * @return znak na zadané pozici vstupního řetězce, nebo prázdný řetězec
     */
    private static String getSafeChar(final String input, final int index) {
        if ((index < 0) || (index >= input.length())) {
            return "";
        } else {
            return input.substring(index, index + 1);
        }
    }
}
