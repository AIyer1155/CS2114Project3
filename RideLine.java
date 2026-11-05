import java.util.ArrayList;
import java.util.List;

public class RideLine {
    public static final int MIN_LINE = 3;
    public static final int MAX_LINE = 100;
    public static final double DEFAULT_BACK_ODDS = 0.70;
    public static final double DEFAULT_FRONT_ODDS = 0.10;

    private final LinkedQueue<Guest> line;
    private final Guest player;
    private final Dice dice;
    private final double backOdds;
    private final double frontOdds;
    private int turn;
    private int punchesWon;
    private int punchesLost;
    private boolean playerBoarded;

    public RideLine(int lineSize, String playerName, Dice dice) {
        this(lineSize, playerName, dice, DEFAULT_BACK_ODDS, DEFAULT_FRONT_ODDS);
    }

    public RideLine(int lineSize, String playerName, Dice dice, double backOdds, double frontOdds) {
        if (lineSize < MIN_LINE || lineSize > MAX_LINE) {
            throw new IllegalArgumentException("Line size must be between " + MIN_LINE + " and " + MAX_LINE);
        }
        if (dice == null) {
            throw new NullPointerException("Dice cannot be null");
        }
        if (playerName == null) {
            throw new NullPointerException("Player name cannot be null");
        }
        if (playerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Player name cannot be blank");
        }
        if (Double.isNaN(frontOdds) || Double.isNaN(backOdds) || frontOdds < 0.0 || backOdds > 1.0 || frontOdds > backOdds) {
            throw new IllegalArgumentException("Invalid odds setup: 0 <= frontOdds <= backOdds <= 1 required");
        }

        this.dice = dice;
        this.backOdds = backOdds;
        this.frontOdds = frontOdds;
        this.turn = 0;
        this.punchesWon = 0;
        this.punchesLost = 0;
        this.playerBoarded = false;

        this.line = new LinkedQueue<>();
        for (int i = 1; i < lineSize; i++) {
            this.line.enqueue(new Guest("Guest " + i, false));
        }
        this.player = new Guest(playerName, true);
        this.line.enqueue(this.player);
    }

    public int playerPosition() {
        if (playerBoarded) {
            return 0;
        }
        List<Guest> guests = line.toList();
        for (int i = 0; i < guests.size(); i++) {
            if (guests.get(i).isPlayer()) {
                return i + 1;
            }
        }
        return 0;
    }

    public double punchOdds() {
        int k = playerPosition();
        int L = line.size();
        if (k <= 1 || playerBoarded) {
            return 0.0;
        }
        if (L == 2 && k == 2) {
            return frontOdds;
        }
        return frontOdds + (backOdds - frontOdds) * ((double) (k - 2) / (double) (L - 2));
    }

    public boolean isGameOver() {
        return playerBoarded;
    }

    public LineSnapshot snapshot() {
        List<String> names = new ArrayList<>();
        for (Guest g : line.toList()) {
            names.add(g.getName());
        }
        int pos = playerPosition();
        boolean canPunch = pos > 1 && !playerBoarded;
        return new LineSnapshot(names, pos, line.size(), punchOdds(), canPunch, turn, punchesWon, punchesLost);
    }

    public TurnResult takeTurn(Action action) {
        if (action == null) {
            throw new NullPointerException("Action cannot be null");
        }
        if (isGameOver()) {
            throw new IllegalStateException("Game is already over");
        }
        if (action == Action.PUNCH && playerPosition() <= 1) {
            throw new IllegalStateException("Cannot punch while at the front of the line");
        }

        Outcome outcome;
        double oddsUsed = 0.0;

        if (action == Action.WAIT) {
            outcome = Outcome.WAITED;
            oddsUsed = 0.0;
        } else {
            oddsUsed = punchOdds();
            boolean success = dice.succeeds(oddsUsed);
            int n = line.size();

            if (success) {
                outcome = Outcome.PUNCH_WON;
                punchesWon++;
                int k = playerPosition();
                Guest aheadGuest = null;

                for (int i = 1; i <= n; i++) {
                    Guest g = line.dequeue();
                    if (i == k - 1) {
                        aheadGuest = g;
                    } else if (i == k) {
                        line.enqueue(g);
                        line.enqueue(aheadGuest);
                    } else {
                        line.enqueue(g);
                    }
                }
            } else {
                outcome = Outcome.PUNCH_LOST;
                punchesLost++;
                Guest playerGuest = null;

                for (int i = 1; i <= n; i++) {
                    Guest g = line.dequeue();
                    if (g.isPlayer()) {
                        playerGuest = g;
                    } else {
                        line.enqueue(g);
                    }
                }
                if (playerGuest != null) {
                    line.enqueue(playerGuest);
                }
            }
        }

        Guest boarded = line.dequeue();
        if (boarded.isPlayer()) {
            playerBoarded = true;
        }
        turn++;

        return new TurnResult(action, outcome, oddsUsed, boarded.getName(), playerBoarded);
    }
}