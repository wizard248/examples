public class Graph<NODE> extends DiGraph<NODE> {
    @Override
    public void addEdge(NODE source, NODE target) {
        super.addEdge(source, target);
        super.addEdge(target, source);
    }
}
