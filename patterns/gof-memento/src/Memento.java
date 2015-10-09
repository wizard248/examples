public class Memento<S> {
    private final S savedState;

    public Memento(final S savedState) {
        this.savedState = savedState;
    }

    public S getSavedState() {
        return savedState;
    }
}
