import java.util.ArrayList;
import java.util.List;

public class RideLine {
    public static final int MIN_LINE = 3;
    public static final int MAX_LINE = 100;
    public static final double DEFAULT_BACK_ODDS = 0.70;
    public static final double DEFAULT_FRONT_ODDS = 0.10;

    private final LinkedQueue<Guest> standardLane;
    private final LinkedQueue<Guest> fastLane;
    private final Guest player;
    private final Dice dice;
    private final double backOdds;
    private final double frontOdds;
    private int turn;
    private int punchesWon;
    private int punchesLost;
    private boolean playerBoarded;
    private boolean fastPassUsed;
    private boolean playerInFastLane;
    private int boardingCycle;

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
        this.fastPassUsed = false;
        this.playerInFastLane = false;
        this.boardingCycle = 0;

        this.standardLane = new LinkedQueue<>();
        this.fastLane = new LinkedQueue<>();

        // Distribute guests between standard and fast lanes (roughly 70/30)
        int fastLaneCount = (int) (lineSize * 0.3);
        int standardLaneCount = lineSize - fastLaneCount - 1;

        for (int i = 1; i <= standardLaneCount; i++) {
            this.standardLane.enqueue(new Guest("Guest " + i, false));
        }

        for (int i = standardLaneCount + 1; i <= standardLaneCount + fastLaneCount; i++) {
            this.fastLane.enqueue(new Guest("Guest " + i, false));
        }

        this.player = new Guest(playerName, true);
        this.standardLane.enqueue(this.player);
    }

    public int playerPosition() {
        if (playerBoarded) {
            return 0;
        }
        List<Guest> guests = playerInFastLane ? fastLane.toList() : standardLane.toList();
        for (int i = 0; i < guests.size(); i++) {
            if (guests.get(i).isPlayer()) {
                return i + 1;
            }
        }
        return 0;
    }

    public double punchOdds() {
        int k = playerPosition();
        LinkedQueue<Guest> currentLane = playerInFastLane ? fastLane : standardLane;
        int L = currentLane.size();
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
        List<String> standardNames = new ArrayList<>();
        for (Guest g : standardLane.toList()) {
            standardNames.add(g.getName());
        }

        List<String> fastNames = new ArrayList<>();
        for (Guest g : fastLane.toList()) {
            fastNames.add(g.getName());
        }

        int pos = playerPosition();
        boolean canPunch = pos > 1 && !playerBoarded;
        boolean canUseFastPass = !fastPassUsed && !playerBoarded && !playerInFastLane;

        return new LineSnapshot(standardNames, fastNames, pos, standardLane.size(), fastLane.size(),
                                punchOdds(), canPunch, canUseFastPass, turn, punchesWon, punchesLost);
    }

    public TurnResult takeTurn(Action action) {
        if (action == null) {
            throw new NullPointerException("Action cannot be null");
        }
        if (isGameOver()) {
            throw new IllegalStateException("Game is already over");
        }

        LinkedQueue<Guest> currentLane = playerInFastLane ? fastLane : standardLane;

        if (action == Action.PUNCH && playerPosition() <= 1) {
            throw new IllegalStateException("Cannot punch while at the front of the line");
        }

        if (action == Action.FAST_PASS && fastPassUsed) {
            throw new IllegalStateException("Fast pass has already been used");
        }

        if (action == Action.FAST_PASS && playerInFastLane) {
            throw new IllegalStateException("Cannot use fast pass while already in fast lane");
        }

        Outcome outcome;
        double oddsUsed = 0.0;

        if (action == Action.WAIT) {
            outcome = Outcome.WAITED;
            oddsUsed = 0.0;
        } else if (action == Action.FAST_PASS) {
            outcome = Outcome.FAST_PASSED;
            oddsUsed = 0.0;
            fastPassUsed = true;

            // Move player from standard to fast lane
            int n = standardLane.size();
            Guest playerGuest = null;
            for (int i = 1; i <= n; i++) {
                Guest g = standardLane.dequeue();
                if (g.isPlayer()) {
                    playerGuest = g;
                } else {
                    standardLane.enqueue(g);
                }
            }
            if (playerGuest != null) {
                fastLane.enqueue(playerGuest);
                playerInFastLane = true;
            }
        } else {
            oddsUsed = punchOdds();
            boolean success = dice.succeeds(oddsUsed);
            int n = currentLane.size();

            if (success) {
                outcome = Outcome.PUNCH_WON;
                punchesWon++;
                int k = playerPosition();
                Guest aheadGuest = null;

                for (int i = 1; i <= n; i++) {
                    Guest g = currentLane.dequeue();
                    if (i == k - 1) {
                        aheadGuest = g;
                    } else if (i == k) {
                        currentLane.enqueue(g);
                        currentLane.enqueue(aheadGuest);
                    } else {
                        currentLane.enqueue(g);
                    }
                }
            } else {
                outcome = Outcome.PUNCH_LOST;
                punchesLost++;
                Guest playerGuest = null;

                for (int i = 1; i <= n; i++) {
                    Guest g = currentLane.dequeue();
                    if (g.isPlayer()) {
                        playerGuest = g;
                    } else {
                        currentLane.enqueue(g);
                    }
                }
                if (playerGuest != null) {
                    currentLane.enqueue(playerGuest);
                }
            }
        }

        // Board next guest (every third from fast lane, rest from standard)
        Guest boarded = null;
        boardingCycle++;
        if (boardingCycle % 3 == 0 && !fastLane.isEmpty()) {
            boarded = fastLane.dequeue();
        } else if (!standardLane.isEmpty()) {
            boarded = standardLane.dequeue();
        } else if (!fastLane.isEmpty()) {
            boarded = fastLane.dequeue();
        }

        if (boarded != null && boarded.isPlayer()) {
            playerBoarded = true;
        }

        turn++;

        String boardedName = boarded != null ? boarded.getName() : "Unknown";
        return new TurnResult(action, outcome, oddsUsed, boardedName, playerBoarded);
    }
}
