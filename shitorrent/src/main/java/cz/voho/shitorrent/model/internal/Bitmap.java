package cz.voho.shitorrent.model.internal;

import java.util.BitSet;
import java.util.Optional;
import java.util.Random;

/**
 * Created by vojta on 18/01/16.
 */
public class Bitmap {
    private final BitSet available;
    private final int size;

    public Bitmap(final int size) {
        this.available = new BitSet(size);
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
                available.set(i);
            } else if (encodedBits[i] == '0') {
                available.clear(i);
            } else {
                throw new IllegalArgumentException("Invalid character (only 0 and 1 are allowed).");
            }
        }
    }

    public int getSize() {
        return size;
    }

    public boolean isAllAvailable() {
        return available.cardinality() == size;
    }

    public double getAvailableRatio() {
        return (double) available.cardinality() / (double) size;
    }

    public void markAvailable(final int index) {
        available.set(index);
    }

    public void markUnavailable(final int index) {
        available.clear(index);
    }

    public void markAllAvailable() {
        this.available.set(0, size);
    }

    public void markAllUnavailable() {
        this.available.clear(0, size);
    }

    public Optional<Integer> getRandomUnavailableIndex() {
        Optional<Integer> result = Optional.empty();
        Random random = new Random();

        if (this.available.cardinality() < size) {
            // TODO very ineffective :)
            while (!result.isPresent()) {
                int index = random.nextInt(size);
                if (!available.get(index)) {
                    result = Optional.of(index);
                    break;
                }
            }
        }

        return result;
    }

    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder(size);

        for (int i = 0; i < size; i++) {
            buffer.append(available.get(i) ? '1' : '0');
        }

        return buffer.toString();
    }

    public boolean isAvailable(final int chunkIndex) {
        return available.get(chunkIndex);
    }

    public boolean isValidIndex(final int chunkIndex) {
        return chunkIndex >= 0 && chunkIndex < size;
    }

    public boolean hasAnyAvailable() {
        return !available.isEmpty();
    }
}
