import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Kompresní algoritmus LZW.
 *
 * @author Vojtěch Hordějčuk
 */
public class LZW {
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
     * Získá abecedu (seznam symbolů) ze vstupního řetězce.
     *
     * @param input vstupní řetězec
     * @return abeceda vstupního řetězce
     */
    public static List<String> getAlphabet(final String input) {
        final List<String> alphabet = new LinkedList<String>();

        for (int i = 0; i < input.length(); i++) {
            final String temp = input.substring(i, i + 1);

            if (!alphabet.contains(temp)) {
                alphabet.add(temp);
            }
        }

        return alphabet;
    }

    /**
     * Zakomprimuje vstupní řetězec algoritmem LZW.
     *
     * @param alphabet abeceda vstupních symbolů
     * @param input vstupní řetězec
     * @return výstupní posloupnost kódových slov
     */
    public static List<LZWCodeword> compress(final List<String> alphabet, final String input) {
        // kořen slovníku

        final Node root = new Node();

        // rozšířit slovník o symboly abecedy

        for (final String temp : alphabet) {
            root.extend(temp);
        }

        // výstupní posloupnost kódových slov

        final List<LZWCodeword> output = new LinkedList<LZWCodeword>();

        // pozice ve vstupním řetězci

        int position = 0;

        // komprimuj, dokud nedojdeš na konec vstupu

        while (position < input.length()) {
            // aktuální uzel pro nejdelší nalezené slovo

            Node best_node = null;

            // délka nejdelšího nalezeného slova

            int best_skip = 0;

            // délka slova, které se nyní zkouší nalézt ve slovníku

            int try_length = 0;

            // vyhledáváné slovo

            String search = null;

            // poslední symbol nejkratšího nenalezeného slova

            String fresh = null;

            do {
                // zkus najít slovo o jeden symbol delší

                try_length++;

                search = input.substring(position, position + try_length);

                fresh = LZW.getSafeChar(input, position + try_length - 1);

                final Node temp = root.find(search);

                if (temp == null) {
                    // slovo již nebylo nalezeno

                    break;
                } else {
                    // uložit si uzel pro toto slovo a jeho délku

                    best_node = temp;
                    best_skip = try_length;
                }
            }
            while ((position + try_length < input.length()));

            // vložit nové kódové slovo na výstup

            output.add(new LZWCodeword(best_node.getIndex()));

            // rozšířir slovník

            if (fresh != null) {
                best_node.extend(fresh);
            }

            // posunout ukazatel o délku nejdelšího nalezeného slova

            position += best_skip;
        }

        return output;
    }

    /**
     * Dekomprimuje posloupnost kódových slov algoritmu LZW.
     *
     * @param alphabet abeceda vstupních symbolů
     * @param codewords vstupní posloupnost kódových slov
     * @return dekomprimovaný řetězec
     */
    public static String decompress(final List<String> alphabet, final List<LZWCodeword> codewords) {
        // tabulka slov

        final List<String> table = new LinkedList<String>();

        for (final String temp : alphabet) {
            table.add(temp);
        }

        // výstupní řetězec

        final StringBuffer output = new StringBuffer();

        for (int i = 0; i < codewords.size(); i++) {
            // aktuální kódové slovo

            LZWCodeword current = null;

            // následující kódové slovo

            LZWCodeword next = null;

            current = codewords.get(i);

            if (i + 1 < codewords.size()) {
                next = codewords.get(i + 1);
            }

            // na výstup vložit řetězec

            output.append(table.get(current.getIndex() - 1));

            if (next != null) {
                table.add(table.get(current.getIndex() - 1) + table.get(next.getIndex() - 1).charAt(0));
            }
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