package ds.bitarray;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Implementation of bit array using long buckets.
 */
public class DefaultBitArray implements BitArray {
    private static final int BITS_PER_BUCKET = Long.SIZE;
    private static final long FIRST_BIT = 1L << (Long.SIZE - 1);
    private final int sizeInBits;
    private final long[] bitBuckets;

    public DefaultBitArray(final int sizeInBits) {
        this.sizeInBits = sizeInBits;
        final int numBuckets = getNumberOfBuckets(sizeInBits);
        this.bitBuckets = new long[numBuckets];
    }

    private static int getNumberOfBuckets(final int sizeInBits) {
        // this is a "ceil" operation shortened
        return (sizeInBits + BITS_PER_BUCKET - 1) / BITS_PER_BUCKET;
    }

    private static long getFlag(final int index) {
        return FIRST_BIT >>> (index % BITS_PER_BUCKET);
    }

    @Override
    public int size() {
        return sizeInBits;
    }

    @Override
    public boolean get(final int index) {
        checkValidIndex(index);
        final int bucketIndex = index / BITS_PER_BUCKET;
        final long flag = getFlag(index);
        return (bitBuckets[bucketIndex] & flag) != 0;
    }

    @Override
    public void set(final int index) {
        checkValidIndex(index);
        final int bucketIndex = index / BITS_PER_BUCKET;
        final long flag = getFlag(index);
        bitBuckets[bucketIndex] |= flag;
    }

    @Override
    public void unset(final int index) {
        checkValidIndex(index);
        final int bucketIndex = index / BITS_PER_BUCKET;
        final long flag = ~(getFlag(index));
        bitBuckets[bucketIndex] &= flag;
    }

    @Override
    public void setAll() {
        for (int i = 0; i < bitBuckets.length; i++) {
            bitBuckets[i] = ~(0L);
        }
    }

    @Override
    public void unsetAll() {
        for (int i = 0; i < bitBuckets.length; i++) {
            bitBuckets[i] = 0L;
        }
    }

    @Override
    public String toString() {
        return IntStream.range(0, sizeInBits).mapToObj(i -> get(i) ? "1" : "0").collect(Collectors.joining());
    }

    private void checkValidIndex(final int index) {
        if (index < 0) {
            throw new IllegalArgumentException(String.format("Index must be >= 0, but was %d.", index));
        }

        if (index >= sizeInBits) {
            throw new IllegalArgumentException(String.format("Index must be < %d, but was %d.", sizeInBits, index));
        }
    }
}
