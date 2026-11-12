import java.util.ArrayList;
import java.util.List;

/**
 * Test helper: a GameView that feeds scripted lines to the controller and
 * records everything the controller shows. When the script runs out,
 * readCommand() returns null (end of input).
 */
public class ScriptedView implements GameView {
    private final String[] input;
    private int next;

    private final List<String> messages = new ArrayList<>();
    private final List<LineSnapshot> states = new ArrayList<>();
    private final List<TurnResult> results = new ArrayList<>();
    private int welcomes;
    private int summaries;
    private boolean lastFinished;

    public ScriptedView(String... input) {
        this.input = input.clone();
    }

    @Override
    public void showWelcome(LineSnapshot snapshot) {
        welcomes++;
    }

    @Override
    public void showState(LineSnapshot snapshot) {
        states.add(snapshot);
    }

    @Override
    public void showResult(TurnResult result) {
        results.add(result);
    }

    @Override
    public void showMessage(String text) {
        messages.add(text);
    }

    @Override
    public void showSummary(LineSnapshot snapshot, boolean finished) {
        summaries++;
        lastFinished = finished;
    }

    @Override
    public String readCommand() {
        if (next >= input.length) {
            return null;
        }
        return input[next++];
    }

    public List<String> getMessages() {
        return messages;
    }

    public List<LineSnapshot> getStates() {
        return states;
    }

    public List<TurnResult> getResults() {
        return results;
    }

    public int getWelcomes() {
        return welcomes;
    }

    public int getSummaries() {
        return summaries;
    }

    public boolean wasLastFinished() {
        return lastFinished;
    }
}
