/**
 * Kódové slovo kompresního algoritmu LZ78.
 *
 * @author Vojtěch Hordějčuk
 */
public class LZ78Codeword {
    private final int index;
    private final String terminal;

    /**
     * Vytvoří nové kódové slovo.
     *
     * @param index index uzlu
     * @param terminal první nenalezený znak
     */
    public LZ78Codeword(final int index, final String terminal) {
        this.index = index;
        this.terminal = terminal;
    }

    /**
     * Vrátí index uzlu.
     *
     * @return index uzlu
     */
    public int getIndex() {
        return this.index;
    }

    /**
     * Vrátí nenalezený znak.
     *
     * @return nenalezený znak
     */
    public String getTerminal() {
        return this.terminal;
    }

    @Override
    public String toString() {
        return String.format("(%d,'%s')", this.index, this.terminal);
    }
}