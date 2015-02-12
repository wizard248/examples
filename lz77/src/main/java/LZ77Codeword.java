/**
 * Kódové slovo kompresního algoritmu LZ77.
 *
 * @author Vojtěch Hordějčuk
 */
public class LZ77Codeword {
    /*
     * pozice prefixu od konce search bufferu
     */
    private final int position;
    /**
     * délka prefixu
     */
    private final int length;
    /**
     * první znak, který nalezen nebyl
     */
    private final String terminal;

    /**
     * Vytvoří nové kódové slovo algoritmu LZ77.
     *
     * @param position pozice prefixu
     * @param length délka prefixu
     * @param terminal první nenalezený znak
     */
    public LZ77Codeword(final int position, final int length, final String terminal) {
        this.position = position;
        this.length = length;
        this.terminal = terminal;
    }

    /**
     * Vrátí pozici prefixu.
     *
     * @return pozice prefixu
     */
    public int getPosition() {
        return this.position;
    }

    /**
     * Vrátí délku prefixu.
     *
     * @return délka prefixu
     */
    public int getLength() {
        return this.length;
    }

    /**
     * Vrátí první nenalezený znak.
     *
     * @return první nenalezený znak
     */
    public String getTerminal() {
        return this.terminal;
    }

    @Override
    public String toString() {
        return String.format("(%d,%d,'%s')", this.position, this.length, this.terminal);
    }
}
