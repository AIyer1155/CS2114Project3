import java.util.ArrayList;
import java.util.List;

/**
 * Test helper: a Dice that returns a scripted list of wins (true) and
 * losses (false) and records how often it was asked, and with what
 * probability. After the script runs out it repeats its last value.
 */
public class FixedDice implements Dice {
    private final boolean[] script;
    private int calls;
    private final List<Double> probabilities = new ArrayList<>();

    public FixedDice(boolean... script) {
        if (script == null || script.length == 0) {
            throw new IllegalArgumentException("Script cannot be empty");
        }
        this.script = script.clone();
    }

    @Override
    public boolean succeeds(double probability) {
        probabilities.add(probability);
        boolean result = script[Math.min(calls, script.length - 1)];
        calls++;
        return result;
    }

    public int getCalls() {
        return calls;
    }

    public List<Double> getProbabilities() {
        return new ArrayList<>(probabilities);
    }
}
