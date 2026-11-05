public class TurnResult {
    private final Action action;
    private final Outcome outcome;
    private final double oddsUsed;
    private final String boardedName;
    private final boolean playerBoarded;

    public TurnResult(Action action, Outcome outcome, double oddsUsed, String boardedName, boolean playerBoarded) {
        if (action == null) {
            throw new NullPointerException("Action cannot be null");
        }
        if (outcome == null) {
            throw new NullPointerException("Outcome cannot be null");
        }
        if (boardedName == null) {
            throw new NullPointerException("Boarded name cannot be null");
        }
        this.action = action;
        this.outcome = outcome;
        this.oddsUsed = oddsUsed;
        this.boardedName = boardedName;
        this.playerBoarded = playerBoarded;
    }

    public Action getAction() {
        return action;
    }

    public Outcome getOutcome() {
        return outcome;
    }

    public double getOddsUsed() {
        return oddsUsed;
    }

    public String getBoardedName() {
        return boardedName;
    }

    public boolean playerBoarded() {
        return playerBoarded;
    }
}
