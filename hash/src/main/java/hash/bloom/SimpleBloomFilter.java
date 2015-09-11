package hash.bloom;

import java.util.BitSet;
import java.util.function.Function;

/**
 * Simple implementation of a bloom filter.
 */
public class SimpleBloomFilter<T> implements BloomFilter<T> {
    private final int numBits;
    private final BitSet bits;
    private final Function<T, Integer>[] hashFunctions;

    public SimpleBloomFilter(final int numBits, final Function<T, Integer>... hashFunctions) {
        this.numBits = numBits;
        this.bits = new BitSet(this.numBits);
        this.hashFunctions = hashFunctions;
    }

    @Override
    public boolean probablyContains(final T element) {
        for (final Function<T, Integer> hashFunction : hashFunctions) {
            final int hashValue = hashFunction.apply(element);
            final int hashBasedIndex = getSafeIndexFromHash(hashValue);

            if (!bits.get(hashBasedIndex)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void add(final T element) {
        for (final Function<T, Integer> hashFunction : hashFunctions) {
            final int hashValue = hashFunction.apply(element);
            final int hashBasedIndex = getSafeIndexFromHash(hashValue);
            bits.set(hashBasedIndex);
        }
    }

    @Override
    public int getNumberOfActiveBits() {
        return bits.cardinality();
    }

    @Override
    public int getNumberOfBits() {
        return numBits;
    }

    @Override
    public int getNumberOfHashFunctions() {
        return hashFunctions.length;
    }

    private int getSafeIndexFromHash(final int hashValue) {
        // this just prevents the overflow and negative numbers
        final int n = getNumberOfBits();
        final int index = hashValue % n;
        return index < 0 ? index + n : index;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(getNumberOfBits());

        for (int i = 0; i < getNumberOfBits(); i++) {
            sb.append(bits.get(i) ? "1" : 0);
        }

        return sb.toString();
    }
}
