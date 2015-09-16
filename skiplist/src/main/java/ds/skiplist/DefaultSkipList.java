package ds.skiplist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.Optional;

/**
 * Skip list implementation according to the original article.
 */
public class DefaultSkipList<K extends Comparable<? super K>, V> implements SkipList<K, V> {
    private static final double P_LEVEL_SKIP = 0.5;
    private static final Logger log = LoggerFactory.getLogger(DefaultSkipList.class);
    private final Element header;
    private final int maxNumberOfLevels;
    private int topLevel;

    public DefaultSkipList(final int maxNumberOfLevels) {
        this.maxNumberOfLevels = maxNumberOfLevels;
        this.topLevel = 0;
        this.header = new Header();
    }

    @Override
    public Optional<V> get(final K key) {
        final Element x = lookup(key, null);

        if (hasKey(x, key)) {
            return Optional.of(getValue(x));
        }

        return Optional.empty();
    }

    private boolean hasKey(Element element, K key) {
        return element != null && element.key.equals(key);
    }

    private V getValue(Element element) {
        return element.value;
    }

    private void setValue(Element element, V newValue) {
        element.value = newValue;
    }

    @Override
    public void insert(final K key, final V value) {
        final Element[] update = createElementArrayAllLevels();
        final Element x = lookup(key, update);

        if (hasKey(x, key)) {
            log.debug("Overriding value: {}", x);
            setValue(x, value);
        } else {
            final int randomItemLevel = getRandomLevel();

            if (randomItemLevel > topLevel) {
                // must extend the list level

                for (int i = topLevel + 1; i <= randomItemLevel; i++) {
                    update[i] = header;
                }

                log.debug("Extending list level from {} to {}.", topLevel, randomItemLevel);
                topLevel = randomItemLevel;
            }

            final Element newElement = new Element(key, value);

            for (int i = 0; i <= randomItemLevel; i++) {
                final Element insertAfter = update[i];
                setForward(newElement, i, getForward(insertAfter, i));
                setForward(insertAfter, i, newElement);
                log.debug("Inserted new element {} after {}.", newElement, insertAfter);
            }
        }
    }

    private Element getForward(Element element, int level) {
        return element.forward[level];
    }

    private void setForward(Element element, int level, Element newForward) {
        element.forward[level] = newForward;
    }

    private boolean hasForward(Element element, int level) {
        return element.forward[level] != null;
    }

    private boolean hasLowerKey(Element element, K key) {
        return element.key.compareTo(key) < 0;
    }

    @Override
    public boolean delete(final K key) {
        final Element[] update = createElementArrayAllLevels();
        final Element x = lookup(key, update);

        if (hasKey(x, key)) {
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
        } else {
            // not present - deletion not necessary

            log.debug("Not deleting - the key [{}] is not present.", key);
            return false;
        }
    }

    private Element lookup(final K key, final Element[] updateTargetOrNull) {
        log.debug("Looking up key [{}]...", key);
        Element x = header;

        for (int i = topLevel; i >= 0; i--) {
            // move forward through level as long as the keys are lower

            while (hasForward(x, i) && hasLowerKey(getForward(x, i), key)) {
                x = getForward(x, i);
            }

            if (updateTargetOrNull != null) {
                // store closest element on each level

                log.debug("Storing closest predecessor for level {}: {}", i, x);
                updateTargetOrNull[i] = x;
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= topLevel; i++) {
            sb.append(String.format("Level %d: %s", i, levelToString(i)));
            sb.append("\n");
        }
        return sb.toString();
    }

    private String levelToString(final int level) {
        final StringBuilder sb = new StringBuilder();
        Element e = header.forward[level];
        while (e != null) {
            sb.append(e.key);
            sb.append(",");
            e = e.forward[level];
        }
        sb.append("<END>");
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private Element[] createElementArrayAllLevels() {
        return (Element[]) Array.newInstance(Element.class, maxNumberOfLevels);
    }

    class Element {
        private final Element[] forward;
        private final K key;
        private V value;

        public Element(final K key, final V value) {
            this.key = key;
            this.value = value;
            this.forward = createElementArrayAllLevels();
        }

        @Override
        public String toString() {
            return String.format("{%s -> %s}", key, value);
        }
    }

    class Header extends Element {
        public Header() {
            super(null, null);
        }

        @Override
        public String toString() {
            return String.format("<HEADER>");
        }
    }
}
