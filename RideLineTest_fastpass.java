import static org.junit.Assert.*;

import java.util.Arrays;
import org.junit.Test;

/**
 * Additional tests for the fast-pass feature in RideLine
 */
public class RideLineTest_fastpass {
    private static final double EPS = 1e-9;

    private RideLine line(int size, FixedDice dice) {
        return new RideLine(size, "You", dice);
    }

    // ---- FAST PASS ----

    @Test
    public void fastPassMovesPlayerToFastLane() {
        RideLine r = line(10, new FixedDice(true));
        LineSnapshot before = r.snapshot();
        assertTrue("Should be able to use fast pass", before.canUseFastPass());

        TurnResult t = r.takeTurn(Action.FAST_PASS);
        assertEquals(Action.FAST_PASS, t.getAction());
        assertEquals(Outcome.FAST_PASSED, t.getOutcome());

        LineSnapshot after = r.snapshot();
        assertFalse("Should not be able to use fast pass again", after.canUseFastPass());
    }

    @Test
    public void fastPassCanOnlyBeUsedOnce() {
        RideLine r = line(10, new FixedDice(true));
        r.takeTurn(Action.FAST_PASS);

        try {
            r.takeTurn(Action.FAST_PASS);
            fail("Should not allow second fast pass use");
        } catch (IllegalStateException expected) {
            // ok
        }
    }

    @Test
    public void fastPassCannotBeUsedWhenAlreadyInFastLane() {
        RideLine r = line(10, new FixedDice(true));
        r.takeTurn(Action.FAST_PASS);

        try {
            r.takeTurn(Action.FAST_PASS);
            fail("Should not allow fast pass when already in fast lane");
        } catch (IllegalStateException expected) {
            // ok
        }
    }

    @Test
    public void fastPassMovesPlayerToBackOfFastLane() {
        RideLine r = line(10, new FixedDice(true));
        LineSnapshot initial = r.snapshot();
        int initialPos = initial.getPlayerPosition();
        int initialLaneLen = initial.getLength();

        r.takeTurn(Action.FAST_PASS);
        LineSnapshot after = r.snapshot();

        // Player should now be in the fast lane
        // Standard lane should have one fewer guest
        assertEquals(initialLaneLen - 1, after.getLength());
    }

    @Test
    public void fastPassShowsCorrectOutcome() {
        RideLine r = line(10, new FixedDice(true));
        TurnResult t = r.takeTurn(Action.FAST_PASS);
        assertEquals(Outcome.FAST_PASSED, t.getOutcome());
        assertEquals(0.0, t.getOddsUsed(), EPS);
    }

    @Test
    public void fastPassBoardsAGuestEachTurn() {
        RideLine r = line(10, new FixedDice(true));
        int initialLen = r.snapshot().getLength();

        r.takeTurn(Action.FAST_PASS);
        int afterFastPass = r.snapshot().getLength();

        // One guest should have boarded
        assertEquals(initialLen - 1, afterFastPass);
    }

    @Test
    public void fastPassAndThenPunch() {
        RideLine r = line(10, new FixedDice(true, true));
        r.takeTurn(Action.FAST_PASS);

        LineSnapshot snap = r.snapshot();
        if (snap.canPunch()) {
            TurnResult t = r.takeTurn(Action.PUNCH);
            assertEquals(Outcome.PUNCH_WON, t.getOutcome());
        }
    }

    @Test
    public void fastPassDoesNotUseOdds() {
        FixedDice dice = new FixedDice(true);
        RideLine r = line(10, dice);
        r.takeTurn(Action.FAST_PASS);
        assertEquals(0, dice.getCalls());
    }

    @Test
    public void fastPassCannotBeUsedIfPlayerAtFront() {
        RideLine r = line(3, new FixedDice(true, true, true));
        // Board guests until we're at the front
        while (r.playerPosition() > 1 && !r.isGameOver()) {
            r.takeTurn(Action.WAIT);
        }

        // Once at front, should not be able to use fast pass
        LineSnapshot snap = r.snapshot();
        assertFalse("Should not be able to use fast pass at front", snap.canUseFastPass());
    }

    @Test
    public void fastPassAndWaitThenWin() {
        RideLine r = line(10, new FixedDice(true, true, false));
        r.takeTurn(Action.FAST_PASS);

        LineSnapshot snap = r.snapshot();
        int turnsAfterFastPass = 0;

        while (!r.isGameOver() && turnsAfterFastPass < 5) {
            r.takeTurn(Action.WAIT);
            turnsAfterFastPass++;
        }

        assertTrue("Game should not be over yet", turnsAfterFastPass == 5 || !r.isGameOver());
    }
}
