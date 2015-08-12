import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public final class FloydWarshall {
    private static final Logger log = LoggerFactory.getLogger(FloydWarshall.class);

    private FloydWarshall() {
        // utility class
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

        return new Output<NODE>() {
            @Override
            public Optional<Integer> getMinimalDistance(final NODE a, final NODE b) {
                final int iA = nodes.indexOf(a);
                final int iB = nodes.indexOf(b);

                if (iA == -1 || iB == -1) {
                    // unknown node
                    return Optional.empty();
                }

                return matrix.get(iA, iB);
            }

            @Override
            public Optional<List<NODE>> getShortestPath(final NODE a, final NODE b) {
                final int iTarget = nodes.indexOf(a);
                int iStart = nodes.indexOf(b);

                if (iStart == -1 || iTarget == -1) {
                    // unknown node
                    return Optional.empty();
                }

                final List<NODE> result = new LinkedList<>();

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
                        // this should never happen as matrix is under our control
                        return Optional.empty();
                    }
                }
            }
        };
    }

}
