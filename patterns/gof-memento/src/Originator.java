public class Originator<S> {
    private S currentState;

    public Memento<S> saveToMemento() {
        return new Memento<>(this.currentState);
    }

    public void restoreFromMemento(final Memento<S> memento) {
        this.currentState = memento.getSavedState();
    }

    // JUST FOR TESTING - class state should not be ever public (due encapsulation)

    S getCurrentStateTestOnly() {
        return currentState;
    }

    void setCurrentStateTestOnly(final S currentState) {
        this.currentState = currentState;
    }
}
