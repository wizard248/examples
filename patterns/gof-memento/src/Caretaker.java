import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Caretaker {
    private static final String ORIGINAL_STATE = "original state";
    private static final String UPDATED_STATE = "updated state";

    @Test
    public void test() {
        final Originator<String> originator = new Originator<>();

        // initialize state
        originator.setCurrentStateTestOnly(ORIGINAL_STATE);
        assertEquals(ORIGINAL_STATE, originator.getCurrentState());

        // save state using memento
        final Memento<String> memento = originator.saveToMemento();

        // change state
        originator.setCurrentStateTestOnly(UPDATED_STATE);
        assertEquals(UPDATED_STATE, originator.getCurrentState());

        // restore state using memento
        originator.restoreFromMemento(memento);
        assertEquals(ORIGINAL_STATE, originator.getCurrentState());
    }
}
