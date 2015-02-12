import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Kompresní algoritmus LZ78.
 *
 * @author Vojtěch Hordějčuk
 */
public class LZ78 {
    /**
     * Uzel slovníku, představující jedno slovo.
     *
     * @author Vojtěch Hordějčuk
     */
    private static class Node {
        /**
         * počítadlo uzlů pro generování unikátního indexu
         */
        private static int counter = 0;
        /**
         * unikátní index uzlu
         */
        private final int index;
        /**
         * hrany k potomkům
         */
        private final Map<String, Node> edges;

        /**
         * Vytvoří nový uzel.
         */
        public Node() {
            this.index = Node.counter;
            this.edges = new HashMap<String, Node>();
            Node.counter++;
        }

        /**
         * Vrátí uzel ve kterém končí daný prefix, nebo NULL.
         *
         * @param prefix hledaný prefix
         * @return uzel ve kterém končí daný prefix, nebo NULL
         */
        public Node find(final String prefix) {
            if (prefix.length() < 1) {
                // PREFIX končí zde

                return this;
            } else {
                final Node next = this.edges.get(prefix.substring(0, 1));

                if (next == null) {
                    // PREFIX není ve slovníku kompletní

                    return null;
                } else {
                    // vyhledat zbytek PREFIXu

                    return next.find(prefix.substring(1));
                }
            }
        }

        /**
         * Rozšíří uzel slovníku o nového potomka a spojí jej zadanou hranou.
         *
         * @param terminal symbol pro danou hranu
         */
        public void extend(final String terminal) {
            this.edges.put(terminal, new Node());
        }

        /**
         * Vrátí unikátní index daného uzlu.
         *
         * @return ID uzlu
         */
        public int getIndex() {
            return this.index;
        }

        @Override
        public String toString() {
            return String.format("(%d, edges to %s)", this.index, this.edges.toString());
        }
    }

    /**
     * Zakomprimuje vstupní řetězec algoritmem LZ78.
     *
     * @param input vstupní řetězec
     * @return výstupní posloupnost kódových slov
     */
    public static List<LZ78Codeword> compress(final String input) {
        // kořen slovníku

        final Node root = new Node();

        // výstupní posloupnost kódových slov

        final List<LZ78Codeword> output = new LinkedList<LZ78Codeword>();

        // pozice ve vstupním řetězci

        int position = 0;

        // komprimuj, dokud nedojdeš na konec vstupu

        while (position < input.length()) {
            // uzel s nejdelším nalezeným prefixem

            Node leaf = root;

            // délka nejdelšího nalezeného prefixu

            int longest_prefix = 0;

            // délka prefixu, který bude v dalším kroku vyzkoušen

            int try_prefix_length = 1;

            // nalézt prefix ve slovníku

            while (position + try_prefix_length <= input.length()) {
                // vygenerovat prefix

                final String prefix = input.substring(position, position + try_prefix_length);

                // nalézt uzel, ve kterém prefix končí

                final Node temp = root.find(prefix);

                if (temp == null) {
                    // prefix nebyl nalezen, spokojíme se s tím co máme

                    break;
                } else {
                    // zapamatovat si uzel obsahující delší prefix

                    leaf = temp;
                    longest_prefix = try_prefix_length;

                    // vyhledat ještě delší prefix

                    try_prefix_length++;
                }
            }

            // posunout ukazatel vstupního řetězce

            position += longest_prefix + 1;

            // načíst první nenalezený znak

            final String terminal = LZ78.getSafeChar(input, position - 1);

            // přidat kódové slovo

            output.add(new LZ78Codeword(leaf.getIndex(), terminal));

            // rozšířit slovník

            leaf.extend(terminal);
        }

        return output;
    }

    /**
     * Dekomprimuje posloupnost kódových slov algoritmu LZ77.
     *
     * @param codewords vstupní posloupnost kódových slov
     * @return dekomprimovaný řetězec
     */
    public static String decompress(final List<LZ78Codeword> codewords) {
        // tabulka slov

        final List<String> table = new LinkedList<String>();

        // výstupní řetězec

        final StringBuilder output = new StringBuilder();

        for (final LZ78Codeword codeword : codewords) {
            final String current;

            if (codeword.getIndex() == 0) {
                // neexistující slovo (nový symbol)

                current = codeword.getTerminal();
            } else {
                // existující slovo + terminál

                current = table.get(codeword.getIndex() - 1) + codeword.getTerminal();
            }

            // vložit slovo na výstup

            output.append(current);

            // rozšířit tabulku slov

            table.add(current);
        }

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
