import java.util.Random;

public class RandomDice implements Dice {
    private final Random random;

    public RandomDice() {
        this(new Random());
    }

    public RandomDice(Random random) {
        if (random == null) {
            throw new NullPointerException("Random instance cannot be null");
        }
        this.random = random;
    }

    @Override
    public boolean succeeds(double probability) {
        if (Double.isNaN(probability) || probability < 0.0 || probability > 1.0) {
            throw new IllegalArgumentException("Probability must be between 0.0 and 1.0");
        }
        if (probability == 1.0) {
            return true;
        }
        if (probability == 0.0) {
            return false;
        }
        return random.nextDouble() < probability;
    }
}
