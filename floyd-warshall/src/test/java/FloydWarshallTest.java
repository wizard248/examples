import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class FloydWarshallTest {
    @Test
    public void testExample1() {
        String[] nodes = {"1", "2", "3", "4"};

        final Output<String> output = calculate(
                nodes,
                new Object[][]{
                        new Object[]{"1", "2", 1},
                        new Object[]{"2", "3", 2},
                        new Object[]{"3", "4", 3},
                        new Object[]{"4", "1", 4},
                        new Object[]{"4", "2", 7}
                }
        );

        assertNoPath(output, "1", "XXX");
        assertNoPath(output, "1", "XXX");
        assertNoPath(output, "XXX", "1");
        assertNoPath(output, "XXX", "XXX");
        assertNoPath(output, "XXX", "YYY");

        assertShortestPath(output, "1", "1", 0, Arrays.asList("1"));
        assertShortestPath(output, "1", "2", 1, Arrays.asList("1", "2"));
        assertShortestPath(output, "1", "3", 3, Arrays.asList("1", "2", "3"));
        assertShortestPath(output, "1", "4", 6, Arrays.asList("1", "2", "3", "4"));
        assertShortestPath(output, "2", "1", 9, Arrays.asList("2", "3", "4", "1"));
        assertShortestPath(output, "2", "2", 0, Arrays.asList("2"));
        assertShortestPath(output, "2", "3", 2, Arrays.asList("2", "3"));
        assertShortestPath(output, "2", "4", 5, Arrays.asList("2", "3", "4"));
        assertShortestPath(output, "3", "1", 7, Arrays.asList("3", "4", "1"));
        assertShortestPath(output, "3", "2", 8, Arrays.asList("3", "4", "2"));
        assertShortestPath(output, "3", "3", 0, Arrays.asList("3"));
        assertShortestPath(output, "3", "4", 3, Arrays.asList("3", "4"));
        assertShortestPath(output, "4", "1", 4, Arrays.asList("4", "1"));
        assertShortestPath(output, "4", "2", 5, Arrays.asList("4", "1", "2"));
        assertShortestPath(output, "4", "3", 7, Arrays.asList("4", "1", "2", "3"));
        assertShortestPath(output, "4", "4", 0, Arrays.asList("4"));
    }

    private static void assertShortestPath(Output<String> output, String a, String b, int expDistance, List<String> expPath) {
        assertEquals(Optional.of(expDistance), output.getMinimalDistance(a, b));
        assertEquals(Optional.of(expPath), output.getShortestPath(a, b));
    }

    private static void assertNoPath(Output<String> output, String a, String b) {
        assertFalse(output.getMinimalDistance(a, b).isPresent());
        assertFalse(output.getShortestPath(a, b).isPresent());
    }

    private static Output<String> calculate(String[] nodes, Object[][] data) {
        return FloydWarshall.calculate(construct(nodes, data));
    }

    private static Input<String> construct(String[] nodes, Object[][] data) {
        return new Input<String>() {
            @Override
            public List<String> getNodes() {
                return Arrays.asList(nodes);
            }

            @Override
            public Optional<Integer> getDistance(String a, String b) {
                for (Object[] datum : data) {
                    if (datum[0].equals(a) && datum[1].equals(b)) {
                        return Optional.of((int) datum[2]);
                    }
                }

                return Optional.empty();
            }
        };
    }
}