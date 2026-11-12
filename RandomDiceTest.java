import static org.junit.Assert.*;

import java.util.Random;
import org.junit.Test;

public class RandomDiceTest {

    @Test
    public void probabilityOneAlwaysSucceedsAndZeroNever() {
        RandomDice dice = new RandomDice(new Random(42));
        for (int i = 0; i < 100; i++) {
            assertTrue(dice.succeeds(1.0));
            assertFalse(dice.succeeds(0.0));
        }
    }

    @Test
    public void middleProbabilityIsRoughlyRight() {
        RandomDice dice = new RandomDice(new Random(42));
        int trues = 0;
        for (int i = 0; i < 10000; i++) {
            if (dice.succeeds(0.3)) {
                trues++;
            }
        }
        assertTrue("got " + trues, trues > 2700 && trues < 3300);
    }

    @Test
    public void sameSeedGivesSameSequence() {
        RandomDice a = new RandomDice(new Random(5));
        RandomDice b = new RandomDice(new Random(5));
        for (int i = 0; i < 50; i++) {
            assertEquals(a.succeeds(0.5), b.succeeds(0.5));
        }
    }

    @Test
    public void probabilityOutsideRangeThrows() {
        RandomDice dice = new RandomDice(new Random(1));
        assertThrows(IllegalArgumentException.class, () -> dice.succeeds(1.5));
        assertThrows(IllegalArgumentException.class, () -> dice.succeeds(-0.1));
        assertThrows(IllegalArgumentException.class, () -> dice.succeeds(Double.NaN));
    }

    @Test
    public void nullRandomThrows() {
        assertThrows(NullPointerException.class, () -> new RandomDice(null));
    }
}
