import static org.junit.Assert.*;

import java.util.Arrays;
import org.junit.Test;

public class GameControllerTest {

    // ---- parseAction ----

    @Test
    public void parseActionAcceptsWaitAndPunchInAnyCase() {
        assertEquals(Action.WAIT, GameController.parseAction("wait"));
        assertEquals(Action.PUNCH, GameController.parseAction("punch"));
        assertEquals(Action.WAIT, GameController.parseAction("WAIT"));
        assertEquals(Action.PUNCH, GameController.parseAction("  PuNcH \t"));
    }

    @Test
    public void parseActionRejectsEverythingElse() {
        for (String bad : new String[] {null, "", "   ", "punhc", "w", "wait wait", "1", "quit"}) {
            try {
                GameController.parseAction(bad);
                fail("should reject: " + bad);
            } catch (IllegalArgumentException expected) {
                assertEquals("Type wait or punch.", expected.getMessage());
            }
        }
    }

    @Test
    public void parseActionRejectsVeryLongInput() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5000; i++) {
            sb.append('x');
        }
        assertThrows(IllegalArgumentException.class, () -> GameController.parseAction(sb.toString()));
    }

    // ---- parseLineSize ----

    @Test
    public void parseLineSizeAcceptsWholeNumbers() {
        assertEquals(15, GameController.parseLineSize("15"));
        assertEquals(15, GameController.parseLineSize(" 15 "));
    }

    @Test
    public void parseLineSizeRejectsNonNumbers() {
        for (String bad : new String[] {"abc", "", "3.5", "1e2", null}) {
            try {
                GameController.parseLineSize(bad);
                fail("should reject: " + bad);
            } catch (IllegalArgumentException expected) {
                assertEquals("Line size must be a whole number from 3 to 100.", expected.getMessage());
            }
        }
    }

    // ---- run ----

    private GameController controller(RideLine model, ScriptedView view) {
        return new GameController(model, view);
    }

    @Test
    public void runPlaysAGameToTheEnd() {
        RideLine model = new RideLine(3, "You", new FixedDice(true));
        ScriptedView view = new ScriptedView("wait", "wait", "wait");
        controller(model, view).run();
        assertTrue(model.isGameOver());
        assertEquals(1, view.getWelcomes());
        assertEquals(3, view.getResults().size());
        assertEquals(1, view.getSummaries());
        assertTrue(view.wasLastFinished());
        assertTrue(view.getMessages().isEmpty());
    }

    @Test
    public void typoShowsOneMessageAndGameContinues() {
        RideLine model = new RideLine(3, "You", new FixedDice(true));
        ScriptedView view = new ScriptedView("punhc", "wait", "wait", "wait");
        controller(model, view).run();
        assertEquals(Arrays.asList("Type wait or punch."), view.getMessages());
        assertEquals(3, model.snapshot().getTurn());
        assertEquals(1, view.getSummaries());
        assertTrue(view.wasLastFinished());
    }

    @Test
    public void punchAtFrontIsRefusedWithoutAskingTheDice() {
        FixedDice dice = new FixedDice(true);
        RideLine model = new RideLine(3, "You", dice);
        ScriptedView view = new ScriptedView("wait", "wait", "punch", "wait");
        controller(model, view).run();
        assertEquals(Arrays.asList("You're at the front, just wait."), view.getMessages());
        assertEquals(0, dice.getCalls());
        assertEquals(3, model.snapshot().getTurn());
    }

    @Test
    public void endOfInputEndsTheGameWithoutBoarding() {
        RideLine model = new RideLine(5, "You", new FixedDice(true));
        ScriptedView view = new ScriptedView("wait");
        controller(model, view).run();
        assertEquals(1, model.snapshot().getTurn());
        assertFalse(model.isGameOver());
        assertEquals(1, view.getSummaries());
        assertFalse(view.wasLastFinished());
    }

    @Test
    public void emptyInputEndsImmediatelyWithoutCrashing() {
        RideLine model = new RideLine(5, "You", new FixedDice(true));
        ScriptedView view = new ScriptedView();
        controller(model, view).run();
        assertEquals(0, model.snapshot().getTurn());
        assertEquals(1, view.getSummaries());
        assertFalse(view.wasLastFinished());
    }

    @Test
    public void punchWinIsReportedToTheView() {
        RideLine model = new RideLine(3, "You", new FixedDice(true));
        ScriptedView view = new ScriptedView("punch", "wait", "wait");
        controller(model, view).run();
        assertEquals(Outcome.PUNCH_WON, view.getResults().get(0).getOutcome());
    }
}
