import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class ValueTypesTest {

    @Test
    public void turnResultStoresFields() {
        TurnResult t = new TurnResult(Action.PUNCH, Outcome.PUNCH_WON, 0.4, "Guest 2", false);
        assertEquals(Action.PUNCH, t.getAction());
        assertEquals(Outcome.PUNCH_WON, t.getOutcome());
        assertEquals(0.4, t.getOddsUsed(), 1e-9);
        assertEquals("Guest 2", t.getBoardedName());
        assertFalse(t.playerBoarded());
    }

    @Test
    public void turnResultRejectsNulls() {
        assertThrows(NullPointerException.class,
                () -> new TurnResult(null, Outcome.WAITED, 0, "G", false));
        assertThrows(NullPointerException.class,
                () -> new TurnResult(Action.WAIT, null, 0, "G", false));
        assertThrows(NullPointerException.class,
                () -> new TurnResult(Action.WAIT, Outcome.WAITED, 0, null, false));
    }

    @Test
    public void lineSnapshotStoresFields() {
        LineSnapshot s = new LineSnapshot(Arrays.asList("A", "You"), 2, 2, 0.1, true, 4, 1, 2);
        assertEquals(Arrays.asList("A", "You"), s.getNames());
        assertEquals(2, s.getPlayerPosition());
        assertEquals(2, s.getLength());
        assertEquals(0.1, s.getPunchOdds(), 1e-9);
        assertTrue(s.canPunch());
        assertEquals(4, s.getTurn());
        assertEquals(1, s.getPunchesWon());
        assertEquals(2, s.getPunchesLost());
    }

    @Test
    public void lineSnapshotCopiesAndProtectsItsNames() {
        List<String> source = new ArrayList<>(Arrays.asList("A", "You"));
        LineSnapshot s = new LineSnapshot(source, 2, 2, 0.1, true, 0, 0, 0);
        source.add("Intruder");
        assertEquals(2, s.getNames().size());
        assertThrows(UnsupportedOperationException.class, () -> s.getNames().add("X"));
    }

    @Test
    public void lineSnapshotRejectsNullNames() {
        assertThrows(NullPointerException.class,
                () -> new LineSnapshot(null, 1, 1, 0, false, 0, 0, 0));
    }
}
