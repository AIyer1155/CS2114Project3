/**
 * Simulates different strategies in Punch Line to analyze game balance.
 * Tests strategies like "always wait" vs "punch at position 8 or worse"
 */
public class StrategySimulator {
    public static void main(String[] args) {
        int simulations = 10000;
        int lineSize = 15;

        System.out.println("=== Punch Line Strategy Simulator ===");
        System.out.println("Simulations: " + simulations);
        System.out.println("Line Size: " + lineSize);
        System.out.println();

        double alwaysWaitAvg = simulateAlwaysWait(simulations, lineSize);
        double aggressivePunchAvg = simulateAggressivePunch(simulations, lineSize);
        double fastPassAvg = simulateWithFastPass(simulations, lineSize);

        System.out.printf("Always Wait:        %.2f turns average%n", alwaysWaitAvg);
        System.out.printf("Punch at pos >= 8:  %.2f turns average%n", aggressivePunchAvg);
        System.out.printf("Fast Pass + Punch:  %.2f turns average%n", fastPassAvg);
        System.out.println();
        System.out.printf("Improvement (punch): %.1f%%%n",
            (1 - aggressivePunchAvg / alwaysWaitAvg) * 100);
        System.out.printf("Improvement (fast):  %.1f%%%n",
            (1 - fastPassAvg / alwaysWaitAvg) * 100);
    }

    private static double simulateAlwaysWait(int count, int lineSize) {
        int totalTurns = 0;

        for (int i = 0; i < count; i++) {
            RideLine r = new RideLine(lineSize, "Player", new RandomDice());

            while (!r.isGameOver()) {
                r.takeTurn(Action.WAIT);
            }

            totalTurns += r.snapshot().getTurn();
        }

        return (double) totalTurns / count;
    }

    private static double simulateAggressivePunch(int count, int lineSize) {
        int totalTurns = 0;

        for (int i = 0; i < count; i++) {
            RideLine r = new RideLine(lineSize, "Player", new RandomDice());

            while (!r.isGameOver()) {
                LineSnapshot snap = r.snapshot();
                Action action;

                // Punch if at position 8 or worse (back half of line), else wait
                if (snap.getPlayerPosition() >= 8 && snap.canPunch()) {
                    action = Action.PUNCH;
                } else {
                    action = Action.WAIT;
                }

                r.takeTurn(action);
            }

            totalTurns += r.snapshot().getTurn();
        }

        return (double) totalTurns / count;
    }

    private static double simulateWithFastPass(int count, int lineSize) {
        int totalTurns = 0;

        for (int i = 0; i < count; i++) {
            RideLine r = new RideLine(lineSize, "Player", new RandomDice());

            while (!r.isGameOver()) {
                LineSnapshot snap = r.snapshot();
                Action action;

                // Use fast pass early if still in back half
                if (snap.canUseFastPass() && snap.getPlayerPosition() >= 8) {
                    action = Action.FAST_PASS;
                } else if (snap.getPlayerPosition() >= 6 && snap.canPunch()) {
                    action = Action.PUNCH;
                } else {
                    action = Action.WAIT;
                }

                r.takeTurn(action);
            }

            totalTurns += r.snapshot().getTurn();
        }

        return (double) totalTurns / count;
    }
}
