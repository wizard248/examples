package ds.skiplist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.Optional;

/**
 * Created by vojta on 15/09/15.
 */
public class DefaultSkipList<K extends Comparable<? super K>, V> implements SkipList<K, V> {
    private static final Logger log = LoggerFactory.getLogger(DefaultSkipList.class);

    private Element onlyHeader;
    private Element header;
    private int numberOfLevels;
    private int topLevel;

    public DefaultSkipList(int numberOfLevels) {
        this.numberOfLevels = numberOfLevels;
        this.topLevel = 0;
        onlyHeader = new Header(null, null);
        this.header = onlyHeader;
    }

    @Override
    public Optional<V> get(K key) {
        // start at head

        Element x = header;

        for (int i = topLevel; i >= 0; i--) {
            // move forward through level as long as the keys are lower

            while (x.hasForward(i) && x.getForward(i).hasLowerKey(key)) {
                x = x.getForward(i);
            }
        }

        // we need to check the lowest level

        x = x.getForward(0);

        if (x != null && x.hasKey(key)) {
            return Optional.of(x.getValue());
        }

        return Optional.empty();
    }

    @Override
    public void insert(K key, V value) {
        // start at head

        Element[] update = (Element[]) Array.newInstance(Element.class, numberOfLevels);
        Element x = header;

        for (int i = topLevel; i >= 0; i--) {
            // move forward through level as long as the keys are lower

            while (x.hasForward(i) && x.getForward(i).hasLowerKey(key)) {
                x = x.getForward(i);
            }

            // store closest element on each level

            update[i] = x;
        }

        // we need to check the lowest level

        x = x.getForward(0);

        if (x != null && x.hasKey(key)) {
            x.setValue(value);
        } else {
            int lvl = getRandomLevel();
            System.out.println("put " + key + " at " + lvl);

            if (lvl > topLevel) {
                for (int i = topLevel + 1; i <= lvl; i++) {
                    update[i] = header;
                }
                topLevel = lvl;
            }

            Element n = new Element(key, value);

            for (int i = 0; i <= lvl; i++) {
                n.setForward(i, update[i].getForward(i));
                update[i].setForward(i, n);
            }
        }
    }

    @Override
    public boolean delete(K key) {
        // start at head

        Element[] update = (Element[]) Array.newInstance(Element.class, numberOfLevels);
        Element x = header;

        for (int i = topLevel; i >= 0; i--) {
            // move forward through level as long as the keys are lower

            while (x.hasForward(i) && x.getForward(i).hasLowerKey(key)) {
                x = x.getForward(i);
            }

            // store closest element on each level

            update[i] = x;
        }

        // we need to check the lowest level

        x = x.getForward(0);

        if (x != null && x.hasKey(key)) {
            // present - delete node by joining the list

            for (int i = 0; i <= topLevel; i++) {
                if (update[i].getForward(i) == x) {
                    // skip the node being removed
                    update[i].setForward(i, x.getForward(i));
                } else {
                    // no need to continue further
                    break;
                }
            }

            // lower the list level if necessary

            while (topLevel > 0 && header.getForward(topLevel) == null) {
                topLevel--;
            }

            return true;
        } else {
            // not present - deletion not necessary

            return false;
        }
    }

    private Element lookup(K key, Element[] updateTargetOrNull) {
        Element x = header;

        for (int i = topLevel; i >= 0; i--) {
            // move forward through level as long as the keys are lower

            while (x.hasForward(i) && x.getForward(i).hasLowerKey(key)) {
                x = x.getForward(i);
            }

            if (updateTargetOrNull != null) {
                // store closest element on each level

                updateTargetOrNull[i] = x;
            }
        }

        // only the lowest-level successor can be the candidate for sure

        return x.getForward(0);
    }

    private int getRandomLevel() {
        int level = 0;

        while (Math.random() < 0.5 && level < numberOfLevels - 1) {
            level++;
        }

        return level;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= topLevel; i++) {
            sb.append(String.format("Level %d: %s", i, toStringSingleLevel(i)));
            sb.append("\n");
        }
        return sb.toString();
    }

    public String toStringSingleLevel(int level) {
        StringBuilder sb = new StringBuilder();
        Element e = header.forward[level];
        while (e != null) {
            sb.append(e.key);
            sb.append(",");
            e = e.forward[level];
        }
        sb.append("<END>");
        return sb.toString();
    }

    class Element {
        private Element[] forward;
        private K key;
        private V value;

        public Element(K key, V value) {
            this.key = key;
            this.value = value;
            this.forward = (Element[]) Array.newInstance(Element.class, numberOfLevels);
        }

        public Element getForward(int i) {
            return forward[i];
        }

        public boolean hasLowerKey(K other) {
            return this.key.compareTo(other) < 0;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }

        public boolean hasKey(K key) {
            return this.key.equals(key);
        }

        public void setForward(int i, Element newForward) {
            forward[i] = newForward;
        }

        public boolean hasForward(int i) {
            return forward[i] != null;
        }
    }

    class Header extends Element {

        public Header(K key, V value) {
            super(key, value);
        }

        @Override
        public boolean hasLowerKey(K other) {
            throw new IllegalStateException();
        }

        @Override
        public V getValue() {
            throw new IllegalStateException();
        }

        @Override
        public void setValue(V value) {
            throw new IllegalStateException();
        }

        @Override
        public boolean hasKey(K key) {
            throw new IllegalStateException();
        }
    }
}
