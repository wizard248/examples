package ds.trie;

public interface Trie<VALUE> {
    VALUE get(char[] key);

    Iterable<VALUE> getPrefix(char[] key);

    VALUE put(char[] key, VALUE newValue);

    VALUE remove(char[] key);
}
