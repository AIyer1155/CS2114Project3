import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class ConsoleView implements GameView {
    private final BufferedReader in;
    private final PrintStream out;

    public ConsoleView(InputStream in, PrintStream out) {
        if (in == null) {
            throw new NullPointerException("InputStream cannot be null");
        }
        if (out == null) {
            throw new NullPointerException("PrintStream cannot be null");
        }
        this.in = new BufferedReader(new InputStreamReader(in));
        this.out = out;
    }

    @Override
    public void showWelcome(LineSnapshot snapshot) {
        if (snapshot == null) {
            throw new NullPointerException("Snapshot cannot be null");
        }
        out.println("=========================================");
        out.println("          WELCOME TO PUNCH LINE          ");
        out.println("=========================================");
        out.printf("You are in line with %d total guests.%n", snapshot.getLength());
        out.println();
    }

    @Override
    public void showState(LineSnapshot snapshot) {
        if (snapshot == null) {
            throw new NullPointerException("Snapshot cannot be null");
        }
        out.println("-----------------------------------------");
        out.printf("Turn %d | Line Length: %d%n", snapshot.getTurn(), snapshot.getLength());
        out.println("Line: " + String.join(", ", snapshot.getNames()));
        if (snapshot.getPlayerPosition() > 0) {
            out.printf("Your position: %d of %d%n", snapshot.getPlayerPosition(), snapshot.getLength());
        }
        if (snapshot.canPunch()) {
            out.printf("Punch success odds: %.1f%%%n", snapshot.getPunchOdds() * 100.0);
            out.print("Choose action (wait / punch): ");
        } else {
            out.println("You are at the front of the line!");
            out.print("Choose action (wait): ");
        }
        out.flush();
    }

    @Override
    public void showResult(TurnResult result) {
        if (result == null) {
            throw new NullPointerException("TurnResult cannot be null");
        }
        out.println();
        if (result.getAction() == Action.WAIT) {
            out.println("You waited patiently.");
        } else if (result.getOutcome() == Outcome.PUNCH_WON) {
            out.printf("PUNCH SUCCESS! (odds were %.1f%%) You cut ahead one spot!%n", result.getOddsUsed() * 100.0);
        } else {
            out.printf("PUNCH FAILED! (odds were %.1f%%) You got sent to the back of the line!%n", result.getOddsUsed() * 100.0);
        }
        out.printf("Guest '%s' boarded the ride.%n", result.getBoardedName());
        out.println();
    }

    @Override
    public void showMessage(String text) {
        if (text == null) {
            throw new NullPointerException("Message text cannot be null");
        }
        out.println("-> " + text);
    }

    @Override
    public void showSummary(LineSnapshot snapshot, boolean finished) {
        if (snapshot == null) {
            throw new NullPointerException("Snapshot cannot be null");
        }
        out.println("=========================================");
        out.println("               GAME OVER                 ");
        out.println("=========================================");
        if (finished) {
            out.println("Congratulations! You boarded the ride!");
        } else {
            out.println("Game ended before boarding.");
        }
        out.printf("Total turns taken: %d%n", snapshot.getTurn());
        out.printf("Punches won:  %d%n", snapshot.getPunchesWon());
        out.printf("Punches lost: %d%n", snapshot.getPunchesLost());
        out.println("=========================================");
    }

    @Override
    public String readCommand() {
        try {
            String line = in.readLine();
            if (line == null) {
                return null;
            }
            if (line.length() > MAX_LINE_READ) {
                return line.substring(0, MAX_LINE_READ);
            }
            return line;
        } catch (IOException e) {
            return null;
        }
    }
}
