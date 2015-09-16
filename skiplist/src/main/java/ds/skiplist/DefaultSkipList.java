package ds.skiplist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.Optional;

/**
 * Skip list implementation according to the original article.
 */
public class DefaultSkipList<K extends Comparable<? super K>, V> implements SkipList<K, V> {
    /**
     * logger instance
     */
    private static final Logger log = LoggerFactory.getLogger(DefaultSkipList.class);
    /**
     * probability of skipping to a higher level
     */
    private static final double P_LEVEL_SKIP = 0.5;
    /**
     * header element (always the same, never empty)
     */
    private final Element header;
    /**
     * maximum number of levels allowed
     */
    private final int maxNumberOfLevels;
    /**
     * top level currently in the list
     */
    private int topLevel;

    public DefaultSkipList(final int maxNumberOfLevels) {
        this.maxNumberOfLevels = maxNumberOfLevels;
        this.topLevel = 0;
        this.header = new Header();
    }

    // READING
    // =======

    @Override
    public Optional<V> get(final K key) {
        final Element x = lookup(key, null);

        if (hasKey(x, key)) {
            return Optional.of(getValue(x));
        }

        return Optional.empty();
    }

    // WRITING
    // =======

    @Override
    public void insert(final K key, final V value) {
        final Element[] update = createArrayOfElements();
        final Element x = lookup(key, update);

        if (hasKey(x, key)) {
            log.debug("Overriding value: {}", x);
            setValue(x, value);
            return;
        }

        final int randomItemLevel = getRandomLevel();
        topLevel = Math.max(topLevel, randomItemLevel);

        final Element newElement = new Element(key, value);

        for (int i = 0; i <= randomItemLevel; i++) {
            final Element insertAfter = update[i];
            setForward(newElement, i, getForward(insertAfter, i));
            setForward(insertAfter, i, newElement);
            log.debug("Inserted new element {} after {}.", newElement, insertAfter);
        }
    }

    @Override
    public boolean delete(final K key) {
        final Element[] update = createArrayOfElements();
        final Element x = lookup(key, update);

        if (!hasKey(x, key)) {
            // not present - deletion not necessary

            log.debug("Not deleting - the key [{}] is not present.", key);
            return false;
        }

        // present - delete node by joining the list

        for (int i = 0; i <= topLevel; i++) {
            if (getForward(update[i], i) == x) {
                // skip the node being removed
                setForward(update[i], i, getForward(x, i));
            } else {
                // no need to continue further
                break;
            }
        }

        // lower the list level if necessary

        while (topLevel > 0 && !hasForward(header, topLevel)) {
            log.debug("Lowering list level from {} to one less.", topLevel);
            topLevel--;
        }

        log.debug("Removed key [{}].", key);
        return true;
    }

    // HELPER METHODS
    // ==============

    /**
     * Important helper method for performing key lookup.
     * Besides the lookup it can also build an array of closest predecessors for each level.
     * If the target array is given as argument instead of NULL, it will result to this:
     * <ul>
     * <li>levels outside of range - initialized with header</li>
     * <li>levels in range - the closest element preceding the key looked up is stored</li>
     * </ul>
     * This array of predecessors can be used to simplify other operations.
     *
     * @param key key to lookup
     * @param closestPredecessorsTarget array of predecessors to update (must be of sufficient length)
     * @return best candidate found or NULL
     */
    private Element lookup(final K key, final Element[] closestPredecessorsTarget) {
        if (closestPredecessorsTarget != null) {
            // initialize predecessors with header

            for (int i = 0; i < maxNumberOfLevels; i++) {
                closestPredecessorsTarget[i] = header;
            }
        }

        log.debug("Looking up key [{}]...", key);
        Element x = header;

        for (int i = topLevel; i >= 0; i--) {
            // move forward through level as long as the keys are lower

            while (hasForward(x, i) && hasLowerKey(getForward(x, i), key)) {
                x = getForward(x, i);
            }

            if (closestPredecessorsTarget != null) {
                // store closest element on each level

                log.debug("Storing closest predecessor for level {}: {}", i, x);
                closestPredecessorsTarget[i] = x;
            }
        }

        // only the lowest-level successor can be the candidate for sure

        final Element candidate = getForward(x, 0);
        log.debug("Candidate returned: {}", candidate);
        return candidate;
    }

    private int getRandomLevel() {
        int level = 0;

        while (Math.random() < P_LEVEL_SKIP && level < maxNumberOfLevels - 1) {
            level++;
        }

        return level;
    }

    private Element getForward(final Element element, final int level) {
        return element.forward[level];
    }

    private void setForward(final Element element, final int level, final Element newForward) {
        element.forward[level] = newForward;
    }

    private boolean hasForward(final Element element, final int level) {
        return element.forward[level] != null;
    }

    private boolean hasLowerKey(final Element element, final K key) {
        return element.key.compareTo(key) < 0;
    }

    private boolean hasKey(final Element element, final K key) {
        return element != null && element.key.equals(key);
    }

    private V getValue(final Element element) {
        return element.value;
    }

    private void setValue(final Element element, final V newValue) {
        element.value = newValue;
    }

    @SuppressWarnings("unchecked")
    private Element[] createArrayOfElements() {
        return (Element[]) Array.newInstance(Element.class, maxNumberOfLevels);
    }

    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder(256);

        for (int i = 0; i <= topLevel; i++) {
            buffer.append("L");
            buffer.append(String.valueOf(i));
            buffer.append(": ");

            Element e = getForward(header, i);

            while (e != null) {
                buffer.append(String.valueOf(e));
                buffer.append(",");

                e = getForward(e, i);
            }

            buffer.append("<END>\n");
        }

        return buffer.toString();
    }

    /**
     * Generic element.
     */
    private class Element {
        private final Element[] forward;
        private final K key;
        private V value;

        public Element(final K key, final V value) {
            this.key = key;
            this.value = value;
            this.forward = createArrayOfElements();
        }

        @Override
        public String toString() {
            return String.format("{%s => %s}", key, value);
        }
    }

    /**
     * Special header element.
     */
    private class Header extends Element {
        public Header() {
            super(null, null);
        }

        @Override
        public String toString() {
            return "<HEADER>";
        }
    }
}
