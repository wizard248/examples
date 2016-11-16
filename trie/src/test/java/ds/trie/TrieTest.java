package ds.trie;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class TrieTest {
    private Trie<String> toTest;

    @Before
    public void setUp() {
        toTest = new GenericTrie<>();
    }

    @Test
    public void testEmptyTrie() {
        assertNoValue("");
        assertNoValue("XXX");
    }

    @Test
    public void testSingleNodeTrie() {
        putToTrie("a");

        assertNoValue("");
        assertNoValue("XXX");
        assertCorrectValue("a");
    }

    @Test
    public void testSingleLevelTrie() {
        List<String> values = Arrays.asList("a", "b", "c", "d", "e");

        putToTrie(values);

        assertNoValue("");
        assertNoValue("XXX");
        assertCorrectValue(values);
    }

    @Test
    public void testSnailTrie() {
        List<String> values = Arrays.asList("a", "ab", "abc", "abcd", "abcde");

        putToTrie(values);

        assertNoValue("");
        assertNoValue("XXX");
        assertCorrectValue(values);
    }

    @Test
    public void testNormalTrie() {
        List<String> values = Arrays.asList(
                "hello", "world", "how", "are", "you", "i", "am", "fine", "thank", "you", "what", "the", "hell"
        );

        putToTrie(values);

        assertNoValue("");
        assertNoValue("XXX");
        assertCorrectValue(values);
    }

    private void putToTrie(String key) {
        toTest.put(key.toCharArray(), deriveValueForKey(key));
    }

    private void putToTrie(Iterable<String> keys) {
        keys.forEach(this::putToTrie);
    }

    private void assertNoValue(String key) {
        assertNull(toTest.get(key.toCharArray()));
    }

    private void assertCorrectValue(Iterable<String> keys) {
        keys.forEach(this::assertCorrectValue);
    }

    private void assertCorrectValue(String key) {
        assertNotNull(toTest.get(key.toCharArray()));
        assertEquals(deriveValueForKey(key), toTest.get(key.toCharArray()));
    }

    private String deriveValueForKey(String key) {
        return String.format("value_for_%s", key);
    }
}