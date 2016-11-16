package ds.trie;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class GenericTrie<VALUE> implements Trie<VALUE> {
    private final LookupCallback<VALUE, Iterable<VALUE>> GET_PREFIX_CALLBACK = new LookupCallback<VALUE, Iterable<VALUE>>() {
        @Override
        public Iterable<VALUE> found(final Node<VALUE> parentOfActiveRoot, final Node<VALUE> activeRoot) {
            final List<VALUE> result = new LinkedList<>();
            final Stack<Node<VALUE>> stack = new Stack<>();
            stack.push(activeRoot);
            while (!stack.isEmpty()) {
                final Node<VALUE> temp = stack.pop();
                if (temp.value != null) {
                    result.add(temp.value);
                }
                temp.children.nodes().forEach(stack::push);
            }
            return result;
        }
    };

    private final LookupCallback<VALUE, VALUE> GET_CALLBACK = new LookupCallback<VALUE, VALUE>() {
        @Override
        public VALUE found(final Node<VALUE> parentOfActiveRoot, final Node<VALUE> activeRoot) {
            return activeRoot.value;
        }
    };

    private final LookupCallback<VALUE, VALUE> REMOVE_CALLBACK = new LookupCallback<VALUE, VALUE>() {
        @Override
        public VALUE found(final Node<VALUE> parentOfActiveRoot, final Node<VALUE> activeRoot) {
            final VALUE oldValue = activeRoot.value;
            parentOfActiveRoot.children.remove(activeRoot.keyFragment);
            return oldValue;
        }
    };

    private final Node<VALUE> root;

    public GenericTrie() {
        root = new Node<>();
        root.children = createChildrenMap();
    }

    @Override
    public VALUE put(final char[] key, final VALUE newValue) {
        Node<VALUE> activeRoot = root;

        for (final char activeKeyFragment : key) {
            // try to lookup the current key contribution among children
            Node<VALUE> child = activeRoot.children.get(activeKeyFragment);

            if (child == null) {
                child = new Node<>();
                child.keyFragment = activeKeyFragment;
                child.children = createChildrenMap();
                activeRoot.children.put(child.keyFragment, child);
            }

            activeRoot = child;
        }

        // we have found the complete key
        final VALUE previousValue = activeRoot.value;
        activeRoot.value = newValue;
        return previousValue;
    }

    @Override
    public VALUE get(final char[] key) {
        return lookup(key, GET_CALLBACK);
    }

    @Override
    public Iterable<VALUE> getPrefix(final char[] key) {
        return lookup(key, GET_PREFIX_CALLBACK);
    }

    @Override
    public VALUE remove(final char[] key) {
        return lookup(key, REMOVE_CALLBACK);
    }

    private <OUTCOME> OUTCOME lookup(final char[] key, final LookupCallback<VALUE, OUTCOME> callback) {
        Node<VALUE> parentOfActiveRoot = null;
        Node<VALUE> activeRoot = root;

        for (final char activeKeyFragment : key) {
            // try to lookup the current key contribution among children
            final Node<VALUE> child = activeRoot.children.get(activeKeyFragment);

            if (child == null) {
                return callback.notFound(activeRoot);
            }

            parentOfActiveRoot = activeRoot;
            activeRoot = child;
        }

        // we have found the complete key
        return callback.found(parentOfActiveRoot, activeRoot);
    }

    private CharacterToNodeMap<VALUE> createChildrenMap() {
        return new CharacterToNodeHashMap<>('z' - 'a');
    }

    private static class Node<VALUE> {
        private char keyFragment;
        private VALUE value;
        private CharacterToNodeMap<VALUE> children;
    }

    private interface CharacterToNodeMap<VALUE> {
        Node<VALUE> get(char keyContribution);

        void put(char keyContribution, Node<VALUE> newNode);

        void remove(char keyContribution);

        Collection<Node<VALUE>> nodes();
    }

    private interface LookupCallback<VALUE, OUTCOME> {
        OUTCOME found(Node<VALUE> parentOfActiveRoot, Node<VALUE> activeRoot);

        default OUTCOME notFound(final Node<VALUE> nonTerminalNode) {
            return null;
        }
    }

    private static class CharacterToNodeHashMap<VALUE> implements CharacterToNodeMap<VALUE> {
        private final Map<Character, Node<VALUE>> nodes;

        private CharacterToNodeHashMap(final int expectedNumberOfChildren) {
            this.nodes = new HashMap<>(expectedNumberOfChildren);
        }

        @Override
        public Node<VALUE> get(final char keyContribution) {
            return nodes.get(keyContribution);
        }

        @Override
        public void put(final char keyContribution, final Node<VALUE> newNode) {
            nodes.put(keyContribution, newNode);
        }

        @Override
        public void remove(final char keyContribution) {
            nodes.remove(keyContribution);
        }

        @Override
        public Collection<Node<VALUE>> nodes() {
            return nodes.values();
        }
    }
}
