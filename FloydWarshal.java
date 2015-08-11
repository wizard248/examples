import java.util.*;

public class FloydWarshall {
    public static void main(final String[] args) {
        final Output<Integer> o = calculate(new Input<Integer>() {
            @Override
            public List<Integer> getNodes() {
                return Arrays.asList(1, 2, 3, 4);
            }

            @Override
            public Optional<Integer> getDistance(final Integer a, final Integer b) {
                if (a == 1 && b == 2) {
                    return Optional.of(1);
                }
                if (a == 2 && b == 3) {
                    return Optional.of(2);
                }
                if (a == 3 && b == 4) {
                    return Optional.of(3);
                }
                if (a == 4 && b == 1) {
                    return Optional.of(4);
                }
                if (a == 4 && b == 2) {
                    return Optional.of(7);
                }
                return Optional.empty();
            }
        });

        System.out.println("1 to 4 = " + o.getShortestPath(1, 4).toString());
        System.out.println("1 to 2 = " + o.getShortestPath(1, 2).toString());
        System.out.println("2 to 1 = " + o.getShortestPath(2, 1).toString());
        System.out.println("1 to 1 = " + o.getShortestPath(1, 1).toString());
    }

    public static <NODE> Output<NODE> calculate(final Input<NODE> input) {
        final List<NODE> nodes = input.getNodes();
        final Matrix<Integer, NODE> matrix = new Matrix<>(nodes.size());

        for (int iX = 0; iX < matrix.size(); iX++) {
            for (int iY = 0; iY < matrix.size(); iY++) {
                final NODE xNode = nodes.get(iX);
                final NODE yNode = nodes.get(iY);

                if (iX == iY) {
                    // the same node - distance is zero
                    matrix.setMinimumDistance(iX, iY, 0);
                    matrix.setPredecessor(iX, iY, xNode);
                } else {
                    final Optional<Integer> distance = input.getDistance(xNode, yNode);

                    if (distance.isPresent()) {
                        // edge is defined - define distance
                        matrix.setMinimumDistance(iX, iY, distance.get());
                        matrix.setPredecessor(iX, iY, xNode);
                    }
                }
            }
        }

        //matrix.print();

        for (int iDetour = 0; iDetour < matrix.size(); iDetour++) {
            for (int iX = 0; iX < matrix.size(); iX++) {
                for (int iY = 0; iY < matrix.size(); iY++) {
                    final Optional<Integer> detourPart1 = matrix.get(iX, iDetour);
                    final Optional<Integer> detourPart2 = matrix.get(iDetour, iY);

                    if (detourPart1.isPresent() && detourPart2.isPresent()) {
                        final int detourDistance = detourPart1.get() + detourPart2.get();
                        final Optional<Integer> currentDistance = matrix.get(iX, iY);

                        if (!currentDistance.isPresent() || detourDistance < currentDistance.get()) {
                            // the detour is better than what we have so far
                            final NODE detourNode = nodes.get(iDetour);
                            matrix.setMinimumDistance(iX, iY, detourDistance);
                            matrix.setPredecessor(iX, iY, detourNode);
                        }
                    }
                }
            }
        }

        matrix.print();

        return new Output<NODE>() {
            @Override
            public Optional<Integer> getMinimalDistance(final NODE a, final NODE b) {
                final int iA = nodes.indexOf(a);
                final int iB = nodes.indexOf(b);
                return matrix.get(iA, iB);
            }

            @Override
            public Optional<List<NODE>> getShortestPath(final NODE a, final NODE b) {
                final List<NODE> result = new LinkedList<>();
                final int iTarget = nodes.indexOf(a);
                int iStart = nodes.indexOf(b);

                while (true) {
                    result.add(nodes.get(iStart));

                    if (iStart == iTarget) {
                        // we reached the target node
                        // (we must reverse the path, because we started at the end)
                        Collections.reverse(result);
                        return Optional.of(result);
                    }

                    final Optional<NODE> parent = matrix.getParent(iTarget, iStart);

                    if (parent.isPresent()) {
                        // advance to next node
                        iStart = nodes.indexOf(parent.get());
                    } else {
                        return Optional.empty();
                    }
                }
            }
        };
    }

    public interface Input<NODE> {
        List<NODE> getNodes();

        Optional<Integer> getDistance(NODE a, NODE b);
    }

    public interface Output<NODE> {
        Optional<Integer> getMinimalDistance(NODE a, NODE b);

        Optional<List<NODE>> getShortestPath(NODE a, NODE b);
    }

    private static class Matrix<VALUE, PARENT> {
        private final List<VALUE> matrix;
        private final List<PARENT> parent;
        private final int n;

        public Matrix(final int n) {
            this.matrix = new ArrayList<>(n * n);
            this.parent = new ArrayList<>(n * n);

            for (int i = 0; i < n * n; i++) {
                this.matrix.add(null);
                this.parent.add(null);
            }

            this.n = n;
        }

        public int size() {
            return n;
        }

        public Optional<VALUE> get(final int x, final int y) {
            return Optional.ofNullable(matrix.get(map(x, y)));
        }

        public Optional<PARENT> getParent(final int x, final int y) {
            return Optional.ofNullable(parent.get(map(x, y)));
        }

        public void setMinimumDistance(final int x, final int y, final VALUE value) {
            matrix.set(map(x, y), value);

        }

        public void setPredecessor(final int x, final int y, final PARENT value) {
            parent.set(map(x, y), value);
        }

        private int map(final int x, final int y) {
            return x * n + y;
        }

        public void print() {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    System.out.print(getParent(i, j) + " ");
                }
                System.out.println();
            }
        }
    }
}
