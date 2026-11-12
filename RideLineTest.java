import static org.junit.Assert.*;

import java.util.Arrays;
import org.junit.Test;

public class RideLineTest {
    private static final double EPS = 1e-9;

    private RideLine line(int size, FixedDice dice) {
        return new RideLine(size, "You", dice);
    }

    // ---- construction ----

    @Test
    public void startsWithPlayerAtBackAndFullOdds() {
        RideLine r = line(5, new FixedDice(true));
        LineSnapshot s = r.snapshot();
        assertEquals(Arrays.asList("Guest 1", "Guest 2", "Guest 3", "Guest 4", "You"), s.getNames());
        assertEquals(5, r.playerPosition());
        assertEquals(0.70, r.punchOdds(), EPS);
        assertEquals(0, s.getTurn());
        assertFalse(r.isGameOver());
    }

    @Test
    public void sizesThreeAndOneHundredAreAccepted() {
        assertEquals(3, line(3, new FixedDice(true)).snapshot().getLength());
        assertEquals(100, line(100, new FixedDice(true)).snapshot().getLength());
    }

    @Test
    public void badSizesThrowIllegalArgument() {
        for (int bad : new int[] {2, 101, 0, -5}) {
            try {
                line(bad, new FixedDice(true));
                fail("size " + bad + " should be rejected");
            } catch (IllegalArgumentException expected) {
                // ok
            }
        }
    }

    @Test
    public void badConstructorArgumentsThrow() {
        assertThrows(NullPointerException.class, () -> new RideLine(5, "You", null));
        assertThrows(NullPointerException.class, () -> new RideLine(5, null, new FixedDice(true)));
        assertThrows(IllegalArgumentException.class, () -> new RideLine(5, " ", new FixedDice(true)));
        assertThrows(IllegalArgumentException.class,
                () -> new RideLine(5, "You", new FixedDice(true), 0.2, 0.5));
        assertThrows(IllegalArgumentException.class,
                () -> new RideLine(5, "You", new FixedDice(true), 1.2, 0.1));
        assertThrows(IllegalArgumentException.class,
                () -> new RideLine(5, "You", new FixedDice(true), Double.NaN, 0.1));
    }

    // ---- odds ----

    @Test
    public void oddsFallLinearlyAsPlayerMovesUp() {
        RideLine r = line(5, new FixedDice(true));
        assertEquals(0.70, r.punchOdds(), EPS);
        r.takeTurn(Action.WAIT); // position 4 of 4
        assertEquals(0.70, r.punchOdds(), EPS);
        r.takeTurn(Action.WAIT); // position 3 of 3
        assertEquals(0.70, r.punchOdds(), EPS);
        r.takeTurn(Action.WAIT); // position 2 of 2 -> front odds
        assertEquals(0.10, r.punchOdds(), EPS);
    }

    @Test
    public void oddsInTheMiddleOfALongLine() {
        RideLine r = line(10, new FixedDice(true));
        // p(k,L) = f + (b-f)(k-2)/(L-2); k=10, L=10 -> 0.70
        assertEquals(0.70, r.punchOdds(), EPS);
        r.takeTurn(Action.PUNCH); // win: swap to 9 of 10, front boards -> 8 of 9
        // k=8, L=9 -> 0.10 + 0.60 * 6/7
        assertEquals(0.10 + 0.60 * 6.0 / 7.0, r.punchOdds(), EPS);
    }

    @Test
    public void customOddsAreUsed() {
        RideLine r = new RideLine(5, "You", new FixedDice(true), 1.0, 0.0);
        r.takeTurn(Action.PUNCH); // win, front guest boards: [G2, G3, You, G4]
        // k=3, L=4 -> 0 + 1*(1)/(2) = 0.5
        assertEquals(0.50, r.punchOdds(), EPS);
    }

    // ---- WAIT ----

    @Test
    public void waitBoardsTheFrontGuest() {
        RideLine r = line(5, new FixedDice(true));
        TurnResult t = r.takeTurn(Action.WAIT);
        assertEquals(Action.WAIT, t.getAction());
        assertEquals(Outcome.WAITED, t.getOutcome());
        assertEquals("Guest 1", t.getBoardedName());
        assertFalse(t.playerBoarded());
        LineSnapshot s = r.snapshot();
        assertEquals(Arrays.asList("Guest 2", "Guest 3", "Guest 4", "You"), s.getNames());
        assertEquals(4, r.playerPosition());
        assertEquals(1, s.getTurn());
    }

    @Test
    public void waitDoesNotAskTheDice() {
        FixedDice dice = new FixedDice(true);
        RideLine r = line(5, dice);
        r.takeTurn(Action.WAIT);
        assertEquals(0, dice.getCalls());
    }

    // ---- PUNCH ----

    @Test
    public void winningPunchSwapsOnePlaceThenFrontBoards() {
        FixedDice dice = new FixedDice(true);
        RideLine r = line(5, dice);
        TurnResult t = r.takeTurn(Action.PUNCH);
        assertEquals(Outcome.PUNCH_WON, t.getOutcome());
        assertEquals(0.70, t.getOddsUsed(), EPS);
        assertEquals("Guest 1", t.getBoardedName());
        LineSnapshot s = r.snapshot();
        assertEquals(Arrays.asList("Guest 2", "Guest 3", "You", "Guest 4"), s.getNames());
        assertEquals(3, r.playerPosition());
        assertEquals(1, s.getPunchesWon());
        assertEquals(0, s.getPunchesLost());
        assertEquals(1, dice.getCalls());
        assertEquals(0.70, dice.getProbabilities().get(0), EPS);
    }

    @Test
    public void losingPunchSendsPlayerToBack() {
        RideLine r = line(5, new FixedDice(true, false));
        r.takeTurn(Action.PUNCH); // win -> [G2, G3, You, G4], position 3
        assertEquals(0.40, r.punchOdds(), EPS);
        TurnResult t = r.takeTurn(Action.PUNCH); // lose at 0.40
        assertEquals(Outcome.PUNCH_LOST, t.getOutcome());
        assertEquals(0.40, t.getOddsUsed(), EPS);
        assertEquals("Guest 2", t.getBoardedName());
        LineSnapshot s = r.snapshot();
        assertEquals(Arrays.asList("Guest 3", "Guest 4", "You"), s.getNames());
        assertEquals(3, r.playerPosition());
        assertEquals(0.70, r.punchOdds(), EPS);
        assertEquals(1, s.getPunchesWon());
        assertEquals(1, s.getPunchesLost());
    }

    @Test
    public void spammingPunchesThatLoseUsesBackOddsEachTime() {
        FixedDice dice = new FixedDice(false);
        RideLine r = line(10, dice);
        // L: 10 -> 9 -> 8; each loss leaves player at back of a shrinking line
        for (int i = 0; i < 3; i++) {
            r.takeTurn(Action.PUNCH);
        }
        assertEquals(3, dice.getCalls());
        for (double p : dice.getProbabilities()) {
            assertEquals(0.70, p, EPS);
        }
        assertEquals(3, r.snapshot().getPunchesLost());
        assertEquals(7, r.snapshot().getLength());
    }

    @Test
    public void punchAtFrontThrowsAndChangesNothing() {
        RideLine r = line(3, new FixedDice(true));
        r.takeTurn(Action.WAIT); // [G2, You], position 2 -- not front yet
        r.takeTurn(Action.WAIT); // G2 boards -> [You], position 1
        assertEquals(1, r.playerPosition());
        int turnBefore = r.snapshot().getTurn();
        assertThrows(IllegalStateException.class, () -> r.takeTurn(Action.PUNCH));
        assertEquals(turnBefore, r.snapshot().getTurn());
        assertFalse(r.isGameOver());
    }

    @Test
    public void nullActionThrows() {
        RideLine r = line(5, new FixedDice(true));
        assertThrows(NullPointerException.class, () -> r.takeTurn(null));
    }

    // ---- ending ----

    @Test
    public void shortestGameEndsWhenPlayerBoardsByWaiting() {
        RideLine r = line(3, new FixedDice(true));
        r.takeTurn(Action.WAIT); // G1 boards, [G2, You]
        assertEquals(0.10, r.punchOdds(), EPS);
        r.takeTurn(Action.WAIT); // G2 boards, [You]
        TurnResult t = r.takeTurn(Action.WAIT); // You board
        assertTrue(t.playerBoarded());
        assertTrue(r.isGameOver());
        assertEquals(0, r.playerPosition());
        assertEquals(3, r.snapshot().getTurn());
    }

    @Test
    public void winningPunchFromSecondPlaceMakesPlayerBoardImmediately() {
        RideLine r = line(3, new FixedDice(true));
        r.takeTurn(Action.WAIT); // [G2, You] -> position 2 of 2, odds 0.10
        TurnResult t = r.takeTurn(Action.PUNCH);
        assertEquals(Outcome.PUNCH_WON, t.getOutcome());
        assertEquals(0.10, t.getOddsUsed(), EPS);
        assertEquals("You", t.getBoardedName());
        assertTrue(t.playerBoarded());
        assertTrue(r.isGameOver());
        assertEquals(0, r.playerPosition());
        assertEquals(2, r.snapshot().getTurn());
    }

    @Test
    public void takingATurnAfterGameOverThrows() {
        RideLine r = line(3, new FixedDice(true));
        for (int i = 0; i < 3; i++) {
            r.takeTurn(Action.WAIT);
        }
        assertTrue(r.isGameOver());
        assertThrows(IllegalStateException.class, () -> r.takeTurn(Action.WAIT));
    }

    // ---- snapshot ----

    @Test
    public void snapshotIsAFrozenCopy() {
        RideLine r = line(5, new FixedDice(true));
        LineSnapshot before = r.snapshot();
        r.takeTurn(Action.WAIT);
        assertEquals(5, before.getLength());
        assertEquals(0, before.getTurn());
        assertEquals(4, r.snapshot().getLength());
    }

    @Test
    public void everyGameEndsWithinLineSizeTurnsWhenOnlyWaiting() {
        RideLine r = line(100, new FixedDice(false));
        int turns = 0;
        while (!r.isGameOver()) {
            r.takeTurn(Action.WAIT);
            turns++;
        }
        assertEquals(100, turns);
    }
}
