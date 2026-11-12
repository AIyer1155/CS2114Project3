import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;

public class ConsoleViewTest {
    private ByteArrayOutputStream bytes;
    private PrintStream out;

    @Before
    public void setUp() {
        bytes = new ByteArrayOutputStream();
        out = new PrintStream(bytes);
    }

    private ConsoleView viewWithInput(String text) {
        return new ConsoleView(new ByteArrayInputStream(text.getBytes()), out);
    }

    private String output() {
        out.flush();
        return bytes.toString();
    }

    private LineSnapshot snap(boolean canPunch, int pos) {
        return new LineSnapshot(Arrays.asList("Guest 1", "You"), pos, 2, 0.7, canPunch, 2, 1, 3);
    }

    @Test
    public void constructorRejectsNulls() {
        assertThrows(NullPointerException.class, () -> new ConsoleView(null, out));
        assertThrows(NullPointerException.class,
                () -> new ConsoleView(new ByteArrayInputStream(new byte[0]), null));
    }

    @Test
    public void readCommandReturnsLinesThenNullAtEndOfInput() {
        ConsoleView v = viewWithInput("wait\npunch\n");
        assertEquals("wait", v.readCommand());
        assertEquals("punch", v.readCommand());
        assertNull(v.readCommand());
    }

    @Test
    public void readCommandOnEmptyInputIsNull() {
        assertNull(viewWithInput("").readCommand());
    }

    @Test
    public void hugeLineIsCutToTheLimitAndReadingContinues() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1_000_000; i++) {
            sb.append('x');
        }
        ConsoleView v = viewWithInput(sb + "\nwait\n");
        String first = v.readCommand();
        assertEquals(GameView.MAX_LINE_READ, first.length());
        assertEquals("wait", v.readCommand());
        assertNull(v.readCommand());
    }

    @Test
    public void showStateWithPunchShowsOddsAndBothChoices() {
        viewWithInput("").showState(snap(true, 2));
        String s = output();
        assertTrue(s, s.contains("Punch success odds: 70.0%"));
        assertTrue(s, s.contains("Choose action (wait / punch): "));
        assertTrue(s, s.contains("Line: Guest 1, You"));
        assertTrue(s, s.contains("Your position: 2 of 2"));
    }

    @Test
    public void showStateAtFrontOffersOnlyWait() {
        viewWithInput("").showState(snap(false, 1));
        String s = output();
        assertTrue(s, s.contains("You are at the front of the line!"));
        assertTrue(s, s.contains("Choose action (wait): "));
        assertFalse(s, s.contains("Punch success odds"));
    }

    @Test
    public void showResultDescribesEachOutcome() {
        ConsoleView v = viewWithInput("");
        v.showResult(new TurnResult(Action.WAIT, Outcome.WAITED, 0, "Guest 1", false));
        v.showResult(new TurnResult(Action.PUNCH, Outcome.PUNCH_WON, 0.7, "Guest 2", false));
        v.showResult(new TurnResult(Action.PUNCH, Outcome.PUNCH_LOST, 0.4, "Guest 3", false));
        String s = output();
        assertTrue(s, s.contains("You waited patiently."));
        assertTrue(s, s.contains("PUNCH SUCCESS! (odds were 70.0%)"));
        assertTrue(s, s.contains("PUNCH FAILED! (odds were 40.0%)"));
        assertTrue(s, s.contains("Guest 'Guest 1' boarded the ride."));
    }

    @Test
    public void showMessagePrintsTheText() {
        viewWithInput("").showMessage("Type wait or punch.");
        assertTrue(output().contains("-> Type wait or punch."));
    }

    @Test
    public void showSummaryDistinguishesFinishedFromQuit() {
        viewWithInput("").showSummary(snap(false, 0), true);
        String done = output();
        assertTrue(done, done.contains("Congratulations! You boarded the ride!"));
        assertTrue(done, done.contains("Total turns taken: 2"));
        assertTrue(done, done.contains("Punches won:  1"));
        assertTrue(done, done.contains("Punches lost: 3"));

        bytes.reset();
        viewWithInput("").showSummary(snap(false, 0), false);
        assertTrue(output().contains("Game ended before boarding."));
    }

    @Test
    public void nullArgumentsAreRejected() {
        ConsoleView v = viewWithInput("");
        assertThrows(NullPointerException.class, () -> v.showWelcome(null));
        assertThrows(NullPointerException.class, () -> v.showState(null));
        assertThrows(NullPointerException.class, () -> v.showResult(null));
        assertThrows(NullPointerException.class, () -> v.showMessage(null));
        assertThrows(NullPointerException.class, () -> v.showSummary(null, true));
    }

    @Test
    public void wholeGameThroughControllerAndConsoleView() {
        RideLine model = new RideLine(3, "You", new FixedDice(true));
        ConsoleView v = viewWithInput("wait\nbogus\nwait\nwait\n");
        new GameController(model, v).run();
        String s = output();
        assertTrue(s, s.contains("-> Type wait or punch."));
        assertTrue(s, s.contains("Congratulations! You boarded the ride!"));
    }
}
