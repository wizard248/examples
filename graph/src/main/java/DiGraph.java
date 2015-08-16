import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by voho on 16.08.15.
 */
public class DiGraph<NV, EV> {
    private final Set<NV> nodes;
    private final Table<NV, NV, EV> matrix;

    public DiGraph() {
        nodes = new LinkedHashSet<>();
        matrix = HashBasedTable.create(32, 32);
    }

    public void addNode(NV value) {
        Preconditions.checkArgument(!nodes.contains(value));
        Preconditions.checkState(!matrix.containsColumn(value));
        Preconditions.checkState(!matrix.containsRow(value));
        nodes.add(value);
    }

    public void addEdge(NV source, NV target, EV value) {
        Preconditions.checkArgument(nodes.contains(source));
        Preconditions.checkArgument(nodes.contains(target));
        Preconditions.checkArgument(!matrix.contains(source, target));
        matrix.put(source, target, value);
    }

    public Set<NV> getNeighbours(NV source) {
        Preconditions.checkArgument(nodes.contains(source));
        return Collections.unmodifiableSet(matrix.row(source).keySet());
    }

    public Set<NV> getAllNodes() {
        return Collections.unmodifiableSet(nodes);
    }

    public Set<DiGraphEdge<NV, EV>> getAllEdges() {
        return matrix
                .cellSet()
                .stream()
                .map(cell -> new DiGraphEdge<>(
                        cell.getRowKey(),
                        cell.getColumnKey(),
                        cell.getValue()
                ))
                .collect(Collectors.toSet());
    }

    public Optional<EV> getEdge(NV source, NV target) {
        return Optional.ofNullable(matrix.get(source, target));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(512);
        sb.append("graph {\n");
        for (NV node : nodes) {
            sb.append("\t");
            sb.append(node.toString());
            Map<NV, EV> row = matrix.row(node);
            sb.append("\n");
            if (row != null && !row.isEmpty()) {
                for (Map.Entry<NV, EV> entry : row.entrySet()) {
                    sb.append("\t --> ");
                    sb.append(entry.getKey());
                    sb.append("\t= ");
                    sb.append(entry.getValue().toString());
                    sb.append("\n");
                }
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
