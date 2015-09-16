package ds.skiplist;

import org.junit.Test;

import java.security.SecureRandom;
import java.util.Random;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SkipListTest {
    private static final Random RANDOM = new SecureRandom();

    @Test
    public void testRev() {
        SkipList<Integer, Integer> list = new DefaultSkipList<>(3);
        for (int i = 10; i >= 1; i--) {
            System.out.println("inserting " + i);
            list.insert(i, i);
            System.out.println(list);
        }
    }

    @Test
    public void test() {
        SkipList<Integer, Integer> list = new DefaultSkipList<>(3);
        System.out.println(list);
        for (int i = 0; i < 10; i++) {
            System.out.println("inserting " + i);
            list.insert(i, i);
        }
        System.out.println(list);
    }

    @Test
    public void hardTest() {
        SkipList<Integer, Integer> list = new DefaultSkipList<>(10);

        for (int i = 1; i <= 1000; i++) {
            // insert

            int random = RANDOM.nextInt(50);
            list.insert(random, random);
            assertTrue(list.get(random).isPresent());
            assertTrue(list.get(random).get().equals(random));

            // delete

            int random2 = RANDOM.nextInt(100);
            list.delete(random2);
            assertFalse(list.get(random2).isPresent());

            // print

            System.out.println("================ " + i);
            System.out.println(list);
        }

    }
}