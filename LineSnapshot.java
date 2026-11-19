import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LineSnapshot {
    private final List<String> names;
    private final List<String> fastLaneNames;
    private final int playerPosition;
    private final int length;
    private final int fastLaneLength;
    private final double punchOdds;
    private final boolean canPunch;
    private final boolean canUseFastPass;
    private final int turn;
    private final int punchesWon;
    private final int punchesLost;

    public LineSnapshot(List<String> names, int playerPosition, int length, double punchOdds,
                        boolean canPunch, int turn, int punchesWon, int punchesLost) {
        this(names, new ArrayList<>(), playerPosition, length, 0, punchOdds, canPunch, false, turn, punchesWon, punchesLost);
    }

    public LineSnapshot(List<String> names, List<String> fastLaneNames, int playerPosition, int length,
                        int fastLaneLength, double punchOdds, boolean canPunch, boolean canUseFastPass,
                        int turn, int punchesWon, int punchesLost) {
        if (names == null) {
            throw new NullPointerException("Names list cannot be null");
        }
        if (fastLaneNames == null) {
            throw new NullPointerException("Fast lane names list cannot be null");
        }
        this.names = Collections.unmodifiableList(new ArrayList<>(names));
        this.fastLaneNames = Collections.unmodifiableList(new ArrayList<>(fastLaneNames));
        this.playerPosition = playerPosition;
        this.length = length;
        this.fastLaneLength = fastLaneLength;
        this.punchOdds = punchOdds;
        this.canPunch = canPunch;
        this.canUseFastPass = canUseFastPass;
        this.turn = turn;
        this.punchesWon = punchesWon;
        this.punchesLost = punchesLost;
    }

    public List<String> getNames() {
        return names;
    }

    public List<String> getFastLaneNames() {
        return fastLaneNames;
    }

    public int getPlayerPosition() {
        return playerPosition;
    }

    public int getLength() {
        return length;
    }

    public int getFastLaneLength() {
        return fastLaneLength;
    }

    public double getPunchOdds() {
        return punchOdds;
    }

    public boolean canPunch() {
        return canPunch;
    }

    public boolean canUseFastPass() {
        return canUseFastPass;
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
