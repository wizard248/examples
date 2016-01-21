package cz.voho.shitorrent.model.internal;

import java.util.BitSet;

/**
 * Created by vojta on 18/01/16.
 */
public class Bitmap {
    private final BitSet flags;
    private final int size;

    public Bitmap(final int size) {
        this.flags = new BitSet(size);
        this.size = size;
    }

    public Bitmap(final String encoded) {
        this(encoded.length());
        updateFromString(encoded);
    }

    private void updateFromString(String encoded) {
        char[] encodedBits = encoded.toCharArray();
        for (int i = 0; i < encodedBits.length; i++) {
            if (encodedBits[i] == '1') {
                flags.set(i);
            } else if (encodedBits[i] == '0') {
                flags.clear(i);
            } else {
                throw new IllegalArgumentException("Invalid character (only 0 and 1 are allowed).");
            }
        }
    }

    public int getSize() {
        return size;
    }

    public boolean isAllAvailable() {
        return flags.cardinality() == size;
    }

    public double getAvailableRatio() {
        return (double) flags.cardinality() / (double) size;
    }

    public void markAvailable(final int index) {
        flags.set(index);
    }

    public void markUnavailable(final int index) {
        flags.clear(index);
    }

    public void markAllAvailable() {
        this.flags.set(0, size);
    }

    public void markAllUnavailable() {
        this.flags.clear(0, size);
    }


    public boolean isAvailable(final int chunkIndex) {
        return flags.get(chunkIndex);
    }

    public boolean isValidIndex(final int chunkIndex) {
        return chunkIndex >= 0 && chunkIndex < size;
    }

    public boolean hasAnyAvailable() {
        return !flags.isEmpty();
    }

    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder(size);

        for (int i = 0; i < size; i++) {
            buffer.append(flags.get(i) ? '1' : '0');
        }

        return buffer.toString();
    }
}
