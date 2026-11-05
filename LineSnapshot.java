import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LineSnapshot {
    private final List<String> names;
    private final int playerPosition;
    private final int length;
    private final double punchOdds;
    private final boolean canPunch;
    private final int turn;
    private final int punchesWon;
    private final int punchesLost;

    public LineSnapshot(List<String> names, int playerPosition, int length, double punchOdds,
                        boolean canPunch, int turn, int punchesWon, int punchesLost) {
        if (names == null) {
            throw new NullPointerException("Names list cannot be null");
        }
        this.names = Collections.unmodifiableList(new ArrayList<>(names));
        this.playerPosition = playerPosition;
        this.length = length;
        this.punchOdds = punchOdds;
        this.canPunch = canPunch;
        this.turn = turn;
        this.punchesWon = punchesWon;
        this.punchesLost = punchesLost;
    }

    public List<String> getNames() {
        return names;
    }

    public int getPlayerPosition() {
        return playerPosition;
    }

    public int getLength() {
        return length;
    }

    public double getPunchOdds() {
        return punchOdds;
    }

    public boolean canPunch() {
        return canPunch;
    }

    public int getTurn() {
        return turn;
    }

    public int getPunchesWon() {
        return punchesWon;
    }

    public int getPunchesLost() {
        return punchesLost;
    }
}
